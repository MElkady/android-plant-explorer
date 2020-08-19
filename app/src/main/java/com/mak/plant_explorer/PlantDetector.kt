package com.mak.plant_explorer

import android.util.Log
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions


typealias LabelsListener = (labels: List<Label>) -> Unit

data class Label(val text: String, val confidencePercent: Int)

enum class Mode(val objectDetectorMode: Int) {
    Stream(CustomObjectDetectorOptions.STREAM_MODE), SingleImage(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
}

class PlantDetector(maxLabels: Int = 1, mode: Mode = Mode.SingleImage, detectMultipleObjects: Boolean= false) {
    private val localModel = LocalModel.Builder().setAssetFilePath("aiy_vision_classifier_plants_V1_3.tflite").build()
    private val customObjectDetectorOptions = createDetectorOptions(localModel, maxLabels, mode, detectMultipleObjects)
    private val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

    fun processImage(image: InputImage, listener: LabelsListener, onCompleted: () -> Unit) {
        objectDetector.process(image).addOnSuccessListener {
            val labels = if (it.isEmpty()) listOf() else it.flatMap { x -> x.labels.map { i -> Label(i.text, (i.confidence * 100).toInt()) } }
            printDetections(labels)
            listener(labels)
        }.addOnFailureListener {
            Log.d(TAG, "Error while processing labels: ${it.message}")
            listener(listOf(ERROR_LABEL))
        }.addOnCompleteListener {
            onCompleted()
        }
    }

    private fun printDetections(labels: List<Label>) {
        if(labels.isNotEmpty()) {
            Log.d(TAG, "Detected:")
            labels.forEach { label ->
                Log.d(TAG, "\t${label.text} - ${label.confidencePercent}")
            }
        } else {
            Log.d(TAG, "Detected: NONE!")
        }
    }

    companion object {
        private const val TAG = "plant_explorer.Detector"
        val ERROR_LABEL = Label("ERROR!", -1)

        private fun createDetectorOptions(localModel: LocalModel, maxLabels: Int, mode: Mode, detectMultipleObjects: Boolean): CustomObjectDetectorOptions {
            val builder = CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(mode.objectDetectorMode)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.1f)
                .setMaxPerObjectLabelCount(maxLabels)
            if(detectMultipleObjects) {
                builder.enableMultipleObjects()
            }
            return builder.build()
        }
    }
}