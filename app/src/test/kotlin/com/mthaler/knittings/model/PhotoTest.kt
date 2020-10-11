package com.mthaler.knittings.model

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mthaler.dbapp.model.Photo
import com.mthaler.knittings.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PhotoTest {

    @Test
    fun testSerializeDeserialize() {
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val p1 = SerializeUtils.deserialize<Photo>(SerializeUtils.serialize(p0))
        assertEquals(p0, p1)
    }

    @Test
    fun testParcelable() {
        val ph0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val p0 = Parcel.obtain()
        ph0.writeToParcel(p0, 0)
        p0.setDataPosition(0)
        val ph1 = Photo.CREATOR.createFromParcel(p0)
        assertEquals(ph0, ph1)
    }
}