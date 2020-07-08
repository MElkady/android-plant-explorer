package com.mak.plant_explorer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_openVideoActivity.setOnClickListener {
            startActivity(Intent(this, VideoActivity::class.java))
        }

        btn_openGalleryActivity.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PICK_IMAGE) {
            if(resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                val intent = Intent(this, GalleryActivity::class.java)
                intent.putExtra(GalleryActivity.EXTRA_IMAGE_URI, data.data!!)
                startActivity(intent)
            } else {
                Snackbar.make(root_layout, "Can't use the image", Snackbar.LENGTH_LONG).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val PICK_IMAGE = 10;
    }
}