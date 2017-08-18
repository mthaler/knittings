package com.mthaler.knittings;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
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
    private String description;

    public Photo(long id, File filename, long knittingID) {
        this.id = id;
        this.filename = filename;
        this.knittingID = knittingID;
        this.description = "";
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

    public void setDescription(String description) {
        this.description = description;
    }

    public static byte[] getBytes(Bitmap preview) {
        if (preview != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            preview.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return out.toByteArray();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", filename=" + filename +
                ", knittingID=" + knittingID +
                ", description='" + description + '\'' +
                '}';
    }
}
