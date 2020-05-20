package top.cloudli.judgeservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class JudgeServiceApplication {

    public static boolean isWindows = System.getProperty("os.name").contains("Windows");

    public static void main(String[] args) {
        SpringApplication.run(JudgeServiceApplication.class, args);
    }

}
