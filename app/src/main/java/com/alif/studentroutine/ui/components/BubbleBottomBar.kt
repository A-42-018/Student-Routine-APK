package com.alif.studentroutine.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alif.studentroutine.ui.navigation.Screen

@Composable
fun BubbleBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    data class BubTab(val route: String, val label: String, val icon: ImageVector)

    val tabs = remember {
        listOf(
            BubTab(Screen.Dashboard.route, "Classes", Icons.Default.CalendarMonth),
            BubTab(Screen.Tasks.route,     "Tasks",   Icons.Default.CheckCircle),
            BubTab(Screen.Notes.createRoute(), "Notes", Icons.Default.StickyNote2),
            BubTab(Screen.Settings.route,  "Settings",Icons.Default.Settings)
        )
    }

    val currentRouteBase = currentRoute?.substringBefore("?")
    val selectedIdx = maxOf(
        0,
        tabs.indexOfFirst { it.route.substringBefore("?") == currentRouteBase }
    )

    // 4 tabs → each slot is 1/8 of width, centres at 1/8, 3/8, 5/8, 7/8
    val notchFraction by animateFloatAsState(
        targetValue = (selectedIdx * 2 + 1) / 8f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "notchFraction"
    )

    val barGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF000000), Color(0xFF0F52BA))
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                spotColor = Color.Black.copy(alpha = 0.25f),
                ambientColor = Color.Black.copy(alpha = 0.16f)
            )
    ) {
        val bubbleX by animateDpAsState(
            targetValue = maxWidth * ((selectedIdx * 2 + 1).toFloat() / 8f) - 28.dp,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "bubbleX"
        )

        // ── Bar with curved notch ──────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barTopY = 28.dp.toPx()
            val notchCenterX = size.width * notchFraction
            val hw = 44.dp.toPx()
            val depth = 26.dp.toPx()

            val path = Path()
            path.moveTo(0f, barTopY)
            path.lineTo(notchCenterX - hw, barTopY)
            path.cubicTo(
                notchCenterX - hw * 0.5f, barTopY,
                notchCenterX - hw * 0.5f, barTopY + depth,
                notchCenterX, barTopY + depth
            )
            path.cubicTo(
                notchCenterX + hw * 0.5f, barTopY + depth,
                notchCenterX + hw * 0.5f, barTopY,
                notchCenterX + hw, barTopY
            )
            path.lineTo(size.width, barTopY)
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
            drawPath(path, brush = barGradient)
        }

        // ── Inactive tab icons ─────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(52.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTabSelected(tab.route) },
                    contentAlignment = Alignment.Center
                ) {
                    if (index != selectedIdx) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = tab.label,
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // ── Active bubble ──────────────────────────────────────
        Box(
            modifier = Modifier
                .offset(x = bubbleX, y = 0.dp)
                .size(56.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF4080FF),
                    ambientColor = Color(0xFF3070FF)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6AABFF), Color(0xFF1C52FF))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tabs[selectedIdx].icon,
                contentDescription = tabs[selectedIdx].label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
    }
