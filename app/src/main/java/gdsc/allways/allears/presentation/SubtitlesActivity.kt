package gdsc.allways.allears.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.databinding.ActivitySubtitlesBinding

class SubtitlesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubtitlesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubtitlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBar 숨기기
        supportActionBar?.hide()

        // "SubtitlesActivity" 텍스트 설정
        binding.textView.text = "SubtitlesActivity"
    }
}
