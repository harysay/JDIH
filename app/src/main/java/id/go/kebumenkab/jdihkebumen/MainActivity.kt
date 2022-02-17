package id.go.kebumenkab.jdihkebumen

import android.Manifest
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.icu.text.DateFormat.MEDIUM
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.Settings
import android.renderscript.RenderScript
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import id.go.kebumenkab.jdihkebumen.service.ApiEndPoint
import org.json.JSONObject
import java.text.DateFormat.MEDIUM


class MainActivity : AppCompatActivity() {
    private lateinit var myWebView: WebView
    var isFirstStart: Boolean = false
    var deviceId : String =""
    var tokenDFB : String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //To play background sound
//        if (sp.getInt("Sound", 0) == 0) {
//            mediaPlayer = MediaPlayer.create(this, R.raw.abc);
//            mediaPlayer.start();
//            mediaPlayer.setLooping(true);
//        }
//        val t = Thread {
            //  Intro App Initialize SharedPreferences
            val getSharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)

            //  Create a new boolean and preference and set it to true
            isFirstStart = getSharedPreferences.getBoolean("firstStart", true)

            //  Check either activity or app is open very first time or not and do action
            if (isFirstStart==true) {
                if ( checkGooglePlayServices() ) {
                    // [START retrieve_current_token]
                    FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(
                        OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w("TAGFirebase", "getInstanceId failed", task.exception)
                                return@OnCompleteListener
                            }
                            val token = task.result?.token
                            val msg = token
                            Log.d("HasilToken", msg.toString())
                            tokenDFB = msg.toString()
                            deviceId = Settings.Secure.getString(contentResolver,
                                Settings.Secure.ANDROID_ID)
                            create()
                            //                    Toast.makeText(baseContext, "TokenFB= "+tokenDFB+" DeviceID= "+deviceId, Toast.LENGTH_LONG).show()
                            //                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                        })
                }else {
                    //You won't be able to send notifications to this device
                    Log.w("MainActivity", "Device doesn't have google play services")
                }
//                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//                    if (!task.isSuccessful) {
//                        Log.w("tokenFailed", "Fetching FCM registration token failed", task.exception)
//                        return@OnCompleteListener
//                    }
//
//                    // Get new FCM registration token
//                    val token = task.result
//
//                    // Log and toast
//                    val msg = token
//                    tokenDFB = msg.toString()
//                })
                //  Launch application introduction screen
                val e = getSharedPreferences.edit()
//                Log.d("TokenWebview", "TokenFB= "+tokenDFB+" DeviceID= "+deviceId)
                e.putBoolean("firstStart", false)
                e.apply()
            }
//        }
//        t.start()

        //Runtime External storage permission for saving download files

        //Runtime External storage permission for saving download files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
            ) {
                Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE - requesting it")
                val permissions =
                        arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissions, 1)
            }
        }
        myWebView = findViewById<View>(R.id.webview) as WebView

        myWebView.apply {
            // Configure related browser settings
            this.settings.loadsImagesAutomatically = true
            this.settings.javaScriptEnabled = true
            myWebView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            // Configure the client to use when opening URLs
            myWebView.webViewClient = WebViewClient()
            // Load the initial URL

            // Enable responsive layout
            myWebView.getSettings().setUseWideViewPort(true)
            // Zoom out if the content width is greater than the width of the viewport
            myWebView.getSettings().setLoadWithOverviewMode(true)
            myWebView.getSettings().setSupportZoom(true)
            myWebView.getSettings().setBuiltInZoomControls(true) // allow pinch to zooom
            myWebView.getSettings().setDisplayZoomControls(false) // disable the default zoom controls on the page

            /* myWebView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                 val i = Intent(Intent.ACTION_VIEW)
                 i.data = Uri.parse(url)
                 startActivity(i)
             })
 */
            //handle downloading
            myWebView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimetype)
                val cookies: String = CookieManager.getInstance().getCookie(url)
                request.addRequestHeader("cookie", cookies)
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription("Downloading File")
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                        url, contentDisposition, mimetype
                    )
                )
                val dm: DownloadManager =
                    getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                Toast.makeText(applicationContext, "Download file", Toast.LENGTH_LONG).show()


            })

            myWebView.loadUrl("https://jdih.kebumenkab.go.id")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
            IntentFilter("MyData")
        )
    }
    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {

        }
    }

    private fun create(){

        val loading = ProgressDialog(this)
        loading.setMessage("Mendaftarkan notifikasi...")
        loading.show()

        AndroidNetworking.post(ApiEndPoint.CREATE)
            .addBodyParameter("device_id",deviceId)
            .addBodyParameter("token",tokenDFB)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {

                override fun onResponse(response: JSONObject?) {

                    loading.dismiss()
//                    Toast.makeText(applicationContext,response?.getString("message"),Toast.LENGTH_SHORT).show()

//                    if(response?.getString("status")?.contains("true")!!){
//                        this@MainActivity.finish()
//                    }

                }

                override fun onError(anError: ANError?) {
                    loading.dismiss()
                    anError?.errorDetail?.toString()?.let { Log.d("ONERROR", it) }
                    Toast.makeText(applicationContext,"Connection Failure", Toast.LENGTH_SHORT).show()                    }


            })

    }

    private fun checkGooglePlayServices(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return if (status != ConnectionResult.SUCCESS) {
            Log.e("MainActivity", "Error")
            // ask user to update google play services.
            false
        } else {
            Log.i("MainActivity", "Google play services updated")
            true
        }
    }
}