package com.amq;

import com.amq.base.TaskData;
import com.amq.conf.TaskMetaInfoManager;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private RabbitTemplate rabbitTemplate;
    /**
     * 用于本地执行任务的taskConsumer。
     */
    private TaskRunnerContainer taskRunnerContainer;

    public TaskScheduler(RabbitTemplate rabbitTemplate,
                         TaskRunnerContainer taskRunnerContainer) {
        this.rabbitTemplate = rabbitTemplate;
        this.taskRunnerContainer = taskRunnerContainer;
    }
    /**
     * 把任务发送到队列中
     *
     * @param taskData 任务数据
     */
    public void sendToQueue(final TaskData<?, ?> taskData) {
        taskData.setQueueDate(new Date());
        taskData.setRunType(TaskData.RUN_TYPE_GLOBAL);
        //String queue = TaskMetaInfoManager.getFitQueue(taskData);
        String exName = "com.runner.TestRunner";//交换机名
        String routeKey = "com.runner.TestRunner";//路由key名
        //方法一
        Message message = rabbitTemplate.getMessageConverter().toMessage(taskData,new MessageProperties());
        rabbitTemplate.send(exName, routeKey, message);
        //方法二
        //rabbitTemplate.convertAndSend(exName, routeKey, taskData);
    }
    /**
     * 同步执行任务，可能会导致阻塞。
     *
     * @param taskData  任务数据
     * @return
     */
    public <TP, RD> TaskData<TP, RD> runTask(final TaskData<TP, RD> taskData,
                                             final TypeReference<TaskData<TP, RD>> typeRef) throws Exception {
        String exName = "com.runner.TestRunner";//交换机名
        String routeKey = "com.runner.TestRunner";//路由key名
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