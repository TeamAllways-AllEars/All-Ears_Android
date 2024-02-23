package gdsc.allways.allears.presentation.decibel

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import gdsc.allways.allears.R

class VoiceRecognitionActivity : AppCompatActivity() {
    private lateinit var recordImageButton: ImageButton
    private lateinit var liveTranscribeTextView: TextView
    private lateinit var constraintLayout: androidx.constraintlayout.widget.ConstraintLayout
    private var isRecording: Boolean = false
    private var clickCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_recognition)

        recordImageButton = findViewById(R.id.recordImageButton)
        //circularProgressBar = findViewById(R.id.circularProgressBar)
        liveTranscribeTextView = findViewById(R.id.liveTranscribeTextView)
        constraintLayout = findViewById(R.id.constraintLayout)

        // ActionBar 숨기기
        supportActionBar?.hide()

        // "SettingActivity" 텍스트 설정
        liveTranscribeTextView.text = "The old book on the dusty shelf caught my attention."
        liveTranscribeTextView.gravity = Gravity.CENTER // 텍스트를 가운데 정렬

        recordImageButton.setOnClickListener {
            toggleRecordingState()
        }
    }

    // 녹음 버튼 누를 때마다 텍스트 변경 & 테두리 완성 되도록 설정
    private fun toggleRecordingState() {
        if (isRecording) {
            clickCount++
            when (clickCount) {
                1 -> {
                    liveTranscribeTextView.text = "Laughter echoed through the quiet, moonlit night."
                    constraintLayout.setBackgroundResource(R.drawable.border2)
                }
                2 -> {
                    liveTranscribeTextView.text = "The aroma of fresh coffee filled the cozy cafe."
                    constraintLayout.setBackgroundResource(R.drawable.border3)
                }
                3 -> {
                    liveTranscribeTextView.text = "Lost in thought, she gazed at the stars above."
                    constraintLayout.setBackgroundResource(R.drawable.border4)
                }
                4 -> {
                    liveTranscribeTextView.text = "Raindrops danced on the window, creating a soothing melody."
                    constraintLayout.setBackgroundResource(R.drawable.border5)
                }
                5 -> {
                    startActivity(Intent(this, VoiceRecognitionSuccessActivity::class.java))
                    finish()
                    return // 종료
                }
            }
        }

        isRecording = !isRecording
        val imageResource = if (isRecording) {
            R.drawable.ic_mic_stop // 녹음 중이면 정지 이미지로 변경
        } else {
            R.drawable.ic_mic // 정지 중이면 녹음 이미지로 변경
        }
        recordImageButton.setImageResource(imageResource)
        liveTranscribeTextView.gravity = Gravity.CENTER // 텍스트를 가운데 정렬
    }

    override fun onDestroy() {
        super.onDestroy()
        if (clickCount < 5) {
            // clickCount가 5보다 작은 경우 VoiceRecognitionFailActivity를 실행
            startActivity(Intent(this, VoiceRecognitionFailActivity::class.java))
        }
    }
}
