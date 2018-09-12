# rabbit-task
springboot rabbitMq
 * 程序启动会先缓存所有TaskRunner实例，并启动所有的mq任务监听器。
 * 通过自定义的MessageListenerAdapter来分发不同的任务到不同的队列中，并根据配置选择相应的TaskRunner去执行队列任务
 * 简化调用方法，如下：
 ![图片](https://raw.githubusercontent.com/HTian1992/rabbit-task/master/doc/003.png)
 ![图片](https://raw.githubusercontent.com/HTian1992/rabbit-task/master/doc/002.png)
 ![图片](https://raw.githubusercontent.com/HTian1992/rabbit-task/master/doc/001.png)