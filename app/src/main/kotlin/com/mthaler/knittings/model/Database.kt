package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import com.mthaler.dbapp.model.Category
import com.mthaler.dbapp.model.Photo
import java.io.Serializable
import java.lang.IllegalArgumentException

data class Database(val knittings: List<Knitting>, val photos: List<Photo>, val categories: List<Category>, val needles: List<Needle>, val rows: List<RowCounter>) : Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            knittings = parcel.readParcelableArray(classLoader)!!.map { it as Knitting },
            photos = parcel.readParcelableArray(classLoader)!!.map { it as Photo },
            categories = parcel.readParcelableArray(classLoader)!!.map { it as Category },
            needles = parcel.readParcelableArray(classLoader)!!.map { it as Needle },
            rows = parcel.readParcelableArray(classLoader)!!.map { it as RowCounter}
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableArray(knittings.toTypedArray(), 0)
        parcel.writeParcelableArray(photos.toTypedArray(), 0)
        parcel.writeParcelableArray(categories.toTypedArray(), 0)
        parcel.writeParcelableArray(needles.toTypedArray(), 0)
        parcel.writeParcelableArray(rows.toTypedArray(), 0)
    }

    override fun describeContents(): Int = 0

    fun checkValidity() {
        checkPhotosValidity()
        checkKnittingsValidity()
    }

    private fun checkPhotosValidity() {
        val missing =  photos.map {it.ownerID}.toSet() - knittings.map { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException("Photos reference non-existing knittings with ids $missing")
        }
    }

    private fun checkKnittingsValidity() {
        val missingCategories = knittings.mapNotNull { it.category }.map { it.id }.toSet() - categories.map { it.id }.toSet()
        if (missingCategories.isNotEmpty()) {
            throw IllegalArgumentException("Knittings reference non-existing categories with ids $missingCategories")
        }
        val missingPhotos = knittings.mapNotNull { it.defaultPhoto }.map { it.id }.toSet() - photos.map { it.id }.toSet()
        if (missingPhotos.isNotEmpty()) {
            throw IllegalArgumentException("Knittings reference non-existing photos with ids $missingPhotos")
        }
    }

    companion object {

        val classLoader: ClassLoader = javaClass.classLoader!!

        @JvmField
        val CREATOR = object : Parcelable.Creator<Database> {
            override fun createFromParcel(parcel: Parcel) = Database(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Database>(size)
        }
    }
}