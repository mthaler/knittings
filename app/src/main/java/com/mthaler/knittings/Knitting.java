package com.mthaler.knittings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private final List<Photo> photos;

    public Knitting(long id) {
        this.id = id;
        title = "";
        description = "";
        started = new Date();
        finished = null;
        needleDiameter = 0.0;
        size = 0.0;
        photos = new ArrayList<>();
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

    public String getPhotoFilename() {
        return "IMG_" + id + ".jpg";
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    @Override
    public String toString() {
        return title;
    }
}
