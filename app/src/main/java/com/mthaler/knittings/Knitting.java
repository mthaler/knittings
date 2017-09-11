package com.mthaler.knittings;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Knitting class stores data for a single knitting
 */
public class Knitting {
    private final long id;
    private String title;
    private String description;
    private Date started;
    private Date finished;
    private double needleDiameter;
    private double size;
    private Photo defaultPhoto;
    private double rating;

    public Knitting(long id) {
        this.id = id;
        title = "";
        description = "";
        started = new Date();
        finished = null;
        needleDiameter = 0.0;
        size = 0.0;
        rating = 0.0;
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

    public double getNeedleDiameter() {
        return needleDiameter;
    }

    public void setNeedleDiameter(double needleDiameter) {
        this.needleDiameter = needleDiameter;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Photo getDefaultPhoto() {
        return defaultPhoto;
    }

    public void setDefaultPhoto(Photo defaultPhoto) {
        this.defaultPhoto = defaultPhoto;
    }

    public String getPhotoFilename() {
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        return "IMG_" + timeStamp + ".jpg";
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return title;
    }
}
