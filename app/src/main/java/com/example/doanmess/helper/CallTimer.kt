package com.example.doanmess.helper
import android.os.Handler
import android.os.Looper
import android.widget.TextView

class CallTimer(private val timerTextView: TextView) {
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    // Hàm định dạng thời gian (mm:ss hoặc hh:mm:ss)
    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            // Nếu có giờ, hiển thị hh:mm:ss
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            // Nếu không có giờ, hiển thị mm:ss
            String.format("%02d:%02d", minutes, secs)
        }
    }

    // Runnable để cập nhật thời gian
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                seconds++
                timerTextView.text = formatTime(seconds) // Cập nhật TextView
                handler.postDelayed(this, 1000) // Lặp lại mỗi giây
            }
        }
    }

    // Bắt đầu đếm thời gian
    fun start() {
        if (!isRunning) {
            isRunning = true
            handler.post(updateTimeRunnable)
        }
    }

    // Dừng đếm thời gian
    fun stop() {
        isRunning = false
        handler.removeCallbacks(updateTimeRunnable)
    }

    // Reset thời gian
    fun reset() {
        stop()
        seconds = 0
        timerTextView.text = "" // Xóa TextView khi reset
    }
}