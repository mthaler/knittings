package com.mthaler.knittings.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class KnittingsDataSourceTest {
    @Test
    public void testAddKnitting() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        KnittingsDataSource ds = KnittingsDataSource.Companion.getInstance(appContext);
        assertTrue(ds.getAllKnittings().isEmpty());
    }
}
