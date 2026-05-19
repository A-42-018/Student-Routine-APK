package com.alif.studentroutine.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OffDayCard(
    dayLabel: String,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "offDay")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val primaryColor   = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val headline = if (isToday) "It's an Off Day! 🎉" else "Free day on $dayLabel 🌴"
    val subtext  = if (isToday)
        "No classes today — enjoy a free day."
    else
        "No classes scheduled. A perfect day to recharge."

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, primaryColor.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.10f),
                            secondaryColor.copy(alpha = 0.10f)
                        )
                    )
                )
        ) {
            // ── Background blob shapes ─────────────────────────
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.12f),
                    radius = size.width * 0.32f,
                    center = Offset(size.width * 0.88f, size.height * 0.08f)
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.10f),
                    radius = size.width * 0.18f,
                    center = Offset(size.width * 0.10f, size.height * 0.55f)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.07f),
                    radius = size.width * 0.14f,
                    center = Offset(size.width * 0.75f, size.height * 0.88f)
                )
            }

            // ── Illustration + copy ────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Floating emoji cluster
                Column(
                    modifier = Modifier
                        .semantics { contentDescription = "Off day, no classes" }
                        .offset(y = (-floatY).dp)
                        .rotate(rotation),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isToday) "🛋️" else "🌴",
                        fontSize = 54.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🎉", fontSize = 22.sp)
                        Text(if (isToday) "😌" else "☀️", fontSize = 22.sp)
                        Text("✨", fontSize = 22.sp)
                    }
                }

                // Headline
                Text(
                    text = headline,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Subtext
                Text(
                    text = subtext,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
