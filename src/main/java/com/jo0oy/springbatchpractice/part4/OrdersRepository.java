package com.jo0oy.springbatchpractice.part4;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    @Query(value = "select sum(o.amount) from Orders o where o.user.id = :userId")
    long findSumAmountByUserId(@Param(value = "userId") Long userId);
}
