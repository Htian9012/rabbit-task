package com.amq;

import com.amq.base.TaskData;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 任务执行器。 通过调用此类，可以实现队列执行，RPC调用，本地调用等功能。
 *
 * @author lzh
 */
@Component
public class TaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskScheduler.class);

    /**
     * rabbitTemplate模板.
     */
    private AmqpTemplate rabbitTemplate;
    /**
     * 用于本地执行任务的taskConsumer。
     */
    private TaskRunnerContainer taskRunnerContainer;

    public TaskScheduler(AmqpTemplate rabbitTemplate,
                         TaskRunnerContainer taskRunnerContainer) {
        this.rabbitTemplate = rabbitTemplate;
        this.taskRunnerContainer = taskRunnerContainer;
    }

    /**
     * 同步执行任务，可能会导致阻塞。
     *
     * @param taskData  任务数据
     * @return
     */
    public <TP, RD> TaskData<TP, RD> runTask(final TaskData<TP, RD> taskData,
                                             final TypeReference<TaskData<TP, RD>> typeRef) throws Exception {
        String exName = "exName-test";//交换机名
        String routeKey = "routeKey-test";//路由key名
        taskData.setQueueDate(new Date());
        if (taskData.getRunType() == TaskData.RUN_TYPE_LOCAL) {
            return taskRunnerContainer.process(taskData);
        } else {
            log.info("全局运行模式,set data,参数: {} ",taskData.getTaskParam());
            // 全局运行模式
            @SuppressWarnings("unchecked")
            TaskData<TP, RD> result =  (TaskData<TP, RD>)rabbitTemplate.convertSendAndReceive(exName, routeKey, taskData,
                    new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties mp = message.getMessageProperties();
                            mp.setPriority(10);
                            mp.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
                            mp.setExpiration("180000");
                            return message;
                        }
                    });
            return result;
        }
    }
}