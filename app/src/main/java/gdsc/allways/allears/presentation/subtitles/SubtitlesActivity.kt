package gdsc.allways.allears.presentation.subtitles

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import gdsc.allways.allears.R
import gdsc.allways.allears.databinding.ActivitySubtitlesBinding
import gdsc.allways.allears.dto.SubtitleListResponseDto
import gdsc.allways.allears.dto.SubtitleResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.text.SimpleDateFormat

class SubtitlesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubtitlesBinding
    private lateinit var apiService: SubtitleService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubtitlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBar 숨기기
        supportActionBar?.hide()

        // SubtitleService 초기화
        apiService = SubtitleService.create()

        // 자막 추가 및 가져오기
        createAndFetchSubtitles()
    }

    private fun createAndFetchSubtitles() {
        // 자막 추가
        val subtitleToAdd = SubtitleResponseDto(3, "Feb 22, 2024", "14:30 PM", "test용 자막")
        //addSubtitle(subtitleToAdd)
        fetchSubtitles()
    }

    private fun addSubtitle(subtitleToAdd: SubtitleResponseDto) {
        val deviceId = getDeviceId() // 기기의 고유 ID 가져오기
        apiService.createSubtitle(deviceId, subtitleToAdd).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 자막 추가 성공 후, 모든 자막 가져오기
                    fetchSubtitles()
                } else {
                    Toast.makeText(this@SubtitlesActivity, "Failed to add subtitle", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@SubtitlesActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchSubtitles() {
        apiService.getAllSubtitles(getDeviceId()).enqueue(object : Callback<SubtitleListResponseDto> {
            override fun onResponse(call: Call<SubtitleListResponseDto>, response: Response<SubtitleListResponseDto>) {
                if (response.isSuccessful) {
                    val subtitles = response.body()?.subtitleResponseDtoList

                    // 자막이 생성된 날짜와 시간을 기준으로 정렬
                    subtitles?.sortedWith(compareBy({ it.createdDate }, { it.createdTime }))?.forEach { subtitle ->
                        val button = Button(this@SubtitlesActivity)
                        button.background = ContextCompat.getDrawable(this@SubtitlesActivity, R.drawable.rounded_button) // 둥근 테두리 설정

                        // createdDate는 굵은 글꼴로 설정
                        val spannableString = SpannableString("${subtitle.createdDate}\n${subtitle.createdTime}")
                        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, subtitle.createdDate.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        button.text = spannableString

                        button.setOnClickListener {
                            fetchSubtitleById(subtitle.id)
                        }

                        // 각 버튼 아래에 간격 설정
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 0, 20)
                        button.layoutParams = params

                        binding.linearLayout.addView(button)
                    }

                    // 마지막에 빈 버튼 추가
                    val emptyButton = Button(this@SubtitlesActivity)
                    emptyButton.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    emptyButton.setBackgroundColor(Color.BLACK) // 배경 색상을 검정색으로 설정
                    emptyButton.isEnabled = false // 클릭 이벤트 처리를 받지 않도록 설정
                    binding.linearLayout.addView(emptyButton)

                } else {
                    Toast.makeText(this@SubtitlesActivity, "Failed to fetch subtitles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SubtitleListResponseDto>, t: Throwable) {
                Toast.makeText(this@SubtitlesActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchSubtitleById(id: Long) {
        apiService.getSubtitleById(id, getDeviceId()).enqueue(object : Callback<SubtitleResponseDto> {
            override fun onResponse(call: Call<SubtitleResponseDto>, response: Response<SubtitleResponseDto>) {
                if (response.isSuccessful) {
                    val subtitle = response.body()
                    // 클릭한 자막의 내용을 가져와 SubtitlesDetailActivity로 전달
                    val intent = Intent(this@SubtitlesActivity, SubtitlesDetailActivity::class.java)
                    intent.putExtra("subtitle_text", subtitle?.subtitleText)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SubtitlesActivity, "Failed to fetch subtitle by id", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SubtitleResponseDto>, t: Throwable) {
                Toast.makeText(this@SubtitlesActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 기기의 고유 ID 가져오기
    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String {
        Log.d("YMC", Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }
}
