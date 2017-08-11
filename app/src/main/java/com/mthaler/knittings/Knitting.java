package com.mthaler.knittings;

import java.util.Date;
import java.util.UUID;

/**
 * The Knitting class stores data for a single knitting
 */
public class Knitting {
    private final UUID id;
    private String title;
    private String description;
    private Date started;
    private Date finished;

    public Knitting() {
        // Generate unique identifier
        id = UUID.randomUUID();
        title = "title";
        description = "description";
        started = new Date();
        finished = new Date();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return title;
    }
}
