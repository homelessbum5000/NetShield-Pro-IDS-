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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CudaQLanguage(
    val id: String,
    val title: String,
    val compilerTag: String,
    val invocationCmd: String,
    val color: Color
) {
    PYTHON(
        id = "python",
        title = "Python (cudaq)",
        compilerTag = "cudaq JIT / MLIR",
        invocationCmd = "python3 -m cudaq --target nvidia-mqpu script.py",
        color = Color(0xFF38BDF8)
    ),
    CPP(
        id = "cpp",
        title = "C++20 (nvq++)",
        compilerTag = "nvq++ Clang / LLVM",
        invocationCmd = "nvq++ -target nvidia-mqpu -std=c++20 main.cpp -o main.out",
        color = Color(0xFFA78BFA)
    )
}

enum class CudaQBackendTarget(val label: String, val desc: String, val speedFactor: Float) {
    NVIDIA_MQPU("nvidia-mqpu", "Multi-GPU Accelerated State Vector Simulator", 12.5f),
    TENSORNET("tensornet", "GPU Matrix Product State (MPS) for high-qubit systems", 8.0f),
    DENSITY_MATRIX("density-matrix-simulator", "Full Noisy Open Quantum System Density Matrix", 4.2f),
    QPP_CPU("qpp-cpu", "Host C++ CPU Reference Simulator", 1.0f)
}

data class CudaQQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class HeterogeneousStage(
    val stageName: String,
    val deviceDomain: String,
    val role: String,
    val latency: String,
    val color: Color
)

data class CudaQModule(
    val id: String,
    val moduleNumber: Int,
    val title: String,
    val subtitle: String,
    val topicTag: String,
    val theoryMarkdown: String,
    val pythonCode: String,
    val cppCode: String,
    val pythonLogs: List<String>,
    val cppLogs: List<String>,
    val heterogeneousPipeline: List<HeterogeneousStage>,
    val executionSummary: String,
    val quiz: CudaQQuizQuestion
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CudaQAcademicStudioCard(
    modifier: Modifier = Modifier
) {
    val modules = remember { getCudaQAcademicModules() }
    var selectedModuleIndex by remember { mutableIntStateOf(0) }
    var selectedLanguage by remember { mutableStateOf(CudaQLanguage.PYTHON) }
    var selectedBackend by remember { mutableStateOf(CudaQBackendTarget.NVIDIA_MQPU) }
    var qubitCount by remember { mutableIntStateOf(6) }
    var isExecutingCell by remember { mutableStateOf(false) }
    var executionProgress by remember { mutableFloatStateOf(0f) }
    var executedOutputs by remember { mutableStateOf<List<String>?>(null) }
    var showArchitecturePipeline by remember { mutableStateOf(false) }
    var showQuizSection by remember { mutableStateOf(false) }
    var selectedQuizOption by remember { mutableIntStateOf(-1) }
    var quizSubmitted by remember { mutableStateOf(false) }
    var completedModuleIds by remember { mutableStateOf(setOf<String>()) }

    val currentModule = modules[selectedModuleIndex]
    val currentCode = if (selectedLanguage == CudaQLanguage.PYTHON) currentModule.pythonCode else currentModule.cppCode
    val currentLogs = if (selectedLanguage == CudaQLanguage.PYTHON) currentModule.pythonLogs else currentModule.cppLogs

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cuda_q_academic_studio_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070F1E)),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with CUDA Quantum branding
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
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "CUDA-Q Academic Materials",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CUDA Quantum Heterogeneous Studio",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Text(
                            text = "Unified C++20 (nvq++) & Python (cudaq) workflows across CPU, GPU & QPU targets",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF064E3B).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "CUDA-Q v0.9 (C++/Py)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFA7F3D0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF1E293B)
            )

            // Module Navigation Selector Chips
            Text(
                text = "Academic Curriculum Modules:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                modules.forEachIndexed { index, mod ->
                    val isSelected = selectedModuleIndex == index
                    val isCompleted = completedModuleIds.contains(mod.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedModuleIndex = index
                            executedOutputs = null
                            showQuizSection = false
                            selectedQuizOption = -1
                            quizSubmitted = false
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCompleted) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("M${mod.moduleNumber}: ${mod.topicTag}", fontSize = 10.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF047857),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color(0xFF34D399) else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Module Detail Banner
            Surface(
                color = Color(0xFF131C31),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Module ${currentModule.moduleNumber}: ${currentModule.title}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentModule.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentModule.theoryMarkdown,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp, lineHeight = 14.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heterogeneous Language Switcher Tab Bar (Python vs C++20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Programming Model Language:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                )

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        CudaQLanguage.values().forEach { lang ->
                            val isLangActive = selectedLanguage == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isLangActive) lang.color.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable {
                                        selectedLanguage = lang
                                        executedOutputs = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isLangActive) lang.color else Color(0xFF64748B))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = lang.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isLangActive) Color.White else Color(0xFF94A3B8),
                                            fontWeight = if (isLangActive) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Parameter Adjustment Row (Backend Simulator & Qubits)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Backend Simulator Selector
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "QPU Target Simulator:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBackend.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "${selectedBackend.speedFactor}x GPU",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF34D399), fontSize = 9.sp)
                            )
                        }
                    }
                }

                // Qubit Register Slider
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Register Size:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Text("$qubitCount Qubits", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                    }
                    Slider(
                        value = qubitCount.toFloat(),
                        onValueChange = { qubitCount = it.toInt() },
                        valueRange = 2f..16f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF10B981),
                            activeTrackColor = Color(0xFF059669),
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Heterogeneous Pipeline Toggle Accordion
            Surface(
                color = Color(0xFF0C1628),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showArchitecturePipeline = !showArchitecturePipeline },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Heterogeneous Architecture Pipeline (CPU ↔ GPU ↔ QPU)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Icon(
                            imageVector = if (showArchitecturePipeline) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (showArchitecturePipeline) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Low-latency heterogeneous execution bridging classical host control and quantum co-processors:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        currentModule.heterogeneousPipeline.forEachIndexed { sIdx, stage ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(stage.color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${sIdx + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = stage.color,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = stage.stageName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = stage.color.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = stage.deviceDomain,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = stage.color,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = stage.role,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                                        )
                                    }
                                }
                                Text(
                                    text = stage.latency,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF34D399),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code Cell Viewer with Language Badge & Command line
            Surface(
                color = Color(0xFF050B14),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = selectedLanguage.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Source: ${selectedLanguage.title} (${selectedLanguage.compilerTag})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = selectedLanguage.color,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentCode))
                                Toast.makeText(context, "${selectedLanguage.title} kernel copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                        }
                    }

                    // Toolchain invocation command tag
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "$ ${selectedLanguage.invocationCmd}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentCode,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 15.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF030712), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Run Cell & Export Notebook / Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isExecutingCell = true
                        executionProgress = 0.1f
                        coroutineScope.launch {
                            delay(250)
                            executionProgress = 0.45f
                            delay(300)
                            executionProgress = 0.8f
                            delay(250)
                            executionProgress = 1.0f
                            isExecutingCell = false
                            executedOutputs = currentLogs
                            completedModuleIds = completedModuleIds + currentModule.id
                        }
                    },
                    enabled = !isExecutingCell,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isExecutingCell) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compiling via nvq++...", fontSize = 11.sp)
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execute ${selectedLanguage.title.substringBefore(" ")} Kernel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        val payload = if (selectedLanguage == CudaQLanguage.PYTHON) {
                            generateJupyterNotebookJson(currentModule, selectedBackend, qubitCount)
                        } else {
                            generateCppSourceFile(currentModule, selectedBackend, qubitCount)
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "CUDA-Q Module ${currentModule.moduleNumber} (${selectedLanguage.title})")
                            putExtra(Intent.EXTRA_TEXT, payload)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export CUDA-Q Source Code"))
                    },
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6))
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (selectedLanguage == CudaQLanguage.PYTHON) "Export .ipynb" else "Export .cpp",
                        fontSize = 11.sp,
                        color = Color(0xFF93C5FD)
                    )
                }
            }

            if (isExecutingCell) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { executionProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Execution Output Terminal
            if (executedOutputs != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF030712),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Heterogeneous Execution Output [${selectedLanguage.compilerTag}]",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF34D399),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Text(
                                text = "Target: ${selectedBackend.label}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        executedOutputs?.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (line.startsWith("✓") || line.contains("Optimal") || line.contains("Complete")) Color(0xFF34D399) else if (line.contains("Error") || line.contains("Warning")) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Knowledge Check Quiz Section
            Surface(
                color = Color(0xFF131C31),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showQuizSection = !showQuizSection },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Module Knowledge Check Quiz",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }
                        Icon(
                            imageVector = if (showQuizSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (showQuizSection) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentModule.quiz.question,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        currentModule.quiz.options.forEachIndexed { optIdx, optText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!quizSubmitted) selectedQuizOption = optIdx
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedQuizOption == optIdx,
                                    onClick = { if (!quizSubmitted) selectedQuizOption = optIdx },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981), unselectedColor = Color(0xFF475569))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (quizSubmitted && optIdx == currentModule.quiz.correctIndex) Color(0xFF34D399) else Color(0xFFCBD5E1),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!quizSubmitted) {
                            Button(
                                onClick = { quizSubmitted = true },
                                enabled = selectedQuizOption != -1,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Answer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            val isCorrect = selectedQuizOption == currentModule.quiz.correctIndex
                            Surface(
                                color = if (isCorrect) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF7F1D1D).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isCorrect) "✓ Correct Answer!" else "✗ Incorrect.",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isCorrect) Color(0xFFA7F3D0) else Color(0xFFFCA5A5),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentModule.quiz.explanation,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateJupyterNotebookJson(module: CudaQModule, backend: CudaQBackendTarget, qubits: Int): String {
    return """
    {
      "cells": [
        {
          "cell_type": "markdown",
          "metadata": {},
          "source": [
            "# CUDA-Q Academic Module ${module.moduleNumber}: ${module.title}\n",
            "## ${module.subtitle}\n\n",
            "${module.theoryMarkdown}\n"
          ]
        },
        {
          "cell_type": "code",
          "execution_count": 1,
          "metadata": {},
          "outputs": [],
          "source": [
            "# Target: ${backend.label} | Register: $qubits Qubits\n",
            "${module.pythonCode.replace("\n", "\\n")}"
          ]
        }
      ],
      "metadata": {
        "kernelspec": {
          "display_name": "CUDA-Q (Python)",
          "language": "python",
          "name": "cudaq"
        },
        "language_info": {
          "name": "cudaq",
          "version": "0.9.0"
        }
      },
      "nbformat": 4,
      "nbformat_minor": 4
    }
    """.trimIndent()
}

private fun generateCppSourceFile(module: CudaQModule, backend: CudaQBackendTarget, qubits: Int): String {
    return """
    // CUDA Quantum C++20 Heterogeneous Source File
    // Module ${module.moduleNumber}: ${module.title}
    // Compile with: nvq++ -target ${backend.label} -std=c++20 main.cpp -o main.out
    
    ${module.cppCode}
    """.trimIndent()
}

private fun getCudaQAcademicModules(): List<CudaQModule> {
    return listOf(
        CudaQModule(
            id = "m1_fundamentals",
            moduleNumber = 1,
            title = "Introduction to CUDA-Q Kernels & QPU Offloading",
            subtitle = "Foundations of hybrid programming, __qpu__ kernel declarations, and GPU-accelerated state vector simulation",
            topicTag = "Fundamentals",
            theoryMarkdown = "CUDA-Q enables single-source C++ and Python hybrid quantum-classical programming. Quantum kernels are declared with the __qpu__ decorator or cudaq.kernel, allocating quantum registers (cudaq::qvector) and executing Clifford + T gate sets seamlessly alongside CUDA GPU tensor kernels.",
            pythonCode = """
import cudaq

@cudaq.kernel
def ghz_state(qubit_count: int):
    # Allocate quantum register on target QPU
    q = cudaq.qvector(qubit_count)
    
    # Place qubit 0 into superposition
    h(q[0])
    
    # Entangle all subsequent qubits
    for i in range(qubit_count - 1):
        cx(q[i], q[i + 1])
        
    # Measure all qubits in computational Z-basis
    mz(q)

# Set backend to multi-GPU accelerated state simulator
cudaq.set_target("nvidia-mqpu")
counts = cudaq.sample(ghz_state, 6, shots_count=10000)
print(counts)
            """.trimIndent(),
            cppCode = """
#include <cudaq.h>
#include <iostream>

// CUDA-Q Single-Source C++20 Quantum Kernel
struct ghz_state {
    void operator()(int num_qubits) __qpu__ {
        // Allocate quantum register on target QPU/GPU
        cudaq::qvector q(num_qubits);
        
        // Superposition on qubit 0
        h(q[0]);
        
        // Multi-qubit entanglement cascade
        for (int i = 0; i < num_qubits - 1; ++i) {
            cx(q[i], q[i + 1]);
        }
        
        // Measure state in computational basis
        mz(q);
    }
};

int main() {
    // Compile & dispatch on NVIDIA Multi-GPU backend
    auto counts = cudaq::sample(10000, ghz_state{}, 6);
    counts.dump();
    return 0;
}
            """.trimIndent(),
            pythonLogs = listOf(
                "✓ Target backend configured: 'nvidia-mqpu' (CUDA Unified Memory enabled)",
                "✓ QPU Kernel 'ghz_state(6)' compiled with cudaq MLIR JIT",
                "✓ Quantum Sampling Complete (10,000 shots in 1.42ms on GPU):",
                "  |000000⟩ : 5,014 shots (50.14%)",
                "  |111111⟩ : 4,986 shots (49.86%)",
                "✓ Entanglement Fidelity F: 0.9998"
            ),
            cppLogs = listOf(
                "✓ nvq++ -target nvidia-mqpu -std=c++20 main.cpp -o main.out",
                "✓ Quake MLIR Dialect lowered to LLVM QIR & PTX",
                "✓ Host C++ thread launched kernel across 4 NVIDIA GPUs via Unified Memory",
                "✓ QPU Sample Result: { 000000: 5022, 111111: 4978 }",
                "✓ Execution runtime: 0.89ms (Host DMA latency: 0.04ms)"
            ),
            heterogeneousPipeline = listOf(
                HeterogeneousStage("Host Control", "CPU (Host)", "Coordinates application lifecycle & memory allocations", "0.02ms", Color(0xFF60A5FA)),
                HeterogeneousStage("Quake MLIR JIT", "Compiler", "Translates C++/Python AST into quantum-classical IR", "0.15ms", Color(0xFFA78BFA)),
                HeterogeneousStage("GPU Unified Memory", "CUDA Driver", "Zero-copy shared memory buffer synchronization", "0.04ms", Color(0xFF34D399)),
                HeterogeneousStage("Multi-GPU Simulation", "NVIDIA QPU", "cuStateVec multi-node state tensor contraction", "1.21ms", Color(0xFFF59E0B))
            ),
            executionSummary = "6-qubit GHZ state verified with near-perfect entanglement fidelity on nvidia-mqpu simulator.",
            quiz = CudaQQuizQuestion(
                question = "In single-source CUDA-Q C++, how is a quantum kernel designated for QPU/GPU compilation?",
                options = listOf(
                    "By annotating member operator() with __qpu__",
                    "By using the #pragma omp quantum directive",
                    "By inheriting from std::thread",
                    "By calling malloc_quantum()"
                ),
                correctIndex = 0,
                explanation = "In CUDA-Q C++, quantum kernels are callable structs or functions annotated with the `__qpu__` attribute, instructing nvq++ to lower quantum operations to Quake MLIR and QIR."
            )
        ),
        CudaQModule(
            id = "m2_vqe",
            moduleNumber = 2,
            title = "Variational Quantum Eigensolver (VQE) with PyTorch Optimizers",
            subtitle = "Hybrid quantum-classical optimization loop, parameter-shift gradients, and molecular/lattice ground states",
            topicTag = "VQE & PyTorch",
            theoryMarkdown = "VQE evaluates expectation values <ψ(θ)| H |ψ(θ)> on quantum hardware or simulators while classical gradient descent algorithms (Adam, COBYLA, L-BFGS) iteratively update variational angles θ to locate the minimum eigenvalue E_0.",
            pythonCode = """
import cudaq
from cudaq import spin
import torch

# Define molecular / lattice Hamiltonian
H = 5.907 - 2.143*spin.z(0) - 2.143*spin.z(1) + 0.398*spin.x(0)*spin.x(1)

@cudaq.kernel
def vqe_ansatz(thetas: list[float]):
    q = cudaq.qvector(2)
    x(q[0])
    ry(thetas[0], q[0])
    ry(thetas[1], q[1])
    cx(q[0], q[1])

# Hybrid optimization loop
optimizer = torch.optim.Adam([torch.tensor([0.1, 0.1], requires_grad=True)], lr=0.05)
# Compute ground energy: E0 = cudaq.observe(vqe_ansatz, H, thetas).expectation()
            """.trimIndent(),
            cppCode = """
#include <cudaq.h>
#include <cudaq/optimizers.h>
#include <iostream>

struct vqe_ansatz {
    void operator()(std::vector<double> thetas) __qpu__ {
        cudaq::qvector q(2);
        x(q[0]);
        ry(thetas[0], q[0]);
        ry(thetas[1], q[1]);
        cx(q[0], q[1]);
    }
};

int main() {
    using namespace cudaq::spin;
    auto H = 5.907 - 2.143 * z(0) - 2.143 * z(1) + 0.398 * x(0) * x(1);
    
    // Built-in GPU-accelerated L-BFGS / Adam classical optimizer
    cudaq::optimizers::lbfgs optimizer;
    auto [min_energy, opt_params] = cudaq::vqe(
        vqe_ansatz{}, H, optimizer, /*num_params=*/2
    );
    
    std::cout << "E0 = " << min_energy << " Hartree\n";
    return 0;
}
            """.trimIndent(),
            pythonLogs = listOf(
                "✓ Loaded Hamiltonian: 4 Pauli terms across 2 active qubits",
                "✓ Starting PyTorch Adam optimizer loop (learning rate η = 0.05)...",
                "  Iteration 10 : <H> = -0.4124 Hartree | Gradient norm = 0.812",
                "  Iteration 25 : <H> = -1.0821 Hartree | Gradient norm = 0.145",
                "  Iteration 50 : <H> = -1.1373 Hartree | Gradient norm = 0.002",
                "✓ Converged to Ground State Energy E0 = -1.1373 Hartree (Target: -1.1373)"
            ),
            cppLogs = listOf(
                "✓ nvq++ -target nvidia-mqpu -std=c++20 main.cpp -lcudaq-optimizers -o vqe.out",
                "✓ Initialized cudaq::optimizers::lbfgs with parameter-shift quantum gradient engine",
                "  Step 0  : Energy = +1.6210 Hartree",
                "  Step 8  : Energy = -0.8920 Hartree",
                "  Step 18 : Energy = -1.13728 Hartree (Convergence criteria met: ΔE < 1e-6)",
                "✓ Optimal variational angles θ = [0.2854, 1.5708]"
            ),
            heterogeneousPipeline = listOf(
                HeterogeneousStage("Parameter Update", "Host CPU", "Evaluates Adam/L-BFGS Hessian approximation", "0.01ms", Color(0xFF60A5FA)),
                HeterogeneousStage("Parameter-Shift QPU", "NVIDIA GPU", "Parallel forward + backward circuit shift evaluations", "0.85ms", Color(0xFFF59E0B)),
                HeterogeneousStage("Expectation <H>", "cuQuantum", "Direct inner-product tensor contraction <ψ|H|ψ>", "0.18ms", Color(0xFF34D399)),
                HeterogeneousStage("Convergence Check", "Host CPU", "Residual loss evaluation against convergence threshold", "0.01ms", Color(0xFFA78BFA))
            ),
            executionSummary = "VQE converged to the exact ground state energy in 50 hybrid iterations.",
            quiz = CudaQQuizQuestion(
                question = "How does CUDA-Q compute exact gradients for variational quantum circuits during hybrid optimization?",
                options = listOf(
                    "Parameter-shift rule evaluated concurrently on QPU / GPU backends",
                    "Random perturbation without derivatives",
                    "Classical finite difference only",
                    "Simulated annealing"
                ),
                correctIndex = 0,
                explanation = "CUDA-Q leverages analytical parameter-shift rules evaluated concurrently on GPUs to produce exact analytical quantum gradients."
            )
        ),
        CudaQModule(
            id = "m3_sqd",
            moduleNumber = 3,
            title = "Sample-based Quantum Diagonalization (SQD) in CUDA-Q",
            subtitle = "Accelerated classical subspace projection & GPU Rayleigh-Ritz matrix diagonalization for noisy quantum samples",
            topicTag = "SQD & Subspaces",
            theoryMarkdown = "SQD samples dominant computational basis bitstrings from a noisy quantum device, constructs an orthonormalized classical configuration subspace { |v_i> }, and projects the full Hamiltonian matrix onto this subspace before solving H_sub c = E S_sub c with cuSOLVER.",
            pythonCode = """
# CUDA-Q Sample-based Quantum Diagonalization (SQD) Pipeline
import cudaq
import numpy as np

# 1. Sample bitstrings from quantum circuit with noise
samples = cudaq.sample(trial_ansatz, shots_count=20000)

# 2. Select top K most frequent configurations
top_k_basis = [b for b, cnt in samples.most_frequent(k=8)]

# 3. Project Hamiltonian matrix elements <v_i| H |v_j> on GPU
H_sub = cudaq.linalg.project_hamiltonian(H, top_k_basis)

# 4. Diagonalize subspace matrix with GPU cuSOLVER
eigenvalues, eigenvectors = np.linalg.eigh(H_sub)
print(f"SQD Postprocessed Ground Energy: {eigenvalues[0]:.6f}")
            """.trimIndent(),
            cppCode = """
#include <cudaq.h>
#include <cudaq/builder.h>
#include <cusolverDn.h>
#include <iostream>

int main() {
    // 1. Quantum sampling on physical/simulated QPU
    auto samples = cudaq::sample(20000, trial_ansatz{});
    
    // 2. Select configuration subspace basis
    auto subspace_basis = samples.get_top_k_bitstrings(8);
    
    // 3. Compute subspace matrix H_ij on NVIDIA GPU via cuBLAS
    cudaq::subspace_matrix H_sub = cudaq::project_subspace(H, subspace_basis);
    
    // 4. Generalized eigenvalue solution with cuSOLVER
    auto [eigenvalues, eigenvectors] = H_sub.eigensolve_gpu();
    std::cout << "SQD Ground Energy: " << eigenvalues[0] << " eV\n";
    return 0;
}
            """.trimIndent(),
            pythonLogs = listOf(
                "✓ Sampled 20,000 shots with 4.5% depolarizing hardware noise",
                "✓ Subspace Selection: Top K = 8 basis configurations isolated",
                "✓ Matrix Projection: 8x8 subspace Hamiltonian matrix constructed via cuBLAS",
                "✓ Generalized Eigenvalue Decomposition Complete:",
                "  Raw Noisy Sample Average  : -10.4501 eV",
                "  SQD Subspace Eigenvalue E0: -14.8124 eV (Benchmark: -14.8200 eV)",
                "✓ Noise Reduction Gain: 96.2% error mitigated"
            ),
            cppLogs = listOf(
                "✓ nvq++ -target nvidia-mqpu -lcusolver -lcublas main.cpp -o sqd.out",
                "✓ Dispatched 20k shots to QPU simulator in 0.62ms",
                "✓ cuBLAS constructed 8x8 subspace Hamiltonian in 0.08ms",
                "✓ cuSOLVER syevd Rayleigh-Ritz eigenvalue solution in 0.04ms",
                "✓ Filtered eigenvalue: -14.8124 eV (Raw expectation: -10.4501 eV)"
            ),
            heterogeneousPipeline = listOf(
                HeterogeneousStage("Quantum Sampling", "QPU / Sim", "Generates configuration bitstrings under hardware noise", "0.62ms", Color(0xFFF59E0B)),
                HeterogeneousStage("Subspace Selection", "Host CPU", "Filters top-K dominant configuration states", "0.03ms", Color(0xFF60A5FA)),
                HeterogeneousStage("Matrix Projection", "GPU cuBLAS", "Dense matrix contraction for subspace Hamiltonian H_sub", "0.08ms", Color(0xFF34D399)),
                HeterogeneousStage("Rayleigh-Ritz Solve", "GPU cuSOLVER", "Generalized Hermitian eigenvalue diagonalization", "0.04ms", Color(0xFFA78BFA))
            ),
            executionSummary = "SQD postprocessing successfully extracted ground state eigenvalue with 96.2% noise attenuation.",
            quiz = CudaQQuizQuestion(
                question = "Why does Sample-based Quantum Diagonalization (SQD) tolerate high hardware noise better than raw expectation measurements?",
                options = listOf(
                    "It filters out noise orthogonal to the configuration subspace by solving generalized Rayleigh-Ritz eigenvalues",
                    "It increases the circuit depth by 100x",
                    "It eliminates the need for quantum measurements",
                    "It converts quantum gates into classical NAND gates"
                ),
                correctIndex = 0,
                explanation = "SQD projects noisy state samples onto a dominant classical basis subspace, discarding orthogonal incoherence and noise."
            )
        ),
        CudaQModule(
            id = "m4_zne",
            moduleNumber = 4,
            title = "Zero-Noise Extrapolation (ZNE) & Error Mitigation",
            subtitle = "Pulse stretching, unitary folding, and polynomial extrapolation to zero-noise limit λ → 0",
            topicTag = "ZNE Mitigation",
            theoryMarkdown = "ZNE intentionally scales physical hardware noise by stretching microwave pulses or folding unitary gates U -> U(U† U)^n by factors λ ∈ [1.0, 1.5, 2.0, 3.0], fitting a Richardson polynomial or exponential decay curve to extrapolate E(λ → 0).",
            pythonCode = """
import cudaq

# Define zero-noise extrapolation pipeline
zne_config = cudaq.ZneConfig(
    noise_scale_factors=[1.0, 1.5, 2.0, 3.0],
    extrapolation_model="richardson_polynomial"
)

# Execute mitigated expectation measurement
mitigated_result = cudaq.observe_mitigated(
    kernel=ansatz_kernel,
    spin_operator=hamiltonian,
    mitigation=zne_config
)
print(f"Mitigated Energy: {mitigated_result.expectation()}")
            """.trimIndent(),
            cppCode = """
#include <cudaq.h>
#include <cudaq/mitigation.h>
#include <iostream>

int main() {
    // Configure Richardson polynomial Zero-Noise Extrapolation
    cudaq::zne_extrapolate zne{
        .scale_factors = {1.0, 1.5, 2.0, 3.0},
        .method = cudaq::extrapolation_method::richardson
    };
    
    // Execute observation across folded unitary circuits
    auto result = cudaq::observe_mitigated(zne, ansatz_kernel{}, hamiltonian);
    std::cout << "Zero-Noise Expectation <H>: " << result.expectation() << "\n";
    return 0;
}
            """.trimIndent(),
            pythonLogs = listOf(
                "✓ Multi-scale noise execution [λ = 1.0x, 1.5x, 2.0x, 3.0x] completed",
                "  λ = 1.0x : E = -12.140 eV",
                "  λ = 1.5x : E = -10.920 eV",
                "  λ = 2.0x : E = -9.710 eV",
                "  λ = 3.0x : E = -7.280 eV",
                "✓ Richardson 2nd-order polynomial fit applied:",
                "  Extrapolated Zero-Noise E(λ → 0) = -14.819 eV (Target: -14.820 eV)"
            ),
            cppLogs = listOf(
                "✓ nvq++ -target nvidia-mqpu -std=c++20 main.cpp -o zne.out",
                "✓ Unitary folding: generated 4 circuit variants with depths [24, 36, 48, 72]",
                "✓ Executed folded batches concurrently on 4 GPU streams in 1.15ms",
                "✓ Richardson polynomial extrapolation result: -14.8192 eV (Exact target: -14.8200 eV)"
            ),
            heterogeneousPipeline = listOf(
                HeterogeneousStage("Unitary Folding", "Host / AST", "Expands gates U -> U(U† U)^n to scale physical noise", "0.05ms", Color(0xFF60A5FA)),
                HeterogeneousStage("Batched QPU Execution", "NVIDIA Multi-GPU", "Parallel execution across multiple noise scale factors λ", "1.15ms", Color(0xFFF59E0B)),
                HeterogeneousStage("Polynomial Curve Fit", "Host CPU", "Richardson algebraic extrapolation to zero-noise limit λ=0", "0.02ms", Color(0xFF34D399)),
                HeterogeneousStage("Result Delivery", "Host Thread", "Returns noise-mitigated physical observable expectation", "0.01ms", Color(0xFFA78BFA))
            ),
            executionSummary = "ZNE extrapolation recovered clean physical eigenvalues from scaled noisy measurements.",
            quiz = CudaQQuizQuestion(
                question = "What is the primary mechanism of Unitary Folding in Zero-Noise Extrapolation?",
                options = listOf(
                    "Replacing gate U with U (U† U)^n to systematically multiply hardware noise without changing ideal unitary logic",
                    "Removing all CNOT gates from the circuit",
                    "Adding classical checksums to quantum registers",
                    "Freezing QPU superconducting qubits"
                ),
                correctIndex = 0,
                explanation = "Since U† U = I (the identity), appending pairs of inverted unitaries preserves the mathematical function while scaling noise."
            )
        ),
        CudaQModule(
            id = "m5_qsvm",
            moduleNumber = 5,
            title = "Quantum Intrusion Detection: QSVM Kernel Estimation",
            subtitle = "Quantum feature maps for high-dimensional network flow vectors and anomaly classification on GPU simulators",
            topicTag = "Quantum ML & IDS",
            theoryMarkdown = "Quantum Support Vector Machines (QSVM) map classical network flow vectors x into exponentially large Hilbert spaces using ZZFeatureMap circuits. The inner products K(x_i, x_j) = |<Φ(x_i)|Φ(x_j)>|² form a quantum kernel matrix evaluated on CUDA-Q to detect zero-day intrusions.",
            pythonCode = """
import cudaq
import numpy as np

@cudaq.kernel
def zz_feature_map(x: list[float]):
    q = cudaq.qvector(len(x))
    # Layer 1: Hadamard superposition
    h(q)
    # Layer 2: Single-qubit phase rotations
    for i in range(len(x)):
        rz(2.0 * x[i], q[i])
    # Layer 3: Entangling ZZ feature interactions
    for i in range(len(x) - 1):
        cx(q[i], q[i + 1])
        rz(2.0 * (np.pi - x[i]) * (np.pi - x[i + 1]), q[i + 1])
        cx(q[i], q[i + 1])

# Compute quantum kernel matrix K_ij for network packet flows
            """.trimIndent(),
            cppCode = """
#include <cudaq.h>
#include <vector>
#include <iostream>

struct zz_feature_map {
    void operator()(std::vector<double> x) __qpu__ {
        cudaq::qvector q(x.size());
        h(q);
        for (std::size_t i = 0; i < x.size(); ++i) {
            rz(2.0 * x[i], q[i]);
        }
        for (std::size_t i = 0; i < x.size() - 1; ++i) {
            cx(q[i], q[i + 1]);
            rz(2.0 * (M_PI - x[i]) * (M_PI - x[i + 1]), q[i + 1]);
            cx(q[i], q[i + 1]);
        }
    }
};

int main() {
    // Quantum Kernel Matrix estimation for 128 network vectors
    std::vector<std::vector<double>> network_features(128, std::vector<double>(6, 0.5));
    auto kernel_matrix = cudaq::compute_kernel_matrix(zz_feature_map{}, network_features);
    std::cout << "Evaluated 128x128 Quantum Kernel Matrix on GPU\n";
    return 0;
}
            """.trimIndent(),
            pythonLogs = listOf(
                "✓ Ingested 128 high-dimensional network flow vectors (packet size, entropy, TCP flags)",
                "✓ Quantum Kernel Matrix K (128x128) evaluated via 'nvidia-mqpu' in 8.4ms",
                "✓ Support Vector Classifier trained with Quantum Kernel:",
                "  Test Accuracy on Malicious Flow Detection: 98.6%",
                "  Zero-Day Quantum Attack Detection Precision: 99.1%",
                "✓ Classification latency: 0.12ms / packet flow"
            ),
            cppLogs = listOf(
                "✓ nvq++ -target nvidia-mqpu -std=c++20 main.cpp -o qsvm.out",
                "✓ Distributed 16,384 quantum state overlap kernels across 8 GPU streams",
                "✓ Kernel matrix evaluation completed in 4.1ms (Speedup: 2.1x over Python JIT)",
                "✓ Quadratic Programming dual optimization solved on CPU host thread in 0.35ms",
                "✓ IDS Zero-Day Detection Accuracy: 98.6%"
            ),
            heterogeneousPipeline = listOf(
                HeterogeneousStage("Packet Ingestion", "Host DMA", "Buffers network telemetry vectors (entropy, ports, flags)", "0.02ms", Color(0xFF60A5FA)),
                HeterogeneousStage("Quantum Feature Map", "QPU Simulator", "Maps features x_i into Hilbert space |Φ(x_i)>", "4.10ms", Color(0xFFF59E0B)),
                HeterogeneousStage("Gram Matrix Inner Prod", "GPU TensorNet", "Computes fidelity matrix K_ij = |<Φ_i|Φ_j>|^2", "0.45ms", Color(0xFF34D399)),
                HeterogeneousStage("Dual QP Solver", "Host CPU", "SVM hyper-plane boundary classification", "0.35ms", Color(0xFFA78BFA))
            ),
            executionSummary = "QSVM achieved 98.6% intrusion detection accuracy with GPU quantum kernel estimation.",
            quiz = CudaQQuizQuestion(
                question = "What advantage does a Quantum Kernel Feature Map offer for network anomaly detection compared to classical RBF kernels?",
                options = listOf(
                    "It maps non-linear packet flow correlations into an exponentially large Hilbert space where attacks become linearly separable",
                    "It reduces packet sizes on Ethernet cables",
                    "It replaces firewalls with optical mirrors",
                    "It operates without network interfaces"
                ),
                correctIndex = 0,
                explanation = "Quantum feature maps utilize entanglement to project non-linear network patterns into high-dimensional Hilbert spaces."
            )
        )
    )
}
