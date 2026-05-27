package com.bazical.app.wallpaper.renderer

import android.graphics.*
import android.view.SurfaceHolder
import com.bazical.app.domain.model.CalendarDay
import java.time.LocalDate

class CalendarRenderer(
    private val surfaceHolder: SurfaceHolder
) {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val backgroundColor = Color.parseColor("#FAFAF8")
    private val todayGradientStart = Color.parseColor("#C84A3E")
    private val todayGradientEnd = Color.parseColor("#A33D33")
    private val textPrimary = Color.parseColor("#2C1810")
    private val textTertiary = Color.parseColor("#8B7355")
    private val weekendRed = Color.parseColor("#C84A3E")
    private val jieqiGreen = Color.parseColor("#10B981")
    private val lunarRed = Color.parseColor("#E74C3C")

    private val stemColors = mapOf(
        "甲" to Color.parseColor("#4ade80"),
        "乙" to Color.parseColor("#86efac"),
        "丙" to Color.parseColor("#f87171"),
        "丁" to Color.parseColor("#fca5a5"),
        "戊" to Color.parseColor("#a78bfa"),
        "己" to Color.parseColor("#c4b5fd"),
        "庚" to Color.parseColor("#fbbf24"),
        "辛" to Color.parseColor("#fde047"),
        "壬" to Color.parseColor("#60a5fa"),
        "癸" to Color.parseColor("#93c5fd")
    )

    private val branchColors = mapOf(
        "子" to Color.parseColor("#60a5fa"),
        "丑" to Color.parseColor("#a78bfa"),
        "寅" to Color.parseColor("#4ade80"),
        "卯" to Color.parseColor("#86efac"),
        "辰" to Color.parseColor("#a78bfa"),
        "巳" to Color.parseColor("#f87171"),
        "午" to Color.parseColor("#fca5a5"),
        "未" to Color.parseColor("#a78bfa"),
        "申" to Color.parseColor("#fbbf24"),
        "酉" to Color.parseColor("#fde047"),
        "戌" to Color.parseColor("#a78bfa"),
        "亥" to Color.parseColor("#60a5fa")
    )

    private var width = 0
    private var height = 0
    private var density = 1f

    fun setDensity(density: Float) {
        this.density = density
    }

    fun render(year: Int, month: Int, days: List<CalendarDay>) {
        val canvas = surfaceHolder.lockCanvas() ?: return
        try {
            if (width == 0 || height == 0) {
                width = canvas.width
                height = canvas.height
            }
            drawBackground(canvas)
            drawCalendar(canvas, year, month, days)
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        backgroundPaint.color = Color.WHITE
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    private fun drawCalendar(canvas: Canvas, year: Int, month: Int, days: List<CalendarDay>) {
        val cellWidth = width / 7f
        val cellHeight = height / 7f

        drawWeekdayHeader(canvas, cellWidth, cellHeight)

        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)
        val daysInMonth = firstDayOfMonth.lengthOfMonth()
        val today = LocalDate.now()

        val prevMonth = firstDayOfMonth.minusMonths(1)
        val daysInPrevMonth = prevMonth.lengthOfMonth()

        var dayIndex = 0
        for (row in 1..6) {
            for (col in 0..6) {
                val cellX = col * cellWidth
                val cellY = row * cellHeight
                val cellRect = RectF(cellX, cellY, cellX + cellWidth, cellY + cellHeight)

                val position = (row - 1) * 7 + col
                val adjustedPosition = position - startDayOfWeek + 1

                val cellData = when {
                    adjustedPosition < 1 -> {
                        CalendarCellInfo(
                            dayNumber = daysInPrevMonth + adjustedPosition,
                            lunarDate = null,
                            isJieqi = false,
                            stem = "",
                            shishen = "",
                            branch = "",
                            branchShishen = "",
                            isOtherMonth = true,
                            isToday = false,
                            isWeekend = col == 0 || col == 6,
                            date = ""
                        )
                    }
                    adjustedPosition > daysInMonth -> {
                        CalendarCellInfo(
                            dayNumber = adjustedPosition - daysInMonth,
                            lunarDate = null,
                            isJieqi = false,
                            stem = "",
                            shishen = "",
                            branch = "",
                            branchShishen = "",
                            isOtherMonth = true,
                            isToday = false,
                            isWeekend = col == 0 || col == 6,
                            date = ""
                        )
                    }
                    else -> {
                        val day = days.getOrNull(dayIndex)
                        dayIndex++
                        val date = LocalDate.of(year, month, adjustedPosition)
                        val isToday = date == today
                        val stem = day?.ganzhi?.firstOrNull() ?: ""
                        val branch = if ((day?.ganzhi?.size ?: 0) > 1) day?.ganzhi?.get(1) ?: "" else ""

                        val lunarDisplay = day?.holiday?.takeIf { it.isNotBlank() }
                            ?: day?.jieqi?.takeIf { it.isNotBlank() }
                            ?: day?.lunarDate?.takeIf { it.isNotBlank() }

                        CalendarCellInfo(
                            dayNumber = adjustedPosition,
                            lunarDate = lunarDisplay,
                            isJieqi = day?.jieqi?.isNotBlank() == true,
                            stem = stem,
                            shishen = day?.shishen ?: "",
                            branch = branch,
                            branchShishen = day?.branchShishen ?: "",
                            isOtherMonth = false,
                            isToday = isToday,
                            isWeekend = col == 0 || col == 6,
                            date = date.toString()
                        )
                    }
                }
                drawCell(canvas, cellRect, cellData)
            }
        }
    }

    private fun drawWeekdayHeader(canvas: Canvas, cellWidth: Float, cellHeight: Float) {
        val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
        textPaint.textSize = 12f * density * 2
        textPaint.textAlign = Paint.Align.CENTER

        for ((index, day) in weekdays.withIndex()) {
            val x = cellWidth * index + cellWidth / 2
            val y = cellHeight / 2 + textPaint.textSize / 3

            textPaint.color = if (index == 0 || index == 6) weekendRed else textTertiary
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(day, x, y, textPaint)
        }
        textPaint.typeface = Typeface.DEFAULT
    }

    private fun drawCell(canvas: Canvas, rect: RectF, cell: CalendarCellInfo) {
        val padding = 2f * density

        if (cell.isToday) {
            drawTodayGradient(canvas, rect)
        } else {
            backgroundPaint.color = backgroundColor
            canvas.drawRect(rect, backgroundPaint)
        }

        canvas.save()
        canvas.clipRect(rect)

        val centerX = rect.centerX()

        textPaint.textSize = 12f * 2 * density
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = when {
            cell.isToday -> Color.WHITE
            cell.isOtherMonth -> Color.argb(77, 44, 24, 16)
            cell.isWeekend -> weekendRed
            else -> textPrimary
        }
        canvas.drawText(cell.dayNumber.toString(), centerX, rect.top + padding + textPaint.textSize, textPaint)

        if (cell.lunarDate != null) {
            textPaint.textSize = 8f * 2 * density
            textPaint.color = when {
                cell.isToday -> Color.argb(179, 255, 255, 255)
                cell.isJieqi -> jieqiGreen
                cell.lunarDate.contains("初一") || cell.lunarDate.contains("十五") -> lunarRed
                else -> textTertiary
            }
            canvas.drawText(cell.lunarDate, centerX, rect.top + padding + textPaint.textSize * 2 + 4 * density, textPaint)
        }

        if (!cell.isOtherMonth && cell.stem.isNotEmpty()) {
            textPaint.textSize = 10f * 2 * density
            textPaint.color = if (cell.isToday) Color.WHITE else getStemColor(cell.stem)
            textPaint.typeface = Typeface.DEFAULT_BOLD
            val row3Text = cell.stem + (if (cell.shishen.isNotEmpty()) " ${cell.shishen}" else "")
            canvas.drawText(row3Text, centerX, rect.top + padding + textPaint.textSize * 3 + 8 * density, textPaint)

            if (cell.branch.isNotEmpty()) {
                textPaint.color = if (cell.isToday) Color.WHITE else getBranchColor(cell.branch)
                val row4Text = cell.branch + (if (cell.branchShishen.isNotEmpty()) " ${cell.branchShishen}" else "")
                canvas.drawText(row4Text, centerX, rect.top + padding + textPaint.textSize * 4 + 12 * density, textPaint)
            }
            textPaint.typeface = Typeface.DEFAULT
        }

        canvas.restore()
    }

    private fun drawTodayGradient(canvas: Canvas, rect: RectF) {
        val gradient = LinearGradient(
            rect.left, rect.top,
            rect.right, rect.bottom,
            todayGradientStart, todayGradientEnd,
            Shader.TileMode.CLAMP
        )
        backgroundPaint.shader = gradient
        canvas.drawRect(rect, backgroundPaint)
        backgroundPaint.shader = null
    }

    private fun getStemColor(stem: String): Int {
        return stemColors[stem] ?: textPrimary
    }

    private fun getBranchColor(branch: String): Int {
        return branchColors[branch] ?: textPrimary
    }

    data class CalendarCellInfo(
        val dayNumber: Int,
        val lunarDate: String?,
        val isJieqi: Boolean,
        val stem: String,
        val shishen: String,
        val branch: String,
        val branchShishen: String,
        val isOtherMonth: Boolean,
        val isToday: Boolean,
        val isWeekend: Boolean,
        val date: String
    )
}