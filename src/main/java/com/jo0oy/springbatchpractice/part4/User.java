package com.jo0oy.springbatchpractice.part4;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Level level;

    private LocalDateTime updatedDateTime;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Orders> orders = new ArrayList<>();

    @Builder
    private User(String username, Level level) {
        this.username = username;
        this.level = level;
    }

    // 연관관계 메서드
    public void addOrders(Orders order) {
        this.orders.add(order);
        order.setUser(this);
    }

    // 생성메서드
    public static User createUser(String username, List<Orders> orders) {
        User user = User.builder()
                .username(username)
                .level(Level.NORMAL)
                .build();

        for (Orders order : orders) {
            user.addOrders(order);
        }

        return user;
    }

    //==비즈니스 로직==//
    public int getTotalAmount() {
        return this.orders.stream()
                .mapToInt(Orders::getAmount)
                .sum();
    }

    public boolean availableLevelUp() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
    }

    public void levelUp() {
        this.level = Level.getNextLevel(this.getTotalAmount());
        this.updatedDateTime = LocalDateTime.now();
    }

    @Getter
    public enum Level {
        VIP(500_000, null),
        GOLD(500_000, VIP),
        SILVER(300_000, GOLD),
        NORMAL(200_000, SILVER);

        private final int nextAmount;
        private final Level nextLevel;

        Level(int nextAmount, Level nextLevel) {
            this.nextAmount = nextAmount;
            this.nextLevel = nextLevel;
        }

        private static boolean availableLevelUp(Level level, int totalAmount) {
            if (Objects.isNull(level)) {
                return false;
            }

            if (Objects.isNull(level.getNextLevel())) {
                return false;
            }

            return totalAmount >= level.getNextAmount();
        }

        private static Level getNextLevel(int totalAmount) {
            if (totalAmount >= Level.VIP.getNextAmount()) {
                return Level.VIP;
            }

            if (totalAmount >= Level.GOLD.getNextAmount()) {
                return Level.GOLD.getNextLevel();
            }

            if (totalAmount >= Level.SILVER.getNextAmount()) {
                return Level.SILVER.getNextLevel();
            }

            if (totalAmount >= Level.NORMAL.getNextAmount()) {
                return Level.NORMAL.getNextLevel();
            }

            return Level.NORMAL;
        }
    }
}
