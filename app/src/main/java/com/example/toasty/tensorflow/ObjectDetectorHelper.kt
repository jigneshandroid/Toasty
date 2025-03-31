package com.example.toasty.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.vision.detector.Detection

data class DetectionResult(val label: String, val confidence: Float, val boundingBox: RectF)

class ObjectDetectorHelper(private val context: Context) {

    private lateinit var objectDetector: ObjectDetector

    init {
        setupObjectDetector()
    }

    private fun setupObjectDetector() {
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5) // Limit results
            .setScoreThreshold(0.5f) // Minimum confidence threshold
            .build()

        objectDetector = ObjectDetector.createFromFileAndOptions(
            context,
            "food.tflite", // Change this to your model filename
            options
        )
    }

    fun detectObjects(bitmap: Bitmap): List<Detection> {
        val image = TensorImage.fromBitmap(bitmap)
        return objectDetector.detect(image)
    }

    fun detectObjectsResult(bitmap: Bitmap): List<DetectionResult> {
        val image = TensorImage.fromBitmap(bitmap)
        val tensorImage = TensorImage.createFrom(image, DataType.FLOAT32)
        // Normalize image to range [0, 1]
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)) // Resize to model's expected input size
            .add(NormalizeOp(0f, 255f)) // Normalize pixels: (pixel - mean) / stddev
            .build()

        val processedImage = imageProcessor.process(tensorImage)

        // Run detection
        val results = objectDetector.detect(processedImage)

        val detections = mutableListOf<DetectionResult>()
        for (result in results) {
            val category = result.categories.firstOrNull()
            val label = category?.label ?: "Unknown"
            val confidence = category?.score ?: 0.0f
            val boundingBox = result.boundingBox

            detections.add(DetectionResult(label, confidence, boundingBox))
        }
        return detections
    }
}
