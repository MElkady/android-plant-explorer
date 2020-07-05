package com.mak.plant_explorer

import android.util.Log
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions


typealias LabelsListener = (labels: List<Label>) -> Unit

data class Label(val text: String, val confidencePercent: Int)

class PlantDetector(maxLabels: Int = 1) {
    private val localModel = LocalModel.Builder().setAssetFilePath("aiy_vision_classifier_plants_V1_2.tflite").build()
    private val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
        .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .setClassificationConfidenceThreshold(0.1f)
        .setMaxPerObjectLabelCount(maxLabels)
        .build()
    private val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

    fun processImage(image: InputImage, listener: LabelsListener, onCompleted: () -> Unit) {
        objectDetector.process(image).addOnSuccessListener {
            listener(if (it.isEmpty()) listOf() else it.flatMap { x -> x.labels.map { i -> Label(i.text, (i.confidence * 100).toInt()) } })
        }.addOnFailureListener {
            Log.d(TAG, "Error while processing labels: ${it.message}")
            listener(listOf(ERROR_LABEL))
        }.addOnCompleteListener {
            onCompleted()
        }
    }

    companion object {
        private const val TAG = "plant_explorer.Detector"
        val ERROR_LABEL = Label("ERROR!", -1)
    }
}