package gdsc.allways.allears.presentation.decibel

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import gdsc.allways.allears.R
import gdsc.allways.allears.databinding.ActivityDecibelBinding
import kotlinx.coroutines.*


class DecibelActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
        private const val NUM_IMAGES = 10
    }

    private lateinit var binding: ActivityDecibelBinding
    private lateinit var imageViews: List<ImageView>
    private val decibelValues = listOf(10, 55, 30) // 임의의 데시벨 값
    private var currentIndex = 0
    private var job: Job? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // TODO: 녹음 실행
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO
                )) {
                showRequestPermissionRationale()
            } else {
                // 설정 창에서 사용자가 직접 권한 변경하도록 함
                showPermissionSettingDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecibelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageViews = listOf(
            binding.image1,
            binding.image2,
            binding.image3,
            binding.image4,
            binding.image5,
            binding.image6,
            binding.image7,
            binding.image8,
            binding.image9,
            binding.image10
        )

        binding.recordImageButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // 코루틴 시작
        startDecibelProcessing()
    }

    private fun startDecibelProcessing() {
        job = CoroutineScope(Dispatchers.Main).launch {
            // 데시벨 처리가 시작되기 전에 팝업을 띄우는 로직 추가
            showStartProcessingPopup()

            for (decibel in decibelValues) {
                updateImagesWithDecibel(decibel)
                delay(1000) // 1초 대기
            }
            // 모든 데시벨 값 처리 후 코루틴을 중지하고 이미지 초기화
            resetAllImages()
        }
    }

    private fun showStartProcessingPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_layout, null)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

        val yesButton = dialogView.findViewById<Button>(R.id.yes_button)
        val noButton = dialogView.findViewById<Button>(R.id.no_button)

        yesButton.setOnClickListener {
            // 사용자가 Yes를 선택한 경우 VoiceRecognitionActivity로 이동
            val intent = Intent(this@DecibelActivity, VoiceRecognitionActivity::class.java)
            startActivity(intent)
            alertDialog.dismiss()
        }

        noButton.setOnClickListener {
            // 사용자가 No를 선택한 경우의 처리 로직을 여기에 추가
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun resetAllImages() {
        imageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable.decibel_unit)
        }
    }

    private fun updateImagesWithDecibel(decibel: Int) {
        imageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable.decibel_unit)
        }

        val indexToUpdate = decibel / 10 - 1
        if (indexToUpdate in imageViews.indices) {
            imageViews[indexToUpdate].setImageResource(R.drawable.current_decibel)
        }
    }

    private fun showRequestPermissionRationale() {
        AlertDialog.Builder(this)
            .setMessage("Allow All Ears to record audio?")
            .setPositiveButton("Allow") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage("You're unable to use this feature without the required permissions. Tap the Settings button to allow All Ears to access the Audio Record.")
            .setPositiveButton("Settings") { _, _ ->
                //navigateToAppSetting()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

//    private fun navigateToAppSetting() {
//        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//            data = Uri.fromParts("package", packageName, null)
//        }
//        startActivity(intent)
//    }
}
