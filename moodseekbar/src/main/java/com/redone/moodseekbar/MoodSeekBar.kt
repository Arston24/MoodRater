package com.redone.moodseekbar

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import jp.wasabeef.blurry.Blurry


class MoodSeekBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    lateinit var view: View
    lateinit var root: View
    lateinit var moodSeekBar: SeekBar


    lateinit var mainMoodText: TextView
    lateinit var setMoodButton: TextView
    lateinit var textIntoBar: TextView
    lateinit var textIntoBar2: TextView
    lateinit var howMoodLabel: ViewGroup

    lateinit var frameSeek: ViewGroup
    lateinit var imageView: ImageView

    lateinit var imageForBlur: ImageView
    lateinit var titleMood: TextView
    lateinit var floatingMoodText: TextView
    var startY: Float = 0f

    private val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MoodSeekBar, 0, 0)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.mood_seek_bar, this)

        mainMoodText = view.findViewById(R.id.mainMoodText)
        moodSeekBar = view.findViewById(R.id.moodSeekBar)
        howMoodLabel = view.findViewById(R.id.howMoodLabel)
        frameSeek = view.findViewById(R.id.frameSeek)
        setMoodButton = view.findViewById(R.id.setMoodButton)
        imageView = view.findViewById(R.id.imageView)
        textIntoBar = view.findViewById(R.id.textIntoBar)
        textIntoBar2 = view.findViewById(R.id.textIntoBar2)
        titleMood = view.findViewById(R.id.titleMood)
        floatingMoodText = view.findViewById(R.id.floatingMoodText)
        imageForBlur = view.findViewById(R.id.imageForBlur)

        howMoodLabel.post {
            startY = howMoodLabel.y
        }


        if (moodSeekBar.progress == 0) {
            moodSeekBar.thumb.alpha = 0
        }

        root = view.rootView


        root.post {
            imageForBlur.post {
                imageForBlur.layoutParams.width = root.width
                imageForBlur.layoutParams.height = root.height
            }

            moodSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (progress < 20) {
                        mainMoodText.text = resources.getString(R.string.bad_mood)
                    } else if (progress in 20..39) {
                        mainMoodText.text = resources.getString(R.string.not_bad_mood)
                    } else if (progress in 40..59) {
                        mainMoodText.text = resources.getString(R.string.nice_mood)
                    } else if (progress in 60..79) {
                        mainMoodText.text = resources.getString(R.string.good_mood)
                    } else {
                        mainMoodText.text = resources.getString(R.string.awesome_mood)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (moodSeekBar.thumb.alpha == 0) {
                        moodSeekBar.thumb.alpha = 255
                        mainMoodText.visibility = View.VISIBLE
                        imageForBlur.visibility = View.VISIBLE
                        Blurry.with(context)
                            .radius(25)
                            .sampling(12)
                            .async()
                            .animate(1000)
                            .capture(root)
                            .into(imageForBlur)

                        val parentCenterY: Float =
                            view.y + view.height / 2 - howMoodLabel.height / 2
                        howMoodLabel.animate().y(parentCenterY).duration = 500
                        titleMood.text = resources.getString(R.string.how_mood)
                        view.setOnTouchListener(rootTouchListener())
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
                view.setOnTouchListener(rootTouchListener())
                titleMood.text = resources.getString(R.string.how_mood)
                imageForBlur.visibility = View.VISIBLE
                Blurry.with(context)
                    .radius(25)
                    .sampling(12)
                    .async()
                    .animate(1000)
                    .capture(view.rootView)
                    .into(imageForBlur)
                val parentCenterY: Float = view.y + view.height / 2 - howMoodLabel.height / 2
                val parentCenterX: Float = view.x + view.width / 2
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
    }


    fun setMood() {
        titleMood.text = resources.getString(R.string.mood_title)
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
                    .x(view.width.toFloat() / 4).duration =
                    500
                view.setOnTouchListener(null)
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
            view.performClick()
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
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
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