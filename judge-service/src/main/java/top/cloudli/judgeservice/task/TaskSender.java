package top.cloudli.judgeservice.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.cloudli.judgeservice.dao.SolutionDao;
import top.cloudli.judgeservice.model.Solution;
import top.cloudli.judgeservice.model.SolutionState;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class TaskSender {

    @Resource
    private SolutionDao solutionDao;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    private Queue queue;

    /**
     * 发送已提交的 solution 到消息队列
     */
    @Scheduled(fixedDelay = 2000, initialDelay = 500)
    public void sendCommitted() {
       List<Solution> solutions = solutionDao.getSubmitted();

       for (Solution solution : solutions) {
           rabbitTemplate.convertAndSend(queue.getName(), solution);
           solution.setState(SolutionState.IN_JUDGE_QUEUE.ordinal());
           solutionDao.updateState(solution);
       }

       log.info("RabbitMQ 已发送 {} 条 solution.", solutions.size());
    }
}