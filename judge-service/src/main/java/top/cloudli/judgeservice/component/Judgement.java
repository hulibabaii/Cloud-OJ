package top.cloudli.judgeservice.component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import top.cloudli.judgeservice.dao.RuntimeDao;
import top.cloudli.judgeservice.dao.SolutionDao;
import top.cloudli.judgeservice.model.*;
import top.cloudli.judgeservice.model.Runtime;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Judgement {

    @Value("${project.test-data-dir}")
    private String testDataDir;

    @Value("${project.target-dir}")
    private String targetDir;

    @Value("${project.max-wait-time}")
    private long maxWaitTime;

    @Resource
    private RuntimeDao runtimeDao;

    @Resource
    private SolutionDao solutionDao;

    private boolean isLinux;

    @PostConstruct
    public void init() {
        isLinux = System.getProperty("os.name").equals("Linux");
    }

    /**
     * 判题
     *
     * @param solution {@link Solution}
     */
    public void judge(Solution solution) {
        List<String> input = getInputData(solution.getProblemId());
        List<String> expect = getOutputData(solution.getProblemId());

        ProcessBuilder cmd = buildCommand(solution);
        log.info("正在执行, solutionId: {}", solution.getSolutionId());

        Runtime runtime = new Runtime(solution.getSolutionId());
        runtimeDao.add(runtime);
        List<String> output = execute(cmd, input, runtime);  // 获取输出

        compareResult(solution, runtime, expect, output);
    }

    /**
     * 比较结果
     *
     * @param solution {@link Solution}
     * @param expect   期望输出
     * @param output   实际输出
     */
    private void compareResult(Solution solution, Runtime runtime, List<String> expect, List<String> output) {
        long timeout = runtimeDao.getTimeout(solution.getProblemId());
        int total = expect.size();
        int passed = 0;

        for (int i = 0; i < output.size(); i++) {
            if (expect.get(i).equals(output.get(i)))
                passed++;
        }

        runtime.setTotal(total);
        runtime.setPassed(passed);
        runtimeDao.update(runtime);

        if (passed == 0) {
            solution.setResult(SolutionResult.WRONG.ordinal());
        } else if (passed < total) {
            solution.setResult(SolutionResult.PARTLY_PASSED.ordinal());
        } else {
            solution.setResult(SolutionResult.ALL_PASSED.ordinal());
        }

        double passRate = (double) passed / total;

        if (passed != 0 && runtime.getTime() > timeout) {
            solution.setResult(SolutionResult.TIMEOUT.ordinal());
            passRate /= 2;      // 时间超限降一半分
        }

        solution.setPassRate(passRate);
        solution.setState(SolutionState.JUDGED.ordinal());
        solutionDao.update(solution);
    }

    /**
     * 执行
     *
     * @param input 输入数据
     * @return 程序执行结果
     */
    private List<String> execute(ProcessBuilder cmd, List<String> input, Runtime runtime) {
        List<String> output = new ArrayList<>();

        try {
            StopWatch stopWatch = new StopWatch("GET-EXECUTE-TIME");
            log.info("cmd: {}", cmd.command());

            if (input.size() == 0) {
                // 执行程序
                stopWatch.start();
                Process process = cmd.start();

                if (!process.waitFor(maxWaitTime, TimeUnit.MILLISECONDS)) {
                    throw new InterruptedException("等待时间过长，可能存在死循环.");
                }

                stopWatch.stop();
                runtime.setTime(stopWatch.getTotalTimeMillis());
                // 获取错误流
                String error = getOutput(process.getErrorStream());

                if (error.isEmpty()) {
                    // 获取标准输出流
                    String temp = getOutput(process.getInputStream());
                    output.add(temp);
                } else {
                    runtime.setInfo(error);
                }
            } else {
                long maxTime = 0;

                for (String s : input) {
                    stopWatch.start();
                    Process process = cmd.start();

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    // 输入数据
                    writer.write(s);
                    writer.close();

                    if (!process.waitFor(maxWaitTime, TimeUnit.MILLISECONDS)) {
                        throw new InterruptedException("等待时间过长，可能存在死循环.");
                    }

                    stopWatch.stop();

                    String error = getOutput(process.getErrorStream());

                    long time = stopWatch.getTotalTimeMillis();
                    maxTime = Math.max(time, maxTime);

                    if (error.isEmpty()) {
                        String temp = getOutput(process.getInputStream());
                        output.add(temp);
                    } else {
                        runtime.setInfo(error);
                        break;
                    }
                }

                runtime.setTime(maxTime);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            runtime.setInfo(e.getMessage());
        } catch (InterruptedException e2) {
            log.error(e2.getMessage());
            runtime.setTime(maxWaitTime);
            runtime.setInfo(e2.getMessage());
        }

        runtime.setOutput(String.join("\n", output));
        runtimeDao.update(runtime);
        return output;
    }

    @SneakyThrows
    private String getOutput(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, isLinux ? "utf-8" : "gbk"))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    /**
     * 构造执行命令
     *
     * @param solution {@link Solution}
     * @return 执行命令
     */
    private ProcessBuilder buildCommand(Solution solution) {
        Language language = Language.get(solution.getLanguage());
        assert language != null;

        String filePath = targetDir + solution.getSolutionId();
        ProcessBuilder builder = new ProcessBuilder();

        switch (language) {
            case C_CPP:
                // NOTE Windows 和 Linux 生成的目标文件不一样
                if (isLinux) {
                    builder.command(filePath + ".o");
                } else {
                    builder.command(filePath + ".exe");
                }
                return builder;
            case JAVA:
                builder.command("java", "Solution");
                // NOTE Java 语言必须切换到 class 文件所在目录
                builder.directory(new File(filePath));
                return builder;
            case PYTHON:
                builder.command("python", filePath + ".py");
                return builder;
            case BASH:
                builder.command("sh", filePath + ".sh");
                return builder;
            default:
                return null;
        }
    }

    /**
     * 获取测试输入数据
     *
     * @param problemId 题目 Id
     * @return 每一组输入数据
     */
    private List<String> getInputData(int problemId) {
        return getData(problemId, ".in");
    }

    /**
     * 获取测试输出数据
     *
     * @param problemId 题目 Id
     * @return 每一组输出数据
     */
    private List<String> getOutputData(int problemId) {
        return getData(problemId, ".out");
    }

    /**
     * 读取测试数据
     *
     * @param problemId 问题 Id
     * @param ext       文件扩展名
     * @return 测试数据集合
     */
    private List<String> getData(int problemId, String ext) {
        List<String> data = new ArrayList<>();

        File dir = new File(testDataDir + problemId);
        File[] files = dir.listFiles(pathname -> {
            String name = pathname.getName();
            return name.substring(name.lastIndexOf('.')).equals(ext);
        });

        if (files == null)
            return data;

        // 按文件名排序
        List<File> fileList = Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        for (File file : fileList) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file))
                );
                data.add(reader.lines().collect(Collectors.joining("\n")));
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
            }
        }

        return data;
    }
}
