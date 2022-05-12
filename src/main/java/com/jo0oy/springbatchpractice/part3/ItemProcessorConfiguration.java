package com.jo0oy.springbatchpractice.part3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ItemProcessorConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job itemProcessorJob() throws Exception {
        return this.jobBuilderFactory.get("itemProcessorJob")
                .incrementer(new RunIdIncrementer())
                .start(this.itemProcessorStep())
                .build();
    }

    @Bean
    public Step itemProcessorStep() {
        return this.stepBuilderFactory.get("itemProcessorStep")
                .<Person, Person>chunk(10)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }


    private ItemWriter<Person> itemWriter() {
        return items -> log.info(items.stream().map(Person::getId).map(String::valueOf).collect(Collectors.joining(", ")));
    }

    private ItemProcessor<? super Person, ? extends Person> itemProcessor() {
        return item -> {
            if (item.getId() % 2 == 0) {
                return item;
            }

            return null;
        };
    }

    private ItemReader<Person> itemReader() {
        return new CustomItemReader<>(getItems());
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            items.add(new Person(i, "test name" + i, i, "test address"));
        }

        return items;
    }
}
