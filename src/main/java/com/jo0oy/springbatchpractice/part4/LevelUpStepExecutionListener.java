package com.jo0oy.springbatchpractice.part4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class LevelUpStepExecutionListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("before level up step");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("after level up step");
        return stepExecution.getExitStatus();
    }
}
