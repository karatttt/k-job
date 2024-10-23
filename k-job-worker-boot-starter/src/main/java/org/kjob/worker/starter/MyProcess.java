package org.kjob.worker.starter;

import lombok.extern.slf4j.Slf4j;
import org.kjob.worker.processor.ProcessResult;
import org.kjob.worker.processor.task.TaskContext;
import org.kjob.worker.processor.type.BasicProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "testProcessor")
public class MyProcess implements BasicProcessor {
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        log.info("单机处理器正在处理");
        log.info(context.getJobParams());
        System.out.println("用户:" + context.getJobParams()+ "该吃药啦！");
        boolean success = true;
        return new ProcessResult(success, context + ": " + success);
    }
}
