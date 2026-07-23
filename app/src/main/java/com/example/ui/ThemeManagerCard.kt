package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeManagerCard(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textColorPrimary = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textColorSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val innerCardBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_manager_card"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.Bedtime else Icons.Default.WbSunny,
                        contentDescription = "Theme Manager",
                        tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "System-Wide Theme & Display",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColorPrimary
                            )
                        )
                        Text(
                            text = "Toggle light/dark modes to reduce battery usage & eye strain",
                            style = MaterialTheme.typography.labelSmall.copy(color = textColorSecondary)
                        )
                    }
                }

                // Active Mode Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkMode) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFFFEF3C7)
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDarkMode) "DARK MODE" else "LIGHT MODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = cardBorder
            )

            // Switch Row
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = innerCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.Bedtime else Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isDarkMode) "Dark Slate Mode (Active)" else "Light Canvas Mode (Active)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDarkMode) "Optimized for OLED dark pixel battery savings & low-light usage"
                                else "High contrast, daytime glare reduction & maximum legibility",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textColorSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7),
                            uncheckedThumbColor = Color(0xFFD97706),
                            uncheckedTrackColor = Color(0xFFFEF3C7)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selection Quick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dark Mode Select Option
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleDarkMode(true) },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isDarkMode) Color(0xFF0284C7).copy(alpha = 0.15f) else innerCardBg
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isDarkMode) Color(0xFF38BDF8) else cardBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = if (isDarkMode) Color(0xFF38BDF8) else textColorSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isDarkMode) Color(0xFF38BDF8) else textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "-35% Battery",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Light Mode Select Option
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleDarkMode(false) },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (!isDarkMode) Color(0xFFFEF3C7) else innerCardBg
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (!isDarkMode) Color(0xFFD97706) else cardBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (!isDarkMode) Color(0xFFD97706) else textColorSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Light Mode",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (!isDarkMode) Color(0xFFB45309) else textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Sunlight Readability",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (!isDarkMode) Color(0xFFB45309) else textColorSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
