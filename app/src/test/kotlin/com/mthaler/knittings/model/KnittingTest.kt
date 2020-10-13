package com.mthaler.knittings.model

import android.os.Build
import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mthaler.dbapp.model.Photo
import com.mthaler.dbapp.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class KnittingTest {

    @Test
    fun testSerializeDeserialize() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        val k1 = SerializeUtils.deserialize<Knitting>(SerializeUtils.serialize(k0))
        assertEquals(k0, k1)
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        c.set(2018, 0, 11)
        val finished = c.time
        val k2 = Knitting(42, "knitting", "my first knitting", started, finished, "3.0", "41.0", p0, 5.0)
        val k3 = SerializeUtils.deserialize<Knitting>(SerializeUtils.serialize(k2))
        assertEquals(k2, k3)
    }

    @Test
    fun testParcelable() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        val p0 = Parcel.obtain()
        k0.writeToParcel(p0, 0)
        p0.setDataPosition(0)
        val k1 = Knitting.CREATOR.createFromParcel(p0)
        assertEquals(k0, k1)
        val ph0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        c.set(2018, 0, 11)
        val finished = c.time
        val k2 = Knitting(42, "knitting", "my first knitting", started, finished, "3.0", "41.0", ph0, 5.0)
        val p1 = Parcel.obtain()
        k2.writeToParcel(p1, 0)
        p1.setDataPosition(0)
        val k3 = Knitting.CREATOR.createFromParcel(p1)
        assertEquals(k2, k3)
    }
}