package com.jo0oy.springbatchpractice.part3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SavePersonConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job savePersonJob() throws Exception {
        return this.jobBuilderFactory.get("savePersonJob")
                .incrementer(new RunIdIncrementer())
                .start(this.savePersonStep(null))
                .listener(new SavePersonListener.SavePersonJobExecutionListener())
                .listener(new SavePersonListener.SavePersonAnnotationJobExecutionListener())
                .build();
    }

    @Bean
    @JobScope
    public Step savePersonStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) throws Exception {
        return this.stepBuilderFactory.get("savePersonStep")
                .<Person,Person>chunk(20)
                .reader(itemReader())
                .processor(itemProcessor(allowDuplicate))
                .writer(itemWriter())
                .listener(new SavePersonListener.SavePersonAnnotationStepExecutionListener())
                .faultTolerant()
                .skip(NotFoundNameException.class)
                .skipLimit(2)
                .build();
    }

    private ItemProcessor<? super Person, ? extends Person> itemProcessor(String allowDuplicate) {
        DuplicateValidateProcessor<Person> duplicateValidateProcessor
                = new DuplicateValidateProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate));

        ItemProcessor<Person, Person> emptyNameValidationProcessor = item -> {
            if (item.isNotEmptyName()) {
                return item;
            }

            throw new NotFoundNameException();
        };

        return new CompositeItemProcessorBuilder<Person, Person>()
                .delegates(new PersonValidationProcessor(), emptyNameValidationProcessor,
                        duplicateValidateProcessor)
                .build();
    }

    private ItemWriter<? super Person> itemWriter() throws Exception {

        JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(entityManagerFactory)
                .build();

        ItemWriter<Person> logItemWriter = items ->  log.info("inserted persons size : {}", items.size());

        CompositeItemWriter<Person> compositeItemWriter = new CompositeItemWriterBuilder<Person>()
                .delegates(jpaItemWriter, logItemWriter)
                .build();

        compositeItemWriter.afterPropertiesSet();

        return compositeItemWriter;
    }

    private ItemReader<? extends Person> itemReader() throws Exception {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "age", "address");
        lineMapper.setLineTokenizer(tokenizer);

        lineMapper.setFieldSetMapper(fieldSet -> new Person(
                fieldSet.readString("name"),
                fieldSet.readInt("age"),
                fieldSet.readString("address")
        ));

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
                .name("itemReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("persons.csv"))
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }
}
