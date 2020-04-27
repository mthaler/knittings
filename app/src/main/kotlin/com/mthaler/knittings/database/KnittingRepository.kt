package com.mthaler.knittings.database

import com.mthaler.knittings.model.Knitting
import java.util.ArrayList

interface KnittingRepository {

    /**
     * Returns all knittings from the database
     *
     * @return all knittings from database
     */
    val allKnittings: ArrayList<Knitting>

    /**
     * Adds the given knitting to the database
     *
     * @param knitting knitting that should be added to the database
     * @param manualID: use knitting ID instead of auto-imcremented id
     */
    fun addKnitting(knitting: Knitting, manualID: Boolean = false): Knitting

    /**
     * Updates a knitting in the database
     *
     * @param knitting knitting that should be updated
     * @return updated knitting
     */
    fun updateKnitting(knitting: Knitting): Knitting

    /**
     * Deletes the given knitting from the database. All photos for the deleted knitting are also deleted
     *
     * @param knitting knitting that should be deleted
     */
    fun deleteKnitting(knitting: Knitting)

    /**
     * Deletes all knittings from the database
     */
    fun deleteAllKnittings()

    /**
     * Gets the knitting with the given id from the database
     *
     * @param id id of the knitting that should be read from database
     * @return knitting for the given id
     */
    fun getKnitting(id: Long): Knitting
}