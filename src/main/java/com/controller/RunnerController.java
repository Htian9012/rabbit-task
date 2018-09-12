package com.controller;

import com.amq.TaskRunnerContainer;
import com.amq.TaskScheduler;
import com.amq.base.TaskData;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/runner")
public class RunnerController {
    @Resource
    private RabbitTemplate rabbitTemplate;//RabbitTemplate继承AmqpTemplate，拥有更多方法
    @Resource
    private RabbitAdmin rabbitAdmin;
    @Resource
    private ConnectionFactory connectionFactory;

    @Autowired
    private TaskScheduler taskScheduler;

    @RequestMapping("/run")
    public TaskData lzh1() throws Exception {
        //队列名
        String queueName = "ququeName-test";
        //交换机名
        String exName = "exName-test";
        //路由key名
        String routeKey = "routeKey-test";
        TaskData<String, String> taskData = new TaskData<String, String>();
        taskData.setRunType(TaskData.RUN_TYPE_AUTO_RPC);
        taskData.setTaskParam(" i am a man.");
        taskData.setTaskClass("com.runner.TestRunner");
        taskData = taskScheduler.runTask(taskData, new TypeReference<TaskData<String, String>>() {
        });
        return taskData;
    }

    @RequestMapping("/register")
    public String lzh(String aa) {
        String queueName = "ququeName-test";//队列名
        String exName = "exName-test";//交换机名
        String routeKey = "routeKey-test";//路由key名
        Queue queue = new Queue(queueName, false);//durable->false：非持久化
        rabbitAdmin.declareQueue(queue);//声明队列
        //创建交换机，设置交换机非持久的，不使用时自动删除的
        DirectExchange ex = new DirectExchange(exName, false, true);
        //声明交换机，如果本来存在个名字相同其他属性不同的交换机，那么再次声明会报异常，这里做删除旧的，声明新的处理
        try {
            rabbitAdmin.declareExchange(ex);
        } catch (Exception e) {
            rabbitAdmin.deleteExchange(exName);
            rabbitAdmin.declareExchange(ex);
        }
        //把队列帮定到交换机并且指定队列接收指定路由key名的数据
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(ex).with(routeKey));

        // 启动任务监听器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setAutoStartup(false);
        container.setTaskExecutor(new SimpleAsyncTaskExecutor(queueName));
        // 提高启动consumer速度。
        container.setStartConsumerMinInterval(1000);
        container.setConsecutiveActiveTrigger(3);
        container.setStopConsumerMinInterval(20000);
        container.setConsecutiveIdleTrigger(3);
        container.setConnectionFactory(connectionFactory);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setQueueNames(queueName);
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(new TaskRunnerContainer(), "process");
        //listenerAdapter.addQueueOrTagToMethodName(queueName,"process");
        //TaskMessageConverter taskMessageConverter = new TaskMessageConverter();
        //listenerAdapter.setMessageConverter(taskMessageConverter);
        container.setMessageListener(listenerAdapter);
        //container.setMessageConverter(taskMessageConverter);
        container.setAutoStartup(true);
        container.afterPropertiesSet();
        container.start();
        return "register RabbitAdmin ok";
    }

}
