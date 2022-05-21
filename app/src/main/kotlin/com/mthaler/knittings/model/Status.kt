package com.mthaler.knittings.model

import android.content.Context
import com.mthaler.knittings.R

/**
 * Enum representing the status of a knitting project
 */
enum class Status {
    PLANNED, IN_THE_WORKS, FINISHED, ON_HOLD, DISCARDED;

    companion object {

        /**
         * Formats the status enum as a string using translated string resources
         *
         * @param context context
         * @param status status enum value
         * @return formatted status enum value
         */
        fun format(context: Context, status: Status): String = when (status) {
            IN_THE_WORKS -> context.resources.getString(R.string.knitting_status_in_the_works)
            FINISHED -> context.resources.getString(R.string.knitting_status_finished)
            PLANNED -> context.resources.getString(R.string.knitting_status_planned)
            ON_HOLD -> context.resources.getString(R.string.knitting_status_on_hold)
            DISCARDED -> context.resources.getString(R.string.knitting_status_discarded)
        }

        /**
         * Creates a list of formatted status enum values using translated string resources
         *
         * @param context context
         * @return list of formatted status enum values
         */
        fun formattedValues(context: Context): List<String> = values().map { format(context, it) }

        /**
         * Parses the status enum from a given string
         *
         * @param context context
         * @param statusStr formatted status string
         * @return status enum value
         */
        fun parse(context: Context, statusStr: String): Status = when (statusStr) {
            context.resources.getString(R.string.knitting_status_in_the_works) -> IN_THE_WORKS
            context.resources.getString(R.string.knitting_status_finished) -> FINISHED
            context.resources.getString(R.string.knitting_status_planned) -> PLANNED
            context.resources.getString(R.string.knitting_status_on_hold) -> ON_HOLD
            context.resources.getString(R.string.knitting_status_discarded) -> DISCARDED
            else -> PLANNED
        }

        /**
         * Returns the drawable resource for the given status
         *
         * @param context context
         * @param status status enum value
         * @return drawable resource for the given status
         */
        fun getDrawableResource(status: Status): Int = when (status) {
            IN_THE_WORKS -> R.drawable.ic_play_circle_outline_black_24dp
            FINISHED -> R.drawable.ic_check_circle_outline_24px
            PLANNED -> R.drawable.ic_outline_assignment_24px
            ON_HOLD -> R.drawable.ic_pause_circle_outline_black_24dp
            DISCARDED -> R.drawable.ic_highlight_off_black_24dp
        }
    }
}