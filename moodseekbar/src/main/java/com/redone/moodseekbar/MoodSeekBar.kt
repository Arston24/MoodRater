package com.redone.moodseekbar

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.*
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.popup_view.view.*
import timber.log.Timber


class MoodSeekBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    lateinit var view: View
    lateinit var root: View
    lateinit var rootSeekBar: ViewGroup
    lateinit var moodSeekBar: SeekBar

    lateinit var popupWindow: PopupWindow

    lateinit var mainMoodText: TextView
    lateinit var setMoodButton: TextView
    lateinit var textIntoBar: TextView
    lateinit var textIntoBar2: TextView
    lateinit var howMoodLabel: ViewGroup
    val screenHeight = resources.displayMetrics.heightPixels

    lateinit var frameSeek: ViewGroup
    lateinit var imageView: ImageView

    lateinit var imageForBlur: ImageView
    lateinit var titleMood: TextView
    lateinit var floatingMoodText: TextView

    lateinit var popupView: View

    val rootScreenshot = getActivity()?.window?.decorView?.rootView

    var startY: Float = 0f

    private val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MoodSeekBar, 0, 0)

    override fun onAttachedToWindow() {
        Timber.plant(Timber.DebugTree())
        super.onAttachedToWindow()
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.mood_seek_bar, this)

        rootSeekBar = view.findViewById(R.id.rootSeekBar)

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

        popupView = inflater.inflate(R.layout.popup_view, null)

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

        view.post {
            startY = view.y
        }

        if (moodSeekBar.progress == 0) {
            moodSeekBar.thumb.alpha = 0
        } else {
            mainMoodText.visibility = View.VISIBLE
            if (moodSeekBar.progress < 20) {
                mainMoodText.text = resources.getString(R.string.bad_mood)
            } else if (moodSeekBar.progress in 20..39) {
                mainMoodText.text = resources.getString(R.string.not_bad_mood)
            } else if (moodSeekBar.progress in 40..59) {
                mainMoodText.text = resources.getString(R.string.nice_mood)
            } else if (moodSeekBar.progress in 60..79) {
                mainMoodText.text = resources.getString(R.string.good_mood)
            } else {
                mainMoodText.text = resources.getString(R.string.awesome_mood)
            }
            setMood(false)
        }

        root = view.rootView


        popupView.moodSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 20) {
                    mainMoodText.text = resources.getString(R.string.bad_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.bad_mood)
                } else if (progress in 20..39) {
                    mainMoodText.text = resources.getString(R.string.not_bad_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.not_bad_mood)
                } else if (progress in 40..59) {
                    mainMoodText.text = resources.getString(R.string.nice_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.nice_mood)
                } else if (progress in 60..79) {
                    mainMoodText.text = resources.getString(R.string.good_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.good_mood)
                } else {
                    mainMoodText.text = resources.getString(R.string.awesome_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.awesome_mood)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        moodSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (progress < 20) {
                    mainMoodText.text = resources.getString(R.string.bad_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.bad_mood)
                } else if (progress in 20..39) {
                    mainMoodText.text = resources.getString(R.string.not_bad_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.not_bad_mood)
                } else if (progress in 40..59) {
                    mainMoodText.text = resources.getString(R.string.nice_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.nice_mood)
                } else if (progress in 60..79) {
                    mainMoodText.text = resources.getString(R.string.good_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.good_mood)
                } else {
                    mainMoodText.text = resources.getString(R.string.awesome_mood)
                    popupView.howMoodLabel.mainMoodText.text = resources.getString(R.string.awesome_mood)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (moodSeekBar.thumb.alpha == 0) {
                    moodSeekBar.thumb.alpha = 255

                    Blurry.with(context)
                        .radius(12)
                        .sampling(16)
                        .async()
                        .animate(2000)
                        .capture(rootScreenshot)
                        .into(popupView.imageForBlur)

                    howMoodLabel.post {
                        val labelLocation = IntArray(2)
                        howMoodLabel.getLocationOnScreen(labelLocation)
                        popupWindow.showAtLocation(root, 0, 0, 0)

                        popupView.moodSeekBar.progress = moodSeekBar.progress

                        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        layoutParams.marginStart = view.left
                        layoutParams.marginEnd = view.left

                        popupView.howMoodLabel.layoutParams = layoutParams

                        popupView.howMoodLabel.y = labelLocation[1] - getStatusBarHeight()

                        popupView.howMoodLabel.animate().y((screenHeight - getStatusBarHeight() * 2) / 2.toFloat() - popupView.howMoodLabel.height).duration = DURATION

                        val locationPopup = IntArray(2)
                        popupView.howMoodLabel.getLocationInWindow(locationPopup)


                        val parentCenterY: Float = (screenHeight / 4 - howMoodLabel.height / 2).toFloat()
//                        view.animate().y(parentCenterY).duration = 500
                        titleMood.text = resources.getString(R.string.how_mood)

                        setMoodButton.visibility = View.VISIBLE
                    }
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        popupView.setMoodButton.setOnClickListener {
            setMood(true)
        }

        imageView.setOnClickListener {
            root.setOnTouchListener(rootTouchListener())
            titleMood.text = resources.getString(R.string.how_mood)

            Blurry.with(context)
                .radius(12)
                .sampling(16)
                .async()
                .animate(2000)
                .capture(rootScreenshot)
                .into(popupView.imageForBlur)

            popupWindow.showAtLocation(root, 0, 0, 0)


            popupView.howMoodLabel.post {
                val labelLocation = IntArray(2)
                howMoodLabel.getLocationOnScreen(labelLocation)

                val parentCenterY: Float = (screenHeight / 4 - howMoodLabel.height / 2).toFloat()
                val parentCenterX: Float = view.x + view.width / 2
//                view.animate().y(parentCenterY).duration = DURATION

                popupView.howMoodLabel.animate().y(parentCenterY).duration = DURATION

                val translateAnimator = ValueAnimator.ofFloat(popupView.titleMood.x, parentCenterX - popupView.mainMoodText.width / 2)
                translateAnimator.duration = DURATION
                translateAnimator.addUpdateListener {
                    val value = translateAnimator.animatedValue as Float
                    popupView.mainMoodText.x = value
                }
                val sizeAnimator = ValueAnimator.ofFloat(22f, 16f)
                sizeAnimator.duration = DURATION
                sizeAnimator.addUpdateListener {
                    val value = sizeAnimator.animatedValue as Float
                    popupView.mainMoodText.textSize = value
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

                popupView.imageView.animate().scaleX(1f).scaleY(1f).y(popupView.moodSeekBar.y + popupView.moodSeekBar.height)
                    .x(popupView.moodSeekBar.x - px).duration = DURATION
                Handler().postDelayed({
                    popupView.imageView.visibility = View.GONE
                    popupView.moodSeekBar.visibility = View.VISIBLE
                    popupView.textIntoBar2.visibility = View.VISIBLE
                    popupView.textIntoBar.visibility = View.VISIBLE
                }, 500)
            }

        }
    }

    fun setMood(animate: Boolean) {
        titleMood.text = resources.getString(R.string.mood_title)
        mainMoodText.visibility = View.VISIBLE

        popupView.titleMood.text = resources.getString(R.string.mood_title)

        popupView.frameSeek.post {
            val bitmapSeekBar = getViewBitmap(popupView.frameSeek)
            imageView.setImageBitmap(bitmapSeekBar)
            popupView.imageView.setImageBitmap(bitmapSeekBar)

            imageView.visibility = View.VISIBLE
            popupView.imageView.visibility = View.VISIBLE

            moodSeekBar.visibility = View.GONE
            popupView.moodSeekBar.visibility = View.GONE

            textIntoBar2.visibility = View.GONE
            popupView.textIntoBar2.visibility = View.GONE

            textIntoBar.visibility = View.GONE
            popupView.textIntoBar.visibility = View.GONE
            popupView.imageView.post {
                if (!animate) {
                    view.y = startY
                    mainMoodText.x = titleMood.x * 1.85f
                    popupView.mainMoodText.x = popupView.titleMood.x * 1.85f

                    mainMoodText.textSize = 22f
                    popupView.mainMoodText.textSize = 22f

                    imageView.scaleX = 0.5f
                    popupView.imageView.scaleX = 0.5f

                    imageView.scaleY = 0.5f
                    popupView.imageView.scaleY = 0.5f

                    imageView.y = mainMoodText.y - imageView.height / 4
                    popupView.imageView.y = popupView.mainMoodText.y - popupView.imageView.height / 4

                    imageView.x = view.width.toFloat() / 4
                    popupView.imageView.x = view.width.toFloat() / 4
                } else {
                    view.animate().y(startY).duration = DURATION
                    val translateAnimator = ValueAnimator.ofFloat(popupView.mainMoodText.x, popupView.titleMood.x)

                    popupView.post {
                        translateAnimator.duration = DURATION
                        translateAnimator.addUpdateListener {
                            val value = translateAnimator.animatedValue as Float
                            mainMoodText.x = value
                            popupView.mainMoodText.x = value
                        }

                        val sizeAnimator = ValueAnimator.ofFloat(16f, 22f)
                        sizeAnimator.duration = DURATION
                        sizeAnimator.addUpdateListener {
                            val value = sizeAnimator.animatedValue as Float
                            mainMoodText.textSize = value
                            popupView.mainMoodText.textSize = value
                        }
                        val set = AnimatorSet()
                        set.playTogether(translateAnimator, sizeAnimator)
                        set.start()
                        imageView.animate().scaleX(0.5f).scaleY(0.5f)
                            .y(mainMoodText.y - imageView.height / 4)
                            .x(view.width.toFloat() / 4).duration = 1

                        val labelLocation = IntArray(2)
                        howMoodLabel.getLocationOnScreen(labelLocation)

                        popupView.howMoodLabel.animate().y(labelLocation[1] - getStatusBarHeight()).duration = DURATION

                        Handler().postDelayed({
                            popupView.imageView.animate().scaleX(0.5f).scaleY(0.5f)
                                .y(mainMoodText.y - imageView.height / 4)
                                .x(view.width.toFloat() / 4).duration =
                                500
                        }, 100)


                    }
                    root.setOnTouchListener(null)
                    setMoodButton.visibility = View.GONE

                    Handler().postDelayed({
                        imageForBlur.visibility = View.GONE
                        imageForBlur.setImageBitmap(null)
                        popupWindow.dismiss()
                    }, 2000)
                }
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
                    setMood(true)
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

    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    private fun getStatusBarHeight(): Float {
        val rectangle = Rect()
        val window = getActivity()?.window
        window?.decorView?.getWindowVisibleDisplayFrame(rectangle)
        val statusBarHeight = rectangle.top
        val contentViewTop = window?.findViewById<View>(Window.ID_ANDROID_CONTENT)?.top ?: 0
        return statusBarHeight - contentViewTop.toFloat()
    }

    companion object {
        private const val DURATION = 500L
    }
}