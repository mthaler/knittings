package com.mthaler.knittings.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.mthaler.knittings.model.Knitting;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class KnittingsDataSourceTest {

    @Before
    public void initDatabase() {
        // delete all knittings
        Context ctx = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(ctx);
        for (Knitting knitting : ds.getAllKnittings()) {
            ds.deleteKnitting(knitting);
        }
    }

    @Test
    public void testAddKnitting() {
        // Context of the app under test.
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
}
