/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package gdsc.allways.allears.presentation

import android.Manifest
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
import gdsc.allways.allears.R
import gdsc.allways.allears.databinding.ActivityDecibelBinding
import gdsc.allways.allears.presentation.D.SpeechAPI
import gdsc.allways.allears.presentation.D.VoiceRecorder
import gdsc.allways.allears.presentation.DecibelActivity.State.RECORDING
import gdsc.allways.allears.presentation.DecibelActivity.State.RELEASE
import kotlinx.coroutines.Job
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

//private const val TAG_DECIBEL = "MediaRecordTest"
private const val TAG_STT = "SpeechToTextTest"

class DecibelActivity : ComponentActivity(), OnTimerTickListener {

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
//        private const val REQUEST_SPEECH_CODE = 0
//        private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.WAKE_LOCK)
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
    var probabilityThreshold: Float = 0.10f // original: 0.3f

    lateinit var tensor:TensorAudio
    lateinit var classifier: AudioClassifier

    private lateinit var imageViews: List<ImageView>
    private var job: Job? = null
    private lateinit var filteredModelOutput: List<Category>
    var speechDecibel: Float = 0f
    val speechDecibels: MutableList<Float> = mutableListOf()

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
            if (binding.liveTranscribeTextView != null && !TextUtils.isEmpty(text)) {
                runOnUiThread {
                    if (isFinal) {
                        binding.liveTranscribeTextView.text = null
                        binding.liveTranscribeTextView.visibility = View.INVISIBLE
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
        classifier = AudioClassifier.createFromFile(this, modelPath);

        // create an audio recorder
        // `tensor`: store the recording for inference & build the format specification for the recorder.
        tensor = classifier.createInputTensorAudio();

        // show the audio recorder specification
        val format = classifier.requiredTensorAudioFormat
//        val recorderSpecs = "Number Of Channels: ${format.channels}\n" +
//                "Sample Rate: ${format.sampleRate}"
        //recorderSpecsTextView.text = recorderSpecs

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

        binding.recordImageButton.setOnClickListener {
            when (state) {
                RELEASE -> {
                    // 권한 확인 후 녹음 실행
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

                    // 녹음 시작과 동시에 '녹음 중(ing)'을 나타내도록 녹음 버튼을 깜빡이기 -- animation 적용
                    val blinkAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.blink_animation)
                    binding.recordImageButton.startAnimation(blinkAnimation)
                }
                RECORDING -> {
                    // 녹음 중지
                    onRecord(false)

                    // '녹음 중이 아님'을 나타내도록 녹음 버튼의 깜박임 중지
                    binding.recordImageButton.clearAnimation()
                }
            }
        }
    }

    private fun onRecord(start: Boolean) = if (start) {
        //startRecording()
        startVoiceRecorder()
    } else {
        //stopRecording()
        stopVoiceRecorder()
    }

    private fun startVoiceRecorder() {
        voiceRecorder = VoiceRecorder(callback, classifier)
        voiceRecorder!!.start()

        timer = Timer()
        timer.scheduleAtFixedRate(1, 500) {

            // Classify audio data
            //val numberOfSamples = tensor.load(record)
            tensor.load(voiceRecorder!!.tensorAudioRecord)
            val output = classifier.classify(tensor)

            // Filter out classifications with low probability
            // Purpose: to have better inference results
            filteredModelOutput = output[0].categories.filter {
                it.score > probabilityThreshold
            }

            // Creating a multiline string with the filtered results
            val allDecibelsStr =
                filteredModelOutput.sortedBy { -it.score }
                    .joinToString(separator = "\n") { "${it.label} -> ${it.score} " }

            speechDecibel = filteredModelOutput.find { it.label == "Speech" }?.score ?: 0f
            val speechDecibelIndex = (speechDecibel * 10).toInt() - 1
            speechDecibels.add(speechDecibel)
            val speechDecibelStr = speechDecibel.toString()

//            val handler = Handler(Looper.getMainLooper())

            // Updating the UI
            if (allDecibelsStr.isNotEmpty() && speechDecibel > 0f) {
                runOnUiThread {
                    // 인식되는 모든 데시벨 출력
                    //binding.temporalTextView.text = allDecibelsStr

                    // Speech의 데시벨만 출력
                    binding.temporalTextView.text = speechDecibelStr

                    reinitializeUI()
                    imageViews[speechDecibelIndex].setImageResource(R.drawable.decibel_myspeech)
                }
            }
        }

        // 코루틴 시작
        //startDecibelProcessing()

        state = RECORDING
    }

    private fun reinitializeUI() {
        imageViews.map { it.setImageResource(R.drawable.decibel_unit) }
    }

//    private fun startDecibelProcessing() {
//        job = CoroutineScope(Dispatchers.Main).launch {
//            val lastSpeechDecibel = speechDecibels.last()
//            updateImagesWithDecibel(lastSpeechDecibel)
//            delay(1000) // 1초 대기
//            // 모든 데시벨 값 처리 후 코루틴을 중지하고 이미지 초기화
//            //resetAllImages()
//        }
//    }

//    private fun updateImagesWithDecibel(decibel: Float) {
//        imageViews.map { imageView ->
//            imageView.setImageResource(R.drawable.decibel_unit)
//        }
//
//        val indexToUpdate = (decibel * 10).toInt() - 1
//        if (indexToUpdate in imageViews.indices) {
//            imageViews[indexToUpdate].setImageResource(R.drawable.current_decibel)
//        }
//    }

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

        Log.e(TAG_STT, "destroy the speechAPI")

        super.onDestroy()
    }

//    private fun stopRecording() {
//        recorder?.apply {
//            stop()
//            release()
//        }
//        recorder = null
//
//        timer.stop()
//
//        // todo recorder 중복
//        voiceRecorder?.stop()
//        voiceRecorder = null
//
//        state = RELEASE
//    }



//    private fun startRecording() {
//        recorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//            setOutputFile(fileName)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//
//            try {
//                prepare()
//            } catch (e: IOException) {
//                Log.e(TAG_DECIBEL, "prepare() failed: $e")
//            }
//
//            start()
//
////            displaySpeechRecognizer()
//        }
//
//        // TODO decibelView 를 decibelContainer 로 변경함에 따른 주석 처리
//        //binding.decibelView.clearDecibel()
//
//        timer.start()
//
//        // todo recorder 중복
//        voiceRecorder = VoiceRecorder(callback)
//        voiceRecorder?.start()
//
//        state = RECORDING
//    }

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

/* New Project 생성시 기본 템플릿에 있었던 코드
// Wear OS 특성상 필요한 코드인지 아직 안 알아본 상태라 남겨 둠.

@Composable
fun WearApp(greetingName: String) {
    AllEarsTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}



@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}
 */