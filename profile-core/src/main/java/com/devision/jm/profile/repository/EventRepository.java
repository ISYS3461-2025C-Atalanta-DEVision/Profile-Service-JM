package com.devision.jm.profile.repository;

import com.devision.jm.profile.model.entity.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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

    // ==================== Core Lookups ====================

    /**
     * Find event by custom eventId
     */
    Optional<Event> findByEventId(String eventId);


    /**
     * Find all events created by a company
     */
    List<Event> findByCompanyId(String companyId);

}