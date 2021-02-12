package com.mohsenoid.switcherapp

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.view.WindowManager.LayoutParams
import android.widget.ImageView


class FloatingIconService : Service(), View.OnClickListener, View.OnTouchListener {

    private lateinit var floatingIcon: ImageView

    private lateinit var windowManager: WindowManager

    private val layoutType =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            LayoutParams.TYPE_PHONE
        }

    //here is all the science of params
    private val myParams = LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT,
        layoutType,
        LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 100
    }

    override fun onCreate() {
        super.onCreate()

        floatingIcon = ImageView(this).apply {
            //a face floating bubble as imageView
            setImageResource(R.drawable.ic_launcher)

            //for moving the picture on touch and slide
            setOnTouchListener(this@FloatingIconService)
            setOnClickListener(this@FloatingIconService)
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // add a floatingfacebubble icon in window
        windowManager.addView(floatingIcon, myParams)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        if (this::floatingIcon.isInitialized && this::windowManager.isInitialized) {
            windowManager.removeView(floatingIcon)
        }
        super.onDestroy()
    }

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = myParams.x
                initialY = myParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_MOVE -> {
                myParams.x = initialX + (event.rawX - initialTouchX).toInt()
                myParams.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(v, myParams)
            }
        }

        return false
    }

    override fun onClick(view: View) {
        val intent = Intent(view.context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}