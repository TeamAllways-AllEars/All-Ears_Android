package gdsc.allways.allears.presentation.setting

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.R
import gdsc.allways.allears.databinding.ActivitySettingBinding
import gdsc.allways.allears.presentation.decibel.VoiceRecognitionActivity

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBar 숨기기
        supportActionBar?.hide()

        // "SettingActivity" 텍스트 설정
        //binding.textView.text = "SettingActivity"

        showStartProcessingPopup()
    }

    private fun showStartProcessingPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_layout, null)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()

        // 배경을 검은색으로 설정
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        // 여백을 없애기 위해 다이얼로그 창 설정
        alertDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        val yesButton = dialogView.findViewById<Button>(R.id.yes_button)
        val noButton = dialogView.findViewById<Button>(R.id.no_button)

        yesButton.setOnClickListener {
            // 사용자가 Yes를 선택한 경우 VoiceRecognitionActivity로 이동
            val intent = Intent(this@SettingActivity, VoiceRecognitionActivity::class.java)
            startActivity(intent)
            finish()
            alertDialog.dismiss()
        }

        noButton.setOnClickListener {
            // 사용자가 No를 선택한 경우의 처리 로직을 여기에 추가
            finish()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

}
