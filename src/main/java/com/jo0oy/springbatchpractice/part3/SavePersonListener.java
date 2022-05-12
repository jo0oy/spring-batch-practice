package com.jo0oy.springbatchpractice.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;

@Slf4j
public class SavePersonListener {
    public static class SavePersonJobExecutionListener implements JobExecutionListener {

        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("beforeJob");
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            log.info("afterJob total cnt : {}", jobExecution.getStepExecutions()
                    .stream().mapToInt(StepExecution::getWriteCount).sum());
        }
    }

    public static class SavePersonAnnotationJobExecutionListener {
        @BeforeJob
        public void beforeJob(JobExecution jobExecution) {
            log.info("annotation beforeJob");
        }

        @AfterJob
        public void afterJob(JobExecution jobExecution) {
            log.info("annotation afterJob total cnt : {}", jobExecution.getStepExecutions()
                    .stream().mapToInt(StepExecution::getWriteCount).sum());
        }
    }

    public static class SavePersonAnnotationStepExecutionListener {
        @BeforeStep
        public void beforeStep(StepExecution stepExecution) {
            log.info("beforeStep");
        }

        @AfterStep
        public void afterStep(StepExecution stepExecution) {
            log.info("afterStep total cnt : {}", stepExecution.getWriteCount());
        }
    }

}
