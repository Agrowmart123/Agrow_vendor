package com.agrowmart.repository;


import com.agrowmart.entity.Subscription;
import com.agrowmart.entity.User;
import com.google.common.base.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s " +
           "WHERE s.active = true " +
           "AND s.expiryDate < :now")
    List<Subscription> findAllExpiredActiveSubscriptions(LocalDateTime now);
    
 
    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.active = true AND s.expiryDate > :now ORDER BY s.startDate DESC LIMIT 1")
    Optional<Subscription> findLatestActiveSubscription(@Param("user") User user, @Param("now") LocalDateTime now);


	java.util.Optional<Subscription> findFirstByUserAndActiveTrueAndExpiryDateAfterOrderByStartDateDesc(User user,
			LocalDateTime now);


}