package com.mthaler.knittings;

import java.util.Date;
import java.util.UUID;

/**
 * The Knitting class stores data for a single knitting
 */
public class Knitting {
    private final long id;
    private String title;
    private String description;
    private Date started;
    private Date finished;

    public Knitting(long id) {
        this.id = id;
        title = "";
        description = "";
        started = new Date();
        finished = new Date();
    }

    public long getId() {
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

    public String getPhotoFilename() {
        return "IMG_" + id + ".jpg";
    }

    @Override
    public String toString() {
        return title;
    }
}
