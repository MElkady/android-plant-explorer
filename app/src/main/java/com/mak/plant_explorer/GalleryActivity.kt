package com.mak.plant_explorer

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.coroutines.*

class GalleryActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)?.let { imageUri ->
            image_preview.setImageURI(imageUri)
            launch {
                val detector = PlantDetector(mode = Mode.SingleImage, detectMultipleObjects = true, maxLabels = 5)
                val inputImage = withContext(Dispatchers.IO) {
                    InputImage.fromFilePath(applicationContext, imageUri)
                }
                detector.processImage(inputImage, { labels ->
                    if (labels.isNotEmpty()) {
                        val sortedLabels = labels.toMutableList();
                        sortedLabels.sortByDescending { it.confidencePercent }
                        val detection = sortedLabels.first()
                        txt_labels.text =
                            if (detection == PlantDetector.ERROR_LABEL) getString(
                                R.string.detection_error
                            ) else "${detection.text} (${detection.confidencePercent}%)"
                        txt_labels.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("https://www.google.com/search?q=${detection.text}")
                            startActivity(intent)
                        }
                    } else {
                        txt_labels.text = getString(R.string.no_results)
                    }
                }, {

                })
            }
        } ?: run {
            Toast.makeText(applicationContext, "Missing activity parameters", Toast.LENGTH_LONG).show()
            Log.e(TAG, "image URI is not provided")
            finish()
        }
    }

    companion object {
        private const val TAG = "plant_explorer.gallery"
        const val EXTRA_IMAGE_URI = "GalleryActivity.imageUri"
    }
}