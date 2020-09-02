package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.lang.IllegalArgumentException

data class Database(val knittings: List<Knitting>, val photos: List<Photo>, val categories: List<Category>, val needles: List<Needle>) : Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            knittings = parcel.readParcelableArray(classLoader).map { it as Knitting },
            photos = parcel.readParcelableArray(classLoader).map { it as Photo },
            categories = parcel.readParcelableArray(classLoader).map { it as Category },
            needles = parcel.readParcelableArray(classLoader).map { it as Needle }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableArray(knittings.toTypedArray(), 0)
        parcel.writeParcelableArray(photos.toTypedArray(), 0)
        parcel.writeParcelableArray(categories.toTypedArray(), 0)
        parcel.writeParcelableArray(needles.toTypedArray(), 0)
    }

    override fun describeContents(): Int = 0

    fun checkValidity() {
        checkPhotosValidity()
        checkKnittingsValidity()
    }

    private fun checkPhotosValidity() {
        val missing =  photos.map {it.ownerID}.toSet() - knittings.map { it.id }.toSet()
        if (missing.size > 0) {
            throw IllegalArgumentException("Photos reference non-existing knittings with ids $missing")
        }
    }

    private fun checkKnittingsValidity() {
        val missingCategories = knittings.mapNotNull { it.category }.map { it.id }.toSet() - categories.map { it.id }.toSet()
        if (missingCategories.size > 0) {
            throw IllegalArgumentException("Knittings reference non-existing categories with ids $missingCategories")
        }
        val missingPhotos = knittings.mapNotNull { it.defaultPhoto }.map { it.id }.toSet() - photos.map { it.id }.toSet()
        if (missingPhotos.size > 0) {
            throw IllegalArgumentException("Knittings reference non-existing photos with ids $missingPhotos")
        }
    }

    companion object {

        val classLoader: ClassLoader = javaClass.classLoader

        @JvmField
        val CREATOR = object : Parcelable.Creator<Database> {
            override fun createFromParcel(parcel: Parcel) = Database(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Database>(size)
        }
    }
}