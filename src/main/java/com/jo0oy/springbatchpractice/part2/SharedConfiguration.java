package com.jo0oy.springbatchpractice.part2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SharedConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job sharedJob() {
        return jobBuilderFactory.get("sharedJob")
                .incrementer(new RunIdIncrementer())
                .start(this.sharedStep())
                .next(this.sharedStep2())
                .build();
    }

    @Bean
    public Step sharedStep() {
        return stepBuilderFactory.get("sharedStep")
                .tasklet(((contribution, chunkContext) -> {
                    StepExecution stepExecution = contribution.getStepExecution();
                    ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
                    stepExecutionContext.putString("stepKey", "step execution step");

                    JobExecution jobExecution = stepExecution.getJobExecution();
                    JobInstance jobInstance = jobExecution.getJobInstance();
                    ExecutionContext jobExecutionExecutionContext = jobExecution.getExecutionContext();
                    jobExecutionExecutionContext.putString("jobKey", "job execution step");
                    JobParameters jobParameters = jobExecution.getJobParameters();

                    log.info("jobName : {}, stepName : {}, param : {}",
                            jobInstance.getJobName(),
                            stepExecution.getStepName(),
                            jobParameters.getLong("run.id"));

                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step sharedStep2() {
        return stepBuilderFactory.get("sharedStep2")
                .tasklet(((contribution, chunkContext) -> {
                    StepExecution stepExecution = contribution.getStepExecution();
                    ExecutionContext stepExecutionExecutionContext = stepExecution.getExecutionContext();

                    JobExecution jobExecution = stepExecution.getJobExecution();
                    ExecutionContext jobExecutionExecutionContext = jobExecution.getExecutionContext();

                    log.info("jobKey : {}, stepKey : {}",
                            jobExecutionExecutionContext.getString("jobKey", "emptyJobKey"),
                            stepExecutionExecutionContext.getString("stepKey", "emptyStepKey"));

                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

}
