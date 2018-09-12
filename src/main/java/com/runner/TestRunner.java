package com.runner;

import com.amq.base.TaskData;
import com.amq.base.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
/**
 * runner类，执行mq队列分发的任务
 * */
@Component
public class TestRunner extends TaskRunner<String,String> {

    private static final Logger log = LoggerFactory.getLogger(TestRunner.class);

    @Override
    public String runTask(TaskData<String, String> taskData) throws Exception {
        log.info("模拟业务处理,参数:{} ",taskData.getTaskParam());
        return "处理成功,返回结果";
    }
}
