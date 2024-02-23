package gdsc.allways.allears.presentation.decibel

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.R

// 목소리 인식 세팅 실패 화면
class VoiceRecognitionFailActivity : AppCompatActivity() {
    private lateinit var recordingCompleteFailTextView: TextView
    private lateinit var voiceRecognitionInfoTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_recognition_fail)

        recordingCompleteFailTextView = findViewById(R.id.recordingCompleteFailTextView)
        voiceRecognitionInfoTextView = findViewById(R.id.voiceRecognitionInfoTextView)

        recordingCompleteFailTextView.gravity = Gravity.CENTER // 텍스트를 가운데 정렬
        voiceRecognitionInfoTextView.gravity = Gravity.CENTER // 텍스트를 가운데 정렬

        // ActionBar 숨기기
        supportActionBar?.hide()

        // 3초 후에 액티비티 종료
        Handler().postDelayed({
            finish()
        }, 3000)
    }
}
