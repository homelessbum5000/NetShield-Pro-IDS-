package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.ImageFormat
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import java.util.EnumMap
import java.util.concurrent.Executors

data class GatewayConfigProfile(
    val serverName: String,
    val gatewayUrl: String,
    val onionEndpoint: String,
    val authToken: String,
    val kyberKeyId: String,
    val port: Int = 443
)

/**
 * ZXing Image Analyzer for CameraX frames
 */
class ZXingQrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        setHints(hints)
    }

    private var isScanningEnabled = true

    fun resetScanning() {
        isScanningEnabled = true
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanningEnabled) {
            imageProxy.close()
            return
        }

        if (imageProxy.format == ImageFormat.YUV_420_888 || imageProxy.format == ImageFormat.YUV_422_888 || imageProxy.format == ImageFormat.YUV_444_888) {
            val buffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = imageProxy.width
            val height = imageProxy.height

            val source = PlanarYUVLuminanceSource(
                data, width, height, 0, 0, width, height, false
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decodeWithState(bitmap)
                result?.text?.let { rawQrText ->
                    isScanningEnabled = false
                    onQrCodeScanned(rawQrText)
                }
            } catch (e: Exception) {
                // QR code not detected in frame
            } finally {
                reader.reset()
            }
        }
        imageProxy.close()
    }
}

/**
 * Utility to generate QR Code Bitmap using ZXing Writer
 */
fun generateQrCodeBitmap(content: String, width: Int = 512, height: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        Log.e("ZXing", "Failed to generate QR code bitmap: ${e.message}")
        null
    }
}

@Composable
fun QrGatewayScannerCard(
    onProfileApplied: (GatewayConfigProfile) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var activeTab by remember { mutableStateOf(0) } // 0 = Scanner, 1 = Generator / Presets
    var activeProfile by remember { mutableStateOf<GatewayConfigProfile?>(null) }
    var rawScannedData by remember { mutableStateOf<String?>(null) }
    var scanStatusMessage by remember { mutableStateOf("Position Gateway QR code within frame") }

    // Sample Gateway Profiles for direct simulation
    val sampleProfiles = remember {
        listOf(
            GatewayConfigProfile(
                serverName = "Quantum-Gateway-Alpha",
                gatewayUrl = "https://gateway-alpha.netshield.io/",
                onionEndpoint = "http://qgatealpha77263.onion/",
                authToken = "jwt_bearer_kyber_alpha_9981",
                kyberKeyId = "KYBER1024_PUB_KEY_0x39F1"
            ),
            GatewayConfigProfile(
                serverName = "Secure-Node-Morpheus",
                gatewayUrl = "https://morpheus-node.netshield.io/",
                onionEndpoint = "http://morpheus28991.onion/",
                authToken = "jwt_bearer_morpheus_7712",
                kyberKeyId = "KYBER1024_PUB_KEY_0x88A2"
            )
        )
    }

    fun parseQrToProfile(text: String): GatewayConfigProfile {
        return try {
            val json = JSONObject(text)
            GatewayConfigProfile(
                serverName = json.optString("server", "Configured-Gateway"),
                gatewayUrl = json.optString("url", "https://gateway.netshield.io/"),
                onionEndpoint = json.optString("onion", "http://gateway.onion/"),
                authToken = json.optString("token", "jwt_token_sample"),
                kyberKeyId = json.optString("key_id", "KYBER_1024_DEFAULT")
            )
        } catch (e: Exception) {
            GatewayConfigProfile(
                serverName = "Scanned Profile",
                gatewayUrl = if (text.startsWith("http")) text else "https://gateway.netshield.io/",
                onionEndpoint = "http://raw-scanned.onion/",
                authToken = "scanned_raw_token",
                kyberKeyId = "KYBER_RAW_" + text.hashCode()
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("qr_gateway_scanner_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Scanner",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Gateway QR Configuration",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Scan ZXing QR Code to provision Gateway endpoint",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF38BDF8),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF38BDF8)
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera Scanner")
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QR Generator / Presets")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                // Camera Scanner View
                if (!hasCameraPermission) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "NetShield Pro requires camera access to scan ZXing encrypted gateway QR codes.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { launcher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Grant Camera Access")
                        }
                    }
                } else {
                    // Camera Preview with ZXing Analyzer
                    val analyzer = remember {
                        ZXingQrCodeAnalyzer { rawText ->
                            rawScannedData = rawText
                            val profile = parseQrToProfile(rawText)
                            activeProfile = profile
                            scanStatusMessage = "Successfully scanned: ${profile.serverName}"
                            onProfileApplied(profile)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = surfaceProvider
                                        }

                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setTargetResolution(Size(1280, 720))
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()

                                        imageAnalysis.setAnalyzer(
                                            Executors.newSingleThreadExecutor(),
                                            analyzer
                                        )

                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                        try {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageAnalysis
                                            )
                                        } catch (exc: Exception) {
                                            Log.e("CameraX", "Use case binding failed", exc)
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Target Overlay Box
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scanStatusMessage,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (activeProfile != null) Color(0xFF10B981) else Color(0xFF94A3B8)
                            )
                        )
                        if (activeProfile != null) {
                            IconButton(onClick = {
                                activeProfile = null
                                rawScannedData = null
                                scanStatusMessage = "Position Gateway QR code within frame"
                                analyzer.resetScanning()
                            }) {
                                Text("Reset", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8)))
                            }
                        }
                    }
                }
            } else {
                // QR Generator & Preset Profiles Simulator
                Column {
                    Text(
                        text = "Generate ZXing QR Code or Quick Load Sample Profile:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    sampleProfiles.forEach { profile ->
                        val profileJson = JSONObject().apply {
                            put("server", profile.serverName)
                            put("url", profile.gatewayUrl)
                            put("onion", profile.onionEndpoint)
                            put("token", profile.authToken)
                            put("key_id", profile.kyberKeyId)
                        }.toString()

                        val qrBitmap = remember(profileJson) {
                            generateQrCodeBitmap(profileJson, 250, 250)
                        }

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                qrBitmap?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "QR Code for ${profile.serverName}",
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.serverName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = profile.gatewayUrl,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF38BDF8),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Kyber Key: ${profile.kyberKeyId}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                                    )
                                }

                                Button(
                                    onClick = {
                                        activeProfile = profile
                                        onProfileApplied(profile)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Apply", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Display Active Profile Details
            if (activeProfile != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46).copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE GATEWAY PROFILE APPLIED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileDetailRow("Server Name", activeProfile!!.serverName)
                        ProfileDetailRow("Gateway URL", activeProfile!!.gatewayUrl)
                        ProfileDetailRow("Onion Endpoint", activeProfile!!.onionEndpoint)
                        ProfileDetailRow("Kyber Key ID", activeProfile!!.kyberKeyId)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}
