package com.runner;

import com.amq.base.TaskData;
import com.amq.base.TaskRunner;
import org.springframework.stereotype.Component;
/**
 * runner类，执行mq队列分发的任务
 * */
@Component
public class TestRunner extends TaskRunner<String,String> {

    @Override
    public String runTask(TaskData<String, String> taskData) throws Exception {
        System.out.println("模拟业务处理参数"+taskData.getTaskParam());
        return "处理成功:返回结果";
    }
}
