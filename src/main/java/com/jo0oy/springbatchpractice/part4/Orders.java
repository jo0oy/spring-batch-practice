package com.jo0oy.springbatchpractice.part4;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long id;

    private String itemName;

    private int amount;

    private LocalDateTime createdDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    private Orders(String itemName, int amount, LocalDateTime createdDateTime) {
        this.itemName = itemName;
        this.amount = amount;
        this.createdDateTime = createdDateTime;
    }

    //연관관계 메서드
    public void setUser(User user) {
        this.user = user;
    }

    //생성 메서드
    public static Orders createOrders(String itemName, int amount, LocalDateTime createdDateTime) {
        Orders order = Orders.builder()
                .itemName(itemName)
                .amount(amount)
                .createdDateTime(createdDateTime)
                .build();

        return order;
    }
}
