package com.devision.jm.profile.repository;

import com.devision.jm.profile.model.entity.Event;
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
}
