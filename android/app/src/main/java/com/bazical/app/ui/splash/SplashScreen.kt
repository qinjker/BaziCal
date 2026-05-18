package com.bazical.app.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bazical.app.ui.theme.Primary
import com.bazical.app.ui.theme.PrimaryVariant
import com.bazical.app.ui.theme.Secondary
import com.bazical.app.ui.theme.Success
import com.bazical.app.ui.theme.TextTertiary
import com.bazical.app.ui.theme.Warning

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val hasUserData by viewModel.hasUserData.collectAsStateWithLifecycle()
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(hasUserData) {
        when (hasUserData) {
            true -> onNavigateToCalendar()
            false -> onNavigateToHome()
            else -> {} // Loading state
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo area
            LogoSection(alpha = alphaAnimation)

            Spacer(modifier = Modifier.height(32.dp))

            // Today energy preview
            TodayPreviewCard(alpha = alphaAnimation)

            Spacer(modifier = Modifier.weight(1f))

            // CTA Button
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Primary.copy(alpha = 0.35f)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "开启我的日历",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hint text
            Text(
                text = "基于你的生日，定制专属能量日历",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Feature icons
            FeatureSection(alpha = alphaAnimation)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LogoSection(alpha: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Primary.copy(alpha = 0.35f * alpha)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Primary, PrimaryVariant)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "十",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App name
        Text(
            text = "十神能量日历",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tagline
        Text(
            text = "每一种能量，都是你的优势",
            fontSize = 14.sp,
            color = TextTertiary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TodayPreviewCard(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.08f * alpha)
            )
            .background(Color.White)
            .padding(28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "今日能量",
                fontSize = 12.sp,
                color = TextTertiary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "壬",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "午",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "正官 · 比肩",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "日主戊土 · 今日运势良好",
                    fontSize = 13.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tags
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                TagChip("贵人运", Warning)
                Spacer(modifier = Modifier.width(10.dp))
                TagChip("事业进步", Success)
                Spacer(modifier = Modifier.width(10.dp))
                TagChip("财运", Warning)
            }
        }
    }
}

@Composable
private fun TagChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun FeatureSection(alpha: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FeatureItem("📅", "每日十神")
        FeatureItem("💬", "正向寄语")
        FeatureItem("🎁", "送礼祝福")
        FeatureItem("📝", "意见反馈")
    }
}

@Composable
private fun FeatureItem(icon: String, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextTertiary
        )
    }
}