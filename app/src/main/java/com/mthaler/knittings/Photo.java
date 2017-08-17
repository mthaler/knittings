package com.mthaler.knittings;

import android.graphics.Bitmap;

import java.io.File;

/**
 * The photo class represents a photo. It has an id (used in the database), a filename and an
 * optional preview that can be displayed in the knittings list
 */
public class Photo {

    private final long id;
    private final File filename;
    private final long knittingID;
    private Bitmap preview;
    private final String description;

    public Photo(long id, File filename, long knittingID, String description) {
        this.id = id;
        this.filename = filename;
        this.knittingID = knittingID;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public File getFilename() {
        return filename;
    }

    public long getKnittingID() {
        return knittingID;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }

    public String getDescription() {
        return description;
    }
}
