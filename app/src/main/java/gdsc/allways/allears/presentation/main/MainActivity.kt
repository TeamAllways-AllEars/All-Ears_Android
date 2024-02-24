package gdsc.allways.allears.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.R
import gdsc.allways.allears.databinding.ActivityMainBinding
import gdsc.allways.allears.presentation.decibelstt.DecibelActivity
import gdsc.allways.allears.presentation.setting.SettingActivity
import gdsc.allways.allears.presentation.subtitles.SubtitlesActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isFirstButtonSelected = false
    private var isSecondButtonSelected = false
    private var isThirdButtonSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBar 숨기기
        supportActionBar?.hide()

        // 버튼 클릭 리스너 설정
        binding.button1.setOnClickListener {
            if (isFirstButtonSelected) {
                navigateToDecibelActivity()
            } else {
                selectButton(1)
                isFirstButtonSelected = true
                isSecondButtonSelected = false
                isThirdButtonSelected = false
            }
        }
        binding.button2.setOnClickListener {
            if (isSecondButtonSelected) {
                navigateToSubtitlesActivity()
            } else {
                selectButton(2)
                isSecondButtonSelected = true
                isFirstButtonSelected = false
                isThirdButtonSelected = false
            }
        }

        binding.button3.setOnClickListener {
            if (isThirdButtonSelected) {
                navigateToSettingActivity()
            } else {
                selectButton(3)
                isThirdButtonSelected = true
                isFirstButtonSelected = false
                isSecondButtonSelected = false
            }
        }

        binding.root.setOnClickListener {
            resetButtonState()
        }
    }

    private fun resetButtonState() {
        isFirstButtonSelected = false
        isSecondButtonSelected = false
        isThirdButtonSelected = false

        binding.button1.setImageResource(R.drawable.decibel)
        binding.button2.setImageResource(R.drawable.subtitles)
        binding.button3.setImageResource(R.drawable.setting)
        binding.decibelText.visibility = View.GONE
        binding.subtitlesText.visibility = View.GONE
        binding.settingText.visibility = View.GONE
        binding.logo.visibility = View.VISIBLE
    }

    // DecibelActivity로 화면 전환
    private fun navigateToDecibelActivity() {
        val intent = Intent(this, DecibelActivity::class.java)
        startActivity(intent)
    }

    // SubtitlesActivity로 화면 전환
    private fun navigateToSubtitlesActivity() {
        val intent = Intent(this, SubtitlesActivity::class.java)
        startActivity(intent)
    }

    // SettingActivity로 화면 전환
    private fun navigateToSettingActivity() {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

    // 버튼 선택 시 처리
    private fun selectButton(buttonNumber: Int) {
        // 모든 버튼 초기화
        binding.button1.setImageResource(R.drawable.decibel)
        binding.button2.setImageResource(R.drawable.subtitles)
        binding.button3.setImageResource(R.drawable.setting)
        binding.decibelText.visibility = android.view.View.GONE
        binding.subtitlesText.visibility = android.view.View.GONE
        binding.settingText.visibility = android.view.View.GONE
        // 로고 이미지 숨기기
        binding.logo.visibility = android.view.View.GONE

        // 선택한 버튼에 따라 이미지 및 텍스트 설정
        when (buttonNumber) {
            1 -> {
                binding.button1.setImageResource(R.drawable.selected_decibel)
                binding.decibelText.visibility = android.view.View.VISIBLE
            }
            2 -> {
                binding.button2.setImageResource(R.drawable.selected_subtitles)
                binding.subtitlesText.visibility = android.view.View.VISIBLE
            }
            3 -> {
                binding.button3.setImageResource(R.drawable.selected_setting)
                binding.settingText.visibility = android.view.View.VISIBLE
            }
        }
    }
}
