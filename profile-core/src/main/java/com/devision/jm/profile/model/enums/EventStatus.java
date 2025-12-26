package com.devision.jm.profile.model.enums;

/**
 * Event Status Enum
 *
 * Represents the lifecycle status of an event.
 */
public enum EventStatus {

    /**
     * Event created, waiting for file uploads to complete
     */
    PENDING,

    /**
     * Event is active and visible
     */
    ACTIVE,

    /**
     * File upload failed
     */
    FAILED
}
