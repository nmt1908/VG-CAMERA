package com.example.vgcamera

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.vgcamera.ui.theme.VGCameraTheme
import com.example.vgcamera.ui.utils.CoordinateMapper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MeshTestActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val detector: FaceMeshDetector by lazy { FaceMeshDetection.getClient() }

    override fun onCreate(savedInstanceState: Bundle?) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContentView(ComposeView(this).apply {
            setContent { VGCameraTheme { MeshTestScreen() } }
        })
    }

    @Composable
    fun MeshTestScreen() {
        var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var meshPoints by remember { mutableStateOf<FloatArray?>(null) }
        var imgSize by remember { mutableStateOf(IntSize(0, 0)) }
        var rotationDegrees by remember { mutableStateOf(0) }
        
        // --- REALTIME DATA ---
        var realtimeMesh by remember { mutableStateOf<FloatArray?>(null) }
        var realtimeImgSize by remember { mutableStateOf(IntSize(0, 0)) }
        var realtimeRot by remember { mutableStateOf(0) }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (capturedBitmap == null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        PreviewView(context).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            startCamera(this) { points, w, h, rot ->
                                realtimeMesh = points
                                realtimeImgSize = IntSize(w, h)
                                realtimeRot = rot
                            }
                        }
                    }
                )
                
                // Overlay Realtime Mesh
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val previewSize = IntSize(size.width.toInt(), size.height.toInt())
                    if (realtimeMesh != null && realtimeImgSize.width > 0) {
                        drawAestheticMesh(realtimeMesh!!, realtimeImgSize, previewSize, realtimeRot, isFront = true)
                    }
                }

                Button(
                    onClick = { 
                        capturePhoto { bitmap, rot -> 
                            capturedBitmap = bitmap
                            rotationDegrees = rot
                            analyzeBitmap(bitmap) { points -> 
                                meshPoints = points
                                imgSize = IntSize(bitmap.width, bitmap.height)
                            }
                        } 
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) { Text("CHỤP (FREEZE) ĐỂ SOI KỸ") }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val previewSize = IntSize(size.width.toInt(), size.height.toInt())
                        if (meshPoints != null) {
                            // Ảnh chụp đã được xoay đứng và mirror sẵn trong toUprightBitmap
                            // Nên ở đây isFront = false vì bitmap đã là kết quả cuối
                            drawAestheticMesh(meshPoints!!, imgSize, previewSize, 0, isFront = false)
                        }
                    }

                    Button(
                        onClick = { capturedBitmap = null; meshPoints = null },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    ) { Text("THỬ LẠI (QUAY CẢNH REALTIME)") }
                }
            }
        }
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAestheticMesh(
        points: FloatArray,
        imageSize: IntSize,
        previewSize: IntSize,
        rotation: Int,
        isFront: Boolean
    ) {
        val cyanColor = Color(0xFF00E5FF)
        val cyanDim = Color(0x6600E5FF)

        val eyeLeft = listOf(33, 7, 163, 144, 145, 153, 154, 155, 133, 173, 157, 158, 159, 160, 161, 246)
        val eyeRight = listOf(362, 382, 381, 380, 374, 373, 390, 249, 263, 466, 388, 387, 386, 385, 384, 398)
        val eyebrowLeft = listOf(70, 63, 105, 66, 107, 55, 65, 52, 53, 46)
        val eyebrowRight = listOf(300, 293, 334, 296, 336, 285, 295, 282, 283, 276)
        
        // Mũi đầy đủ (Sống mũi + Cánh mũi)
        val noseBridge = listOf(168, 6, 197, 195, 5, 4, 1)
        val noseBase = listOf(98, 97, 2, 326, 327)

        // Môi trên (Vòng khép kín)
        val upperLip = listOf(61, 185, 40, 39, 37, 0, 267, 269, 270, 409, 291, 415, 310, 311, 312, 13, 82, 81, 80, 191)
        
        // Môi dưới (Vòng khép kín)
        val lowerLip = listOf(61, 146, 91, 181, 84, 17, 314, 405, 321, 375, 291, 308, 324, 318, 402, 317, 14, 87, 178, 88, 95, 78)
        
        val face = listOf(10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109)

        fun drawGroup(indices: List<Int>, closed: Boolean = true) {
            val path = androidx.compose.ui.graphics.Path()
            var moved = false
            indices.forEach { idx ->
                if (idx * 2 + 1 < points.size) {
                    val p = android.graphics.PointF(points[idx * 2], points[idx * 2 + 1])
                    val mapped = CoordinateMapper.mapPoint(p, imageSize, previewSize, rotation, isFrontCamera = isFront)
                    if (!moved) { path.moveTo(mapped.x, mapped.y); moved = true }
                    else path.lineTo(mapped.x, mapped.y)
                    drawCircle(color = cyanColor, radius = 1.dp.toPx(), center = mapped)
                }
            }
            if (closed) path.close()
            drawPath(path, cyanDim, style = Stroke(width = 0.8.dp.toPx()))
        }

        drawGroup(eyeLeft); drawGroup(eyeRight)
        drawGroup(eyebrowLeft, false); drawGroup(eyebrowRight, false)
        drawGroup(noseBridge, false); drawGroup(noseBase, false)
        drawGroup(upperLip); drawGroup(lowerLip)
        drawGroup(face)
    }

    private fun startCamera(previewView: PreviewView, onMesh: (FloatArray?, Int, Int, Int) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            
            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val rotation = imageProxy.imageInfo.rotationDegrees
                val inputImage = InputImage.fromMediaImage(imageProxy.image!!, rotation)
                detector.process(inputImage)
                    .addOnSuccessListener { meshes ->
                        if (meshes.isNotEmpty()) {
                            val pts = meshes[0].allPoints
                            val res = FloatArray(pts.size * 2)
                            for (i in pts.indices) {
                                res[i * 2] = pts[i].position.x
                                res[i * 2 + 1] = pts[i].position.y
                            }
                            // NẾU XOAY 90/270 -> PHẢI ĐẢO CHIỀU RỘNG/CAO CỦA BUFFER
                            val isRotated = rotation == 90 || rotation == 270
                            val actualW = if (isRotated) imageProxy.height else imageProxy.width
                            val actualH = if (isRotated) imageProxy.width else imageProxy.height
                            onMesh(res, actualW, actualH, rotation)
                        } else {
                            onMesh(null, 0, 0, 0)
                        }
                    }
                    .addOnFailureListener { onMesh(null, 0, 0, 0) }
                    .addOnCompleteListener { imageProxy.close() }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageCapture, analysis)
            } catch (e: Exception) { Log.e("MeshTest", "Binding failed", e) }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto(onSuccess: (Bitmap, Int) -> Unit) {
        imageCapture?.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val rotation = image.imageInfo.rotationDegrees
                val bitmap = image.toUprightBitmap()
                runOnUiThread { onSuccess(bitmap, rotation) }
                image.close()
            }
            override fun onError(e: ImageCaptureException) { Log.e("MeshTest", "Fail", e) }
        })
    }

    private fun analyzeBitmap(bitmap: Bitmap, onResult: (FloatArray?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        detector.process(image)
            .addOnSuccessListener { meshes ->
                if (meshes.isNotEmpty()) {
                    val pts = meshes[0].allPoints
                    val res = FloatArray(pts.size * 2)
                    for (i in pts.indices) {
                        res[i * 2] = pts[i].position.x
                        res[i * 2 + 1] = pts[i].position.y
                    }
                    onResult(res)
                } else onResult(null)
            }
            .addOnFailureListener { onResult(null) }
    }

    private fun androidx.camera.core.ImageProxy.toUprightBitmap(): Bitmap {
        // Wait, why didn't I use the standard ImageProxy.toBitmap()?
        // ImageProxy has planes. I'll use a simpler version.
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val rotation = imageInfo.rotationDegrees
        val matrix = android.graphics.Matrix()
        if (rotation != 0) matrix.postRotate(rotation.toFloat())
        // 2. Lật ngang (Mirroring) để giống soi gương - Cho Camera trước
        // TRỤC ĐỐI XỨNG PHẢI LÀ TÂM ẢNH
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
