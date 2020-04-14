package com.redone.moodseekbar

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Handler
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import extension.dpToPx
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

    val sizeTextViewMap: ArrayMap<String, Float> = ArrayMap()

    lateinit var frameSeek: ViewGroup
    lateinit var imageView: ImageView

    lateinit var titleMood: TextView
    private var titleTextColor = 0
    private var moodTextColor = 0
    private var progress = 0
    private var moodsArray: Array<CharSequence>? = null
    private var colorsArray: IntArray? = null


    lateinit var popupView: View
    var howMoodLabelLocation = IntArray(2)

    val rootScreenshot = getActivity()?.window?.decorView?.rootView

    var startY: Float = 0f

    init {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MoodSeekBar, 0, 0)
        titleTextColor = a.getColor(R.styleable.MoodSeekBar_titleTextColor, Color.WHITE)
        moodTextColor = a.getColor(R.styleable.MoodSeekBar_moodTextColor, ContextCompat.getColor(context, R.color.default_mood_color))
        progress = a.getInt(R.styleable.MoodSeekBar_seekBarProgress, 0)
        moodsArray = a.getTextArray(R.styleable.MoodSeekBar_strings)

        val colorsId: Int = a.getResourceId(R.styleable.MoodSeekBar_colors, 0)
        if (colorsId != 0) {
            colorsArray = a.resources.getIntArray(colorsId)
        }

        if (moodsArray == null) {
            moodsArray = resources.getTextArray(R.array.moodsArray)
        }
        if (colorsArray == null) {
            colorsArray = resources.getIntArray(R.array.colorsArray)
        }

        a.recycle()
    }

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
        titleMood.setTextColor(titleTextColor)
        mainMoodText.setTextColor(moodTextColor)
        moodSeekBar.progress = progress

        popupView = inflater.inflate(R.layout.popup_view, null)
        popupView.titleMood.setTextColor(titleTextColor)
        popupView.mainMoodText.setTextColor(moodTextColor)

        val progressDrawable = createProgressDrawable(context)

        popupWindow = PopupWindow(
            popupView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        moodSeekBar.progressDrawable = progressDrawable
        popupView.moodSeekBar.progressDrawable = progressDrawable

        setupPopup()

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
            popupView.moodSeekBar.progress = moodSeekBar.progress
            mainMoodText.visibility = View.VISIBLE
            setMoodText(progress)
            setMood(false)
        }

        root = view.rootView

        popupView.moodSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setMoodText(progress)
                moodSeekBar.progress = progress
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
                setMoodText(progress)
                popupView.moodSeekBar.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (moodSeekBar.thumb.alpha == 0) {
                    moodSeekBar.thumb.alpha = 255
                    popupView.setOnTouchListener(rootTouchListener())

                    Blurry.with(context)
                        .radius(12)
                        .sampling(16)
                        .async()
                        .animate(1000)
                        .capture(rootScreenshot)
                        .into(popupView.imageForBlur)

                    popupWindow.showAtLocation(root, 0, 0, 0)

                    howMoodLabel.post {
                        howMoodLabel.getLocationOnScreen(howMoodLabelLocation)
                        popupView.moodSeekBar.visibility = View.VISIBLE

                        popupView.howMoodLabel.post {
                            setupPopup()
                            popupView.howMoodLabel.mainMoodText.post {
                                popupView.howMoodLabel.y = howMoodLabelLocation[1] - getStatusBarHeight()
                                popupView.howMoodLabel.mainMoodText.y = context.dpToPx(36)

//                                popupView.moodSeekBar.progress = moodSeekBar.progress

                                popupView.howMoodLabel.animate().y((screenHeight - getStatusBarHeight()) / 2.toFloat() - howMoodLabel.height / 2).duration = DURATION

                                val locationPopup = IntArray(2)
                                popupView.howMoodLabel.getLocationInWindow(locationPopup)

                                titleMood.text = resources.getString(R.string.how_mood)
                                popupView.titleMood.text = resources.getString(R.string.how_mood)
                            }
                        }
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
            howMoodLabel.getLocationOnScreen(howMoodLabelLocation)
            popupView.setOnTouchListener(rootTouchListener())
            titleMood.text = resources.getString(R.string.how_mood)
            popupView.titleMood.text = resources.getString(R.string.how_mood)

            Blurry.with(context)
                .radius(12)
                .sampling(16)
                .async()
                .animate(1000)
                .capture(rootScreenshot)
                .into(popupView.imageForBlur)
            popupWindow.showAtLocation(root, 0, 0, 0)

            popupView.howMoodLabel.post {
                popupView.howMoodLabel.imageView.y = imageView.y
                val parentCenterY: Float = (screenHeight / 4 - howMoodLabel.height / 2).toFloat()
                val parentCenterX: Float = view.x + view.width / 2
                setupPopup()

                popupView.howMoodLabel.mainMoodText.post {
                    popupView.howMoodLabel.mainMoodText.x = titleMood.x
                    popupView.howMoodLabel.mainMoodText.y = context.dpToPx(31)
                    popupView.howMoodLabel.y = howMoodLabelLocation[1] - getStatusBarHeight()

                    popupView.howMoodLabel.animate().y((screenHeight - getStatusBarHeight()) / 2.toFloat() - howMoodLabel.height / 2).duration = DURATION

                    val translateAnimator = ValueAnimator.ofFloat(popupView.titleMood.x, parentCenterX - popupView.mainMoodText.width / 1.81f)
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

                    popupView.imageView.animate().scaleX(1f).scaleY(1f).y(popupView.moodSeekBar.y + moodSeekBar.height)
                        .x(moodSeekBar.x - context.dpToPx(8)).duration = DURATION
                    Handler().postDelayed({
                        popupView.imageView.visibility = View.GONE
                        popupView.moodSeekBar.visibility = View.VISIBLE
                        popupView.textIntoBar2.visibility = View.VISIBLE
                        popupView.textIntoBar.visibility = View.VISIBLE
                    }, DURATION)
                }
            }
        }
    }

    private fun setupPopup() {
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.marginStart = view.left
        layoutParams.marginEnd = view.left
        layoutParams.gravity = Gravity.TOP
        popupView.howMoodLabel.layoutParams = layoutParams
        popupView.howMoodLabel.y = howMoodLabelLocation[1] - getStatusBarHeight()
    }

    fun setMood(animate: Boolean) {
        titleMood.text = resources.getString(R.string.mood_title)
        mainMoodText.visibility = View.VISIBLE
        popupView.titleMood.text = resources.getString(R.string.mood_title)

        frameSeek.post {
            val bitmapSeekBar = if (popupView.moodSeekBar.visibility == View.VISIBLE) {
                getViewBitmap(popupView.frameSeek)
            } else {
                getViewBitmap(frameSeek)
            }

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
            imageView.post {
                if (!animate) {
                    view.y = startY
                    mainMoodText.x = titleMood.x
                    popupView.mainMoodText.x = titleMood.x
                    mainMoodText.textSize = 22f
                    popupView.mainMoodText.textSize = 22f

                    imageView.scaleX = 0.5f
                    popupView.imageView.scaleX = 0.5f

                    imageView.scaleY = 0.5f
                    popupView.imageView.scaleY = 0.5f

                    imageView.y = mainMoodText.y - imageView.height / 4
                    popupView.howMoodLabel.imageView.y = mainMoodText.y - imageView.height / 4

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

                        howMoodLabel.getLocationOnScreen(howMoodLabelLocation)

                        popupView.howMoodLabel.animate().y(howMoodLabelLocation[1] - getStatusBarHeight()).duration = DURATION

                        popupView.imageView.animate().scaleX(0.5f).scaleY(0.5f)
                            .y(popupView.mainMoodText.y - popupView.imageView.height / 4 - context.dpToPx(5))
                            .x(view.width.toFloat() / 4).duration =
                            500
                    }
                    popupView.setOnTouchListener(null)
                    setMoodButton.visibility = View.GONE

                    Handler().postDelayed({
                        popupView.imageForBlur.setImageBitmap(null)
                        popupWindow.dismiss()
                    }, 600)
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
                    popupView.moodSeekBar.progress = progress
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
                    popupView.moodSeekBar.progress = progress
                }
                MotionEvent.ACTION_UP -> {
                    setMood(true)
                    popupView.setOnTouchListener(null)
                    setMoodButton.visibility = View.GONE
                    Handler().postDelayed({
                        popupView.imageForBlur.setImageBitmap(null)
                        popupWindow.dismiss()
                    }, 600)
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
        return statusBarHeight.toFloat()
    }

    private fun setMoodText(progress: Int) {
        moodsArray?.let {
            var start = 0
            var end = 0
            moodsArray?.forEach { item ->
                val arraySize = moodsArray?.size ?: 0
                end += 100 / arraySize
                if (progress in start..end) {
                    mainMoodText.text = item
                    popupView.howMoodLabel.mainMoodText.text = item
                }
                start = end
            }
        }
    }

    private fun createProgressDrawable(context: Context): Drawable? {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, colorsArray)
        gd.cornerRadius = 0f
        gd.cornerRadii = floatArrayOf(64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f)

        val roundRectShape = RoundRectShape(floatArrayOf(64f, 64f, 64f, 64f, 64f, 64f, 64f, 64f), null, null)
        val shape = ShapeDrawable(roundRectShape)
        shape.paint.style = Paint.Style.FILL
        shape.paint.color = ContextCompat.getColor(context, android.R.color.transparent)
        shape.paint.style = Paint.Style.STROKE
        shape.paint.strokeWidth = 1f
        shape.paint.color = ContextCompat.getColor(context, R.color.bar_shape_color)
        val clipDrawable = ClipDrawable(gd, Gravity.START,
            ClipDrawable.HORIZONTAL)
        return LayerDrawable(arrayOf<Drawable>(
            clipDrawable, shape))
    }

    companion object {
        private const val DURATION = 500L
    }
}