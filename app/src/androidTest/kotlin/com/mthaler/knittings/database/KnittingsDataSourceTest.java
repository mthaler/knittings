package com.mthaler.knittings.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import com.mthaler.knittings.model.Knitting;
import com.mthaler.knittings.model.Photo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class KnittingsDataSourceTest {

    @Before
    public void initDatabase() {
        deleteAllKnittings();
    }

    @After
    public void cleanDatabase() {
        deleteAllKnittings();
    }

    @Test
    public void testAddKnitting() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        assertTrue(ds.getAllKnittings().isEmpty());
        Knitting knitting1 = ds.createKnitting("test knitting 1", "first test knitting", new Date(), null, 3.0, 42.0, 4.0);
        Knitting knitting2 = ds.createKnitting("test knitting 2", "second test knitting", new Date(), null, 4.0, 43.0, 5.0);
        Knitting knitting3 = ds.createKnitting("test knitting 3", "third test knitting", new Date(), null, 2.0, 39.0, 3.0);
        assertEquals(3, ds.getAllKnittings().size());
        ArrayList<Knitting> knittings = new ArrayList<>();
        knittings.add(knitting1);
        knittings.add(knitting2);
        knittings.add(knitting3);
        assertEquals(knittings, ds.getAllKnittings());
    }

    @Test
    public void testDeleteKnitting() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        assertTrue(ds.getAllKnittings().isEmpty());
        Knitting knitting1 = ds.createKnitting("test knitting 1", "first test knitting", new Date(), null, 3.0, 42.0, 4.0);
        Knitting knitting2 = ds.createKnitting("test knitting 2", "second test knitting", new Date(), null, 4.0, 43.0, 5.0);
        Knitting knitting3 = ds.createKnitting("test knitting 3", "third test knitting", new Date(), null, 2.0, 39.0, 3.0);
        assertEquals(3, ds.getAllKnittings().size());
        ds.deleteKnitting(knitting2);
        assertEquals(2, ds.getAllKnittings().size());
        ArrayList<Knitting> knittings = new ArrayList<>();
        knittings.add(knitting1);
        knittings.add(knitting3);
        assertEquals(knittings, ds.getAllKnittings());
    }

    @Test
    public void testUpdateKnitting() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        assertTrue(ds.getAllKnittings().isEmpty());
        Knitting knitting1 = ds.createKnitting("test knitting 1", "first test knitting", new Date(), null, 3.0, 42.0, 4.0);
        Knitting knitting2 = ds.createKnitting("test knitting 2", "second test knitting", new Date(), null, 4.0, 43.0, 5.0);
        Knitting knitting3 = ds.createKnitting("test knitting 3", "third test knitting", new Date(), null, 2.0, 39.0, 3.0);
        assertEquals(3, ds.getAllKnittings().size());
        Knitting updated = knitting2.copy(knitting2.getId(), knitting2.getTitle(), "Updated knitting", knitting2.getStarted(), knitting2.getFinished(),
                knitting2.getNeedleDiameter(), knitting2.getSize(), knitting2.getDefaultPhoto(), knitting2.getRating());
        Knitting result = ds.updateKnitting(updated);
        assertEquals(updated, result);
        assertEquals(3, ds.getAllKnittings().size());
        ArrayList<Knitting> knittings = new ArrayList<>();
        knittings.add(knitting1);
        knittings.add(updated);
        knittings.add(knitting3);
        assertEquals(knittings, ds.getAllKnittings());
    }

    @Test
    public void testGetKnitting() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        assertTrue(ds.getAllKnittings().isEmpty());
        Knitting knitting1 = ds.createKnitting("test knitting 1", "first test knitting", new Date(), null, 3.0, 42.0, 4.0);
        Knitting knitting2 = ds.createKnitting("test knitting 2", "second test knitting", new Date(), null, 4.0, 43.0, 5.0);
        Knitting knitting3 = ds.createKnitting("test knitting 3", "third test knitting", new Date(), null, 2.0, 39.0, 3.0);
        assertEquals(3, ds.getAllKnittings().size());
        assertEquals(knitting1, ds.getKnitting(knitting1.getId()));
        assertEquals(knitting2, ds.getKnitting(knitting2.getId()));
        assertEquals(knitting3, ds.getKnitting(knitting3.getId()));
    }

    @Test
    public void testCreatePhoto() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        assertTrue(ds.getAllKnittings().isEmpty());
        Knitting knitting1 = ds.createKnitting("test knitting 1", "first test knitting", new Date(), null, 3.0, 42.0, 4.0);
        Knitting knitting2 = ds.createKnitting("test knitting 2", "second test knitting", new Date(), null, 4.0, 43.0, 5.0);
        Knitting knitting3 = ds.createKnitting("test knitting 3", "third test knitting", new Date(), null, 2.0, 39.0, 3.0);
        assertEquals(3, ds.getAllKnittings().size());
        assertTrue(ds.getAllPhotos().isEmpty());
        Photo photo = ds.createPhoto(new File("/path/to/photo"), knitting2.getId(), null, "test");
        assertEquals(1, ds.getAllPhotos().size());
        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(photo);
        assertEquals(photos, ds.getAllPhotos());
        assertTrue(ds.getAllPhotos(knitting1).isEmpty());
        assertEquals(1, ds.getAllPhotos(knitting2).size());
        assertEquals(photos, ds.getAllPhotos(knitting2));
        assertTrue(ds.getAllPhotos(knitting3).isEmpty());
        ds.deleteKnitting(knitting2);
        assertTrue(ds.getAllPhotos().isEmpty());
    }

    @Test
    public void testUpdatePhoto() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        assertTrue(ds.getAllKnittings().isEmpty());
        assertTrue(ds.getAllPhotos().isEmpty());
        Knitting knitting = ds.createKnitting("test knitting 1", "first test knitting", new Date(), null, 3.0, 42.0, 4.0);
        assertEquals(1, ds.getAllKnittings().size());
        assertTrue(ds.getAllPhotos().isEmpty());
        Photo photo = ds.createPhoto(new File("/path/to/photo"), knitting.getId(), null, "test");
        assertEquals(1, ds.getAllPhotos().size());
        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(photo);
        assertEquals(photos, ds.getAllPhotos());
        Photo updated = photo.copy(photo.getId(), photo.getFilename(), photo.getKnittingID(), "updated description", photo.getPreview());
        ds.updatePhoto(updated);
        ArrayList<Photo> photos2 = new ArrayList<>();
        photos2.add(updated);
        assertEquals(photos2, ds.getAllPhotos());
    }

    private void deleteAllKnittings() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        for (Knitting knitting : ds.getAllKnittings()) {
            ds.deleteKnitting(knitting);
        }
    }
}
