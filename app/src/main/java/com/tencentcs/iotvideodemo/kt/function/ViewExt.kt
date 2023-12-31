package com.tencentcs.iotvideodemo.kt.function

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.tencentcs.iotvideodemo.kt.utils.StringUtils
import com.tencentcs.iotvideodemo.kt.utils.ViewUtils

fun getResDrawable(resId: Int): Drawable? {
    return try {
        ViewUtils.getDrawable(resId)
    } catch (e: Exception) {
        null
    }
}

fun View?.toggleVisible() {
    this?.apply {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
}

fun View.click(click: (View) -> Unit) {
    this.setOnClickListener(click)
}

fun doClick(click: (View) -> Unit, vararg views: View) {
    for (view in views) {
        view.click {
            click(it)
        }
    }
}

//View根据高计算宽
fun View.computeWidthWithH(ratio: Float) {
    post {
        val params = this.layoutParams
        params.width = (this.height * ratio).toInt()
        layoutParams = params
    }
}

//根据屏幕宽来计算宽
fun View.computeWidthWithScreenW(ratio: Float) {
    post {
        val params = this.layoutParams
        params.width = (ViewUtils.screenWidth * ratio).toInt()
        layoutParams = params
    }
}

//View根据宽计算高
fun View.computeHeightWithW(ratio: Float) {
    this.post {
        val params = this.layoutParams
        params.height = (this.width * ratio).toInt()
        this.layoutParams = params
    }
}

fun View.setHeightWithW(wRatio: Float, hRatio: Float) {
    val params = layoutParams
    if (params is ConstraintLayout.LayoutParams) {
        params.dimensionRatio = "W : $wRatio:$hRatio"
        layoutParams = params
    }
}

fun View.setWidthWithH(wRatio: Float, hRatio: Float) {
    val params = layoutParams
    if (params is ConstraintLayout.LayoutParams) {
        params.dimensionRatio = "H : $wRatio:$hRatio"
        layoutParams = params
    }
}

fun View.updateWH(width: Int = layoutParams.width, height: Int = layoutParams.height) {
    val params = layoutParams
    params.width = width
    params.height = height
    layoutParams = params
}

/*
* View设置以高度为准的圆角矩形
* */
fun View.roundHeight(solidColor: Int = Color.WHITE, strokeW: Float = 0f, strokeColor: Int = Color.TRANSPARENT) {
    post {
        roundInternal(height * 0.5f, solidColor, strokeW, strokeColor)
    }
}

/**
 * 四个角，r的size必须为4
 * */
fun View.roundArray(r: FloatArray, solidColor: Int = Color.WHITE, strokeW: Float = 0f, strokeColor: Int = Color.TRANSPARENT) {
    roundInternalArray(r.map {
        ViewUtils.dip2px(it).toFloat()
    }.toFloatArray(), solidColor, strokeW, strokeColor)
}

fun View.round(r: Float = 2f, solidColor: Int = Color.WHITE, strokeW: Float = 0f, strokeColor: Int = Color.TRANSPARENT) {
    roundInternal(ViewUtils.dip2px(r).toFloat(), solidColor, strokeW, strokeColor)
}

fun View.roundInternal(r: Float = ViewUtils.dip2px(2f).toFloat(), solidColor: Int = Color.WHITE, strokeW: Float = 0f, strokeColor: Int = Color.TRANSPARENT) {
    val roundDrawable = RoundDrawable(r, solidColor, strokeW, strokeColor)
    this.background = roundDrawable.build()
    //避免子View影响到背景
    this.clipToOutline = true
}

fun View.roundInternalArray(r: FloatArray, solidColor: Int = Color.WHITE, strokeW: Float = 0f, strokeColor: Int = Color.TRANSPARENT) {
    val roundDrawable = RoundDrawable(r, solidColor, strokeW, strokeColor)
    this.background = roundDrawable.build()
    //避免子View影响到背景
    this.clipToOutline = true
}

class RoundDrawable(private val rArray: FloatArray,
                    private var solidColor: Int = Color.WHITE,
                    private val strokeW: Float = 0f,
                    private val strokeColor: Int = Color.TRANSPARENT) {

    constructor(r: Float = ViewUtils.dip2px(2f).toFloat(),
                solidColor: Int = Color.WHITE, strokeW: Float = 0f,
                strokeColor: Int = Color.TRANSPARENT) : this(floatArrayOf(r, r, r, r), solidColor,
            strokeW, strokeColor)

    init {
        if (rArray.size != 4) {
            throw IllegalArgumentException("round corner size must is 4!!!")
        }
    }

    fun build(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.mutate()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.setColor(this.solidColor)
        gradientDrawable.setStroke(ViewUtils.dip2px(strokeW), strokeColor)
        if (rArray[0] == rArray[1] && rArray[2] == rArray[3] && rArray[1] == rArray[2]) {
            gradientDrawable.cornerRadius = rArray[0]
        } else {
            gradientDrawable.cornerRadii = floatArrayOf(
                    rArray[0], rArray[0],
                    rArray[1], rArray[1],
                    rArray[2], rArray[2],
                    rArray[3], rArray[3]
            )
        }
        return gradientDrawable
    }
}

fun pressColorAll(normalColor: Int, pressColor: Int, vararg views: View) {
    if (views.isEmpty()) {
        return
    }
    views.forEach {
        it.pressColor(normalColor, pressColor)
    }
}

fun View.press(@DrawableRes normalRes: Int, @DrawableRes pressRes: Int) {
    this.background = getPressDrawable(normalRes, pressRes)
}

fun View.press(normal: Drawable, press: Drawable) {
    this.background = getPressDrawable(normal, press)
}

fun View.pressColor(normalColor: Int, pressColor: Int) {
    this.background = getPressDrawable(ColorDrawable(normalColor), ColorDrawable(pressColor))
}

//-----checkBox之类的设置button的drawable
fun CompoundButton.checkedButton(normal: Drawable, press: Drawable, drawableHandle: (StateListDrawable) -> Unit = {}) {
    val checkedDrawable = getCheckedDrawable(normal, press)
    drawableHandle.invoke(checkedDrawable)
    this.buttonDrawable = checkedDrawable
}

fun CompoundButton.checkedButton(@DrawableRes normalRes: Int, @DrawableRes pressRes: Int,
                                 drawableHandle: (StateListDrawable) -> Unit = {}) {
    val checkedDrawable = getCheckedDrawable(normalRes, pressRes)
    drawableHandle.invoke(checkedDrawable)
    this.buttonDrawable = checkedDrawable
}

fun CompoundButton.checkedColorButton(normalColor: Int, checkedColor: Int,
                                      drawableHandle: (StateListDrawable) -> Unit = {}) {
    val checkedDrawable = getCheckedDrawable(ColorDrawable(normalColor), ColorDrawable(checkedColor))
    drawableHandle.invoke(checkedDrawable)
    this.buttonDrawable = checkedDrawable
}

fun View.checked(@DrawableRes normalRes: Int, @DrawableRes pressRes: Int) {
    this.background = getCheckedDrawable(normalRes, pressRes)
}

fun View.checked(normal: Drawable, press: Drawable, drawableHandle: (StateListDrawable) -> Unit = {}) {
    val checkedDrawable = getCheckedDrawable(normal, press)
    drawableHandle.invoke(checkedDrawable)
    this.background = checkedDrawable
}

fun View.checkedColor(normalColor: Int, checkedColor: Int) {
    this.background = getCheckedDrawable(ColorDrawable(normalColor), ColorDrawable(checkedColor))
}

fun View.selected(@DrawableRes normalRes: Int, @DrawableRes pressRes: Int) {
    this.background = getSelectedDrawable(normalRes, pressRes)
}

fun View.selected(normal: Drawable, press: Drawable) {
    this.background = getSelectedDrawable(normal, press)
}

fun View.selectedColor(normalColor: Int, checkedColor: Int) {
    this.background = getSelectedDrawable(ColorDrawable(normalColor), ColorDrawable(checkedColor))
}

fun View.event(click: ((View) -> Unit)? = null, doubleTap: ((MotionEvent?) -> Unit)? = null,
               longPress: ((MotionEvent?) -> Unit)? = null, onTouch: ((MotionEvent) -> Unit)? = null) {
    this.isLongClickable = true
    val gestureDetector = GestureDetector(this.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            Log.d("event", "onSingleTapConfirmed ")
            click?.apply {
                invoke(this@event)
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            Log.d("event", "onDoubleTap ")
            doubleTap?.apply {
                invoke(e)
            }
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)
            Log.d("event", "onLongPress ")
            longPress?.apply { invoke(e) }
        }
    })

    this.setOnTouchListener { v, event ->
        onTouch?.invoke(event)
        gestureDetector.onTouchEvent(event)
    }
}

fun ViewGroup.clipChild(isClip: Boolean) {
    clipToPadding = isClip
    clipChildren = false
}

fun View.hideSoftKeyboard() {
    val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun TabLayout.addOtherIndicator(viewPager: ViewPager, view: View) {
    post {
        val tabW = width.toFloat() / tabCount
        view.updateWH(tabW.toInt())
        if (view.layoutParams.width == 0) {
            return@post
        }
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.d("other", "pos: $position offset: $positionOffset pixels: $positionOffsetPixels")
                view.translationX = position * tabW + (tabW * positionOffset)
            }
        })
    }
}


/**
 * @param text      测量的文字
 * @param textPaint 绘制文字的画笔
 * @return 特定画笔画出的文字的宽高
 */
fun measureText(text: String, textPaint: TextPaint): IntArray {
    val ints = IntArray(2)
    if (StringUtils.isBlank(text)) {
        ints[0] = 0
        ints[1] = 0
        return ints
    }
    val arr = text.trim { it <= ' ' }.split("\n".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
    var maxLength = 0f
    for (s in arr) {
        maxLength = Math.max(maxLength, textPaint.measureText(s))
    }
    val width = (maxLength + 2.5f).toInt()
    val staticLayout = StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL,
            1f, 0f, true)
    ints[0] = width
    ints[1] = staticLayout.height
    return ints
}

/**
 * 测量大小
 */
fun measureText(text: String, width: Int, textPaint: TextPaint): IntArray {
    val ints = IntArray(2)
    if (StringUtils.isBlank(text)) {
        ints[0] = 0
        ints[1] = 0
        return ints
    }
    val staticLayout = StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
    ints[0] = width
    ints[1] = staticLayout.height
    return ints
}