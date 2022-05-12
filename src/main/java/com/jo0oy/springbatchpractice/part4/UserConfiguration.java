package com.jo0oy.springbatchpractice.part4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernatePagingItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.HibernatePagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UserConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final UserRepository userRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job userJob() throws Exception {
        return this.jobBuilderFactory.get("userJob")
                .incrementer(new RunIdIncrementer())
                .start(this.saveUserStep())
                .next(this.userLevelUpStep())
                .listener(new LevelUpJobExecutionListener(userRepository))
                .next(new JobParameterDecide("date"))
                .on(JobParameterDecide.CONTINUE.getName())
                .to(this.orderStatisticsStep(null))
                .build()
                .build();
    }

    @Bean
    public Step saveUserStep() {
        return this.stepBuilderFactory.get("saveUserStep")
                .tasklet(new SaveUserTasklet(userRepository))
                .build();
    }

    @Bean
    public Step userLevelUpStep() throws Exception {
        return this.stepBuilderFactory.get("userLevelUpStep")
                .<User, User>chunk(CHUNK_SIZE)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .listener(new LevelUpStepExecutionListener())
                .build();
    }

    @Bean
    @JobScope
    public Step orderStatisticsStep(@Value("#{jobParameters[date]}") String date) throws Exception {
        return this.stepBuilderFactory.get("orderStatisticsStep")
                .<OrderStatistics, OrderStatistics>chunk(CHUNK_SIZE)
                .reader(orderStatisticsItemReader(date))
                .writer(orderStatisticsItemWriter(date))
                .build();
    }

    private ItemWriter<? super OrderStatistics> orderStatisticsItemWriter(String date) throws Exception {
        YearMonth yearMonth = YearMonth.parse(date);

        String fileName = yearMonth.getYear() + "년_" + yearMonth.getMonthValue() + "월_일별_주문_금액.csv";

        BeanWrapperFieldExtractor<OrderStatistics> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"amount", "date"});

        DelimitedLineAggregator<OrderStatistics> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<OrderStatistics> itemWriter = new FlatFileItemWriterBuilder<OrderStatistics>()
                .resource(new FileSystemResource("output/" + fileName))
                .lineAggregator(lineAggregator)
                .encoding("UTF-8")
                .name("orderStatisticsItemWriter")
                .headerCallback(writer -> writer.write("total_amount, date"))
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    private ItemReader<? extends OrderStatistics> orderStatisticsItemReader(String date) throws Exception {
        YearMonth yearMonth = YearMonth.parse(date);

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", LocalDateTime.of(yearMonth.atDay(1), LocalTime.of(0, 0, 0)));
        params.put("endDate", LocalDateTime.of(yearMonth.atEndOfMonth(), LocalTime.of(23, 59, 59)));

        Map<String, Order> sorting = new HashMap<>();
        sorting.put("date", Order.ASCENDING);

        JdbcPagingItemReader<OrderStatistics> itemReader = new JdbcPagingItemReaderBuilder<OrderStatistics>()
                .dataSource(dataSource)
                .rowMapper((rs, rowNum) -> OrderStatistics.builder()
                        .amount(rs.getString(1))
                        .date(LocalDate.parse(rs.getString(2)))
                        .build())
                .pageSize(CHUNK_SIZE)
                .name("orderStatisticsItemReader")
                .selectClause("sum(amount), date_format(created_date_time, '%Y-%m-%d') as date")
                .fromClause("orders")
                .whereClause("created_date_time >= :startDate and created_date_time <= :endDate")
                .groupClause("date_format(created_date_time, '%Y-%m-%d')")
                .parameterValues(params)
                .sortKeys(sorting)
                .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }

    private ItemWriter<? super User> itemWriter() {
        return users ->
            users.forEach(x -> {
                log.info("level up step : item writer");
                    x.levelUp();
                    userRepository.save(x);
            });
    }

    private ItemProcessor<? super User, ? extends User> itemProcessor() {
        return user -> {
            if (user.availableLevelUp()) {
                return user;
            }

            return null;
        };
    }

    private ItemReader<? extends User> itemReader() throws Exception {
//        JpaPagingItemReader<User> itemReader = new JpaPagingItemReaderBuilder<User>()
//                .queryString("select u from User u")
//                .entityManagerFactory(entityManagerFactory)
//                .pageSize(CHUNK_SIZE)
//                .name("userItemReader")
//                .build();

        HibernatePagingItemReader<User> itemReader = new HibernatePagingItemReaderBuilder<User>()
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .useStatelessSession(false)
                .queryString("select u from User u")
                .name("userItemReader")
                .pageSize(CHUNK_SIZE)
                .build();

        itemReader.afterPropertiesSet();

        return itemReader;
    }

}
