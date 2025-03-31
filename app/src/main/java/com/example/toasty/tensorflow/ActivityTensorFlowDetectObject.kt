package com.example.toasty.tensorflow

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.toasty.R

class ActivityTensorFlowDetectObject : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var textureView: PreviewView
    private lateinit var overlayView: SurfaceView
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tensorflow_detect_object)

        textureView = findViewById(R.id.view_finder)
        overlayView = findViewById(R.id.overlay_view)
        resultTextView = findViewById(R.id.result_text)

        objectDetectorHelper = ObjectDetectorHelper(this)
        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(textureView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val bitmap = imageProxyToBitmap(imageProxy)
        val detections = objectDetectorHelper.detectObjectsResult(bitmap)
        runOnUiThread {
            drawBoundingBoxes(detections)
            updateResultsText(detections)
        }

        imageProxy.close()
    }

    private fun drawBoundingBoxes(detections: List<DetectionResult>) {
        val canvas = overlayView.holder.lockCanvas()
        canvas?.let {
            it.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR) // Clear previous frame
            val paint = Paint().apply {
                color = android.graphics.Color.RED
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }

            for (detection in detections) {
                it.drawRect(detection.boundingBox, paint)
            }

            overlayView.holder.unlockCanvasAndPost(it)
        }
    }

    private fun updateResultsText(detections: List<DetectionResult>) {
        val resultsText = detections.joinToString("\n") {
            "${it.label}: ${String.format("%.2f", it.confidence * 100)}%"
        }
        resultTextView.text = resultsText
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}