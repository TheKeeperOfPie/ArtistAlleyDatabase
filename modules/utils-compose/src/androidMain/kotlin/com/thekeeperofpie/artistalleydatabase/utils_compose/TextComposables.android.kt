package com.thekeeperofpie.artistalleydatabase.utils_compose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Build
import android.os.SystemClock
import android.text.Html
import android.text.NoCopySpan
import android.text.Selection
import android.text.Spannable
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.BuildCompat
import androidx.core.text.HtmlCompat
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asDrawable
import coil3.request.ImageRequest
import de.charlex.compose.material3.toAnnotatedString

@Composable
actual fun CustomHtmlText(
    text: String,
    modifier: Modifier,
    urlSpanStyle: SpanStyle,
    colorMapping: Map<Color, Color>,
    color: Color,
    fontSize: TextUnit,
    fontStyle: FontStyle?,
    fontWeight: FontWeight?,
    fontFamily: FontFamily?,
    letterSpacing: TextUnit,
    textDecoration: TextDecoration?,
    textAlign: TextAlign?,
    lineHeight: TextUnit,
    overflow: TextOverflow,
    softWrap: Boolean,
    minLines: Int,
    maxLines: Int,
    inlineContent: Map<String, InlineTextContent>,
    onTextLayout: (TextLayoutResult) -> Unit,
    style: TextStyle,
    onFallbackClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    detectTaps: Boolean,
) {
    val annotatedString = remember(text) {
        Html.fromHtml(text.trim(), Html.FROM_HTML_MODE_LEGACY)
            .trim()
            .toAnnotatedString(urlSpanStyle, colorMapping)
            .let {
                if (minLines > 1 && maxLines < Int.MAX_VALUE) {
                    buildAnnotatedString {
                        append(it)
                        repeat(minLines - 1) {
                            append("\n")
                        }
                    }
                } else it
            }
    }

    HtmlText(
        modifier = modifier,
        annotatedString = annotatedString,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        minLines = minLines,
        maxLines = maxLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style,
        onFallbackClick = onFallbackClick,
        onLongClick = onLongClick,
        detectTaps = detectTaps,
    )
}

@Composable
actual fun ImageHtmlText(
    text: String,
    color: Color,
    modifier: Modifier,
    maxLines: Int?,
    onClickFallback: () -> Unit,
) {
    val context = LocalContext.current
    var updateMillis by remember { mutableLongStateOf(-1L) }
    val screenWidth =
        LocalDensity.current.run { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    val imageGetter = remember {
        ImageGetterWrapper(CoilImageGetter(context, maxWidth = screenWidth) {
            updateMillis = SystemClock.uptimeMillis()
        })
    }
    DisposableEffect(context) {
        onDispose { imageGetter.delegate = null }
    }

    val htmlText = remember(text, updateMillis) {
        HtmlCompat.fromHtml(text.trim(), 0, imageGetter, null)
            .trim()
    }

    val textColor = color.takeOrElse { LocalContentColor.current }.toArgb()
    val linkMovementMethod = remember { LinkMovementMethodWithOnClick(onClickFallback) }
    AndroidView(
        modifier = modifier.onSizeChanged {
            (imageGetter.delegate as? CoilImageGetter)?.maxWidth = it.width
        },
        factory = {
            TextView(it).apply {
                if (maxLines != null) {
                    this.maxLines = maxLines
                }
                setTextColor(textColor)
                ellipsize = TextUtils.TruncateAt.END
                movementMethod = linkMovementMethod
            }
        },
        update = { it.text = htmlText },
    )
}

private class CoilImageGetter(
    private val context: Context,
    var maxWidth: Int,
    private val onUpdate: (String) -> Unit,
) : Html.ImageGetter {

    private val loader = SingletonImageLoader.get(context)

    private val sourceToDrawable = mutableMapOf<String, Drawable>()

    private val brokenDrawable = context.getDrawable(R.drawable.baseline_broken_image_24)

    @OptIn(ExperimentalCoilApi::class)
    override fun getDrawable(source: String?): Drawable {
        if (source == null) return CustomDrawableWrapper.EMPTY
        val drawable = sourceToDrawable[source]
        if (drawable != null) {
            return drawable
        }
        val wrapper = CustomDrawableWrapper()

        loader.enqueue(
            ImageRequest.Builder(context)
                .data(source)
                .listener(
                    onSuccess = { request, result ->
                        val newDrawable = result.image.asDrawable(context.resources).apply {
                            val widthToHeightRatio = intrinsicHeight / intrinsicWidth
                            val width = intrinsicWidth.coerceAtMost(maxWidth)
                            setBounds(0, 0, width, width * widthToHeightRatio)
                        }
                        sourceToDrawable[source] = newDrawable
                        wrapper.delegate = newDrawable
                        onUpdate(source)
                    },
                    onError = { _, _ -> wrapper.delegate = brokenDrawable },
                )
                .build()
        )

        return wrapper
    }
}

private class CustomDrawableWrapper : DrawableWrapper(null) {

    companion object {
        val EMPTY = CustomDrawableWrapper()
    }

    var delegate: Drawable? = null
        set(value) {
            field = value ?: return
            val width = value.intrinsicWidth
            val height = value.intrinsicHeight
            value.setBounds(0, 0, width, height)
            setBounds(0, 0, width, height)
        }

    override fun draw(canvas: Canvas) {
        delegate?.draw(canvas)
    }
}

private class ImageGetterWrapper(var delegate: Html.ImageGetter? = null) : Html.ImageGetter {
    override fun getDrawable(source: String?) =
        source?.let { delegate?.getDrawable(source) } ?: CustomDrawableWrapper.EMPTY
}

private class LinkMovementMethodWithOnClick(private val onClickFallback: () -> Unit) :
    ScrollingMovementMethod() {
    override fun canSelectArbitrarily(): Boolean {
        return true
    }

    override fun handleMovementKey(
        widget: TextView, buffer: Spannable, keyCode: Int,
        movementMetaState: Int, event: KeyEvent,
    ): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> if (KeyEvent.metaStateHasNoModifiers(
                    movementMetaState
                )
            ) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 && action(
                        CLICK,
                        widget,
                        buffer
                    )
                ) {
                    return true
                }
            }
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event)
    }

    override fun up(widget: TextView, buffer: Spannable): Boolean {
        return if (action(UP, widget, buffer)) true else super.up(widget, buffer)
    }

    override fun down(widget: TextView, buffer: Spannable): Boolean {
        return if (action(DOWN, widget, buffer)) true else super.down(widget, buffer)
    }

    override fun left(widget: TextView, buffer: Spannable): Boolean {
        return if (action(UP, widget, buffer)) true else super.left(widget, buffer)
    }

    override fun right(widget: TextView, buffer: Spannable): Boolean {
        return if (action(DOWN, widget, buffer)) true else super.right(widget, buffer)
    }

    private fun action(what: Int, widget: TextView, buffer: Spannable): Boolean {
        val layout = widget.layout
        val padding = widget.totalPaddingTop +
                widget.totalPaddingBottom
        val areaTop = widget.scrollY
        val areaBot = areaTop + widget.height - padding
        val lineTop = layout.getLineForVertical(areaTop)
        val lineBot = layout.getLineForVertical(areaBot)
        val first = layout.getLineStart(lineTop)
        val last = layout.getLineEnd(lineBot)
        val candidates = buffer.getSpans(
            first, last,
            ClickableSpan::class.java
        )
        val a = Selection.getSelectionStart(buffer)
        val b = Selection.getSelectionEnd(buffer)
        var selStart = Math.min(a, b)
        var selEnd = Math.max(a, b)
        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selEnd = buffer.length
                selStart = selEnd
            }
        }
        if (selStart > last) {
            selEnd = Int.MAX_VALUE
            selStart = selEnd
        }
        if (selEnd < first) {
            selEnd = -1
            selStart = selEnd
        }
        var bestStart: Int
        var bestEnd: Int
        when (what) {
            CLICK -> {
                if (selStart == selEnd) {
                    return false
                }
                val links = buffer.getSpans(
                    selStart, selEnd,
                    ClickableSpan::class.java
                )
                if (links.size != 1) {
                    return false
                }
                val link = links[0]
//                if (link is TextLinkSpan) {
//                    link.onClick(widget, TextLinkSpan.INVOCATION_METHOD_KEYBOARD)
//                } else {
                link.onClick(widget)
//                }
            }
            UP -> {
                bestStart = -1
                bestEnd = -1
                var i = 0
                while (i < candidates.size) {
                    val end = buffer.getSpanEnd(candidates[i])
                    if (end < selEnd || selStart == selEnd) {
                        if (end > bestEnd) {
                            bestStart = buffer.getSpanStart(candidates[i])
                            bestEnd = end
                        }
                    }
                    i++
                }
                if (bestStart >= 0) {
                    Selection.setSelection(buffer, bestEnd, bestStart)
                    return true
                }
            }
            DOWN -> {
                bestStart = Int.MAX_VALUE
                bestEnd = Int.MAX_VALUE
                var i = 0
                while (i < candidates.size) {
                    val start = buffer.getSpanStart(candidates[i])
                    if (start > selStart || selStart == selEnd) {
                        if (start < bestStart) {
                            bestStart = start
                            bestEnd = buffer.getSpanEnd(candidates[i])
                        }
                    }
                    i++
                }
                if (bestEnd < Int.MAX_VALUE) {
                    Selection.setSelection(buffer, bestStart, bestEnd)
                    return true
                }
            }
        }
        return false
    }

    @BuildCompat.PrereleaseSdkCheck
    @OptIn(BuildCompat.PrereleaseSdkCheck::class)
    @SuppressLint("UnsafeOptInUsageError")
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        if (!BuildCompat.isAtLeastV()) {
            val action = event.action
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop
                x += widget.scrollX
                y += widget.scrollY
                val layout = widget.layout
                val isOutOfLineBounds: Boolean = if (y < 0 || y > layout.height) {
                    true
                } else {
                    val line = layout.getLineForVertical(y)
                    (x < layout.getLineLeft(line)
                            || x > layout.getLineRight(line))
                }
                if (isOutOfLineBounds) {
                    Selection.removeSelection(buffer)
                    if (action == MotionEvent.ACTION_UP) {
                        onClickFallback()
                    }

                    // The same as super.onTouchEvent() in LinkMovementMethod.onTouchEvent(), i.e.
                    // ScrollingMovementMethod.onTouchEvent().
                    return Touch.onTouchEvent(widget, buffer, event)
                }
            }
        }
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val links = buffer.getSpans(
                off, off,
                ClickableSpan::class.java
            )
            if (links.isNotEmpty()) {
                val link = links[0]
                if (action == MotionEvent.ACTION_UP) {
//                    if (link is TextLinkSpan) {
//                        link.onClick(
//                            widget, TextLinkSpan.INVOCATION_METHOD_TOUCH
//                        )
//                    } else {
                    link.onClick(widget)
//                    }
                } else {
                    if (widget.context.applicationInfo.targetSdkVersion
                        >= Build.VERSION_CODES.P
                    ) {
                        // Selection change will reposition the toolbar. Hide it for a few ms for a
                        // smoother transition.
//                        widget.hideFloatingToolbar(HIDE_FLOATING_TOOLBAR_DELAY_MS)
                    }
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link),
                        buffer.getSpanEnd(link)
                    )
                }
                return true
            } else {
                Selection.removeSelection(buffer)
                if (action == MotionEvent.ACTION_UP) {
                    onClickFallback()
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.removeSelection(text)
        text.removeSpan(FROM_BELOW)
    }

    override fun onTakeFocus(view: TextView, text: Spannable, dir: Int) {
        Selection.removeSelection(text)
        if (dir and View.FOCUS_BACKWARD != 0) {
            text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT)
        } else {
            text.removeSpan(FROM_BELOW)
        }
    }

    companion object {
        private const val CLICK = 1
        private const val UP = 2
        private const val DOWN = 3

        //        private const val HIDE_FLOATING_TOOLBAR_DELAY_MS = 200
        private val FROM_BELOW: Any = NoCopySpan.Concrete()
    }
}
