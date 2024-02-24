/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package gdsc.allways.allears.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import gdsc.allways.allears.R
import gdsc.allways.allears.databinding.ActivityDecibelBinding
import gdsc.allways.allears.dto.SubtitleCreateRequestDto
import gdsc.allways.allears.presentation.DecibelActivity.State.RECORDING
import gdsc.allways.allears.presentation.DecibelActivity.State.RELEASE
import gdsc.allways.allears.presentation.speechtotext.SpeechAPI
import gdsc.allways.allears.presentation.speechtotext.VoiceRecorder
import gdsc.allways.allears.presentation.subtitles.SubtitleService
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate


class DecibelActivity : ComponentActivity(), OnTimerTickListener {

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
        private const val TAG_STT = "SpeechToTextTest"
    }

    // 상태 관리
        // 릴리즈 -> 녹음중 -> 릴리즈 -> ...
    private enum class State {
        RELEASE, RECORDING
    }

    private lateinit var binding: ActivityDecibelBinding
    private lateinit var timer: Timer
    private var fileName: String = ""
    private var state: State = RELEASE

    // Defining the model to be used
    var modelPath = "1.tflite"

    // Defining the minimum threshold
    var probabilityThreshold: Float = 0.05f // original: 0.3f

    lateinit var tensor:TensorAudio
    lateinit var classifier: AudioClassifier

    private lateinit var imageViews: List<ImageView>

    private lateinit var filteredModelOutput: List<Category>
    private var speechDecibel: Float = 0f
    private val speechDecibels: MutableList<Float> = mutableListOf()
    private var speechDecibelIndex: Int = 0
    private var surroundingSounds = listOf<Category>()
    private var surroundingDecibel: Float = 0f
    private var surroundingDecibelIndex: Int = 0
    private var subtitleChunk: String = ""
    private var userDeviceId = ""

    private val subtitles = mutableListOf<String>()
    //private lateinit var subtitleAdapter: ArrayAdapter<String>
    private lateinit var subtitleApiService: SubtitleService

    private var speechAPI: SpeechAPI? = null
    private var voiceRecorder: VoiceRecorder? = null
    private val callback: VoiceRecorder.Callback = object : VoiceRecorder.Callback() {
        override fun onVoiceStart() {
            speechAPI?.startRecognizing(voiceRecorder!!.sampleRate)
        }

        override fun onVoice(data: ByteArray, size: Int) {
            speechAPI?.recognize(data, size)
        }

        override fun onVoiceEnd() {
            speechAPI?.finishRecognizing()
        }
    }

    private val listener: SpeechAPI.Listener =
        SpeechAPI.Listener { text, isFinal ->
            if (isFinal) {
                voiceRecorder!!.dismiss()
            }
            if (!TextUtils.isEmpty(text)) {
                runOnUiThread {
                    if (isFinal) {
                        binding.liveTranscribeTextView.text = null
                        binding.liveTranscribeTextView.visibility = View.INVISIBLE

                        subtitles.add(text)
                        //subtitleAdapter.notifyDataSetChanged()
                    } else {
                        binding.liveTranscribeTextView.text = text
                        binding.liveTranscribeTextView.visibility = View.VISIBLE
                    }
                }
            }
        }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 녹음 실행
            onRecord(true)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO
                )
            ) {
                showRequestPermissionRationale()
            } else {
                // 설정 창에서 사용자가 직접 권한 변경하도록 함
                showPermissionSettingDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecibelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Record to the external cache directory for visibility
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        //timer = Timer(this)

        // Initialization
        // load the model from the assets folder
        classifier = AudioClassifier.createFromFile(this, modelPath)

        // create an audio recorder
        // `tensor`: store the recording for inference & build the format specification for the recorder.
        tensor = classifier.createInputTensorAudio()

        speechAPI = SpeechAPI(this)
        speechAPI!!.addListener(listener)

        imageViews = listOf(
            binding.image1,
            binding.image2,
            binding.image3,
            binding.image4,
            binding.image5,
            binding.image6,
            binding.image7,
            binding.image8,
            binding.image9,
            binding.image10
        )

        userDeviceId = getMyDeviceId()

        //subtitleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, subtitles)
//        binding.mySubtitleListView.adapter = subtitleAdapter
//        binding.mySubtitleListView.isVerticalScrollBarEnabled = false

        subtitleApiService = SubtitleService.create()

        binding.recordImageButton.setOnClickListener {
            when (state) {
                RELEASE -> {
                    // 권한 확인 후 녹음 실행
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

                    // 녹음 시작과 동시에 '녹음 중(ing)'을 나타내도록 빨강색 녹음 버튼을 깜빡이기 (animation 적용)
                    binding.recordImageButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this, R.drawable.ic_mic
                        )
                    )
                    val blinkAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.blink_animation)
                    binding.recordImageButton.startAnimation(blinkAnimation)
                }
                RECORDING -> {
                    // 녹음 중지
                    onRecord(false)

                    // '녹음 중이 아님'을 나타내도록 녹음 버튼의 깜박임 중지
                    binding.recordImageButton.clearAnimation()
                    // 그리고 노란색 멈춤 버튼이 띄워짐
                    binding.recordImageButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this, R.drawable.ic_mic_stop
                        )
                    )

                    // subtitles 리스트 -> joinToString
                    subtitleChunk = subtitles.joinToString(". ")
                    Log.d(TAG_STT, subtitleChunk)
                    // 서버에다 POST로 해당 스트링 보내기
                        // 말을 했을 때만 서버에다 POST. 즉, 빈 문자열은 POST 하지 않음.
                    if (subtitleChunk.isNotEmpty()) {
                        Log.i(TAG_STT, "subtitleChunk is NOT empty")
                        subtitleApiService.createSubtitle(userDeviceId, subtitleChunk).enqueue(object : Callback<SubtitleCreateRequestDto> {
                            override fun onResponse(call: Call<SubtitleCreateRequestDto>, response: Response<SubtitleCreateRequestDto>) {
                                if (response.isSuccessful) {
                                    Log.i(TAG_STT, "Retrofit createSubtitle()_onResponse()_성공")
                                }
                            }

                            override fun onFailure(call: Call<SubtitleCreateRequestDto>, t: Throwable) {
                                Log.e(TAG_STT, "Retrofit createSubtitle()_onFailure(): $t")
                            }
                        })

                        // subtitles 리스트 비우기
                        subtitles.removeAll(subtitles)
                        //subtitleAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun getMyDeviceId(): String {
        Log.d("ALLEars_getMyDeviceId", Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun onRecord(start: Boolean) = if (start) {
        //startRecording()
        startVoiceRecorder()
    } else {
        //stopRecording()
        stopVoiceRecorder()
    }

    private fun startVoiceRecorder() {
        voiceRecorder?.stop()

        voiceRecorder = VoiceRecorder(callback, classifier)
        voiceRecorder!!.start()

        // 코루틴 시작
        //startDecibelProcessing()

        var durationIndex = 0

        timer = Timer()
        timer.scheduleAtFixedRate(1, 500) {

            // Classify audio data
            //val numberOfSamples = tensor.load(record)
            tensor.load(voiceRecorder?.tensorAudioRecord)
            val output = classifier.classify(tensor)

            // Filter out classifications with low probability
            // Purpose: to have better inference results
            filteredModelOutput = output[0].categories.filter {
                it.score > probabilityThreshold
            }

            // Creating a multiline string with the filtered results
            val allDecibelsStr =
                filteredModelOutput.sortedBy { -it.score }  // sortedBy(): 오름차순
                    .joinToString(separator = "\n") { "${it.label} -> ${it.score} " }
            Log.d(TAG_STT, allDecibelsStr)

            speechDecibel = filteredModelOutput.find { it.label == "Speech" }?.score ?: 0f
            Log.v(TAG_STT, "speechDecibel: $speechDecibel")

            surroundingSounds = filteredModelOutput.filter { it.label != "Speech" && it.label != "Silence" }
            if (surroundingSounds.isNotEmpty()) {
                surroundingDecibel = surroundingSounds.maxOf { it.score } + 0.05f    // 보강
            } else {
                surroundingDecibel = 0f
            }
            Log.v(TAG_STT, "nonSpeechDecibel: $surroundingDecibel")

            if (speechDecibel == 1.0f)
                speechDecibelIndex = 9
            else
                speechDecibelIndex = (speechDecibel * 10).toInt()
            Log.v(TAG_STT, "speechDecibelIndex: $speechDecibelIndex")

            if (surroundingDecibel >= 1.0f) {
                surroundingDecibelIndex = 9
            } else {
                surroundingDecibelIndex = (surroundingDecibel * 10).toInt()
            }
            Log.v(TAG_STT, "surroundingDecibelIndex: $surroundingDecibelIndex")
            //speechDecibels.add(speechDecibel)

            // Updating the UI
            if (allDecibelsStr.isNotEmpty()) {
                runOnUiThread {
                    // Speech의 데시벨 뷰
                    reinitializeUI()
                    startSurroundingDecibelProcess(surroundingDecibelIndex)
                    imageViews[speechDecibelIndex].setImageResource(R.drawable.decibel_myspeech)
                }
            }
        }
        state = RECORDING
    }

    private fun startSurroundingDecibelProcess(surroundingDecibelIndex: Int) {
        when (surroundingDecibelIndex) {
            0 -> {
                imageViews[surroundingDecibelIndex].setImageResource(R.drawable.current_decibel)
                imageViews[surroundingDecibelIndex + 1].setImageResource(R.drawable.current_decibel)
            }
            in 1 until 9 -> {
                imageViews[surroundingDecibelIndex - 1].setImageResource(R.drawable.current_decibel)
                imageViews[surroundingDecibelIndex].setImageResource(R.drawable.current_decibel)
                imageViews[surroundingDecibelIndex + 1].setImageResource(R.drawable.current_decibel)
            }
            9 -> {
                imageViews[surroundingDecibelIndex].setImageResource(R.drawable.current_decibel)
                imageViews[surroundingDecibelIndex - 1].setImageResource(R.drawable.current_decibel)
            }
        }
    }

    private fun reinitializeUI() {
        imageViews.map { it.setImageResource(R.drawable.decibel_unit) }
    }

    private fun stopVoiceRecorder() {
        voiceRecorder?.stop()
        voiceRecorder = null

        timer.cancel()

        speechDecibels.clear()

        state = RELEASE
    }

    override fun onDestroy() {
        speechAPI?.removeListener(listener)
        speechAPI?.destroy()
        speechAPI = null

        Log.d(TAG_STT, "destroy the speechAPI")

        super.onDestroy()
    }

    private fun showRequestPermissionRationale() {
        AlertDialog.Builder(this)
            .setMessage("Allow All Ears to record audio?")
            .setPositiveButton("Allow") { _: DialogInterface, _: Int ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
            .setNegativeButton("Deny") { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }
            .show()
    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage("You're unable to use this feature without the required permissions. Tap the Settings button to allow All Ears to access the Audio Record.")
            .setPositiveButton("Settings") { _: DialogInterface, _: Int ->
                navigateToAppSetting()
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }
            .show()
    }

    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    // TODO decibelView 를 decibelContainer 로 변경함에 따른 주석 처리
    override fun onTick(duration: Long) {
        //binding.decibelView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
    }
}
