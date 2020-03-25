package com.redone.moodrater

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.popup_view.view.*


class MainActivity : AppCompatActivity(), View.OnTouchListener {

    lateinit var seekBar: SeekBar
    lateinit var floatingMoodText: TextView
    lateinit var popupMoodText: TextView
    lateinit var popupWindow: PopupWindow
    lateinit var howMoodLabel: ViewGroup
    lateinit var moodLabel: ViewGroup
    lateinit var root: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val chooser = findViewById<View>(R.id.chooser)
        val mainMoodText = findViewById<TextView>(R.id.mainMoodText)
        val moodSeekBar = findViewById<SeekBar>(R.id.moodSeekBar)
        root = findViewById<ViewGroup>(R.id.root)
        moodLabel = findViewById<ViewGroup>(R.id.moodLabel)
        howMoodLabel = findViewById<ViewGroup>(R.id.howMoodLabel)

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popupView = inflater.inflate(R.layout.popup_view, null)
        seekBar = popupView.seekBar
        floatingMoodText = popupView.floatingMoodText

        val moodRater = popupView.seekBar

        popupView.setOnTouchListener(this)


        popupWindow = PopupWindow(
            popupView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.isClippingEnabled = true
        popupWindow.isFocusable = true
        popupWindow.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
        popupWindow.contentView.isFocusableInTouchMode = true


        popupMoodText = popupView.popupMoodText

        moodRater.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                moodSeekBar.progress = progress
                if (progress < 20) {
                    mainMoodText.text = getString(R.string.bad_mood)
                    popupMoodText.text = getString(R.string.bad_mood)
                } else if (progress in 20..39) {
                    mainMoodText.text = getString(R.string.not_bad_mood)
                    popupMoodText.text = getString(R.string.not_bad_mood)
                } else if (progress in 40..59) {
                    mainMoodText.text = getString(R.string.nice_mood)
                    popupMoodText.text = getString(R.string.nice_mood)
                } else if (progress in 60..79) {
                    mainMoodText.text = getString(R.string.good_mood)
                    popupMoodText.text = getString(R.string.good_mood)
                } else {
                    mainMoodText.text = getString(R.string.awesome_mood)
                    popupMoodText.text = getString(R.string.awesome_mood)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        chooser.setOnClickListener {
            Blurry.with(this)
                .radius(10)
                .sampling(8)
                .async()
                .animate(500)
                .onto(root)
            popupWindow.animationStyle = R.style.Animation
            popupWindow.showAtLocation(root, 0, 0, 0)
        }

        popupView.setMoodButton.setOnClickListener {
            moodLabel.visibility = View.VISIBLE
            howMoodLabel.visibility = View.GONE
            popupWindow.dismiss()
            Handler().postDelayed({
                Blurry.delete(root)
            }, 500)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val seekBarPosition = IntArray(2)
        seekBar.getLocationOnScreen(seekBarPosition)
        val seekBarWidth = seekBar.width
        val bias = seekBarWidth / 100
        val x = event?.x?.toInt() ?: 0
        val y = event?.y?.toInt() ?: 0
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                floatingMoodText.visibility = View.VISIBLE
                val progress = when {
                    x < seekBarPosition[0] -> {
                        0
                    }
                    x > (seekBarPosition[0] + seekBarWidth) -> {
                        100
                    }
                    else -> {
                        (x - seekBarPosition[0]) / bias
                    }
                }
                seekBar.progress = progress
                floatingMoodText.text = popupMoodText.text
                floatingMoodText.x = x.toFloat()
                floatingMoodText.y = y.toFloat() - 200f
            }
            MotionEvent.ACTION_MOVE -> {
                val progress = when {
                    x < seekBarPosition[0] -> {
                        0
                    }
                    x > (seekBarPosition[0] + seekBarWidth) -> {
                        100
                    }
                    else -> {
                        (x - seekBarPosition[0]) / bias
                    }
                }
                seekBar.progress = progress
                floatingMoodText.text = popupMoodText.text
                floatingMoodText.x = x.toFloat()
                floatingMoodText.y = y.toFloat() - 200f
            }
            MotionEvent.ACTION_UP -> {
                moodLabel.visibility = View.VISIBLE
                howMoodLabel.visibility = View.GONE
                floatingMoodText.visibility = View.GONE
                popupWindow.dismiss()
                Handler().postDelayed({
                    Blurry.delete(root)
                }, 500)
            }
        }
        return true
    }
}
