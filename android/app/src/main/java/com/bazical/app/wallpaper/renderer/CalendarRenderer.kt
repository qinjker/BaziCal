package com.bazical.app.wallpaper.renderer

import android.graphics.*
import android.view.SurfaceHolder
import com.bazical.app.domain.model.CalendarDay
import java.time.LocalDate
import kotlin.math.min

class CalendarRenderer(
    private val surfaceHolder: SurfaceHolder
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Color palette from CalendarScreen.kt
    private val backgroundColor = Color.parseColor("#FAFAF8")
    private val todayGradientStart = Color.parseColor("#C84A3E")
    private val todayGradientEnd = Color.parseColor("#A33D33")
    private val textPrimary = Color.parseColor("#2C1810")
    private val textTertiary = Color.parseColor("#8B7355")
    private val weekendRed = Color.parseColor("#C84A3E")
    private val jieqiGreen = Color.parseColor("#10B981")
    private val lunarRed = Color.parseColor("#E74C3C")

    // Stem colors
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

    // Branch colors
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

    // Font sizes (in px, converted from dp)
    private val dayNumberSize = 12f * 2 // 12sp * density
    private val lunarTextSize = 8f * 2  // 8sp * density
    private val ganzhiSize = 10f * 2    // 10sp * density

    private var width = 0
    private var height = 0
    private var density = 1f

    fun setDensity(density: Float) {
        this.density = density
    }

    fun render(
        year: Int,
        month: Int,
        days: List<CalendarDay>,
        userBirthday: String? = null
    ) {
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
        val cellHeight = height / 7f // 6 rows + 1 header

        // Draw weekday header
        drawWeekdayHeader(canvas, cellWidth, cellHeight)

        // Calculate calendar grid
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
        val daysInMonth = firstDayOfMonth.lengthOfMonth()
        val today = LocalDate.now()

        // Calculate offset for padding days
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
                        // Previous month padding
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
                        // Next month padding
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
                        // Current month
                        val day = days.getOrNull(dayIndex)
                        dayIndex++
                        val date = LocalDate.of(year, month, adjustedPosition)
                        val isToday = date == today
                        val stem = day?.ganzhi?.firstOrNull() ?: ""
                        val branch = if ((day?.ganzhi?.size ?: 0) > 1) day?.ganzhi?.get(1) ?: "" else ""

                        // Priority: holiday > jieqi > lunarDate
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

        // Draw cell background
        if (cell.isToday) {
            drawTodayGradient(canvas, rect)
        } else {
            backgroundPaint.color = backgroundColor
            canvas.drawRect(rect, backgroundPaint)
        }

        // Clip to cell bounds for text
        canvas.save()
        canvas.clipRect(rect)

        val centerX = rect.centerX()
        val centerY = rect.centerY()

        // Row 1: Day number
        textPaint.textSize = dayNumberSize * density
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = when {
            cell.isToday -> Color.WHITE
            cell.isOtherMonth -> textPrimary and 0x4DFFFFFF // 30% alpha
            cell.isWeekend -> weekendRed
            else -> textPrimary
        }
        canvas.drawText(
            cell.dayNumber.toString(),
            centerX,
            rect.top + padding + textPaint.textSize,
            textPaint
        )

        // Row 2: Lunar date / Jieqi
        if (cell.lunarDate != null) {
            textPaint.textSize = lunarTextSize * density
            textPaint.color = when {
                cell.isToday -> Color.WHITE and 0xB3FFFFFF // 70% alpha
                cell.isJieqi -> jieqiGreen
                cell.lunarDate.contains("初一") || cell.lunarDate.contains("十五") -> lunarRed
                else -> textTertiary
            }
            canvas.drawText(
                cell.lunarDate,
                centerX,
                rect.top + padding + textPaint.textSize * 2 + 4 * density,
                textPaint
            )
        }

        // Row 3 & 4: GanZhi + ShiShen (only for current month)
        if (!cell.isOtherMonth && cell.stem.isNotEmpty()) {
            // Row 3: Stem + Shishen
            textPaint.textSize = ganzhiSize * density
            textPaint.color = if (cell.isToday) Color.WHITE else getStemColor(cell.stem)
            textPaint.typeface = Typeface.DEFAULT_BOLD
            val row3Text = cell.stem + (if (cell.shishen.isNotEmpty()) " ${cell.shishen}" else "")
            canvas.drawText(
                row3Text,
                centerX,
                rect.top + padding + textPaint.textSize * 3 + 8 * density,
                textPaint
            )

            // Row 4: Branch + BranchShishen
            if (cell.branch.isNotEmpty()) {
                textPaint.color = if (cell.isToday) Color.WHITE else getBranchColor(cell.branch)
                val row4Text = cell.branch + (if (cell.branchShishen.isNotEmpty()) " ${cell.branchShishen}" else "")
                canvas.drawText(
                    row4Text,
                    centerX,
                    rect.top + padding + textPaint.textSize * 4 + 12 * density,
                    textPaint
                )
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