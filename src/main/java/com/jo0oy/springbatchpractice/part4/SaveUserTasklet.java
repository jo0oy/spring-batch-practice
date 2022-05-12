package com.jo0oy.springbatchpractice.part4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet {

    private final UserRepository userRepository;
    private final int SIZE = 400;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<User> users = createUsers();

        Collections.shuffle(users);

        userRepository.saveAll(users);

        return RepeatStatus.FINISHED;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        int[] amountArr = new int[]{5000, 10_000, 30_000, 50_000, 100_000, 150_000, 200_000, 300_000, 400_000, 500_000};

        for (int i = 1; i <= SIZE; i++) {
            List<Orders> orders = new ArrayList<>();

            for (int j = 0; j < (int)((Math.random() * 3) + 1); j++) {
                orders.add(Orders.createOrders("itemName" + (int)((Math.random()*100)+1),
                        amountArr[(int)(Math.random()*9)],
                        LocalDateTime.of(LocalDate.of(2021, 11, (int) (Math.random() * 10 + 1)), LocalTime.of(10, 0, 0)))
                );
            }

            users.add(User.createUser("username" + i, orders));
        }

        return users;
    }
}
