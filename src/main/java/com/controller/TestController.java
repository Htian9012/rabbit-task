package com.controller;

import com.amq.TaskRunnerContainer;
import com.amq.TaskScheduler;
import com.amq.base.TaskData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/testController")
public class TestController {
    @Resource
    private RabbitTemplate rabbitTemplate;//RabbitTemplate继承AmqpTemplate，拥有更多方法
    @Resource
    private RabbitAdmin rabbitAdmin;
    @Resource
    private ConnectionFactory connectionFactory;

    /**
     * 使用RabbitAdmin可以动态创建、队列，不需要提现@Bean注入
     * @param aa
     * @return
     */
    @RequestMapping("/testRabbitAdmin")
    public String testRabbitAdmin(String aa){
        String queueName = "ququeName-test";//队列名
        String exName = "exName-test";//交换机名
        String routeKey = "routeKey-test";//路由key名
        Queue queue = new Queue(queueName,false);//durable->false：非持久化
        rabbitAdmin.declareQueue(queue);//声明队列
        //创建交换机，设置交换机非持久的，不使用时自动删除的
        DirectExchange ex = new DirectExchange(exName,false,true);
        //声明交换机，如果本来存在个名字相同其他属性不同的交换机，那么再次声明会报异常，这里做删除旧的，声明新的处理
        try {
            rabbitAdmin.declareExchange(ex);
        } catch (Exception e) {
            rabbitAdmin.deleteExchange(exName);
            rabbitAdmin.declareExchange(ex);
        }
        //把队列帮定到交换机并且指定队列接收指定路由key名的数据
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(ex).with(routeKey));
        //创建注册器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        //设置消息确认模式手工确认后再从队列里面移除
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        //声明一个监听器（消费者）
        container.setMessageListener(new ChannelAwareMessageListener(){
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                System.out.println(" [x] Received '" + new String(message.getBody()) + "' start id: "+Thread.currentThread().getId());
                Thread.sleep(5000);//模拟处理业务
                System.out.println(" [x] Received '" + new String(message.getBody()) + "' end id: "+Thread.currentThread().getId());
                //回复给发送者消息
                rabbitTemplate.convertAndSend(message.getMessageProperties().getReplyTo(),"i am get msg :"+new String(message.getBody()));
                //确认消息接收成功，告诉队列可以把消息从队列移除
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        });
        //声明从什么名字的队列接收消息
        container.setQueueNames(queueName);
        //注册
        container.start();
        //设置消息响应超时为6秒
        rabbitTemplate.setReplyTimeout(6000);
        //同步发送并且接收响应消息对象
        Object obj = rabbitTemplate.convertSendAndReceive(exName,routeKey,"msg -- "+aa);
        if(obj == null){
            System.out.println("obj is null");
        }else{
            System.out.println("--- result : "+obj.toString());
        }
        container.shutdown();
        //删除队列（没有使用的，队列中没有消息的）
        rabbitAdmin.deleteQueue(queueName,true,true);
        return "testRabbitAdmin ok";
    }


    /**
     * 一条消息发送给一个队列，一个队列可以有多个消费者，但每条消息只能被一个消费者消费一次
     * @return
     */
    @RequestMapping(value = "/testA")
    public String testA(){
        //把消息指定发送给队列queueA
        rabbitTemplate.convertAndSend("queueA","msgA-"+System.currentTimeMillis());
        return "testA ok";
    }


    /**
     * faout交换机，routingKey不起作用，消息发给交换机，交换机可以绑定多条队列，每条队列可以有多个消费者，但每个队列的一条消息只能被一个消费者消费一次
     * @return
     */
    @RequestMapping(value = "/testB")
    public String testB(){
        //把消息指定发送给交换机fanoutExchange-queueB，参数（交换机名称，路由键值，消息），fanout类型交换机路由键值填空
        rabbitTemplate.convertAndSend("fanoutExchange-queueB", "", "msgB-"+System.currentTimeMillis());
        return "testB ok";
    }


    /**
     * faout交换机，routingKey起作用，消息发给交换机，交换机可以绑定多条队列，每条队列可以有多个消费者，但每个队列的一条消息只能被一个消费者消费一次
     * @return
     */
    @RequestMapping(value = "/testDirect")
    public String testDirect(){
        rabbitTemplate.convertAndSend("directExchange-queueDirect", "direct-liqihua", "msgDirect-"+System.currentTimeMillis());
        return "testDirect ok";
    }

    /**
     * topic类型交换机
     * @return
     */
    @RequestMapping(value = "/testC")
    public String testC(){
        //把消息指定发送给交换机topicExchange-queueC，参数（交换机名称，路由键值，消息）
        rabbitTemplate.convertAndSend("topicExchange-queueC", "aa.ERROR", "msgC-aabbcc-"+System.currentTimeMillis());//消息匹配给路由键值aa.ERROR
        rabbitTemplate.convertAndSend("topicExchange-queueC", "aa.INFO", "msgC-qqwwee-"+System.currentTimeMillis());//消息匹配给路由键值aa.INFO
        rabbitTemplate.convertAndSend("topicExchange-queueC", "aa.abcd", "msgC-abcd-"+System.currentTimeMillis());//消息匹配给路由键值aa.abcd
        return "testC ok";
    }


    /**
     * 发送消息同时接收返回对象
     * @return
     */
    @RequestMapping(value = "/testD")
    public String testD(){
        String msg = "msgA-"+System.currentTimeMillis();
        Object obj = rabbitTemplate.convertSendAndReceive("queueD", msg);
        System.out.println("--- testD obj : "+obj.toString());
        return "testD ok";
    }

    /**
     * 发送消息同时接收返回对象-发送对象消息
     * @return
     */
    @RequestMapping(value = "/testE")
    public String testE(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("abc", "value-abc");
        Object obj = rabbitTemplate.convertSendAndReceive("queueE", map);
        System.out.println("--- testE finished. obj : "+obj.toString());
        return "testE ok";
    }


}
