package com.rehman.blurhash

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

class UpdateManager(private val context: Context) {

    private var pendingApkPath: String? = null
    private var installPermissionLauncher: ActivityResultLauncher<Intent>? = null
    fun setPermissionLauncher(launcher: ActivityResultLauncher<Intent>) {
        installPermissionLauncher = launcher
    }

    fun checkForUpdate() {
        val currentVersion = BuildConfig.VERSION_NAME
        val url =
            "https://api.github.com/repos/AbdulRehman-Pro/BlurHashColorPalette/releases/latest"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UpdateCheck", "Failed to fetch release info", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body()?.string()?.let { jsonResponse ->
                        val json = JSONObject(jsonResponse)
                        val latestVersion = json.getString("tag_name").removePrefix("v")
                        val apkName = json.getJSONArray("assets")
                            .getJSONObject(0).getString("name")
                        val downloadUrl = json.getJSONArray("assets")
                            .getJSONObject(0).getString("browser_download_url")

                        Log.d("UpdateCheck", "Apk Name: $apkName => Url: $downloadUrl")
                        Log.d(
                            "UpdateCheck",
                            "Latest Version: $latestVersion => Current Version: $currentVersion"
                        )

                        if (latestVersion > currentVersion) {
                            showUpdateDialog(context, apkName, downloadUrl)
                        }
                    }
                } else {
                    Log.e(
                        "UpdateCheck",
                        "Failed to fetch release info :${response.code()} - ${response.message()}"
                    )
                }
            }
        })
    }

    private fun checkInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // No permission needed for Android < 8
        }
    }

    private fun requestInstallPermission(apkPath: String) {
        pendingApkPath = apkPath // Store the APK path
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            installPermissionLauncher?.launch(intent)
        }
    }

    private fun showUpdateDialog(context: Context, apkName: String, downloadUrl: String) {
        Handler(Looper.getMainLooper()).post {
            AlertDialog.Builder(context)
                .setTitle("Update Available")
                .setMessage("A new version is available. Download now?")
                .setPositiveButton("Download") { _, _ ->
                    downloadAndInstall(apkName, downloadUrl)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun downloadAndInstall(apkName: String, apkUrl: String) {

        val destination =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$apkName"

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                    if (downloadId == -1L) {
                        Log.e("UpdateManager", "Download ID is invalid. Skipping installation.")
                        return
                    }

                    context.unregisterReceiver(this)

                    // Proceed to install APK
                    installApk(destination)
                }
            }
        }


        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

    }


    fun installApk(apkPath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !checkInstallPermission()) {
            requestInstallPermission(apkPath)
            return
        }

        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            Log.e("InstallAPK", "APK file not found!")
            return
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(installIntent)
    }

    fun handlePermissionResult() {
        if (checkInstallPermission()) {
            Log.d("InstallAPK", "Permission granted, retrying installation...")
            pendingApkPath?.let {
                installApk(it) // Retry installation
                pendingApkPath = null // Clear pending path
            }
        } else {
            Toast.makeText(
                context,
                "Permission denied! Enable manually in settings.",
                Toast.LENGTH_LONG
            ).show()
        }

    }
}
