package com.example.agesnap.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agesnap.data.model.AgeResult
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatisticsCards(
    result: AgeResult,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Primary 3 cards: TOTAL DAYS, TOTAL WEEKS, TOTAL MONTHS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "TOTAL DAYS",
                value = numberFormat.format(result.totalDays),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "TOTAL WEEKS",
                value = numberFormat.format(result.totalWeeks),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "TOTAL MONTHS",
                value = numberFormat.format(result.totalMonths),
                modifier = Modifier.weight(1f)
            )
        }

        // Secondary cards: CALENDAR HOURS and subtle TOTAL MINUTES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "CALENDAR HOURS",
                sublabel = "totalDays × 24",
                value = numberFormat.format(result.totalHours),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "TOTAL MINUTES",
                sublabel = "calendar estimate",
                value = numberFormat.format(result.totalMinutes),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    sublabel: String? = null
) {
    val accessibleDesc = "$label: $value"

    OutlinedCard(
        modifier = modifier.semantics { contentDescription = accessibleDesc },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            if (sublabel != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}
