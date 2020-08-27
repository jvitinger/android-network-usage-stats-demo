package eu.vitinger.datausagestats

import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.log10
import kotlin.math.max

object ConvertUtils {

    private const val UNIT_SIZE = 1000
    private const val UNITS_PREFIXES = "kMGTPE"
    private const val DEFAULT_MAX_DIGITS = 3

    fun getSizeWithUnit(bytes: Long): String {
        return getSizeWithUnit(bytes, DEFAULT_MAX_DIGITS)
    }

    private fun getSizeWithUnit(bytes: Long, maxDigits: Int): String {
        val unit = getUnit(bytes)
        val size = getSize(bytes, maxDigits, unit)
        val layoutDirection =
            TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
        val addRtlChar = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL

        //Add RTL character to supported versions.
        return size + (if (addRtlChar) " \u200F" else " ") + unit
    }

    private fun getUnit(bytes: Long): String {
        val unitIndex =
            (Math.log(bytes.toDouble()) / Math.log(UNIT_SIZE.toDouble())).toInt() - 1
        return when {
            bytes < UNIT_SIZE.toLong() -> "B"
            else -> UNITS_PREFIXES[unitIndex].toString() + "B"
        }
    }

    private fun getSize(bytes: Long, maxDigits: Int, unit: String): String {
        return trimDecimalZeros(
            getSizeWithDecimalZeros(bytes, maxDigits, unit)
        )
    }

    private fun getSizeWithDecimalZeros(
        bytes: Long,
        maxDigits: Int,
        unit: String
    ): String {
        val converted = convertExact(bytes, unit)
        val digitsBeforeDecimalPoint: Int =
            if (converted.toInt() == 0) 1 else log10(converted + 1).toInt()
        val precision = max(0, maxDigits - digitsBeforeDecimalPoint)
        return String.format("%." + precision + "f", converted)
    }

    private fun convertExact(bytes: Long, unit: String): Double {
        val exp = UNITS_PREFIXES.indexOf(unit[0]) + 1
        return bytes / Math.pow(
            UNIT_SIZE.toDouble(),
            exp.toDouble()
        )
    }

    private fun trimDecimalZeros(number: String): String {
        return if (number.indexOf(
                DecimalFormatSymbols.getInstance().decimalSeparator
            ) > 0
        ) {
            number.replace("[0]*$".toRegex(), "").replace("[.,]$".toRegex(), "")
        } else number
    }
}