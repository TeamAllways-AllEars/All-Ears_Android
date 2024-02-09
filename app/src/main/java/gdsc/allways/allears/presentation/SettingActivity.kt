package gdsc.allways.allears.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBar 숨기기
        supportActionBar?.hide()

        // "SettingActivity" 텍스트 설정
        binding.textView.text = "SettingActivity"
    }
}
