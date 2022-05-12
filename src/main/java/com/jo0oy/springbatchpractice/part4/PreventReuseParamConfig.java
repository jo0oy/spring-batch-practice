package com.jo0oy.springbatchpractice.part4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PreventReuseParamConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job testParamJob() {
        return this.jobBuilderFactory.get("testParamJob")
                .incrementer(new RunIdIncrementer())
                .start(this.testParamStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step testParamStep(@Value("#{jobParameters[date]}") String date) {
        return this.stepBuilderFactory.get("testParamStep")
                .tasklet(((contribution, chunkContext) -> {
                    log.info("testing params step>>>>>>>>");

                    log.info("date = {} >>>>>>>>>", date);

                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

}
