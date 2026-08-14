package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class InfrastructureEnvironment(val title: String, val badge: String, val desc: String) {
    AI_ML_CLUSTER(
        title = "AI / ML GPU Training & Inference",
        badge = "H100 / Blackwell Cluster",
        desc = "High-throughput tensor pipelines, preventing model weights theft & training data poisoning."
    ),
    ENTERPRISE_DATACENTER(
        title = "Standard Enterprise Data Center",
        badge = "Bare-Metal / Hybrid Cloud",
        desc = "Micro-segmented multi-tenant workloads, zero-trust perimeter & regulatory compliance."
    )
}

enum class NvidiaDpuSolution(
    val vendorName: String,
    val solutionTitle: String,
    val sourceCitation: String,
    val keyCapability: String,
    val architectureRole: String,
    val throughputRating: String,
    val tagColor: Color
) {
    CHECK_POINT(
        vendorName = "Check Point",
        solutionTitle = "Check Point AI Factory / AI Cloud Protect",
        sourceCitation = "Check Point Quantum / NVIDIA Partner",
        keyCapability = "Offloads Deep Packet Inspection (DPI) & zero-day threat prevention directly onto BlueField DPUs, securing AI pipelines and data centers without stealing host GPU or CPU cycles.",
        architectureRole = "Zero-Day Threat Defense & AI Pipeline Shield",
        throughputRating = "400 Gbps Line-Rate",
        tagColor = Color(0xFFEC4899)
    ),
    CISCO(
        vendorName = "Cisco",
        solutionTitle = "Cisco Hybrid Mesh Firewall with BlueField",
        sourceCitation = "Cisco Security & NVIDIA BlueField",
        keyCapability = "Extends stateful workload segmentation and micro-segmentation right inside servers via NVIDIA BlueField DPUs to stop lateral threats early.",
        architectureRole = "Intra-Server Microsegmentation & Lateral Movement Blocker",
        throughputRating = "320 Gbps Sub-microsecond",
        tagColor = Color(0xFF06B6D4)
    ),
    PALO_ALTO(
        vendorName = "Palo Alto Networks",
        solutionTitle = "Palo Alto Networks VM-Series NGFW",
        sourceCitation = "NVIDIA Developer & PAN-OS DPU Offload",
        keyCapability = "Integrates with BlueField DPUs to intelligently optimize traffic inspection, accelerating threat detection while scaling high-throughput enterprise data.",
        architectureRole = "Intelligent Intelligent Hardware Offload & App-ID Acceleration",
        throughputRating = "400 Gbps Hardware-Accelerated",
        tagColor = Color(0xFFF97316)
    )
}

data class DpuOffloadBenefit(
    val title: String,
    val subtitle: String,
    val highlight: String,
    val iconColor: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NvidiaDpuAcceleratedSecurityCard(
    modifier: Modifier = Modifier
) {
    var isDpuOffloadActive by remember { mutableStateOf(true) }
    var selectedEnv by remember { mutableStateOf(InfrastructureEnvironment.AI_ML_CLUSTER) }
    var selectedSolution by remember { mutableStateOf(NvidiaDpuSolution.CHECK_POINT) }
    var isRunningDpuInspection by remember { mutableStateOf(false) }
    var inspectionProgress by remember { mutableFloatStateOf(0f) }
    var inspectionResultLogs by remember { mutableStateOf<List<String>?>(null) }
    var showArchitectureBreakdown by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Infinite breathing glow for active DPU hardware offload
    val infiniteTransition = rememberInfiniteTransition(label = "dpu_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val dpuBenefits = listOf(
        DpuOffloadBenefit(
            title = "Zero Performance Loss",
            subtitle = "Security processing happens on the DPU instead of the host CPU/GPU, keeping AI training and high-frequency workloads at maximum speed.",
            highlight = "100% Host GPU Cycles Preserved",
            iconColor = Color(0xFF10B981)
        ),
        DpuOffloadBenefit(
            title = "Isolates Workloads at Hardware Level",
            subtitle = "Hardware-level physical isolation prevents model theft, training data poisoning, and prompt injection attacks at the infrastructure layer.",
            highlight = "Hardware Ring-0 Protection",
            iconColor = Color(0xFF38BDF8)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nvidia_dpu_accelerated_security_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF081220)),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF76B900).copy(alpha = 0.7f)) // NVIDIA Green accent
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with NVIDIA BlueField DPU Brand
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
                            .background(Color(0xFF76B900).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = "NVIDIA DPU Security",
                            tint = Color(0xFF76B900),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NVIDIA BlueField DPU Security",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Text(
                            text = "Hardware-accelerated zero-trust & AI pipeline threat offloading",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }
                }

                // DPU Offload Engine Master Switch
                Switch(
                    checked = isDpuOffloadActive,
                    onCheckedChange = { newVal ->
                        isDpuOffloadActive = newVal
                        Toast.makeText(
                            context,
                            if (newVal) "NVIDIA BlueField DPU Hardware Offload: ACTIVE" else "DPU Offload Disabled: Security running on host CPU/GPU",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF76B900),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("dpu_offload_switch")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

            // Infrastructure Target Selector (AI/ML vs Enterprise)
            Text(
                text = "Target Infrastructure Environment:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfrastructureEnvironment.values().forEach { env ->
                    val isSelected = selectedEnv == env
                    Surface(
                        color = if (isSelected) Color(0xFF0F2B1D) else Color(0xFF131C31),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF76B900) else Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedEnv = env }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = env.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color(0xFF86EFAC) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF76B900), modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = env.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Top NVIDIA-Accelerated Security Solution Selection
            Text(
                text = "Integrated Enterprise Security Solution:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NvidiaDpuSolution.values().forEach { sol ->
                    val isSelected = selectedSolution == sol
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedSolution = sol
                            inspectionResultLogs = null
                        },
                        label = {
                            Text(
                                text = sol.vendorName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = sol.tagColor.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) sol.tagColor else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Solution Feature Card
            Surface(
                color = Color(0xFF111C33),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedSolution.tagColor.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedSolution.solutionTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = selectedSolution.tagColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = selectedSolution.tagColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = selectedSolution.throughputRating,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = selectedSolution.tagColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedSolution.keyCapability,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0), fontSize = 11.sp, lineHeight = 16.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Role: ${selectedSolution.architectureRole}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Text(
                            text = "Source: ${selectedSolution.sourceCitation}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Why Hardware Offloading Matters Section
            Surface(
                color = Color(0xFF070F1E),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF76B900), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Why Hardware Offloading Matters",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Surface(
                            color = Color(0xFF166534).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isDpuOffloadActive) "DPU ACTIVE" else "HOST OVERHEAD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isDpuOffloadActive) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    dpuBenefits.forEachIndexed { idx, benefit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(benefit.iconColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = benefit.iconColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = benefit.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = benefit.highlight,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = benefit.iconColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = benefit.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp, lineHeight = 14.sp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Run BlueField DPU Pipeline Benchmark & Deep Architecture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isRunningDpuInspection = true
                        inspectionProgress = 0.15f
                        coroutineScope.launch {
                            delay(200)
                            inspectionProgress = 0.5f
                            delay(250)
                            inspectionProgress = 0.85f
                            delay(200)
                            inspectionProgress = 1.0f
                            isRunningDpuInspection = false
                            inspectionResultLogs = listOf(
                                "✓ BlueField-3 DPU PCIe Gen 5.0 Link: Established (16 lanes @ 32 GT/s)",
                                "✓ Offloaded Engine: ${selectedSolution.solutionTitle}",
                                "✓ Target Workload: ${selectedEnv.title} (${selectedEnv.badge})",
                                "✓ Host CPU Utilization: 0.2% (Offload factor: 99.8% on DPU ARM Cores)",
                                "✓ Host GPU (H100/Blackwell) VRAM Contention: ZERO (Full 80GB VRAM dedicated to LLM)",
                                "✓ Zero-Day AI Pipeline Threat Shield: 0 blocked prompt injection vectors",
                                "✓ Lateral Movement Micro-segmentation: Active across all server PCI boundaries",
                                "✓ Line-Rate Packet Processing: 388.4 Gbps sustained with 0 dropped frames"
                            )
                        }
                    },
                    enabled = !isRunningDpuInspection,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558B2F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isRunningDpuInspection) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Offloading to DPU...", fontSize = 11.sp)
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Benchmark DPU Offload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { showArchitectureBreakdown = !showArchitectureBreakdown },
                    modifier = Modifier.weight(0.7f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = if (showArchitectureBreakdown) "Hide Info" else "Arch Info",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            if (isRunningDpuInspection) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { inspectionProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFF76B900),
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Real-Time Benchmark Execution Log Display
            if (inspectionResultLogs != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF030712),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF76B900).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF76B900), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BlueField DPU Telemetry Logs",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF76B900),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Text(
                                text = "DOCA 2.5 / DPDK Line-Rate",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        inspectionResultLogs?.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (line.startsWith("✓")) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }

            // Expanded Architecture Explanation
            if (showArchitectureBreakdown) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NVIDIA BlueField Data Processing Unit (DPU) Architecture",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• BlueField DPUs offload, accelerate, and isolate data center infrastructure services including networking, storage, cybersecurity, and management.\n" +
                                    "• By running deep packet inspection directly on DPU ARM cores and hardware accelerators (DOCA), host CPUs and GPUs remain 100% focused on compute-intensive AI workloads.\n" +
                                    "• Micro-segmentation on the DPU establishes an isolated trust boundary, ensuring that compromised containers or VMs cannot tamper with underlying server infrastructure.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 9.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
