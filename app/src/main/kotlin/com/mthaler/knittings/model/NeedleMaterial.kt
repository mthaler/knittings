package com.mthaler.knittings.model

import android.content.Context
import com.mthaler.knittings.R

enum class NeedleMaterial {
    OTHER, ALUMINUM, BAMBOO, METAL, PLASTIC, WOOD;

    companion object {
        /**
         * Formats the needle material enum as a string using translated string resources
         *
         * @param context context
         * @param status needle material enum value
         * @return formatted needle material enum value
         */
        fun format(context: Context, material: NeedleMaterial): String = when(material) {
            OTHER -> context.resources.getString(R.string.needle_material_other)
            ALUMINUM -> context.resources.getString(R.string.needle_material_aluminum)
            BAMBOO -> context.resources.getString(R.string.needle_material_bamboo)
            METAL -> context.resources.getString(R.string.needle_material_metal)
            PLASTIC -> context.resources.getString(R.string.needle_material_plastic)
            WOOD -> context.resources.getString(R.string.needle_material_wood)
        }

        /**
         * Creates a list of formatted needle material enum values using translated string resources
         *
         * @param context context
         * @return list of formatted needle material enum values
         */
        fun formattedValues(context: Context): List<String> = NeedleMaterial.values().map { NeedleMaterial.format(context, it) }

        /**
         * Parses the needle material enum from a given string
         *
         * @param context context
         * @param materialStr formatted needle material string
         * @return needle material enum value
         */
        fun parse(context: Context, materialStr: String): NeedleMaterial = when(materialStr) {
            context.resources.getString(R.string.needle_material_other) -> OTHER
            context.resources.getString(R.string.needle_material_aluminum) -> ALUMINUM
            context.resources.getString(R.string.needle_material_bamboo) -> BAMBOO
            context.resources.getString(R.string.needle_material_metal) -> METAL
            context.resources.getString(R.string.needle_material_plastic) -> PLASTIC
            context.resources.getString(R.string.needle_material_wood) -> WOOD
            else -> OTHER
        }
    }
}