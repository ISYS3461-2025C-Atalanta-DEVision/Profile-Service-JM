package com.devision.jm.profile.repository;

import com.devision.jm.profile.model.entity.Event;
import com.devision.jm.profile.model.enums.EventStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Event Repository
 *
 * Data access layer for Event entity.
 * Implements A.2.2: Repository Layer (Data Access Layer)
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    /**
     * Find all events by company ID
     *
     * @param companyId Company ID (Profile.userId)
     * @return List of events for the company
     */
    List<Event> findByCompanyId(String companyId);

    /**
     * Find events by company ID and status
     *
     * @param companyId Company ID (Profile.userId)
     * @param status Event status
     * @return List of events matching criteria
     */
    List<Event> findByCompanyIdAndStatus(String companyId, EventStatus status);

    /**
     * Find event by event ID
     *
     * @param eventId Business event ID
     * @return Event if found
     */
    Optional<Event> findByEventId(String eventId);

    /**
     * Check if event exists by event ID
     *
     * @param eventId Business event ID
     * @return true if exists
     */
    boolean existsByEventId(String eventId);

    /**
     * Delete event by event ID
     *
     * @param eventId Business event ID
     */
    void deleteByEventId(String eventId);

    /**
     * Delete all events by company ID
     * Used when a company/user is deleted from Auth Service
     *
     * @param companyId Company ID (Profile.userId)
     */
    void deleteByCompanyId(String companyId);
}
