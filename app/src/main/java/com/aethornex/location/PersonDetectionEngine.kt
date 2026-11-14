package com.aethornex.location

import android.content.Context
import android.graphics.Bitmap

import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector

class PersonDetectionEngine(context: Context) {

    private val detector: ObjectDetector

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("efficientdet_lite0.tflite")
            .build()

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setScoreThreshold(0.35f)
            .setMaxResults(20)
            .build()

        detector = ObjectDetector.createFromOptions(
            context.applicationContext,
            options
        )
    }

    fun detect(bitmap: Bitmap): List<PersonBox> {
        val image = BitmapImageBuilder(bitmap).build()
        val result = detector.detect(image)

        return result.detections().mapNotNull { detection ->
            val person = detection.categories().firstOrNull {
                it.categoryName().equals("person", ignoreCase = true)
            }

            if (person == null) {
                null
            } else {
                val rect = detection.boundingBox()

                PersonBox(
                    rect.left,
                    rect.top,
                    rect.right,
                    rect.bottom,
                    person.score()
                )
            }
        }
    }

    fun close() {
        detector.close()
    }
}
