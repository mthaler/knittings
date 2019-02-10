package com.mthaler.knittings.model

import android.content.Context
import android.support.v4.app.Fragment
import com.mthaler.knittings.R
import java.io.Serializable

data class Needle(val id: Long, val name: String, val description: String, val size: String, val length: String, val material: String, val inUse: Boolean, val type: String) : Serializable {

    companion object {
        val Context.materials: Array<String>
            get() = arrayOf(resources.getString(R.string.needle_material_other), resources.getString(R.string.needle_material_aluminum),
                    resources.getString(R.string.needle_material_bamboo), resources.getString(R.string.needle_material_metal),
                    resources.getString(R.string.needle_material_plastic), resources.getString(R.string.needle_material_wood))

        val Fragment.materials: Array<String>
            get() = context?.materials ?: emptyArray()

        val Context.types: Array<String>
            get() = arrayOf(resources.getString(R.string.needle_type_other), resources.getString(R.string.needle_type_circular),
                    resources.getString(R.string.needle_type_point), resources.getString(R.string.needle_type_set),
                    resources.getString(R.string.needle_type_round), resources.getString(R.string.needle_type_coat))
        
        val Fragment.types: Array<String>
            get() = context?.types ?: emptyArray()
    }
}