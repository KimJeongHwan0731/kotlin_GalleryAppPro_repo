package com.example.galleryapppro

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.galleryapppro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 인텐트를 통해 갤러리 앱에서 클릭을 하면 클릭된 이미지 Uri를 가져와서, 컨텐트리졸버를 이용해서 inputStream, BitMapFactory를 통해서
        // 이미지 뷰 가져온다.
        val requestLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val uri: Uri = it.data!!.data!!

                // 1.1 이미지를 가져오면 OOM이 발생 할 수 있으므로, 화면에 출력될 원하는 사이즈의 비율을 설정해야된다.
                val inSampleSize = calculateInSampleSize(
                    uri,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize)
                )
                // 1.2 비트맵 옵션설정 비율설정
                val option = BitmapFactory.Options()
                option.inSampleSize = inSampleSize

                try {
                    // 1.3 컨텐트리졸버 사용해서 uri를 통해서 내가 원하는 정보를 가져온다. (uri -> inputStream)
                    var inputStream = contentResolver.openInputStream(uri)
                    // 1.4 inputStream으로 비트맵팩토리를 이용해서 이미지를 가져온다. (OOM 방지하기 위해서, 옵션에 사이즈 비율을 저장)
                    val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                    // 1.5 이미지뷰에 비트맵을 저장시키면 된다.
                    bitmap?.let {
                        binding.ivPicture.setImageBitmap(bitmap)
                    } ?: let {
                        Log.e("MainActivity", "bitmapFactory를 통해서 가져온 비트맵 null 발생")
                    }
                    inputStream?.close()
                } catch (e: java.lang.Exception) {
                    Log.e("MainActivity", "${e.printStackTrace()}")
                }
            }

        // 2. 갤러리앱에 암시적 인텐트 방법으로 요청
        binding.btnCallImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestLauncher.launch(intent)
        }
    }

    // 이미지 비율을 계산하는 함수
    fun calculateInSampleSize(uri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        // 이미지의 정보만 가져옴
        options.inJustDecodeBounds = true

        try {
            // 컨텐트리졸버를 이용해 이미지 정보를 다시 가져온다.
            var inputStream = contentResolver.openInputStream(uri)
            // 진짜 inputStream 통해서 비트맵을 가져오는것이 아니라, 비트맵 정보만 options 저장해서 가져온다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: java.lang.Exception) {
            Log.e("MainActivity", "calculateInSampleSize inputStream ${e.printStackTrace()}")
        }

        // 갤러리앱에서 가져올 실제 이미지 사이즈
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}