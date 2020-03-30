package com.mthaler.knittings.model

import android.content.Context
import com.mthaler.knittings.R

enum class NeedleType {
    OTHER, CIRCULAR, POINT, SET, ROUND, COAT;

    companion object {
        /**
         * Formats the needle type enum as a string using translated string resources
         *
         * @param context context
         * @param needleType needle type enum value
         * @return formatted needle type enum value
         */
        fun format(context: Context, needleType: NeedleType): String = when(needleType) {
            OTHER -> context.resources.getString(R.string.needle_type_other)
            CIRCULAR -> context.resources.getString(R.string.needle_type_circular)
            POINT -> context.resources.getString(R.string.needle_type_point)
            SET -> context.resources.getString(R.string.needle_type_set)
            ROUND -> context.resources.getString(R.string.needle_type_round)
            COAT -> context.resources.getString(R.string.needle_type_coat)
        }

        /**
         * Creates a list of formatted needle type enum values using translated string resources
         *
         * @param context context
         * @return list of formatted needle type enum values
         */
        fun formattedValues(context: Context): List<String> = values().map { format(context, it) }

        /**
         * Parses the needle type enum from a given string
         *
         * @param context context
         * @param typeStr formatted needle type string
         * @return needle type enum value
         */
        fun parse(context: Context, typeStr: String?): NeedleType = when(typeStr) {
            context.resources.getString(R.string.needle_type_other) -> OTHER
            context.resources.getString(R.string.needle_type_circular) -> CIRCULAR
            context.resources.getString(R.string.needle_type_point) -> POINT
            context.resources.getString(R.string.needle_type_set) -> SET
            context.resources.getString(R.string.needle_type_round) -> ROUND
            context.resources.getString(R.string.needle_type_coat) -> COAT
            else -> OTHER
        }
    }
}