package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayReadingStat
import com.example.data.ReadingAnalytics
import com.example.data.WeekBucketStat

enum class ActivityAnalysisTimeSpan {
    WEEKLY,
    MONTHLY
}

enum class MonthlyViewType {
    FOUR_WEEKS,
    DAILY_30_DAYS
}

/**
 * ActivityAnalysisChart
 *
 * Comprehensive interactive visual chart supporting both Weekly (7 Days)
 * and Monthly (30 Days / 4 Weeks) activity analysis.
 */
@Composable
fun ActivityAnalysisChart(
    analytics: ReadingAnalytics,
    accentColorHex: Long,
    selectedDateKey: String?,
    onSelectDay: (DayReadingStat) -> Unit,
    modifier: Modifier = Modifier,
    initialTimeSpan: ActivityAnalysisTimeSpan = ActivityAnalysisTimeSpan.WEEKLY
) {
    var currentTimeSpan by remember { mutableStateOf(initialTimeSpan) }
    var monthlyViewType by remember { mutableStateOf(MonthlyViewType.FOUR_WEEKS) }
    var selectedWeekIndex by remember { mutableStateOf(3) } // default to current week
    var selectedMonthDayKey by remember { mutableStateOf(selectedDateKey) }

    val accentColor = Color(accentColorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_analysis_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1B27)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row with Title and Time-Span Segmented Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (currentTimeSpan == ActivityAnalysisTimeSpan.WEEKLY)
                                    Icons.Default.BarChart else Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (currentTimeSpan == ActivityAnalysisTimeSpan.WEEKLY)
                                "Weekly Activity Analysis" else "Monthly Activity Analysis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentTimeSpan == ActivityAnalysisTimeSpan.WEEKLY)
                                "Last 7 days breakdown" else "Last 30 days & 4-week trend",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                }

                // Time Span Switcher: Weekly vs Monthly
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    MiniFilterTab(
                        label = "Weekly",
                        isSelected = currentTimeSpan == ActivityAnalysisTimeSpan.WEEKLY,
                        accentColor = accentColor,
                        onClick = { currentTimeSpan = ActivityAnalysisTimeSpan.WEEKLY },
                        testTag = "filter_weekly_btn"
                    )
                    MiniFilterTab(
                        label = "Monthly",
                        isSelected = currentTimeSpan == ActivityAnalysisTimeSpan.MONTHLY,
                        accentColor = accentColor,
                        onClick = { currentTimeSpan = ActivityAnalysisTimeSpan.MONTHLY },
                        testTag = "filter_monthly_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Transition between Weekly and Monthly
            Crossfade(
                targetState = currentTimeSpan,
                animationSpec = tween(300),
                label = "TimeSpanTransition"
            ) { span ->
                when (span) {
                    ActivityAnalysisTimeSpan.WEEKLY -> {
                        WeeklyChartSection(
                            weeklyStats = analytics.weeklyDayStats,
                            maxDaySeconds = analytics.maxDaySecondsInWeek,
                            accentColor = accentColor,
                            selectedDateKey = selectedDateKey,
                            onSelectDay = onSelectDay
                        )
                    }
                    ActivityAnalysisTimeSpan.MONTHLY -> {
                        MonthlyChartSection(
                            analytics = analytics,
                            accentColor = accentColor,
                            monthlyViewType = monthlyViewType,
                            onChangeViewType = { monthlyViewType = it },
                            selectedWeekIndex = selectedWeekIndex,
                            onSelectWeek = { selectedWeekIndex = it },
                            selectedMonthDayKey = selectedMonthDayKey,
                            onSelectMonthDay = { stat ->
                                selectedMonthDayKey = stat.dateKey
                                onSelectDay(stat)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Weekly Chart View: 7-day bar chart with selection inspector
 */
@Composable
private fun WeeklyChartSection(
    weeklyStats: List<DayReadingStat>,
    maxDaySeconds: Long,
    accentColor: Color,
    selectedDateKey: String?,
    onSelectDay: (DayReadingStat) -> Unit
) {
    val selectedStat = weeklyStats.firstOrNull { it.dateKey == selectedDateKey }
        ?: weeklyStats.firstOrNull { it.isToday }
        ?: weeklyStats.lastOrNull()

    Column {
        // 7-Day Bars Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            weeklyStats.forEach { stat ->
                val isSelected = (stat.dateKey == selectedStat?.dateKey)
                DayBarItem(
                    stat = stat,
                    maxDaySeconds = maxDaySeconds,
                    accentColor = accentColor,
                    isSelected = isSelected,
                    onClick = { onSelectDay(stat) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Inspector Card
        selectedStat?.let { stat ->
            DayInspectorCard(stat = stat, accentColor = accentColor)
        }
    }
}

/**
 * Monthly Chart View: 4-Week trend overview + 30-Day interactive daily scrollable bars
 */
@Composable
private fun MonthlyChartSection(
    analytics: ReadingAnalytics,
    accentColor: Color,
    monthlyViewType: MonthlyViewType,
    onChangeViewType: (MonthlyViewType) -> Unit,
    selectedWeekIndex: Int,
    onSelectWeek: (Int) -> Unit,
    selectedMonthDayKey: String?,
    onSelectMonthDay: (DayReadingStat) -> Unit
) {
    Column {
        // Monthly Metrics Summary Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MonthlyMetricChip(
                label = "30-Day Total",
                value = formatReadingDuration(analytics.monthSeconds),
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            MonthlyMetricChip(
                label = "Daily Avg",
                value = "${formatBriefDuration(analytics.dailyAverageThisMonthSeconds)}/d",
                accentColor = Color(0xFF81C784),
                modifier = Modifier.weight(0.9f)
            )
            MonthlyMetricChip(
                label = "Active Days",
                value = "${analytics.activeDaysInMonthCount} / 30",
                accentColor = Color(0xFFFFB74D),
                modifier = Modifier.weight(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sub-mode toggle: 4-Week Overview vs 30-Day Daily Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (monthlyViewType == MonthlyViewType.FOUR_WEEKS)
                    "4-Week Distribution" else "30-Day Daily Activity",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MiniFilterTab(
                    label = "4 Weeks",
                    isSelected = monthlyViewType == MonthlyViewType.FOUR_WEEKS,
                    accentColor = accentColor,
                    onClick = { onChangeViewType(MonthlyViewType.FOUR_WEEKS) },
                    testTag = "subview_4weeks_btn"
                )
                MiniFilterTab(
                    label = "30 Days",
                    isSelected = monthlyViewType == MonthlyViewType.DAILY_30_DAYS,
                    accentColor = accentColor,
                    onClick = { onChangeViewType(MonthlyViewType.DAILY_30_DAYS) },
                    testTag = "subview_30days_btn"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (monthlyViewType) {
            MonthlyViewType.FOUR_WEEKS -> {
                // 4-Week Distribution Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    analytics.monthlyWeekStats.forEach { week ->
                        val isSelected = (week.weekIndex == selectedWeekIndex)
                        WeekBucketBarItem(
                            week = week,
                            maxWeekSeconds = analytics.maxWeekSecondsInMonth,
                            accentColor = accentColor,
                            isSelected = isSelected,
                            onClick = { onSelectWeek(week.weekIndex) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inspector for the selected week
                val selectedWeek = analytics.monthlyWeekStats.getOrNull(selectedWeekIndex)
                    ?: analytics.monthlyWeekStats.lastOrNull()
                selectedWeek?.let { week ->
                    WeekInspectorCard(
                        week = week,
                        totalMonthSeconds = analytics.monthSeconds,
                        accentColor = accentColor
                    )
                }
            }

            MonthlyViewType.DAILY_30_DAYS -> {
                // 30-Day Interactive Scrollable Bars
                val scrollState = rememberScrollState()
                val selectedStat = analytics.monthlyDayStats.firstOrNull { it.dateKey == selectedMonthDayKey }
                    ?: analytics.monthlyDayStats.firstOrNull { it.isToday }
                    ?: analytics.monthlyDayStats.lastOrNull()

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        analytics.monthlyDayStats.forEach { stat ->
                            val isSelected = (stat.dateKey == selectedStat?.dateKey)
                            CompactDayBarItem(
                                stat = stat,
                                maxDaySeconds = analytics.maxDaySecondsInMonth,
                                accentColor = accentColor,
                                isSelected = isSelected,
                                onClick = { onSelectMonthDay(stat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    selectedStat?.let { stat ->
                        DayInspectorCard(stat = stat, accentColor = accentColor)
                    }
                }
            }
        }
    }
}

/**
 * 4-Week Individual Bar Item
 */
@Composable
private fun WeekBucketBarItem(
    week: WeekBucketStat,
    maxWeekSeconds: Long,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heightRatio = if (maxWeekSeconds > 0L) {
        (week.totalSeconds.toFloat() / maxWeekSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val targetBarHeight = if (week.totalSeconds > 0L) {
        (22.dp + (78.dp * heightRatio))
    } else {
        8.dp
    }
    val animatedBarHeight by animateDpAsState(
        targetValue = targetBarHeight,
        animationSpec = tween(400),
        label = "WeekBarHeight"
    )

    val barColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor
            week.isCurrentWeek -> accentColor.copy(alpha = 0.85f)
            week.totalSeconds > 0L -> accentColor.copy(alpha = 0.45f)
            else -> Color.White.copy(alpha = 0.12f)
        },
        label = "WeekBarColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = formatBriefDuration(week.totalSeconds),
            fontSize = 10.sp,
            fontWeight = if (isSelected || week.isCurrentWeek) FontWeight.Bold else FontWeight.Normal,
            color = if (week.totalSeconds > 0) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(if (isSelected) 26.dp else 20.dp)
                .height(animatedBarHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (week.totalSeconds > 0) {
                        Brush.verticalGradient(listOf(accentColor, barColor))
                    } else {
                        Brush.verticalGradient(listOf(barColor, barColor))
                    }
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .padding(top = 2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = week.shortLabel,
            fontSize = 11.sp,
            fontWeight = if (week.isCurrentWeek || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = when {
                week.isCurrentWeek -> accentColor
                isSelected -> Color.White
                else -> Color.White.copy(alpha = 0.55f)
            }
        )

        if (week.isCurrentWeek) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Compact day bar for 30-day scrollable chart
 */
@Composable
private fun CompactDayBarItem(
    stat: DayReadingStat,
    maxDaySeconds: Long,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val heightRatio = if (maxDaySeconds > 0L) {
        (stat.totalSeconds.toFloat() / maxDaySeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val targetBarHeight = if (stat.totalSeconds > 0L) {
        (14.dp + (86.dp * heightRatio))
    } else {
        6.dp
    }
    val animatedBarHeight by animateDpAsState(
        targetValue = targetBarHeight,
        animationSpec = tween(350),
        label = "CompactBarHeight"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = if (stat.totalSeconds > 0) formatBriefDuration(stat.totalSeconds) else "",
            fontSize = 7.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .width(if (isSelected) 14.dp else 10.dp)
                .height(animatedBarHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when {
                        isSelected -> accentColor
                        stat.isToday -> accentColor.copy(alpha = 0.85f)
                        stat.totalSeconds > 0L -> accentColor.copy(alpha = 0.45f)
                        else -> Color.White.copy(alpha = 0.10f)
                    }
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stat.dateKey.split("-").lastOrNull()?.trimStart('0')?.ifEmpty { "0" } ?: "",
            fontSize = 9.sp,
            fontWeight = if (stat.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (stat.isToday) accentColor else Color.White.copy(alpha = 0.5f)
        )
    }
}

/**
 * Inspector Card for Selected Week
 */
@Composable
private fun WeekInspectorCard(
    week: WeekBucketStat,
    totalMonthSeconds: Long,
    accentColor: Color
) {
    val percentage = if (totalMonthSeconds > 0L) {
        ((week.totalSeconds.toDouble() / totalMonthSeconds.toDouble()) * 100).toInt()
    } else 0

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, if (week.isCurrentWeek) accentColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = week.weekLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${week.dateRangeLabel}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append("${week.sessionCount} session${if (week.sessionCount != 1) "s" else ""}")
                        if (week.totalBreakSeconds > 0L) {
                            append(" • Break: ${formatReadingDuration(week.totalBreakSeconds)}")
                        }
                        if (totalMonthSeconds > 0L) {
                            append(" • $percentage% of month")
                        }
                    },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }

            Text(
                text = formatReadingDuration(week.totalSeconds),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (week.totalSeconds > 0) accentColor else Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Inspector Card for Selected Day
 */
@Composable
private fun DayInspectorCard(
    stat: DayReadingStat,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, if (stat.isToday) accentColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stat.displayLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (stat.isToday) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "TODAY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = buildString {
                        append("${stat.dateSubtitle} • ${stat.sessionCount} session${if (stat.sessionCount != 1) "s" else ""}")
                        if (stat.totalBreakSeconds > 0L) {
                            append(" • Break: ${formatReadingDuration(stat.totalBreakSeconds)}")
                        }
                    },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }

            Text(
                text = formatReadingDuration(stat.totalSeconds),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (stat.totalSeconds > 0) accentColor else Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Small Segmented Filter Tab Button
 */
@Composable
private fun MiniFilterTab(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)) else null,
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Metric chip in monthly analysis header
 */
@Composable
private fun MonthlyMetricChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

/**
 * Standard 7-Day Bar Item
 */
@Composable
private fun DayBarItem(
    stat: DayReadingStat,
    maxDaySeconds: Long,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heightRatio = if (maxDaySeconds > 0L) {
        (stat.totalSeconds.toFloat() / maxDaySeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val targetBarHeight = if (stat.totalSeconds > 0L) {
        (16.dp + (84.dp * heightRatio))
    } else {
        6.dp
    }
    val animatedBarHeight by animateDpAsState(
        targetValue = targetBarHeight,
        animationSpec = tween(400),
        label = "BarHeight"
    )

    val barColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor
            stat.isToday -> accentColor.copy(alpha = 0.85f)
            stat.totalSeconds > 0L -> accentColor.copy(alpha = 0.45f)
            else -> Color.White.copy(alpha = 0.12f)
        },
        label = "BarColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = if (stat.totalSeconds > 0) formatBriefDuration(stat.totalSeconds) else if (isSelected) "0m" else "",
            fontSize = 9.sp,
            fontWeight = if (isSelected || stat.isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (stat.totalSeconds > 0) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.35f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(if (isSelected) 18.dp else 14.dp)
                .height(animatedBarHeight)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (stat.totalSeconds > 0) {
                        Brush.verticalGradient(listOf(accentColor, barColor))
                    } else {
                        Brush.verticalGradient(listOf(barColor, barColor))
                    }
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .padding(top = 2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stat.dayName,
            fontSize = 11.sp,
            fontWeight = if (stat.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = when {
                stat.isToday -> accentColor
                isSelected -> Color.White
                else -> Color.White.copy(alpha = 0.5f)
            }
        )

        if (stat.isToday) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Backward-compatible WeeklyReadingChart wrapper.
 */
@Composable
fun WeeklyReadingChart(
    weeklyStats: List<DayReadingStat>,
    maxDaySeconds: Long,
    accentColorHex: Long,
    selectedDateKey: String?,
    onSelectDay: (DayReadingStat) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(accentColorHex)
    val selectedStat = weeklyStats.firstOrNull { it.dateKey == selectedDateKey }
        ?: weeklyStats.firstOrNull { it.isToday }
        ?: weeklyStats.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_reading_chart_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1B27)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Weekly Activity Analysis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Last 7 days breakdown",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            WeeklyChartSection(
                weeklyStats = weeklyStats,
                maxDaySeconds = maxDaySeconds,
                accentColor = accentColor,
                selectedDateKey = selectedDateKey,
                onSelectDay = onSelectDay
            )
        }
    }
}

/**
 * Compact duration formatting for chart bars (e.g. "45m", "1.2h", "30s", "0")
 */
private fun formatBriefDuration(seconds: Long): String {
    if (seconds == 0L) return "0"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}
