package gdsc.allways.allears.presentation.decibel

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.R

// 목소리 인식 세팅 성공 화면
class VoiceRecognitionSuccessActivity : AppCompatActivity() {
    private lateinit var recordingCompleteTextView: TextView
    private lateinit var voiceRecognitionInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_recognition_success)

        recordingCompleteTextView = findViewById(R.id.recordingCompleteTextView)
        voiceRecognitionInfoTextView = findViewById(R.id.voiceRecognitionInfoTextView)

        recordingCompleteTextView.gravity = Gravity.CENTER // 텍스트를 가운데 정렬
        voiceRecognitionInfoTextView.gravity = Gravity.CENTER // 텍스트를 가운데 정렬

        // ActionBar 숨기기
        supportActionBar?.hide()

        // 3초 후에 액티비티 종료
        Handler().postDelayed({
            finish()
        }, 3000)
    }
}
