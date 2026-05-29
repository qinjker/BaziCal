package com.bazical.app.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.bazical.app.ui.theme.Primary
import com.bazical.app.ui.theme.PrimaryVariant
import com.bazical.app.ui.theme.Secondary
import com.bazical.app.ui.theme.TextPrimary
import com.bazical.app.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object ShareCardGenerator {

    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT = 1920
    private const val MARGIN = 60f

    fun generateShareCard(
        context: Context,
        year: Int,
        month: Int,
        day: Int,
        lunarDate: String,
        dayStem: String,
        dayBranch: String,
        monthShishen: String,
        energyDescription: String = ""
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#F7F4EF"))

        // Header gradient background
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val headerGradient = LinearGradient(
            0f, 0f, 0f, 400f,
            Primary.toArgb(), PrimaryVariant.toArgb(),
            Shader.TileMode.CLAMP
        )
        headerPaint.shader = headerGradient
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), 400f, headerPaint)

        // Date text
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINESE)
        val solarDate = LocalDate.of(year, month, day).format(dateFormatter)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 56f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(solarDate, CARD_WIDTH / 2f, 150f, datePaint)

        // Lunar date
        val lunarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 255, 255, 255)
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("农历 $lunarDate", CARD_WIDTH / 2f, 220f, lunarPaint)

        // Day pillar (日主)
        val dayPillarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 80f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("【$dayStem$dayBranch】", CARD_WIDTH / 2f, 340f, dayPillarPaint)

        // Energy section card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val cardRect = RectF(MARGIN, 480f, CARD_WIDTH - MARGIN, 900f)
        canvas.drawRoundRect(cardRect, 30f, 30f, cardPaint)

        // Card shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(30, 0, 0, 0)
        }
        canvas.drawRoundRect(RectF(MARGIN + 4, 484f, CARD_WIDTH - MARGIN + 4, 904f), 30f, 30f, shadowPaint)
        canvas.drawRoundRect(cardRect, 30f, 30f, cardPaint)

        // 本月能量 title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TextPrimary.toArgb()
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("本月能量", CARD_WIDTH / 2f, 580f, titlePaint)

        // Month shishen
        val shishenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Secondary.toArgb()
            textSize = 72f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(monthShishen, CARD_WIDTH / 2f, 700f, shishenPaint)

        // Energy description
        val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TextTertiary.toArgb()
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        val descText = energyDescription.ifEmpty { "今天与你同行的能量是$monthShishen" }
        canvas.drawText(descText, CARD_WIDTH / 2f, 800f, descPaint)

        // Divider line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E8E0D5")
            strokeWidth = 2f
        }
        canvas.drawLine(MARGIN + 80, 860f, CARD_WIDTH - MARGIN - 80, 860f, linePaint)

        // Slogan
        val sloganPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Primary.toArgb()
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("每一天，都算数", CARD_WIDTH / 2f, 1100f, sloganPaint)

        // App name
        val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TextTertiary.toArgb()
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("BaziCal · 八字历", CARD_WIDTH / 2f, 1400f, appNamePaint)

        // QR code placeholder area
        val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAF8F4")
        }
        val qrRect = RectF(CARD_WIDTH / 2f - 150f, 1550f, CARD_WIDTH / 2f + 150f, 1850f)
        canvas.drawRoundRect(qrRect, 20f, 20f, qrBgPaint)

        // QR text
        val qrTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TextTertiary.toArgb()
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("扫码下载App", CARD_WIDTH / 2f, 1750f, qrTextPaint)

        // Decorative elements
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Primary.toArgb()
        }
        // Top left dot
        canvas.drawCircle(MARGIN + 30, 40f, 15f, dotPaint)
        // Top right dot
        canvas.drawCircle(CARD_WIDTH - MARGIN - 30, 40f, 15f, dotPaint)

        return bitmap
    }
}
