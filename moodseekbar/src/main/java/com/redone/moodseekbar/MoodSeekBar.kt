package com.redone.moodseekbar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Handler
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import extension.dpToPx
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.popup_view.view.*
import timber.log.Timber


class MoodSeekBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    lateinit var view: View
    lateinit var root: View
    var popupView: View? = null
    lateinit var rootSeekBar: ViewGroup
    lateinit var moodSeekBar: SeekBar

    lateinit var dialog: Dialog

    lateinit var mainMoodText: TextView
    lateinit var setMoodButton: TextView
    var textIntoBarEnd: TextView? = null
    var textIntoBarStart: TextView? = null
    lateinit var howMoodLabel: ViewGroup
    val screenHeight = resources.displayMetrics.heightPixels

    val sizeTextViewMap: ArrayMap<String, Float> = ArrayMap()

    lateinit var frameSeek: ViewGroup
    lateinit var imageView: ImageView

    var titleMood: TextView? = null
    lateinit var floatingMoodText: TextView
    private var titleTextColor = 0
    private var moodTextColor = 0
    private var progress = 0
    var statusBarHeight: Float = 0f

    var moodsArray: Array<CharSequence>? = null

    var colorsArray: IntArray? = null

    private var progressAnimator = ValueAnimator.ofInt(0)

    var howMoodLabelLocation = IntArray(2)

    val rootScreenshot = getActivity()?.window?.decorView?.rootView

    var titleTextQuestion: String? = null
        set(value) {
            field = value
            titleMood?.text = titleTextQuestion
            popupView?.titleMood?.text = titleTextQuestion
        }

    var titleTextMood: String? = null
        set(value) {
            field = value
            titleMood?.text = titleTextMood
            popupView?.titleMood?.text = titleTextMood
        }

    var startSeekbarText: String? = null
        set(value) {
            field = value
            textIntoBarStart?.text = startSeekbarText
            popupView?.textIntoBarStart?.text = startSeekbarText
        }

    var endSeekbarText: String? = null
        set(value) {
            field = value
            textIntoBarEnd?.text = endSeekbarText
            popupView?.textIntoBarEnd?.text = endSeekbarText
        }

    var startY: Float = 0f
    private val thumb = ShapeDrawable(OvalShape())
    private val ring = ShapeDrawable(OvalShape())
    var progressDrawable: Drawable? = null
    var thumbDrawable: Drawable? = null
    var barIsAnimating = false

    init {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MoodSeekBar, 0, 0)
        titleTextColor = a.getColor(R.styleable.MoodSeekBar_titleTextColor, Color.WHITE)
        moodTextColor = a.getColor(R.styleable.MoodSeekBar_moodTextColor, ContextCompat.getColor(context, R.color.default_mood_color))
        progress = a.getInt(R.styleable.MoodSeekBar_seekBarProgress, 0)
        moodsArray = a.getTextArray(R.styleable.MoodSeekBar_strings)
        titleTextQuestion = a.getString(R.styleable.MoodSeekBar_titleTextQuestion)
            ?: resources.getString(R.string.how_mood)
        titleTextMood = a.getString(R.styleable.MoodSeekBar_titleTextMood)
            ?: resources.getString(R.string.mood_title)
        startSeekbarText = a.getString(R.styleable.MoodSeekBar_startSeekbarText)
            ?: resources.getString(R.string.bad_mood)
        endSeekbarText = a.getString(R.styleable.MoodSeekBar_endSeekbarText)
            ?: resources.getString(R.string.awesome_mood)

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
        textIntoBarEnd = view.findViewById(R.id.textIntoBarEnd)
        textIntoBarStart = view.findViewById(R.id.textIntoBarStart)
        titleMood = view.findViewById(R.id.titleMood)
        floatingMoodText = view.findViewById(R.id.floatingMoodText)
        titleMood?.setTextColor(titleTextColor)
        mainMoodText.setTextColor(moodTextColor)
        moodSeekBar.progress = progress


        textIntoBarStart?.text = startSeekbarText
        textIntoBarEnd?.text = endSeekbarText
        titleMood?.text = titleTextQuestion

        popupView = inflater.inflate(R.layout.popup_view, null)
        popupView?.titleMood?.setTextColor(titleTextColor)
        popupView?.mainMoodText?.setTextColor(moodTextColor)
        popupView?.textIntoBarEnd?.text = endSeekbarText
        popupView?.textIntoBarStart?.text = startSeekbarText
        popupView?.titleMood?.text = titleTextQuestion

        root = view.rootView

        progressDrawable = createProgressDrawable(context)

        moodSeekBar.progressDrawable = progressDrawable
        popupView?.moodSeekBar?.progressDrawable = progressDrawable
        thumbDrawable = createThumbDrawable(context, 0)
        popupView?.moodSeekBar?.thumb = thumbDrawable
        popupView?.moodSeekBar?.thumbOffset = context.dpToPx(12).toInt()

        setupPopup()

        view.post {
            startY = view.y
        }

        if (moodSeekBar.progress == 0) {
            moodSeekBar.thumb.alpha = 0
        } else {
            setMood(false)
            popupView?.moodSeekBar?.progress = moodSeekBar.progress
            mainMoodText.visibility = View.VISIBLE
            val size = colorsArray?.size ?: 0
            val part = 100 / (size - 1)
            moodSeekBar.thumb = createThumbDrawable(context, colorsArray?.get(progress / part) ?: 0)
            moodSeekBar.thumbOffset = context.dpToPx(12).toInt()
            setMoodText(progress, false, false)
        }

        moodSeekBar.setOnTouchListener { view, motionEvent ->
            if (dialog.isShowing) {
                true
            }
            if (frameSeek.scaleX < 1) {
                barIsAnimating = true
                openPopup()
                true
            } else {
                false
            }
        }

        popupView?.moodSeekBar?.setOnTouchListener { view, motionEvent ->
            barIsAnimating
        }

        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar)

        dialog.addContentView(popupView!!, params)
        dialog.window?.attributes?.windowAnimations = R.style.Dialog_Animation


        popupView?.isFocusableInTouchMode = true
        popupView?.requestFocus()
        popupView?.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_DOWN) {
                setMood(true)
            }
            true
        }

        howMoodLabel.post {
            statusBarHeight = getStatusHeight()
        }

        popupView?.moodSeekBar?.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    setMoodText(progress, false, false)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    setMoodText(popupView?.moodSeekBar?.progress ?: 0, true, false)
                }
            })

        moodSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val size = colorsArray?.size ?: 0
                    val part = 100 / (size - 1)
                    popupView?.moodSeekBar?.progress = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (moodSeekBar.progress != 0) {
                        openPopup()
                    } else {
                        popupView?.mainMoodText?.visibility = View.VISIBLE
                        popupView?.titleMood?.visibility = View.VISIBLE
                        popupView?.mainMoodText?.alpha = 0f
                        popupView?.titleMood?.alpha = 0f
                        popupView?.setMoodButton?.visibility = View.VISIBLE
                        popupView?.cancelButton?.visibility = View.VISIBLE
                        if (moodSeekBar.thumb.alpha == 0) {
                            popupView?.setOnTouchListener(rootTouchListener())
                            moodSeekBar.thumb.alpha = 255
                            Blurry.with(context)
                                .radius(12)
                                .sampling(16)
                                .async()
                                .capture(rootScreenshot)
                                .into(popupView?.imageForBlur)
                            if (moodSeekBar.progress == 0) {
                                ring.paint.color = colorsArray?.get(0) ?: 0
                                mainMoodText.setTextColor(colorsArray?.get(0) ?: 0)
                                popupView?.mainMoodText?.setTextColor(colorsArray?.get(0) ?: 0)
                                mainMoodText.text = moodsArray?.get(0) ?: ""
                                popupView?.mainMoodText?.text = moodsArray?.get(0) ?: ""
                            }

                            howMoodLabel.post {
                                howMoodLabel.getLocationOnScreen(howMoodLabelLocation)
                                popupView?.moodSeekBar?.visibility = View.VISIBLE
                                setupPopup()
                                popupView?.howMoodLabel?.y = howMoodLabelLocation[1] - statusBarHeight
                                dialog.show()

                                moodSeekBar.thumb = thumbDrawable
                                moodSeekBar.thumbOffset = context.dpToPx(12).toInt()
                                popupView?.howMoodLabel?.post {
                                    popupView?.howMoodLabel?.mainMoodText?.post {
                                        popupView?.howMoodLabel?.mainMoodText?.y = context.dpToPx(34)

                                        popupView?.moodSeekBar?.progress = moodSeekBar.progress

                                        popupView?.howMoodLabel?.animate()?.y((screenHeight - statusBarHeight) / 2.toFloat() - howMoodLabel.height / 2)?.duration = DURATION

                                        Handler().postDelayed({
                                            popupView?.mainMoodText?.animate()?.alpha(1f)?.duration = 500
                                            popupView?.titleMood?.animate()?.alpha(1f)?.duration = 500
                                        }, DURATION)

                                        val locationPopup = IntArray(2)
                                        popupView?.howMoodLabel?.getLocationInWindow(locationPopup)

                                        popupView?.titleMood?.text = titleTextQuestion
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    setMoodText(popupView?.moodSeekBar?.progress ?: 0, true, false)
                }
            })

        popupView?.setMoodButton?.setOnClickListener {
            if (!barIsAnimating) {
                setMood(true)
            }
        }

        popupView?.cancelButton?.setOnClickListener {
            if (!barIsAnimating) {
                barIsAnimating = true
                howMoodLabel.visibility = View.VISIBLE
                titleMood?.text = titleTextQuestion
                val seekBarY = popupView?.moodSeekBar?.y ?: 0f
                frameSeek.scaleX = 1f
                frameSeek.scaleY = 1f
                frameSeek.x = moodSeekBar.x - context.dpToPx(8)
                frameSeek.y = seekBarY + moodSeekBar.height
                moodSeekBar.progress = 0
                popupView?.moodSeekBar?.progress = 0
                popupView?.moodSeekBar?.thumb?.alpha = 0
                moodSeekBar.thumb.alpha = 0
                popupView?.mainMoodText?.visibility = View.GONE
                mainMoodText.visibility = View.GONE
                popupView?.titleMood?.visibility = View.GONE
                howMoodLabel.getLocationOnScreen(howMoodLabelLocation)

                popupView?.howMoodLabel?.animate()?.y(howMoodLabelLocation[1] - statusBarHeight)?.duration = DURATION

                Handler().postDelayed({
                    popupView?.imageForBlur?.setImageBitmap(null)
                    dialog.dismiss()
                    barIsAnimating = false
                }, 350)
            }
        }
    }

    fun openPopup() {
        if (dialog.isShowing == true) {
            return
        }

        textIntoBarStart?.visibility = View.VISIBLE
        textIntoBarEnd?.visibility = View.VISIBLE
        popupView?.textIntoBarStart?.visibility = View.VISIBLE
        popupView?.textIntoBarEnd?.visibility = View.VISIBLE

        popupView?.mainMoodText?.visibility = View.VISIBLE
        popupView?.titleMood?.visibility = View.VISIBLE

        popupView?.mainMoodText?.alpha = 0f
        popupView?.titleMood?.alpha = 0f

        howMoodLabel.getLocationOnScreen(howMoodLabelLocation)
        popupView?.setOnTouchListener(null)
        popupView?.titleMood?.text = titleTextQuestion

        Blurry.with(context)
            .radius(12)
            .sampling(16)
            .async()
            .capture(rootScreenshot)
            .into(popupView?.imageForBlur)
        setupPopup()

        popupView?.howMoodLabel?.y = howMoodLabelLocation[1] - statusBarHeight

        popupView?.moodSeekBar?.progressDrawable = moodSeekBar.progressDrawable
        popupView?.moodSeekBar?.invalidate()
        dialog.show()
        popupView?.howMoodLabel?.post {
            popupView?.howMoodLabel?.mainMoodText?.post {
                val thumbLeft = thumbDrawable?.bounds?.left ?: 0
                val thumbRight = thumbDrawable?.bounds?.right ?: 0
                val thumbTop = thumbDrawable?.bounds?.top ?: 0
                val thumbBottom = thumbDrawable?.bounds?.bottom ?: 0

                if (popupView?.moodSeekBar?.thumb?.bounds?.top != 0) {
                    val thumbAnimator = ValueAnimator.ofInt(0, 30)
                    thumbAnimator.addUpdateListener {
                        val value = thumbAnimator.animatedValue as Int

                        val additional = if (popupView?.moodSeekBar?.progress == 0) {
                            -value / 10
                        } else if (popupView?.moodSeekBar?.progress == 100) {
                            value / 6
                        } else {
                            0
                        }
                        thumbDrawable?.setBounds(thumbLeft - value - additional, thumbTop - value, thumbRight + value - additional, thumbBottom + value)
                        popupView?.moodSeekBar?.invalidate()
                    }

                    thumbAnimator.duration = DURATION
                    thumbAnimator.start()

                    val progressLeft = progressDrawable?.bounds?.left ?: 0
                    val progressRight = progressDrawable?.bounds?.right ?: 0
                    val progressTop = progressDrawable?.bounds?.top ?: 0
                    val progressBottom = progressDrawable?.bounds?.bottom ?: 0

                    val progressAnimator = ValueAnimator.ofInt(0, 30)
                    progressAnimator.addUpdateListener {
                        val value = progressAnimator.animatedValue as Int
                        progressDrawable?.setBounds(progressLeft, progressTop - value, progressRight, progressBottom + value)
                        popupView?.moodSeekBar?.invalidate()
                    }
                    progressAnimator.duration = DURATION
                    progressAnimator.start()
                }


                popupView?.howMoodLabel?.animate()?.y((screenHeight - statusBarHeight) / 2.toFloat() - howMoodLabel.height / 2)?.duration = DURATION
                val seekBarY = popupView?.moodSeekBar?.y ?: 0f
                popupView?.frameSeek?.animate()?.scaleX(1f)?.scaleY(1f)?.y(seekBarY + moodSeekBar.height)
                    ?.x(moodSeekBar.x - context.dpToPx(8))?.duration = DURATION
                Handler().postDelayed({
                    howMoodLabel.visibility = View.GONE
                }, 100)

                Handler().postDelayed({
                    popupView?.mainMoodText?.animate()?.alpha(1f)?.duration = 500
                    popupView?.titleMood?.animate()?.alpha(1f)?.duration = 500
                }, DURATION)

                Handler().postDelayed({
                    popupView?.setOnTouchListener(rootTouchListener())
                    popupView?.setMoodButton?.visibility = View.VISIBLE
                    popupView?.cancelButton?.visibility = View.VISIBLE
                    barIsAnimating = false
                }, DURATION)
            }
        }
    }

    private fun setupPopup() {
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.marginStart = view.left
        layoutParams.marginEnd = view.left
        layoutParams.gravity = Gravity.TOP
        popupView?.howMoodLabel?.layoutParams = layoutParams
        popupView?.howMoodLabel?.y = howMoodLabelLocation[1] - statusBarHeight
    }

    private fun setMood(animate: Boolean) {
        barIsAnimating = true
        popupView?.cancelButton?.visibility = View.GONE
        popupView?.mainMoodText?.visibility = View.GONE
        popupView?.titleMood?.visibility = View.GONE

        howMoodLabel.visibility = View.VISIBLE
        popupView?.setMoodButton?.visibility = View.GONE
        mainMoodText.visibility = View.VISIBLE
        popupView?.titleMood?.text = titleTextMood
        titleMood?.text = titleTextMood

        textIntoBarStart?.visibility = View.GONE
        textIntoBarEnd?.visibility = View.GONE
        popupView?.textIntoBarStart?.visibility = View.GONE
        popupView?.textIntoBarEnd?.visibility = View.GONE

        frameSeek.post {
            if (!animate) {
                view.y = startY
                mainMoodText.x = titleMood?.x ?: 0f
                mainMoodText.textSize = 22f
                frameSeek.scaleX = 0.5f
                popupView?.frameSeek?.scaleX = 0.5f
                frameSeek.scaleY = 0.5f
                popupView?.frameSeek?.scaleY = 0.5f

                val thumbLeft = moodSeekBar.thumb?.bounds?.left ?: 0
                val thumbRight = moodSeekBar.thumb?.bounds?.right ?: 0
                val thumbTop = moodSeekBar.thumb?.bounds?.top ?: 0
                val thumbBottom = moodSeekBar.thumb?.bounds?.bottom ?: 0

                val value = 30
                val additional = if (moodSeekBar.progress == 0) {
                    -value / 10
                } else if (popupView?.moodSeekBar?.progress == 100) {
                    value / 6
                } else {
                    0
                }
                thumbDrawable?.setBounds(thumbLeft + value + additional, thumbTop + value, thumbRight - value + additional, thumbBottom - value)

                val progressLeft = progressDrawable?.bounds?.left ?: 0
                val progressRight = progressDrawable?.bounds?.right ?: 0
                val progressTop = progressDrawable?.bounds?.top ?: 0
                val progressBottom = progressDrawable?.bounds?.bottom ?: 0

                val progressValue = 30
                progressDrawable?.setBounds(progressLeft, progressTop + progressValue, progressRight, progressBottom - progressValue)
                popupView?.moodSeekBar?.post {
//                    popupView?.moodSeekBar?.progressDrawable?.setBounds(progressLeft, progressTop + progressValue, progressRight, progressBottom - progressValue)
//                    popupView?.moodSeekBar?.thumb?.setBounds(thumbLeft + value + additional, thumbTop + value, thumbRight - value + additional, thumbBottom - value)
                    popupView?.howMoodLabel?.mainMoodText?.y = context.dpToPx(34)
                    popupView?.moodSeekBar?.invalidate()
                }

                frameSeek.y = mainMoodText.y - context.dpToPx(12)
                popupView?.frameSeek?.y = mainMoodText.y - context.dpToPx(76)

                frameSeek.x = view.width.toFloat() / 4
                popupView?.frameSeek?.x = view.width.toFloat() / 4
                moodSeekBar.invalidate()

            } else {
                val thumbLeft = thumbDrawable?.bounds?.left ?: 0
                val thumbRight = thumbDrawable?.bounds?.right ?: 0
                val thumbTop = thumbDrawable?.bounds?.top ?: 0
                val thumbBottom = thumbDrawable?.bounds?.bottom ?: 0

                val thumbAnimator = ValueAnimator.ofInt(0, 30)
                thumbAnimator.addUpdateListener {
                    val value = thumbAnimator.animatedValue as Int
                    val additional = if (popupView?.moodSeekBar?.progress == 0) {
                        -value / 10
                    } else if (popupView?.moodSeekBar?.progress == 100) {
                        value / 6
                    } else {
                        0
                    }
                    thumbDrawable?.setBounds(thumbLeft + value + additional, thumbTop + value, thumbRight - value + additional, thumbBottom - value)
                    popupView?.moodSeekBar?.invalidate()
                }
                thumbAnimator.duration = DURATION
                thumbAnimator.start()

                val progressLeft = progressDrawable?.bounds?.left ?: 0
                val progressRight = progressDrawable?.bounds?.right ?: 0
                val progressTop = progressDrawable?.bounds?.top ?: 0
                val progressBottom = progressDrawable?.bounds?.bottom ?: 0

                val progressAnimator = ValueAnimator.ofInt(0, 30)
                progressAnimator.addUpdateListener {
                    val value = progressAnimator.animatedValue as Int
                    progressDrawable?.setBounds(progressLeft, progressTop + value, progressRight, progressBottom - value)
                    popupView?.moodSeekBar?.invalidate()
                }
                progressAnimator.duration = DURATION
                progressAnimator.start()

                view.animate().y(startY).duration = DURATION
                val translateAnimator = ValueAnimator.ofFloat(popupView?.mainMoodText?.x
                    ?: 0f, popupView?.titleMood?.x ?: 0f)

                popupView?.post {
                    translateAnimator.duration = DURATION
                    translateAnimator.addUpdateListener {
                        val value = translateAnimator.animatedValue as Float
                        mainMoodText.x = value
                    }

                    val sizeAnimator = ValueAnimator.ofFloat(16f, 22f)
                    sizeAnimator.duration = DURATION
                    sizeAnimator.addUpdateListener {
                        val value = sizeAnimator.animatedValue as Float
                        mainMoodText.textSize = value
                    }
                    val set = AnimatorSet()
                    set.playTogether(translateAnimator, sizeAnimator)
                    set.start()

                    frameSeek.animate().scaleX(0.5f).scaleY(0.5f)
                        .y(mainMoodText.y - context.dpToPx(12))
                        .x(view.width.toFloat() / 4).duration = 1

                    howMoodLabel.getLocationOnScreen(howMoodLabelLocation)

                    popupView?.howMoodLabel?.animate()?.y(howMoodLabelLocation[1] - statusBarHeight)?.duration = DURATION
                    popupView?.frameSeek?.animate()?.scaleX(0.5f)?.scaleY(0.5f)
                        ?.y(mainMoodText.y - context.dpToPx(12))
                        ?.x(view.width.toFloat() / 4)?.duration =
                        DURATION
                }
                popupView?.setOnTouchListener(null)
                setMoodButton.visibility = View.GONE


                Handler().postDelayed({
                    popupView?.imageForBlur?.setImageBitmap(null)
                    dialog.dismiss()
                }, 500)
            }
        }
    }


    private fun rootTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { v, event ->
            if (!barIsAnimating) {
                root.performClick()
                val seekBarPosition = IntArray(2)
                popupView?.frameSeek?.getLocationOnScreen(seekBarPosition)
                val seekBarWidth = popupView?.moodSeekBar?.width ?: 0
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
                        popupView?.moodSeekBar?.progress = progress
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
                        popupView?.moodSeekBar?.progress = progress
                    }
                    MotionEvent.ACTION_UP -> {
                        barIsAnimating = true
                        popupView?.setMoodButton?.visibility = View.GONE
                        popupView?.cancelButton?.visibility = View.GONE
                        popupView?.setOnTouchListener(null)
                        setMoodText(popupView?.moodSeekBar?.progress ?: 0, true, true)
                    }
                }

            }
            true
        }
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

    private fun getStatusHeight(): Float {
        val rectangle = Rect()
        val window = getActivity()?.window
        window?.decorView?.getWindowVisibleDisplayFrame(rectangle)
        val statusBarHeight = rectangle.top
        return statusBarHeight.toFloat()
    }


    private fun setMoodText(progress: Int, animate: Boolean, close: Boolean) {
        moodsArray?.let {
            var start = 0
            var end = 0
            var center = 0
            if (progress == 0) {
                mainMoodText.text = moodsArray?.get(0)
                popupView?.howMoodLabel?.mainMoodText?.text = moodsArray?.get(0)
            }
            moodsArray?.forEachIndexed { index, item ->
                val arraySize = moodsArray?.size ?: 0
                val chunk = 100 / (arraySize - 1)
                center = start + ((100 / (arraySize - 1)) / 2)
                end += if (start == 0) {
                    (chunk / 2) - 1
                } else {
                    100 / (arraySize - 1)
                }
                if (progress in start..end) {
                    mainMoodText.text = item
                    popupView?.howMoodLabel?.mainMoodText?.text = item
                    ring.paint.color = colorsArray?.get(index) ?: 0
                    popupView?.mainMoodText?.setTextColor(colorsArray?.get(index) ?: 0)
                    mainMoodText.setTextColor(colorsArray?.get(index) ?: 0)
                    val animator = if (start == 0) {
                        ValueAnimator.ofInt(progress, 0)
                    } else if (progress < 87) {
                        ValueAnimator.ofInt(progress, center)
                    } else {
                        ValueAnimator.ofInt(progress, 100)
                    }
                    if (animate && !animator.isRunning) {
                        animator.duration = 100
                        animator.addUpdateListener {
                            val value = animator.animatedValue as Int
                            popupView?.moodSeekBar?.progress = value
                            moodSeekBar.progress = value
                        }
                        animator.start()
                        animator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                if (close) {
                                    setMood(true)
                                }
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                        })
                    }

                }
                start = end + 1

            }
        }
    }

    private fun createProgressDrawable(context: Context): Drawable? {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, colorsArray)
        gd.cornerRadii = floatArrayOf(context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40))

        val roundRectShape = RoundRectShape(floatArrayOf(context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40), context.dpToPx(40)), null, null)
        val shape = ShapeDrawable(roundRectShape)
        shape.paint.style = Paint.Style.FILL
        shape.paint.color = ContextCompat.getColor(context, android.R.color.transparent)
        shape.paint.style = Paint.Style.STROKE
        shape.paint.strokeWidth = context.dpToPx(1)
        shape.paint.color = ContextCompat.getColor(context, R.color.bar_shape_color)
        val clipDrawable = ClipDrawable(gd, Gravity.START,
            ClipDrawable.HORIZONTAL)
        return LayerDrawable(arrayOf<Drawable>(
            clipDrawable, shape))
    }

    private fun createThumbDrawable(context: Context, color: Int): Drawable? {
        thumb.intrinsicWidth = context.dpToPx(60).toInt()
        thumb.intrinsicHeight = context.dpToPx(60).toInt()
        thumb.paint.style = Paint.Style.FILL
        thumb.paint.color = Color.WHITE
        thumb.setPadding(context.dpToPx(5).toInt(), context.dpToPx(5).toInt(), context.dpToPx(5).toInt(), context.dpToPx(5).toInt())

        ring.intrinsicWidth = context.dpToPx(10).toInt()
        ring.intrinsicHeight = context.dpToPx(10).toInt()
        ring.paint.style = Paint.Style.STROKE
        ring.paint.strokeWidth = context.dpToPx(2)
        ring.paint.color = color
        return LayerDrawable(arrayOf<Drawable>(
            thumb, ring))
    }

    private fun setColorToThumb(progress: Int) {
        popupView?.moodSeekBar?.post {
            if (progress > 15) {
                val barPos = IntArray(2)
                popupView?.moodSeekBar?.getLocationOnScreen(barPos)

                val left = popupView?.moodSeekBar?.thumb?.bounds?.left ?: 0

                val returnedBitmap = Bitmap.createBitmap(popupView?.width
                    ?: 0, popupView?.height ?: 0, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(returnedBitmap)
                val paint = Paint()
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                val height = popupView?.moodSeekBar?.height ?: 0

                popupView?.rootPopupView?.draw(canvas)
                val pixel = returnedBitmap.getPixel(left + barPos[0], barPos[1] + height / 2 - statusBarHeight.toInt())
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

//                popupWindow.contentView.setBackgroundColor(Color.rgb(r, g, b))
                ring.paint.color = Color.rgb(r, g, b)
                popupView?.moodSeekBar?.invalidate()

//                returnedBitmap.setPixel(left + barPos[0], barPos[1] + height / 2, Color.BLACK)
//                popupView?.testImage?.visibility = View.VISIBLE
//                popupView?.testImage?.setImageBitmap(returnedBitmap)
            } else {
                ring.paint.color = colorsArray?.get(0) ?: 0
                popupView?.moodSeekBar?.invalidate()
//                popupWindow.contentView.setBackgroundColor(colorsArray?.get(0) ?: 0)
            }
        }

    }

    companion object {
        private const val DURATION = 300L
    }
}