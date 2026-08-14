package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SampleBasedQuantumDiagonalizationCard(
    sqdState: SqdExecutionState,
    onRunSqd: (
        target: SqdHamiltonianTarget,
        shots: Int,
        noise: Float,
        k: Int,
        krylov: Int,
        zneEnabled: Boolean,
        zneModel: ZneExtrapolationModel,
        liveEntropy: Boolean
    ) -> Unit,
    onGenerateJsonExport: (SqdExecutionState.Completed) -> String,
    onGenerateCsvExport: (SqdExecutionState.Completed) -> String,
    modifier: Modifier = Modifier
) {
    var selectedTarget by remember { mutableStateOf(SqdHamiltonianTarget.LATTICE_SVP_CRYPTO) }
    var selectedShots by remember { mutableIntStateOf(10000) }
    var selectedNoisePct by remember { mutableFloatStateOf(4.5f) }
    var selectedSubspaceK by remember { mutableIntStateOf(8) }
    var selectedKrylovOrder by remember { mutableIntStateOf(1) }
    var isZneEnabled by remember { mutableStateOf(true) }
    var selectedZneModel by remember { mutableStateOf(ZneExtrapolationModel.RICHARDSON_POLYNOMIAL) }
    var isLiveEntropyLinked by remember { mutableStateOf(false) }

    var showTheoryGuide by remember { mutableStateOf(false) }
    var showSubspaceMatrix by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isProcessing = sqdState is SqdExecutionState.Processing
    val completedState = sqdState as? SqdExecutionState.Completed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sample_based_quantum_diagonalization_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D1A)),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Sample-based Quantum Diagonalization",
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sample-based Quantum Diagonalization",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Text(
                            text = "Classical postprocessing of noisy quantum samples for high-accuracy eigenvalue estimation",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (completedState != null) {
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Telemetry",
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(
                        color = Color(0xFF1E1B4B),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isProcessing) Color(0xFFF59E0B) else Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isProcessing) "Computing" else "SQD Engine",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFC7D2FE),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = Color(0xFF1E293B)
            )

            // Progress Banner when computing
            AnimatedVisibility(visible = isProcessing, enter = fadeIn(), exit = fadeOut()) {
                val processing = sqdState as? SqdExecutionState.Processing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1B4B).copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = processing?.phase ?: "Postprocessing noisy quantum measurements...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC7D2FE), fontWeight = FontWeight.Medium, fontSize = 11.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF818CF8),
                            strokeWidth = 2.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { processing?.progress ?: 0.5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF818CF8),
                        trackColor = Color(0xFF312E81),
                    )
                }
            }

            // Results Comparison Section (If completed)
            if (completedState != null) {
                // Key Accuracy Gain Hero Banner
                Surface(
                    color = Color(0xFF064E3B).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "+${"%.1f".format(completedState.errorReductionPct)}% Error Mitigation Achieved",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color(0xFFA7F3D0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Subspace projection + ${if (completedState.isZneEnabled) "ZNE (${completedState.zneModel.label})" else "Rayleigh-Ritz"} filtered out ${"%.1f".format(completedState.noiseLevelPct)}% hardware noise, reducing error from ${"%.2f".format(completedState.rawErrorPct)}% down to ${"%.2f".format(completedState.sqdErrorPct)}%.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD1FAE5), fontSize = 11.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3-Way Eigenvalue Comparison Matrix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Raw Noisy Quantum Estimate
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Raw Noisy Sample", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "%.4f".format(completedState.rawNoisyEstimate),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Error: +${"%.2f".format(completedState.rawErrorPct)}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEF4444), fontSize = 9.sp)
                            )
                        }
                    }

                    // SQD Classical Post-processed Estimate
                    OutlinedCard(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F2A3F).copy(alpha = 0.8f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SQD Postprocessed", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "%.4f".format(completedState.sqdEigenvalueEstimate),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "SQD Error: ${"%.2f".format(completedState.sqdErrorPct)}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            )
                        }
                    }

                    // Exact Benchmark Ground Energy
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF818CF8)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exact Ground E₀", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "%.4f".format(completedState.exactGroundEnergy),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFC7D2FE),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Theoretical Ideal",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                            )
                        }
                    }
                }

                // Zero-Noise Extrapolation (ZNE) Curve Strip
                if (completedState.isZneEnabled && completedState.znePoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF1E1B4B).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4F46E5).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ZNE Extrapolation Curve (${completedState.zneModel.label})",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFC7D2FE), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    )
                                }
                                Text(
                                    text = "E(λ→0) = ${"%.4f".format(completedState.zneExtrapolatedEnergy)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                completedState.znePoints.forEach { pt ->
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("λ = ${pt.scaleFactor}x", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 8.sp))
                                            Text(
                                                text = "%.3f".format(pt.measuredEnergy),
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFCA5A5), fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    color = Color(0xFF0369A1).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("λ → 0 (ZNE)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 8.sp))
                                        Text(
                                            text = "%.3f".format(completedState.zneExtrapolatedEnergy),
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spectral Hierarchy & Energy Gap Card
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Diagonalized Energy Spectrum Hierarchy",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                            Text(
                                text = "Energy Gap Δ: ${"%.3f".format(completedState.energyGap)} eV",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFF59E0B), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val states = listOf("E₀ (Ground)" to completedState.sqdEigenvalueEstimate) +
                                    completedState.excitedStates.mapIndexed { idx, v -> "E${idx + 1} (Excited)" to v }

                            states.forEachIndexed { index, (name, value) ->
                                Surface(
                                    color = if (index == 0) Color(0xFF0369A1).copy(alpha = 0.4f) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (index == 0) Color(0xFF38BDF8) else Color(0xFF475569)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(name, style = MaterialTheme.typography.labelSmall.copy(color = if (index == 0) Color(0xFF38BDF8) else Color(0xFF94A3B8), fontSize = 9.sp))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "%.3f".format(value),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subspace Sample Basis & Matrix Viewer Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subspace Configurations (K = ${completedState.subspaceDimK}${if (completedState.krylovOrder > 1) ", Krylov Order ${completedState.krylovOrder}" else ""})",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (showSubspaceMatrix) "Hide Matrix Elements" else "View Projected H_sub Matrix",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF818CF8), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showSubspaceMatrix = !showSubspaceMatrix }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Top Basis Samples Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    completedState.topBasisSamples.take(8).forEach { sample ->
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sample.bitstring,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${sample.shotCount} shots (${"%.1f".format(sample.probability * 100)}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                                )
                            }
                        }
                    }
                }

                // Subspace Matrix Heatmap
                AnimatedVisibility(visible = showSubspaceMatrix, enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(Color(0xFF0F172A), shape = RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF334155), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Projected Subspace Hamiltonian Matrix <v_i | H | v_j> (Condition Number κ = ${"%.2f".format(completedState.conditionNumber)})",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            completedState.subspaceMatrix.take(6).forEachIndexed { rowIdx, row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    row.take(6).forEachIndexed { colIdx, value ->
                                        val isDiagonal = rowIdx == colIdx
                                        Surface(
                                            color = if (isDiagonal) Color(0xFF1E3A8A) else Color(0xFF1E293B),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "%.2f".format(value),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isDiagonal) Color(0xFF93C5FD) else Color(0xFF94A3B8),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 8.sp,
                                                    fontWeight = if (isDiagonal) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Controls Section
            Text(
                text = "Target Physical & Threat Hamiltonian:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Target Hamiltonian Selector Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SqdHamiltonianTarget.values().forEach { target ->
                    val isSelected = selectedTarget == target
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTarget = target },
                        label = { Text(target.title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4F46E5),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color(0xFF818CF8) else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parameter Configuration Row (Subspace K, Krylov Depth, Shots)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subspace K Dimension
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Subspace (K = $selectedSubspaceK):",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(4, 8, 12, 16).forEach { k ->
                            val isKSelected = selectedSubspaceK == k
                            Surface(
                                color = if (isKSelected) Color(0xFF6366F1) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedSubspaceK = k }
                            ) {
                                Text(
                                    text = "$k",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isKSelected) Color.White else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Krylov Expansion Order
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Krylov Order:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1 to "1 (Std)", 2 to "2 (H v)", 3 to "3 (H² v)").forEach { (order, label) ->
                            val isOrderSelected = selectedKrylovOrder == order
                            Surface(
                                color = if (isOrderSelected) Color(0xFF8B5CF6) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedKrylovOrder = order }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isOrderSelected) Color.White else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantum Shots Selection Row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quantum Sampling Shots ($selectedShots shots):",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1000 to "1,000", 5000 to "5,000", 10000 to "10,000", 25000 to "25,000").forEach { (shots, label) ->
                        val isShotSelected = selectedShots == shots
                        Surface(
                            color = if (isShotSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedShots = shots }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isShotSelected) Color.White else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Noise Level Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Simulated Quantum Hardware Noise:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                    Text(
                        text = "${"%.1f".format(selectedNoisePct)}% Error Rate",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selectedNoisePct > 8f) Color(0xFFEF4444) else Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
                Slider(
                    value = selectedNoisePct,
                    onValueChange = { selectedNoisePct = it },
                    valueRange = 0.5f..15.0f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF818CF8),
                        activeTrackColor = Color(0xFF6366F1),
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Advanced Hybrid Features: ZNE and Live Entropy Link
            Surface(
                color = Color(0xFF131C31),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ZNE Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Zero-Noise Extrapolation (ZNE) Hybrid", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            Text("Extrapolates pulse-scaled noise curves to λ = 0", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp))
                        }
                        Switch(
                            checked = isZneEnabled,
                            onCheckedChange = { isZneEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6366F1),
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    if (isZneEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ZneExtrapolationModel.values().forEach { model ->
                                val isModelSelected = selectedZneModel == model
                                Surface(
                                    color = if (isModelSelected) Color(0xFF4F46E5) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedZneModel = model }
                                ) {
                                    Text(
                                        text = when (model) {
                                            ZneExtrapolationModel.RICHARDSON_POLYNOMIAL -> "Richardson"
                                            ZneExtrapolationModel.EXPONENTIAL_DECAY -> "Exponential"
                                            ZneExtrapolationModel.LINEAR_REGRESSION -> "Linear"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isModelSelected) Color.White else Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E293B))

                    // Live Network Packet Entropy Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Live Packet Entropy Flux Link", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                if (isLiveEntropyLinked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFF065F46), shape = RoundedCornerShape(4.dp)) {
                                        Text("STREAMING", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6EE7B7), fontSize = 8.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text("Modulate Hamiltonian diagonals in real-time from active network socket traffic", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp))
                        }
                        Switch(
                            checked = isLiveEntropyLinked,
                            onCheckedChange = { isLiveEntropyLinked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981),
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Run SQD Execution Button
            Button(
                onClick = {
                    onRunSqd(
                        selectedTarget,
                        selectedShots,
                        selectedNoisePct,
                        selectedSubspaceK,
                        selectedKrylovOrder,
                        isZneEnabled,
                        selectedZneModel,
                        isLiveEntropyLinked
                    )
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_sqd_postprocessing_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Diagonalizing Subspace...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Execute Sample-based Quantum Diagonalization", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Theoretical Guide Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showTheoryGuide = !showTheoryGuide }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showTheoryGuide) "Hide Theoretical Foundations" else "How does Sample-based Quantum Diagonalization work?",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                )
            }

            AnimatedVisibility(visible = showTheoryGuide, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFF1E293B), shape = RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔬 SQD Classical Postprocessing Mechanism:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFC7D2FE), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    Text(
                        text = "1. Quantum Sampling: Quantum hardware samples bitstring configurations from the state |ψ⟩ with noise.\n" +
                               "2. Low-Energy Subspace: We select the K most frequently measured basis states {|v₁⟩, ..., |v_K⟩}.\n" +
                               "3. Krylov Subspace Expansion: When order > 1, the basis is expanded with powers of the Hamiltonian {v, H v, H² v}.\n" +
                               "4. Classical Projection: The Hamiltonian matrix elements H_ij = ⟨v_i|H|v_j⟩ and overlap S_ij = ⟨v_i|v_j⟩ are computed classically.\n" +
                               "5. Generalized Eigenproblem: Solving H_sub c = E S_sub c projects out orthogonal hardware noise, yielding high-precision ground & excited energy eigenvalues.\n" +
                               "6. Zero-Noise Extrapolation: Scales noise across λ factors to fit and extrapolate to the zero-noise limit λ → 0.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.sp)
                    )
                }
            }
        }
    }

    // Export Telemetry Dialog
    if (showExportDialog && completedState != null) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF818CF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export SQD Telemetry Report", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Export the latest quantum diagonalization spectrum, subspace matrix, and noise mitigation benchmarks:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 12.sp)
                    )

                    Button(
                        onClick = {
                            val json = onGenerateJsonExport(completedState)
                            clipboardManager.setText(AnnotatedString(json))
                            Toast.makeText(context, "JSON telemetry copied to clipboard", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Full JSON Telemetry")
                    }

                    Button(
                        onClick = {
                            val csv = onGenerateCsvExport(completedState)
                            clipboardManager.setText(AnnotatedString(csv))
                            Toast.makeText(context, "CSV telemetry copied to clipboard", Toast.LENGTH_SHORT).show()
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy CSV Dataset")
                    }

                    OutlinedButton(
                        onClick = {
                            val report = """
                            Sample-based Quantum Diagonalization (SQD) Report
                            Target: ${completedState.target.title}
                            Ground State Energy (SQD): ${completedState.sqdEigenvalueEstimate} (Exact Benchmark: ${completedState.exactGroundEnergy})
                            Error Mitigation: +${"%.1f".format(completedState.errorReductionPct)}%
                            ZNE Zero-Noise Limit: ${completedState.zneExtrapolatedEnergy}
                            Condition Number: ${"%.3f".format(completedState.conditionNumber)}
                            Execution Time: ${completedState.executionTimeMs} ms
                            """.trimIndent()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "SQD Quantum Telemetry Report")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share SQD Report"))
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share via Android Share Sheet", color = Color(0xFF818CF8))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}
