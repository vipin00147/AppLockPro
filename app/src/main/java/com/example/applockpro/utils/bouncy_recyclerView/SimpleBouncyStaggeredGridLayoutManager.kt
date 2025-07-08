package com.example.applockpro.utils.bouncy_recyclerView // Or your desired package

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.applockpro.R
import kotlin.math.abs

// This is the anim duration time to bounce back and it multiplied by the strength
private const val _animDuration: Int = 300

internal class SimpleBouncyStaggeredGridLayoutManager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtr: Int = 0,
    defStyleRes: Int = 0,
    // StaggeredGridLayoutManager specific parameters
    private var spanCount: Int = 2,
    orientation: Int = RecyclerView.VERTICAL
) : StaggeredGridLayoutManager(spanCount, orientation) {

    // This is the max overscroll amount which is based on the screen size and multiplied by the tension
    private var _maxOverscroll: Double = 0.0

    private var _currentState: BouncyState = BouncyState.UP

    private var _bounceInterpolator: TimeInterpolator = DecelerateInterpolator()

    private var _bounceBackAnimator: ValueAnimator? = null

    private var _overscrollTotal: Double = 0.0
    val overscrollTotal: Double
        get() = _overscrollTotal

    // Offsets might behave differently or be less relevant in StaggeredGrid.
    // Consider if you still need them or how they should apply.
    private var _startIndexOffset: Int = 0
    var startIndexOffset: Int = _startIndexOffset

    private var _endIndexOffset: Int = 0
    var endIndexOffset: Int = _endIndexOffset

    private var _friction: Float = 1.0f
    var friction: Float = _friction

    private var _tension: Float = 1.0f
    var tension: Float = _tension
    val isVertical: Boolean
        get() = orientation == RecyclerView.VERTICAL

    private var onOverscrollEvents: MutableList<OverscrollEvent> = mutableListOf()

    init {
        val displayMetrics = DisplayMetrics()
        val windowManager: WindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        _maxOverscroll = if (isVertical) displayMetrics.heightPixels.toDouble() / 4
        else displayMetrics.widthPixels.toDouble() / 3

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.bouncy_scroller)

            _friction = a.getFloat(R.styleable.bouncy_scroller_friction, _friction)
            _tension = a.getFloat(R.styleable.bouncy_scroller_tension, _tension)
            _startIndexOffset = a.getInt(R.styleable.bouncy_scroller_startIndexOffset, _startIndexOffset)
            _endIndexOffset = a.getInt(R.styleable.bouncy_scroller_endIndexOffset, _endIndexOffset)
            spanCount = a.getInt(R.styleable.bouncy_scroller_grid_span_count, 2)
            // You might want to read spanCount and orientation from attributes as well
            // spanCount = a.getInt(R.styleable.bouncy_scroller_spanCount, spanCount)
            // val layoutOrientation = a.getInt(R.styleable.bouncy_scroller_android_orientation, orientation)
            // if (layoutOrientation != orientation) {
            //    this.orientation = layoutOrientation
            // }

            a.recycle()
        }
    }

    override fun supportsPredictiveItemAnimations(): Boolean = false

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return handleScroll(dy, recycler, state)
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return handleScroll(dx, recycler, state)
    }

    fun setState(state: BouncyState) {
        if (_currentState == BouncyState.DOWN && state == BouncyState.UP) {
            bounceBack()
        } else if (_currentState == BouncyState.UP && state == BouncyState.DOWN) {
            clearAnimations()
        }
        _currentState = state
    }

    fun registerOnOverscrollEvent(event: OverscrollEvent) {
        if (!onOverscrollEvents.contains(event)) {
            onOverscrollEvents.add(event)
        }
    }

    fun unregisterOnOverscrollEvent(event: OverscrollEvent) {
        onOverscrollEvents.remove(event)
    }

    private fun handleScroll(
        delta: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        var toScroll = delta

        if (abs(_overscrollTotal) > 0 &&
            ((toScroll > 0 && _overscrollTotal < 0) || (toScroll < 0 && _overscrollTotal > 0))
        ) {
            if (abs(_overscrollTotal) >= abs(toScroll)) {
                //updateOverscroll(toScroll.toDouble())
                return 0 // Consume the scroll
            } else {
                toScroll -= _overscrollTotal.toInt()
                resetOverscrollState() // Resetting overscroll, not all children translations yet
            }
        }

        val scrollRange: Int = if (isVertical) {
            super.scrollVerticallyBy(toScroll, recycler, state)
        } else {
            super.scrollHorizontallyBy(toScroll, recycler, state)
        }

        val overscroll = toScroll - scrollRange
        if (overscroll != 0) { // Only apply overscroll if there was actual overscrolling
            var dampen = if (_currentState == BouncyState.UP) 1.25 else 1.0
            dampen -= abs(_overscrollTotal) / (_maxOverscroll * (1.0 / _friction))
            //updateOverscroll(overscroll * dampen)
        }

        return scrollRange
    }

    private fun updateOverscroll(overscroll: Double) {
        if (abs(overscroll) < Double.MIN_VALUE && _overscrollTotal == 0.0) {
             // Avoid unnecessary calculations if overscroll is negligible and we're not already overscrolled
            return
        }

        _overscrollTotal += overscroll

        // Clamp overscroll to avoid excessive values, though friction should handle this
        // _overscrollTotal = _overscrollTotal.coerceIn(-_maxOverscroll * 2, _maxOverscroll * 2)


        translateCells(false)

        if (_currentState == BouncyState.UP && abs(_overscrollTotal) > 0) {
            bounceBack()
        }
    }

    private fun translateCells(animating: Boolean) {
        // For StaggeredGridLayout, translating all children uniformly is the simplest approach.
        // More complex behavior (e.g., parallax based on span index) would require more logic.
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            view?.let {
                if (isVertical) {
                    it.translationY = -_overscrollTotal.toFloat()
                } else {
                    it.translationX = -_overscrollTotal.toFloat()
                }
            }
        }

        for (event in onOverscrollEvents) {
            event(animating)
        }
    }


    private fun bounceBack() {
        clearAnimations()

        _bounceBackAnimator = ValueAnimator.ofFloat(_overscrollTotal.toFloat(), 0f).apply {
            interpolator = _bounceInterpolator
            this.duration = (_animDuration * (1.0f / _tension)).toLong()
            addUpdateListener { animation ->
                _overscrollTotal = (animation.animatedValue as Float).toDouble()
                translateCells(true)
            }
            addListener(
                onEnd = { bounceBackEnded() },
                onCancel = { bounceBackEnded() }
            )
            start()
        }
    }

    private fun bounceBackEnded() {
        resetOverscrollVisuals()
    }

    // Resets only the overscroll amount, used when scroll direction changes during overscroll
    private fun resetOverscrollState() {
        _overscrollTotal = 0.0
        // We don't necessarily reset translations here, as the normal scroll will continue
    }


    // Resets visuals and animation state fully
    private fun resetOverscrollVisuals() {
        _overscrollTotal = 0.0
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            view?.let {
                if (isVertical) {
                    it.translationY = 0f
                } else {
                    it.translationX = 0f
                }
            }
        }
        clearAnimations()
    }

    private fun clearAnimations() {
        _bounceBackAnimator?.let {
            it.removeAllUpdateListeners()
            it.removeAllListeners()
            it.cancel()
        }
        _bounceBackAnimator = null
    }

    // You might not need these specific overscrollStart/End methods if all children are translated uniformly
    // private fun overcrollStart() {
    //     for ( i in _startIndexOffset until childCount) {
    //         translateCell(i)
    //     }
    // }

    // private fun overscrollEnd() {
    //     for ( i in (childCount - _endIndexOffset - 1) downTo 0) {
    //         translateCell(i)
    //     }
    // }

    // safeGetChildAt is part of LayoutManager, no need to redefine unless custom logic is needed.
}