package gdsc.allways.allears.presentation.voicerecognition

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.R

// 목소리 인식 세팅 실패 화면
class VoiceRecognitionFailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_recognition_fail)

        // ActionBar 숨기기
        supportActionBar?.hide()

        // 3초 후에 액티비티 종료
        Handler().postDelayed({
            finish()
        }, 3000)
    }
}
