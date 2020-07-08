package com.mak.plant_explorer

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)?.let {
            image_preview.setImageURI(it)
            val detector = PlantDetector()
            detector.processImage(InputImage.fromFilePath(applicationContext, it), { labels ->
                if (labels.isNotEmpty()) {
                    val detection = labels.first()
                    txt_labels.text =
                        if (detection == PlantDetector.ERROR_LABEL) getString(
                            R.string.detection_error
                        ) else "${detection.text} (${detection.confidencePercent}%)"
                } else {
                    txt_labels.text = getString(R.string.no_results)
                }
            }, {

            })
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