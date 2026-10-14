package com.example.agesnap.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun DateSelectionCard(
    birthDate: LocalDate?,
    asOfDate: LocalDate,
    onSelectBirthDateClicked: () -> Unit,
    onSelectAsOfDateClicked: () -> Unit,
    onResetToTodayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val accessibleFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    val birthDateText = birthDate?.format(displayFormatter) ?: "Select birth date"
    val asOfDateText = asOfDate.format(displayFormatter)

    val birthAccessibleText = if (birthDate != null) {
        "Date of birth, ${birthDate.format(accessibleFormatter)}."
    } else {
        "Date of birth, not selected. Tap to choose."
    }

    val asOfAccessibleText = "Calculate age as of ${asOfDate.format(accessibleFormatter)}."

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Birth Date Picker Item
            DateInputRow(
                label = "DATE OF BIRTH",
                valueText = birthDateText,
                isPlaceholder = birthDate == null,
                contentDesc = birthAccessibleText,
                onClick = onSelectBirthDateClicked
            )

            // Divider / Spacer
            Spacer(modifier = Modifier.height(2.dp))

            // As-Of Date Picker Item with TODAY button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateInputRow(
                    label = "CALCULATE AGE AS OF",
                    valueText = asOfDateText,
                    isPlaceholder = false,
                    contentDesc = asOfAccessibleText,
                    onClick = onSelectAsOfDateClicked,
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    onClick = onResetToTodayClicked,
                    modifier = Modifier
                        .heightIn(min = 52.dp)
                        .align(Alignment.Bottom),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Today,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DateInputRow(
    label: String,
    valueText: String,
    isPlaceholder: Boolean,
    contentDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .semantics { contentDescription = contentDesc },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isPlaceholder) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
