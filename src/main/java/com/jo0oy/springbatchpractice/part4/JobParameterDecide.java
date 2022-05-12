package com.jo0oy.springbatchpractice.part4;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

@Slf4j
@RequiredArgsConstructor
public class JobParameterDecide implements JobExecutionDecider {

    public static final FlowExecutionStatus CONTINUE = new FlowExecutionStatus("CONTINUE");
    private final String key;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        String value = jobExecution.getJobParameters().getString(key);
        log.info("key : {}, value : {}", key, value);

        if (StringUtils.isEmpty(value)) {
            return FlowExecutionStatus.COMPLETED;
        }

        return CONTINUE;
    }
}
