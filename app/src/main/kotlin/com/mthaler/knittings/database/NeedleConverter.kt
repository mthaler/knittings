package com.mthaler.knittings.database

import android.content.Context
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.database.table.NeedleTable
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import java.lang.Exception

class NeedleConverter(val context: Context) {

    fun convert(dbRow: Map<String, Any?>): Needle {
        val id = dbRow.getLong(NeedleTable.Cols.ID)
        val name = dbRow.getString(NeedleTable.Cols.NAME)
        val description = dbRow.getString(NeedleTable.Cols.DESCRIPTION)
        val size = dbRow.getString(NeedleTable.Cols.SIZE)
        val length = dbRow.getString(NeedleTable.Cols.DESCRIPTION)
        val materialStr = dbRow.getString(NeedleTable.Cols.MATERIAL)
        val material = try {
            NeedleMaterial.valueOf(materialStr)
        } catch (ex: Exception) {
            NeedleMaterial.parse(context, materialStr)
        }
        val inUse = dbRow.getInt(NeedleTable.Cols.IN_USE)
        val typeStr = dbRow.getString(NeedleTable.Cols.TYPE)
        val type = try {
            NeedleType.valueOf(typeStr)
        } catch (ex: Exception) {
            NeedleType.parse(context, typeStr)
        }
        return Needle(id, name, description, size, length, material, inUse > 0, type)
    }
}