package com.mthaler.knittings.model

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mthaler.knittings.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NeedleTest {

    @Test
    fun testSerializeDeserialize() {
        val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET)
        val n1 = SerializeUtils.deserialize<Needle>(SerializeUtils.serialize(n0))
        assertEquals(n0, n1)
    }

    @Test
    fun testParcelable() {
        val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET)
        val p0 = Parcel.obtain()
        n0.writeToParcel(p0, 0)
        p0.setDataPosition(0)
        val n1 = Needle.CREATOR.createFromParcel(p0)
        assertEquals(n0, n1)
    }
}