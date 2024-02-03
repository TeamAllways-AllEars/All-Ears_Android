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
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import gdsc.allways.allears.databinding.ActivityDecibelBinding

class DecibelActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    private lateinit var binding: ActivityDecibelBinding

    private val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // TODO 녹음 실행
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.RECORD_AUDIO
                    )) {
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

        binding.recordImageButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

            /*
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // TODO 녹음 실행
                    // TODO You can use the API that requires the permission.
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO
                ) -> {
                    showRequestPermissionRationale()
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_RECORD_AUDIO_CODE
                    )
                }
            }

             */
        }
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

    /*
    // TODO onRequestPermissionsResult() 의 deprecation 으로 인한 에러 해결 필요
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_RECORD_AUDIO_CODE -> {
                // 만약 request 가 거부되었다면, grantResults 는 empty 상태일 것이다.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // TODO 녹음 실행
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.RECORD_AUDIO
                        )) {
                        showRequestPermissionRationale()
                    } else {
                        // 설정 창에서 사용자가 직접 권한 변경하도록 함
                        showPermissionSettingDialog()
                    }
                }
                return
            }
            else -> return
        }
    }

     */
}

/*
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