package com.jo0oy.springbatchpractice.part4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class LevelUpJobExecutionListener implements JobExecutionListener {

    private final UserRepository userRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        LocalDateTime startTime = jobExecution.getStartTime()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        LocalDateTime endTime = jobExecution.getEndTime()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        Collection<User> updatedUsers = userRepository.findAllByUpdatedDateTimeBetween(startTime, endTime);

        long totalTime = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();

        log.info("회원 등급 업데이트 배치 프로그램");
        log.info("--------------------------");
        log.info("총 데이터 업데이트 {}건, 처리시간 {}", updatedUsers.size(), totalTime);

    }
}