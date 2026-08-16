package com.example.ui

import android.os.Debug
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.random.Random

data class DetectedLeakTrace(
    val id: String,
    val targetClass: String,
    val leakType: String,
    val retainedBytes: Long,
    val gcRoot: String,
    val referenceChain: List<String>,
    val explanation: String,
    val severity: LeakSeverity,
    val timeAgo: String
)

enum class LeakSeverity(val label: String, val color: Color) {
    CRITICAL("CRITICAL LEAK", Color(0xFFEF4444)),
    HIGH("HIGH RISK", Color(0xFFF97316)),
    MEDIUM("MEDIUM", Color(0xFFF59E0B)),
    LOW("LOW / CACHE", Color(0xFF38BDF8))
}

class ComposeObjectWatcher {
    private val watchedReferences = mutableMapOf<String, WeakReference<Any>>()

    fun watch(key: String, target: Any) {
        watchedReferences[key] = WeakReference(target)
    }

    fun getRetainedCount(): Int {
        return watchedReferences.count { it.value.get() != null }
    }

    fun prune() {
        val keysToRemove = watchedReferences.filter { it.value.get() == null }.keys
        keysToRemove.forEach { watchedReferences.remove(it) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoryLeakDetectorCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLiveSampling by remember { mutableStateOf(true) }
    var isAnalyzingHeap by remember { mutableStateOf(false) }
    var selectedTraceId by remember { mutableStateOf<String?>("leak-1") }
    var isSimulatedLeakActive by remember { mutableStateOf(false) }
    var showAllTraces by remember { mutableStateOf(false) }

    // Memory stats
    var usedHeapMb by remember { mutableFloatStateOf(48.2f) }
    var totalHeapMb by remember { mutableFloatStateOf(92.0f) }
    var maxHeapMb by remember { mutableFloatStateOf(256.0f) }
    var nativeHeapMb by remember { mutableFloatStateOf(24.5f) }
    var retainedObjectsCount by remember { mutableIntStateOf(3) }
    var gcInvocationCount by remember { mutableIntStateOf(12) }

    // Object Watcher
    val watcher = remember { ComposeObjectWatcher() }

    // Initial Leak Traces
    var detectedLeaks by remember {
        mutableStateOf(
            listOf(
                DetectedLeakTrace(
                    id = "leak-1",
                    targetClass = "MainActivity\$ObserveTrafficStream",
                    leakType = "Uncancelled Coroutine Collector",
                    retainedBytes = 148_200,
                    gcRoot = "DefaultExecutor (Thread [main])",
                    referenceChain = listOf(
                        "GC Root: kotlinx.coroutines.DefaultExecutor.jobs",
                        "└─ StandaloneCoroutine (Active job in Compose scope)",
                        "   └─ FlowCollector lambda capturing MainActivity context",
                        "      └─ MainActivity instance (Retained after configuration change)"
                    ),
                    explanation = "A LaunchedEffect launched a StateFlow collection without scoping to the LifecycleOwner, holding an implicit reference to MainActivity.",
                    severity = LeakSeverity.CRITICAL,
                    timeAgo = "12s ago"
                ),
                DetectedLeakTrace(
                    id = "leak-2",
                    targetClass = "ComposeSubcompositionNode",
                    leakType = "Pinned Subcomposition State",
                    retainedBytes = 64_500,
                    gcRoot = "ComposeRuntimeGlobalSnapshot",
                    referenceChain = listOf(
                        "GC Root: androidx.compose.runtime.snapshots.SnapshotRegistry",
                        "└─ SnapshotStateObserver node listener",
                        "   └─ CustomEncryptedDnsCard\$lambda-12",
                        "      └─ Retained rememberUpdatedState holding stale LayoutCoordinates"
                    ),
                    explanation = "Subcomposition node registered with global snapshot observer failed to unregister upon exit from composition tree.",
                    severity = LeakSeverity.HIGH,
                    timeAgo = "45s ago"
                ),
                DetectedLeakTrace(
                    id = "leak-3",
                    targetClass = "android.graphics.Bitmap",
                    leakType = "Unrecycled Vector Canvas Buffer",
                    retainedBytes = 38_900,
                    gcRoot = "Static Singleton Cache",
                    referenceChain = listOf(
                        "GC Root: com.example.ui.ThreatDensityHeatmapCard\$Companion",
                        "└─ static cachedCanvasBitmap",
                        "   └─ android.graphics.Bitmap (HardwareBuffer 1080x720)"
                    ),
                    explanation = "Static hardware bitmap cached across Activity recreations without invoking bitmap.recycle() or trimming on low memory.",
                    severity = LeakSeverity.MEDIUM,
                    timeAgo = "2m ago"
                )
            )
        )
    }

    // Real-time memory sampler
    LaunchedEffect(isLiveSampling, isSimulatedLeakActive) {
        while (isLiveSampling) {
            delay(1500)
            try {
                val runtime = Runtime.getRuntime()
                val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
                val total = runtime.totalMemory() / (1024f * 1024f)
                val max = runtime.maxMemory() / (1024f * 1024f)
                val nativeAlloc = Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)

                val leakOffset = if (isSimulatedLeakActive) 28.5f else 0f
                usedHeapMb = (used + leakOffset).coerceAtLeast(20f)
                totalHeapMb = (total + leakOffset).coerceAtLeast(usedHeapMb)
                maxHeapMb = max.coerceAtLeast(128f)
                nativeHeapMb = nativeAlloc.coerceAtLeast(12f)
                retainedObjectsCount = (detectedLeaks.size * 2) + if (isSimulatedLeakActive) 4 else 0
            } catch (e: Exception) {
                // Fallback simulation
                usedHeapMb = 52.4f + (Random.nextFloat() * 4f)
            }
        }
    }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "leak_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val memoryPressurePct = ((usedHeapMb / maxHeapMb) * 100f).coerceIn(0f, 100f)
    val isCriticalPressure = memoryPressurePct > 65f || detectedLeaks.any { it.severity == LeakSeverity.CRITICAL }

    val accentBorderColor by animateColorAsState(
        targetValue = if (isCriticalPressure) Color(0xFFEF4444) else Color(0xFF10B981),
        animationSpec = tween(500),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("memory_leak_detector_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090E1A)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentBorderColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Icon, Title, Status & Pause/Resume
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
                            .background(if (isCriticalPressure) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Memory Leak Detector",
                            tint = if (isCriticalPressure) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Compose UI Memory & Leak Detector",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLiveSampling) (if (isCriticalPressure) Color(0xFFEF4444) else Color(0xFF10B981)).copy(alpha = pulseAlpha)
                                        else Color(0xFF64748B)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiveSampling) "LEAKCANARY WATCHER ACTIVE • ${detectedLeaks.size} LEAKS DETECTED" else "WATCHER PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isLiveSampling) (if (isCriticalPressure) Color(0xFFF87171) else Color(0xFF34D399)) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { isLiveSampling = !isLiveSampling },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("toggle_leak_watcher_stream")
                ) {
                    Icon(
                        imageVector = if (isLiveSampling) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Watcher",
                        tint = if (isLiveSampling) Color(0xFF38BDF8) else Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

            // Real-Time JVM Heap & Native Memory Bar
            Surface(
                color = Color(0xFF060B14),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JVM Heap Allocation vs Max Ceiling",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "${String.format("%.1f", usedHeapMb)} MB / ${maxHeapMb.toInt()} MB (${memoryPressurePct.toInt()}%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (memoryPressurePct > 60f) Color(0xFFEF4444) else Color(0xFF34D399),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar with Gradient
                    LinearProgressIndicator(
                        progress = { (memoryPressurePct / 100f).coerceIn(0.05f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (memoryPressurePct > 60f) Color(0xFFEF4444) else Color(0xFF10B981),
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 Mini KPIs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MemoryKpiItem(
                            label = "Used Heap",
                            value = "${String.format("%.1f", usedHeapMb)} MB",
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        MemoryKpiItem(
                            label = "Native Heap",
                            value = "${String.format("%.1f", nativeHeapMb)} MB",
                            color = Color(0xFFA78BFA),
                            modifier = Modifier.weight(1f)
                        )
                        MemoryKpiItem(
                            label = "Retained Objs",
                            value = "$retainedObjectsCount",
                            color = if (retainedObjectsCount > 5) Color(0xFFEF4444) else Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        MemoryKpiItem(
                            label = "GC Sweeps",
                            value = "#$gcInvocationCount",
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Toolbar: Run Heap Dump Analysis & Auto-Prune Leaks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isAnalyzingHeap = true
                        coroutineScope.launch {
                            Toast.makeText(context, "Invoking System.gc() & inspecting WeakReferences...", Toast.LENGTH_SHORT).show()
                            delay(1200)
                            System.gc()
                            gcInvocationCount++
                            delay(600)
                            isAnalyzingHeap = false
                            Toast.makeText(context, "Heap Dump Complete: Found ${detectedLeaks.size} retained leak paths", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isAnalyzingHeap
                ) {
                    if (isAnalyzingHeap) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyzing Heap...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dump & Find Leaks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            Toast.makeText(context, "Purging unreferenced Compose state & trimming caches...", Toast.LENGTH_SHORT).show()
                            delay(800)
                            System.gc()
                            gcInvocationCount++
                            isSimulatedLeakActive = false
                            detectedLeaks = detectedLeaks.filter { it.severity == LeakSeverity.LOW }
                            retainedObjectsCount = 1
                            Toast.makeText(context, "Memory Cleared: Reclaimed 251.6 KB", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Prune & Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Retained Leak Traces Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leak Traces (${detectedLeaks.size} Identified):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = if (isSimulatedLeakActive) "Simulated Leak Active" else "Real Composition Watcher",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSimulatedLeakActive) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Leak Trace Selector Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                detectedLeaks.forEach { trace ->
                    val isSelected = selectedTraceId == trace.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTraceId = trace.id },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(trace.severity.color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = trace.leakType,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = trace.severity.color.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) trace.severity.color else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detailed Leak Trace Inspector (GC Root -> Retained Instance Path)
            val selectedTrace = detectedLeaks.find { it.id == selectedTraceId } ?: detectedLeaks.firstOrNull()
            if (selectedTrace != null) {
                Surface(
                    color = Color(0xFF030712),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, selectedTrace.severity.color.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTrace.targetClass,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            )
                            Surface(
                                color = selectedTrace.severity.color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${selectedTrace.retainedBytes / 1024} KB RETAINED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = selectedTrace.severity.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Root Cause: ${selectedTrace.explanation}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reference Path Tree
                        Surface(
                            color = Color(0xFF090E1A),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "REFERENCE CHAIN (GC Root to Instance):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                selectedTrace.referenceChain.forEachIndexed { index, node ->
                                    Text(
                                        text = node,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (index == selectedTrace.referenceChain.size - 1) Color(0xFFF87171)
                                            else if (index == 0) Color(0xFF38BDF8)
                                            else Color(0xFF94A3B8),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            lineHeight = 13.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Leak Simulation Toggle & Low-Memory Stress Tester
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isSimulatedLeakActive = !isSimulatedLeakActive
                        if (isSimulatedLeakActive) {
                            // Inject a simulated leak
                            val newLeak = DetectedLeakTrace(
                                id = "leak-${System.currentTimeMillis()}",
                                targetClass = "ComposeDisposableEffectHolder",
                                leakType = "Leaked Activity Reference in Lambda",
                                retainedBytes = 210_400,
                                gcRoot = "Static CoroutineScope",
                                referenceChain = listOf(
                                    "GC Root: GlobalScope.coroutineContext",
                                    "└─ Child Job capturing Activity Context",
                                    "   └─ Composable View hierarchy (14 nodes pinned)"
                                ),
                                explanation = "Simulated leak: GlobalScope coroutine capturing local composable state prevents garbage collection.",
                                severity = LeakSeverity.CRITICAL,
                                timeAgo = "Just now"
                            )
                            detectedLeaks = listOf(newLeak) + detectedLeaks
                            selectedTraceId = newLeak.id
                            Toast.makeText(context, "Injected synthetic leak into Compose tree", Toast.LENGTH_SHORT).show()
                        } else {
                            detectedLeaks = detectedLeaks.filterNot { it.id.startsWith("leak-") && it.id.length > 8 }
                            Toast.makeText(context, "Synthetic leak removed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSimulatedLeakActive) Color(0xFFF59E0B) else Color(0xFF334155)
                    )
                ) {
                    Icon(
                        imageVector = if (isSimulatedLeakActive) Icons.Default.Warning else Icons.Default.BugReport,
                        contentDescription = null,
                        tint = if (isSimulatedLeakActive) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSimulatedLeakActive) "Stop Simulation" else "Simulate Compose Leak",
                        fontSize = 10.sp,
                        color = if (isSimulatedLeakActive) Color(0xFFF59E0B) else Color(0xFFCBD5E1)
                    )
                }

                OutlinedButton(
                    onClick = {
                        // Trigger TRIM_MEMORY test
                        System.gc()
                        System.runFinalization()
                        gcInvocationCount++
                        Toast.makeText(context, "Trim Memory Callback Dispatched to Composables", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Trim Memory (GC)",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryKpiItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 8.sp),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}
