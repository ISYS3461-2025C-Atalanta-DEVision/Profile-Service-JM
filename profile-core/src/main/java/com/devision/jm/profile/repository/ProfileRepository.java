package com.devision.jm.profile.repository;

import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.model.enums.SubscriptionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Profile Repository
 *
 * Data access layer for Profile entity.
 * Implements A.2.2: Repository Layer (Data Access Layer)
 */
@Repository
public interface ProfileRepository extends MongoRepository<Profile, String> {

    /**
     * Find profile by userId (from Auth Service)
     */
    Optional<Profile> findByUserId(String userId);

    /**
     * Find profile by email
     */
    Optional<Profile> findByEmailIgnoreCase(String email);

    /**
     * Check if profile exists for userId
     */
    boolean existsByUserId(String userId);

    /**
     * Check if email is already in use by another profile
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find profiles by country (for sharding/analytics)
     */
    List<Profile> findByCountry(String country);

    /**
     * Search profiles by company name (partial match)
     */
    List<Profile> findByCompanyNameContainingIgnoreCase(String companyName);

    /**
     * Find profiles by email or company name (admin search)
     */
    List<Profile> findByEmailContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            String email, String companyName);

    // ==================== Subscription Notification Queries (6.1.2) ====================

    /**
     * Find profiles expiring in a date range that haven't been notified yet
     * Used to send 7-day expiry warning notifications
     */
    @Query("{ 'subscription_end_date': { $gte: ?0, $lte: ?1 }, 'subscription_type': { $ne: 'EXPIRED' }, 'expiry_notification_sent': { $ne: true } }")
    List<Profile> findProfilesExpiringBetweenAndNotNotified(LocalDateTime start, LocalDateTime end);

    /**
     * Find profiles that have expired and haven't been notified
     */
    @Query("{ 'subscription_end_date': { $lt: ?0 }, 'subscription_type': { $ne: 'EXPIRED' }, 'expired_notification_sent': { $ne: true } }")
    List<Profile> findExpiredProfilesNotNotified(LocalDateTime now);

    /**
     * Find profiles by subscription type
     */
    List<Profile> findBySubscriptionType(SubscriptionType subscriptionType);
}
