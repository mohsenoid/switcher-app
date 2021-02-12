package com.mohsenoid.switcherapp

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mohsenoid.switcherapp.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
    }

    override fun onResume() {
        super.onResume()
        hideFloatingIcon()

        // Check if the application has draw over other apps permission or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            printLog("Allow ${getString(R.string.app_name)} to display over other apps...")

            Handler(Looper.getMainLooper()).postDelayed({
                // Open the settings screen to grant the permission
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
            }, 3000)
        } else {
            printLog("Permission to display over other apps is granted!")
            initUi()
        }
    }

    private fun initUi() {
        binding.log.movementMethod = ScrollingMovementMethod()

        binding.queryAll.setOnClickListener { queryAllPackages() }
        binding.queryScanner.setOnClickListener { queryScannerApp() }
        binding.openScanner.setOnClickListener { openScannerApp() }
    }

    private fun printLog(message: String) {
        binding.log.text = getString(R.string.log_separator, message, binding.log.text)
    }

    private fun queryAllPackages() {
        // As of Android 11, this method no longer returns information about all apps
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val sortedPackages = packages.map(PackageInfo::packageName).sorted()
        printLog(sortedPackages.joinToString(separator = "\n"))
    }

    private fun queryScannerApp() {
        val results = SCANNER_APP_PACKAGES.map {
            val result = if (packageManager.getLaunchIntentForPackage(it) != null) {
                "Is Available."
            } else {
                "Is Not Available!"
            }
            it to result
        }

        printLog(results.joinToString(separator = "\n") { (pkg, msg) -> "$pkg: $msg" })
    }

    private fun openScannerApp() {
        val launchIntent: Intent? = SCANNER_APP_PACKAGES.mapNotNull {
            packageManager.getLaunchIntentForPackage(it)
        }.firstOrNull()

        if (launchIntent != null) {
            startActivity(launchIntent)
            showFloatingIcon()
        }
    }

    private fun showFloatingIcon() {
        val intent = Intent(this, FloatingIconService::class.java)
        startService(intent)
    }

    private fun hideFloatingIcon() {
        val intent = Intent(this, FloatingIconService::class.java)
        stopService(intent)
    }

    companion object {
        private val SCANNER_APP_PACKAGES = arrayOf(
            "com.glsecl.android.driver",
            "com.glsecl.android.driver.debug",
            "com.glsecl.android.driver.qa",
        )

        private const val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084
    }
}