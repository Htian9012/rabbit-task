package com.config;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MQListener {
	/*
	//消费者：从队列queueA接收消息
	@RabbitListener(queues="queueA")
	public void receiverA1(String msg) {
		System.out.println("--- receiverA1 : "+msg);
	}
	//从队列queueA接收消息
	@RabbitListener(queues="queueA")
	public void receiverA2(String msg) {
		System.out.println("--- receiverA2 : "+msg);
	}
	////////////////////////一条消息发送给一个队列，一个队列可以有多个消费者，但每条消息只能被一个消费者消费一次 - end //////////////////////////
	
	
	//定义3个消费者都从队列queueB1接收消息
	@RabbitListener(queues="queueB1")
	public void receiverB1_1(String msg) {
		System.out.println("--- queueB1 receiverB1_1 : "+msg);
	}
	@RabbitListener(queues="queueB1")
	public void receiverB1_2(String msg) {
		System.out.println("--- queueB1 receiverB1_2 : "+msg);
	}
	@RabbitListener(queues="queueB1")
	public void receiverB1_3(String msg) {
		System.out.println("--- queueB1 receiverB1_3 : "+msg);
	}
	//定义3个消费者都从队列queueB2接收消息
	@RabbitListener(queues="queueB2")
	public void receiverB2_1(String msg) {
		System.out.println("--- queueB2 receiverB2_1 : "+msg);
	}
	@RabbitListener(queues="queueB2")
	public void receiverB2_2(String msg) {
		System.out.println("--- queueB2 receiverB2_2 : "+msg);
	}
	@RabbitListener(queues="queueB2")
	public void receiverB2_3(String msg) {
		System.out.println("--- queueB2 receiverB2_3 : "+msg);
	}
	/////////////////////// faout交换机，routingKey不起作用，消息发给交换机，交换机可以绑定多条队列，每条队列可以有多个消费者，但每个队列的一条消息只能被一个消费者消费一次 - end //////////////////////////



	//从队列queueDirectA接收消息
	@RabbitListener(queues="queueDirectA")
	public void receiverDirectA_1(String msg) {
		System.out.println("--- receiverDirectA_1 : "+msg);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("--- receiverDirectA_1 : end");
	}
	@RabbitListener(queues="queueDirectA")
	public void receiverDirectA_2(String msg) {
		System.out.println("--- receiverDirectA_2 : "+msg);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("--- receiverDirectA_2 : end");
	}
	//从队列queueDirectB接收消息
	@RabbitListener(queues="queueDirectB")
	public void receiverDirectB_1(String msg) {
		System.out.println("--- receiverDirectB_1 : "+msg);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("--- receiverDirectB_1 : end");
	}
	@RabbitListener(queues="queueDirectB")
	public void receiverDirectB_2(String msg) {
		System.out.println("--- receiverDirectB_2 : "+msg);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("--- receiverDirectB_2 : end");
	}




	
	
	//从队列queueC1接收消息
	@RabbitListener(queues="queueC1")
	public void receiverC1_1(String msg) {
		System.out.println("--- receiverC1_1 : "+msg);
	}
	@RabbitListener(queues="queueC1")
	public void receiverC1_2(String msg) {
		System.out.println("--- receiverC1_2 : "+msg);
	}
	//从队列queueC2接收消息
	@RabbitListener(queues="queueC2")
	public void receiverC2_1(String msg) {
		System.out.println("--- receiverC2_1 : "+msg);
	}
	@RabbitListener(queues="queueC2")
	public void receiverC2_2(String msg) {
		System.out.println("--- receiverC2_2 : "+msg);
	}
	//从队列queueC2接收消息
	@RabbitListener(queues="queueC3")
	public void receiverC3_1(String msg) {
		System.out.println("--- receiverC3_1 : "+msg);
	}
	@RabbitListener(queues="queueC3")
	public void receiverC3_2(String msg) {
		System.out.println("--- receiverC3_2 : "+msg);
	}
	/////////////////////// 一对多（订阅匹配） - end //////////////////////////
	
	
	//从队列queueD接收消息
	@RabbitListener(queues="queueD")
	public Object receiverD(String msg) {
		System.out.println("--- receiverD : "+msg);
		Object obj = "result-queueD : "+msg;
		return obj;
	}
	////////////////////////点对点（同步获取返回值） - end //////////////////////////
	
	
	//从队列queueD接收消息
	@RabbitListener(queues="queueE")
	public Object receiverE(Map<String,Object> map) {
		System.out.println("--- receiverD : "+map.get("abc"));
		Object obj = "result-queueE : "+map.get("abc");
		return obj;
	}
	////////////////////////点对点（同步获取返回值，传递对象） - end //////////////////////////
	*/
}
