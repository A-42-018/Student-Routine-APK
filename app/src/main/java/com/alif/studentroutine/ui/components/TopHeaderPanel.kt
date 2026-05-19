package com.alif.studentroutine.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alif.studentroutine.ui.theme.HeaderEnd
import com.alif.studentroutine.ui.theme.OnBackground
import com.alif.studentroutine.ui.theme.Primary
import com.alif.studentroutine.ui.theme.RedAccent
import com.alif.studentroutine.ui.theme.TextGray

private val HeaderShape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)

@Composable
fun RoundedTopHeaderPanel(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationContentDescription: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionContentDescription: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = HeaderShape,
                spotColor = Primary.copy(alpha = 0.35f),
                ambientColor = Primary.copy(alpha = 0.22f)
            )
            .clip(HeaderShape)
            .background(Brush.verticalGradient(listOf(Primary, HeaderEnd)))
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (navigationIcon != null && onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            navigationIcon,
                            contentDescription = navigationContentDescription,
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (actionIcon != null && onActionClick != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        actionIcon,
                        contentDescription = actionContentDescription,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHeroHeader(
    greeting: String,
    dateText: String,
    todayClassesCount: Int,
    pendingTasksCount: Int,
    overdueCount: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = HeaderShape,
                    spotColor = Primary.copy(alpha = 0.35f),
                    ambientColor = Primary.copy(alpha = 0.22f)
                )
                .clip(HeaderShape)
                .background(Brush.verticalGradient(listOf(Primary, HeaderEnd)))
                .padding(horizontal = 22.dp, vertical = 22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Student Routine",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$greeting,👋",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateText,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 48.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeaderStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Today's Classes",
                    value = todayClassesCount.toString(),
                    subText = if (todayClassesCount == 0) "No classes" else "$todayClassesCount remaining",
                    accentColor = Primary,
                    progress = if (todayClassesCount == 0) 0f else 1f
                )
                HeaderStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Pending Tasks",
                    value = pendingTasksCount.toString(),
                    subText = if (overdueCount > 0) "$overdueCount overdue" else "All on track",
                    accentColor = RedAccent,
                    progress = if (pendingTasksCount == 0) 0f else overdueCount.toFloat() / pendingTasksCount
                )
            }
        }
    }
}

@Composable
fun HeaderStatCard(
    label: String,
    value: String,
    subText: String,
    accentColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(16.dp),
            spotColor = Color.Black.copy(alpha = 0.10f),
            ambientColor = Color.Black.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(108.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    text = subText,
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(accentColor)
                    )
                }
            }
        }
    }
}
