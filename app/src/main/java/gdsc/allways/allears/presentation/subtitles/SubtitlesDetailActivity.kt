// SubtitlesDetailActivity.kt

package gdsc.allways.allears.presentation.subtitles

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.R

class SubtitlesDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subtitles_detail)

        // ActionBar 숨기기
        supportActionBar?.hide()

        val subtitleText = intent.getStringExtra("subtitle_text")
        val subtitleTextView = findViewById<TextView>(R.id.subtitleTextView)
        subtitleTextView.text = subtitleText
        subtitleTextView.gravity = Gravity.CENTER // 텍스트를 가운데 정렬
    }
}
