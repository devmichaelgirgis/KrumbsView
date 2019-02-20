package cafe.adriel.krumbsview.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.DrawableRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import cafe.adriel.krumbsview.R
import cafe.adriel.krumbsview.listener.OnSwipeRightListener
import cafe.adriel.krumbsview.model.Krumb
import cafe.adriel.krumbsview.model.KrumbsAnimationDuration
import cafe.adriel.krumbsview.model.KrumbsAnimationType
import cafe.adriel.krumbsview.util.color
import cafe.adriel.krumbsview.util.drawable
import cafe.adriel.krumbsview.util.forEach
import cafe.adriel.krumbsview.util.tintDrawable
import kotlinx.android.synthetic.main.view_krumbs.view.*
import java.util.*

open class KrumbsView(context: Context, attrs: AttributeSet? = null) : LinearLayoutCompat(context, attrs) {

    protected val items = ArrayDeque<Krumb>()
    protected var listener: (() -> Unit)? = null

    val size: Int
        get() = items.size

    init {
        val styleAttrs = context.theme.obtainStyledAttributes(attrs, R.styleable.KrumbsView, 0, 0)
        val startItem = styleAttrs.getString(R.styleable.KrumbsView_krumbsStartItem)
        val boldText = styleAttrs.getBoolean(R.styleable.KrumbsView_krumbsBoldText, true)
        val currentItemTextColor = styleAttrs.getColor(R.styleable.KrumbsView_krumbsCurrentItemTextColor, Color.WHITE)
        val previousItemTextColor = styleAttrs.getColor(
            R.styleable.KrumbsView_krumbsPreviousItemTextColor, color(
                R.color.transparent_white
            ))
        val separatorTintColor = styleAttrs.getColor(
            R.styleable.KrumbsView_krumbsSeparatorTintColor, color(
                R.color.transparent_white
            ))
        val separatorIconId = styleAttrs.getResourceId(
            R.styleable.KrumbsView_krumbsSeparatorIcon,
            R.drawable.ic_keyboard_arrow_right
        )
        val animationType = when(styleAttrs.getInt(R.styleable.KrumbsView_krumbsAnimationType, 1)){
            1 -> KrumbsAnimationType.SLIDE_LEFT_RIGHT
            2 -> KrumbsAnimationType.FADE_IN_OUT
            3 -> KrumbsAnimationType.GROW_SHRINK
            else -> KrumbsAnimationType.NONE
        }
        val animationDuration = when(styleAttrs.getInt(R.styleable.KrumbsView_krumbsAnimationDuration, 0)){
            1 -> KrumbsAnimationDuration.LONG
            else -> KrumbsAnimationDuration.SHORT
        }
        styleAttrs.recycle()

        val layoutInflater = ContextCompat.getSystemService(context, LayoutInflater::class.java)
        val view = layoutInflater?.inflate(R.layout.view_krumbs, this, true)
        view?.apply {
            setOnTouchListener(object : OnSwipeRightListener(context){
                override fun onSwipeRight() {
                    onPreviousItemClicked()
                }
            })

            vBreadcrumbCurrentItemSwitcher.setFactory {
                NonFocusableTextView(
                    ContextThemeWrapper(
                        context,
                        R.style.KrumbsStyle_CurrentItem
                    )
                )
            }
            vBreadcrumbPreviousItemSwitcher.setFactory {
                AppCompatTextView(ContextThemeWrapper(context, R.style.KrumbsStyle_PreviousItem)).apply {
                    setOnClickListener { onPreviousItemClicked() }
                }
            }

            if(!startItem.isNullOrBlank()){
                addItem(Krumb(startItem))
            }
            setBoltText(boldText)
            setCurrentItemTextColor(currentItemTextColor)
            setPreviousItemTextColor(previousItemTextColor)
            setSeparatorIcon(separatorIconId, separatorTintColor)
            setAnimationType(animationType)
            setAnimationDuration(animationDuration)
        }
    }

    protected fun onPreviousItemClicked(){
        removeLastItem()
        listener?.invoke()
    }

    protected fun updateState(){
        if(items.isEmpty()) {
            vBreadcrumbCurrentItemSwitcher.setCurrentText("")

            vBreadcrumbPreviousItemSwitcher.setCurrentText("")
            vBreadcrumbPreviousItemSwitcher.visibility = View.GONE

            vBreadcrumbSeparator.visibility = View.GONE
        } else {
            val currentItem = items.peek()
            if (items.size > 1) {
                vBreadcrumbCurrentItemSwitcher.setText(currentItem?.title)

                val previousItem = items.elementAt(1)
                vBreadcrumbPreviousItemSwitcher.setText(previousItem.title.takeLast(3))
                vBreadcrumbPreviousItemSwitcher.visibility = View.VISIBLE

                vBreadcrumbSeparator.visibility = View.VISIBLE
            } else {
                vBreadcrumbCurrentItemSwitcher.setCurrentText(currentItem?.title)

                vBreadcrumbPreviousItemSwitcher.setCurrentText("")
                vBreadcrumbPreviousItemSwitcher.visibility = View.GONE

                vBreadcrumbSeparator.visibility = View.GONE
            }
        }
    }

    fun setOnPreviousItemClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    fun getCurrentItem(): Krumb? = items.peek()

    fun addItem(item: Krumb) {
        items.push(item)
        updateState()
    }

    fun removeLastItem(){
        if(items.size > 1) items.pop()
        updateState()
    }

    fun removeAllItems() {
        items.clear()
        updateState()
    }

    fun goToFirstItem() {
        if(items.size > 1) {
            while (items.size != 1) items.pop()
            updateState()
        }
    }

    fun setBoltText(bold: Boolean){
        val style = if(bold) Typeface.BOLD else Typeface.NORMAL
        vBreadcrumbCurrentItemSwitcher.forEach {
            if(it is AppCompatTextView) {
                it.setTypeface(null, style)
            }
        }
        vBreadcrumbPreviousItemSwitcher.forEach {
            if(it is AppCompatTextView) {
                it.setTypeface(null, style)
            }
        }
    }

    fun setCurrentItemTextColor(color: Int){
        vBreadcrumbCurrentItemSwitcher.forEach {
            if(it is AppCompatTextView) {
                it.setTextColor(color)
            }
        }
    }

    fun setPreviousItemTextColor(color: Int){
        vBreadcrumbPreviousItemSwitcher.forEach {
            if(it is AppCompatTextView) {
                it.setTextColor(color)
            }
        }
    }

    fun setSeparatorTintColor(color: Int){
        vBreadcrumbSeparator.tintDrawable(color)
    }

    fun setSeparatorIcon(@DrawableRes drawableRes: Int, color: Int? = null){
        drawable(drawableRes)?.let {
            setSeparatorIcon(it, color)
        }
    }

    fun setSeparatorIcon(drawable: Drawable, color: Int? = null){
        vBreadcrumbSeparator.setImageDrawable(drawable)
        color?.let { setSeparatorTintColor(it) }
    }

    fun setAnimationType(type: KrumbsAnimationType){
        val (inAnim, outAnim) = when(type) {
            KrumbsAnimationType.SLIDE_LEFT_RIGHT -> listOf(
                AnimationUtils.loadAnimation(context, R.anim.slide_in_left),
                AnimationUtils.loadAnimation(context, R.anim.slide_out_right))
            KrumbsAnimationType.FADE_IN_OUT -> listOf(
                AnimationUtils.loadAnimation(context, R.anim.fade_in),
                AnimationUtils.loadAnimation(context, R.anim.fade_out))
            KrumbsAnimationType.GROW_SHRINK -> listOf(
                AnimationUtils.loadAnimation(context, R.anim.grow),
                AnimationUtils.loadAnimation(context, R.anim.shrink))
            else -> listOf(null, null)
        }

        with(vBreadcrumbCurrentItemSwitcher){
            inAnimation = inAnim
            outAnimation = outAnim
        }

        with(vBreadcrumbPreviousItemSwitcher){
            inAnimation = inAnim
            outAnimation = outAnim
        }
    }

    fun setAnimationDuration(duration: KrumbsAnimationDuration){
        with(vBreadcrumbCurrentItemSwitcher){
            inAnimation?.duration = duration.duration
            outAnimation?.duration = duration.duration
        }

        with(vBreadcrumbPreviousItemSwitcher){
            inAnimation?.duration = duration.duration
            outAnimation?.duration = duration.duration
        }
    }

}