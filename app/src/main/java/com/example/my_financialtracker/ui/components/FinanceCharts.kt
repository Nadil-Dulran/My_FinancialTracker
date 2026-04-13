package com.example.my_financialtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.my_financialtracker.model.ChartDatum

@Composable
fun HorizontalBarChartCard(
    title: String,
    items: List<ChartDatum>,
    modifier: Modifier = Modifier,
) {
    val maxValue = items.maxOfOrNull { it.value } ?: 1.0

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            items.forEachIndexed { index, item ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            item.label,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            item.valueLabel,
                            modifier = Modifier.padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(99.dp),
                            ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((item.value / maxValue).toFloat().coerceIn(0.08f, 1f))
                                .height(10.dp)
                                .background(
                                    brush = Brush.horizontalGradient(gradientFor(index)),
                                    shape = RoundedCornerShape(99.dp),
                                ),
                        )
                    }
                }
            }
        }
    }
}

private fun gradientFor(index: Int): List<Color> = when (index % 4) {
    0 -> listOf(Color(0xFF0EA5E9), Color(0xFF22D3EE))
    1 -> listOf(Color(0xFF10B981), Color(0xFF6EE7B7))
    2 -> listOf(Color(0xFFF59E0B), Color(0xFFFDE68A))
    else -> listOf(Color(0xFF8B5CF6), Color(0xFFC4B5FD))
}

@Composable
fun PieChartCard(
    title: String,
    items: List<ChartDatum>,
    modifier: Modifier = Modifier,
) {
    val total = items.sumOf { it.value }.takeIf { it > 0.0 } ?: 1.0

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Canvas(
                    modifier = Modifier
                        .size(140.dp)
                        .aspectRatio(1f),
                ) {
                    var startAngle = -90f
                    items.forEachIndexed { index, item ->
                        val sweepAngle = ((item.value / total) * 360f).toFloat()
                        drawArc(
                            color = gradientFor(index).first(),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 42f, cap = StrokeCap.Round),
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = gradientFor(index).first(),
                                            shape = RoundedCornerShape(99.dp),
                                        ),
                                )
                                Text(
                                    item.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                item.valueLabel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
