package com.redone.moodrater

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import jp.wasabeef.blurry.Blurry


class MainActivity : AppCompatActivity() {

    lateinit var moodSeekBar: SeekBar
    lateinit var floatingMoodText: TextView
    lateinit var mainMoodText: TextView
    lateinit var setMoodButton: TextView
    lateinit var textIntoBar: TextView
    lateinit var textIntoBar2: TextView
    lateinit var popupWindow: PopupWindow
    lateinit var howMoodLabel: ViewGroup
    lateinit var root: ViewGroup
    lateinit var frameSeek: ViewGroup
    lateinit var imageView: ImageView
    lateinit var imageForBlur: ImageView
    lateinit var titleMood: TextView
    var startY: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainMoodText = findViewById(R.id.mainMoodText)
        moodSeekBar = findViewById(R.id.moodSeekBar)
        root = findViewById(R.id.root)
        howMoodLabel = findViewById(R.id.howMoodLabel)
        frameSeek = findViewById(R.id.frameSeek)
        setMoodButton = findViewById(R.id.setMoodButton)
        imageView = findViewById(R.id.imageView)
        textIntoBar = findViewById(R.id.textIntoBar)
        textIntoBar2 = findViewById(R.id.textIntoBar2)
        titleMood = findViewById(R.id.titleMood)
        floatingMoodText = findViewById(R.id.floatingMoodText)
        imageForBlur = findViewById(R.id.imageForBlur)

        howMoodLabel.post {
            startY = howMoodLabel.y
        }


        if (moodSeekBar.progress == 0) {
            moodSeekBar.thumb.alpha = 0
        }
        moodSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 20) {
                    mainMoodText.text = getString(R.string.bad_mood)
                } else if (progress in 20..39) {
                    mainMoodText.text = getString(R.string.not_bad_mood)
                } else if (progress in 40..59) {
                    mainMoodText.text = getString(R.string.nice_mood)
                } else if (progress in 60..79) {
                    mainMoodText.text = getString(R.string.good_mood)
                } else {
                    mainMoodText.text = getString(R.string.awesome_mood)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (moodSeekBar.thumb.alpha == 0) {
                    moodSeekBar.thumb.alpha = 255
                    mainMoodText.visibility = View.VISIBLE
                    imageForBlur.visibility = View.VISIBLE
                    val chip = findViewById<ViewGroup>(R.id.chip)
                    Blurry.with(this@MainActivity)
                        .radius(25)
                        .sampling(12)
                        .async()
                        .animate(1000)
                        .capture(root)
                        .into(imageForBlur)
                    val parentCenterY: Float = root.y + root.height / 2 - howMoodLabel.height / 2
                    howMoodLabel.animate().y(parentCenterY).duration = 500
                    titleMood.text = getString(R.string.how_mood)
                    root.setOnTouchListener(rootTouchListener())
                    setMoodButton.visibility = View.VISIBLE
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        setMoodButton.setOnClickListener {
            setMood()
        }

        imageView.setOnClickListener {
            setMoodButton.visibility = View.VISIBLE
            root.setOnTouchListener(rootTouchListener())
            titleMood.text = getString(R.string.how_mood)
            imageForBlur.visibility = View.VISIBLE
            val chip = findViewById<ViewGroup>(R.id.chip)
            Blurry.with(this@MainActivity)
                .radius(25)
                .sampling(12)
                .async()
                .animate(1000)
                .capture(root)
                .into(imageForBlur)
            val parentCenterY: Float = root.y + root.height / 2 - howMoodLabel.height / 2
            val parentCenterX: Float = root.x + root.width / 2
            howMoodLabel.animate().y(parentCenterY).duration = 500

            val translateAnimator =
                ValueAnimator.ofFloat(titleMood.x, parentCenterX - mainMoodText.width * 0.37f)
            translateAnimator.duration = 500
            translateAnimator.addUpdateListener {
                val value = translateAnimator.animatedValue as Float
                mainMoodText.x = value
            }
            val sizeAnimator = ValueAnimator.ofFloat(22f, 16f)
            sizeAnimator.duration = 500
            sizeAnimator.addUpdateListener {
                val value = sizeAnimator.animatedValue as Float
                mainMoodText.textSize = value
            }
            val set = AnimatorSet()
            set.playTogether(translateAnimator, sizeAnimator)
            set.start()

            val dip = 8f
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                resources.displayMetrics
            )

            imageView.animate().scaleX(1f).scaleY(1f).y(moodSeekBar.y + moodSeekBar.height)
                .x(moodSeekBar.x - px).duration = 500
            Handler().postDelayed({
                imageView.visibility = View.GONE
                moodSeekBar.visibility = View.VISIBLE
                textIntoBar2.visibility = View.VISIBLE
                textIntoBar.visibility = View.VISIBLE
            }, 500)

        }
    }

    fun setMood() {
        titleMood.text = getString(R.string.mood_title)
        frameSeek.post {
            val bitmapSeekBar = getViewBitmap(frameSeek)
            imageView.visibility = View.GONE
            imageView.setImageBitmap(bitmapSeekBar)
            imageView.visibility = View.VISIBLE
            moodSeekBar.visibility = View.GONE
            textIntoBar2.visibility = View.GONE
            textIntoBar.visibility = View.GONE
            imageView.post {
                howMoodLabel.animate().y(startY).duration = 500

                val translateAnimator = ValueAnimator.ofFloat(mainMoodText.x, titleMood.x)
                translateAnimator.duration = 500
                translateAnimator.addUpdateListener {
                    val value = translateAnimator.animatedValue as Float
                    mainMoodText.x = value
                }

                val sizeAnimator = ValueAnimator.ofFloat(16f, 22f)
                sizeAnimator.duration = 500
                sizeAnimator.addUpdateListener {
                    val value = sizeAnimator.animatedValue as Float
                    mainMoodText.textSize = value
                }
                val set = AnimatorSet()
                set.playTogether(translateAnimator, sizeAnimator)
                set.start()
                imageView.animate().scaleX(0.5f).scaleY(0.5f)
                    .y(mainMoodText.y - imageView.height / 4)
                    .x(root.width.toFloat() / 4).duration =
                    500
                root.setOnTouchListener(null)
                setMoodButton.visibility = View.GONE

                Handler().postDelayed({
                    imageForBlur.visibility = View.GONE
                    imageForBlur.setImageBitmap(null)
                }, 400)
            }
        }
    }

    fun rootTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { v, event ->
            root.performClick()
            val seekBarPosition = IntArray(2)
            frameSeek.getLocationOnScreen(seekBarPosition)
            val seekBarWidth = moodSeekBar.width
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
                    moodSeekBar.progress = progress
                    floatingMoodText.text = mainMoodText.text
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
                    moodSeekBar.progress = progress
                    floatingMoodText.text = mainMoodText.text
                    floatingMoodText.x = x.toFloat()
                    floatingMoodText.y = y.toFloat() - 200f
                }
                MotionEvent.ACTION_UP -> {
                    floatingMoodText.visibility = View.GONE
                    setMood()
                    root.setOnTouchListener(null)
                    setMoodButton.visibility = View.GONE
                    imageForBlur.setImageBitmap(null)
                }
            }
            true
        }
    }

    fun getViewBitmap(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.TRANSPARENT)
        }
        view.draw(canvas)

        return returnedBitmap
    }
}
