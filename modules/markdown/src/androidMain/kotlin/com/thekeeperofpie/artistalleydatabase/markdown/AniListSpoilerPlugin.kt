package com.thekeeperofpie.artistalleydatabase.markdown

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.Browser
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.text.getSpans
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableSpan
import java.util.regex.Pattern

object AniListSpoilerPlugin : AbstractMarkwonPlugin() {

    // Regex class doesn't work for some reason
    private val pattern = Pattern.compile("""~![\S\s]*?!~""")

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        val spannable = markdown as? Spannable ?: return
        val matcher = pattern.matcher(markdown)
        while (matcher.find()) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            if (spannable.getSpans<SpoilerSpan>(startIndex, endIndex).isNotEmpty()) {
                continue
            }

            val link = spannable.subSequence(startIndex, endIndex).toString()
                .removePrefix("~!")
                .removeSuffix("!~")
                .trim()

            val imageSpans = spannable.getSpans<AsyncDrawableSpan>(startIndex, endIndex)
            imageSpans.forEach(spannable::removeSpan)

            val spoilerSpan = if (imageSpans.isEmpty()) {
                SpoilerTextSpan()
            } else {
                SpoilerImageSpan.create(imageSpans)
            }

            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if (spoilerSpan.unveiled) {
                        if (link.startsWith("http")) {
                            val uri = Uri.parse(link)
                            val context = widget.context
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                .putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
                            try {
                                context.startActivity(intent)
                            } catch (ignored: ActivityNotFoundException) {
                            }
                        }
                    } else {
                        spoilerSpan.unveil()
                        widget.postInvalidate()
                        // Invalidate doesn't seem to be enough
                        (widget as? TextView)?.run { text = text }
                    }
                }

                override fun updateDrawState(ds: TextPaint) = Unit
            }

            spannable.setSpan(
                spoilerSpan,
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            spannable.setSpan(
                clickableSpan,
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private class SpoilerImageSpan(
        private val imageSpans: Array<out AsyncDrawableSpan>,
        theme: MarkwonTheme,
        drawable: AsyncDrawable,
        alignment: Int,
        replacementTextIsLink: Boolean,
    ) : AsyncDrawableSpan(theme, drawable, alignment, replacementTextIsLink), SpoilerSpan {

        companion object {
            private val themeField by lazy {
                AsyncDrawableSpan::class.java.getDeclaredField("theme")
                    .apply { isAccessible = true }
            }
            private val alignmentField by lazy {
                AsyncDrawableSpan::class.java.getDeclaredField("alignment")
                    .apply { isAccessible = true }
            }
            private val replacementTextIsLinkField by lazy {
                AsyncDrawableSpan::class.java.getDeclaredField("replacementTextIsLink")
                    .apply { isAccessible = true }
            }

            fun create(imageSpans: Array<out AsyncDrawableSpan>): SpoilerImageSpan {
                val imageSpan = imageSpans.first()
                val theme = themeField.get(imageSpan) as MarkwonTheme
                val drawable = imageSpan.drawable
                val alignment = alignmentField.getInt(imageSpan)
                val replacementTextIsLink = replacementTextIsLinkField.getBoolean(imageSpan)
                return SpoilerImageSpan(
                    imageSpans,
                    theme,
                    drawable,
                    alignment,
                    replacementTextIsLink,
                )
            }
        }

        override var unveiled = false
            private set

        override fun updateDrawState(textPaint: TextPaint) {
            if (unveiled) {
                textPaint.bgColor = Color.Black.copy(alpha = 0.25f).toArgb()
            } else {
                textPaint.bgColor = Color.Black.toArgb()
                textPaint.color = Color.Black.toArgb()
            }
        }

        override fun getSize(
            paint: Paint,
            text: CharSequence?,
            start: Int,
            end: Int,
            fontMetrics: Paint.FontMetricsInt?,
        ) = imageSpans.first().getSize(paint, text, start, end, fontMetrics)

        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint,
        ) {
            if (unveiled) {
                imageSpans.first().draw(canvas, text, start, end, x, top, y, bottom, paint)
            } else {
                canvas.drawRect(
                    x,
                    top.toFloat(),
                    end.toFloat(),
                    bottom.toFloat(),
                    paint,
                )
            }
        }

        override fun getDrawable() = imageSpans.first().drawable

        override fun unveil() {
            unveiled = true
        }
    }

    private class SpoilerTextSpan : CharacterStyle(), SpoilerSpan {
        override var unveiled = false
            private set

        override fun updateDrawState(textPaint: TextPaint) {
            if (unveiled) {
                textPaint.bgColor = Color.Black.copy(alpha = 0.25f).toArgb()
            } else {
                textPaint.bgColor = Color.Black.toArgb()
                textPaint.color = Color.Black.toArgb()
            }
        }

        override fun unveil() {
            unveiled = true
        }
    }

    interface SpoilerSpan {
        val unveiled: Boolean
        fun unveil()
    }
}
