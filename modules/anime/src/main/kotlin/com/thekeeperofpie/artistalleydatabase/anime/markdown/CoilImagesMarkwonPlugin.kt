@file:OptIn(ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.markdown

import android.graphics.drawable.Drawable
import android.text.Spanned
import android.widget.TextView
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.target.Target
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableLoader
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.DrawableUtils
import io.noties.markwon.image.ImageSpanFactory
import org.commonmark.node.Image
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Copied from Markwon CoilImagesPlugin and converted to coil3.
 */
class CoilImagesMarkwonPlugin internal constructor(
    private val context: PlatformContext,
    coilStore: CoilStore,
    imageLoader: ImageLoader,
) :
    AbstractMarkwonPlugin() {
    interface CoilStore {
        fun load(drawable: AsyncDrawable): ImageRequest

        fun cancel(disposable: Disposable)
    }

    private val coilAsyncDrawableLoader = CoilAsyncDrawableLoader(context, coilStore, imageLoader)

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Image::class.java, ImageSpanFactory())
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.asyncDrawableLoader(coilAsyncDrawableLoader)
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        AsyncDrawableScheduler.unschedule(textView)
    }

    override fun afterSetText(textView: TextView) {
        AsyncDrawableScheduler.schedule(textView)
    }

    private class CoilAsyncDrawableLoader(
        private val context: PlatformContext,
        private val coilStore: CoilStore,
        private val imageLoader: ImageLoader,
    ) :
        AsyncDrawableLoader() {
        private val cache: MutableMap<AsyncDrawable, Disposable?> = HashMap(2)

        override fun load(drawable: AsyncDrawable) {
            val loaded = AtomicBoolean(false)
            val target: Target = AsyncDrawableTarget(context, drawable, loaded)
            val request = coilStore.load(drawable).newBuilder()
                .target(target)
                .build()
            val disposable = imageLoader.enqueue(request)
            if (!loaded.get()) {
                loaded.set(true)
                cache[drawable] = disposable
            }
        }

        override fun cancel(drawable: AsyncDrawable) {
            val disposable = cache.remove(drawable)
            if (disposable != null) {
                coilStore.cancel(disposable)
            }
        }

        override fun placeholder(drawable: AsyncDrawable): Drawable? {
            return null
        }

        private inner class AsyncDrawableTarget(
            private val context: PlatformContext,
            private val drawable: AsyncDrawable,
            private val loaded: AtomicBoolean,
        ) : Target {

            override fun onSuccess(result: coil3.Image) {
                if (cache.remove(drawable) != null
                    || !loaded.get()
                ) {
                    loaded.set(true)
                    if (drawable.isAttached) {
                        drawable.result = result.asDrawable(context.resources)
                            .apply(DrawableUtils::applyIntrinsicBoundsIfEmpty)
                    }
                }
            }

            override fun onStart(placeholder: coil3.Image?) {
                if (placeholder != null && drawable.isAttached) {
                    placeholder.asDrawable(context.resources)
                    drawable.result = placeholder.asDrawable(context.resources)
                        .apply(DrawableUtils::applyIntrinsicBoundsIfEmpty)
                }
            }

            override fun onError(error: coil3.Image?) {
                if (cache.remove(drawable) != null) {
                    if (error != null && drawable.isAttached) {
                        drawable.result = error.asDrawable(context.resources)
                            .apply(DrawableUtils::applyIntrinsicBoundsIfEmpty)
                    }
                }
            }
        }
    }

    companion object {
        fun create(context: PlatformContext): CoilImagesMarkwonPlugin {
            return create(
                context,
                object : CoilStore {
                    override fun load(drawable: AsyncDrawable): ImageRequest {
                        return ImageRequest.Builder(context)
                            .data(drawable.destination)
                            .build()
                    }

                    override fun cancel(disposable: Disposable) {
                        disposable.dispose()
                    }
                },
                SingletonImageLoader.get(context),
            )
        }

        fun create(
            context: PlatformContext,
            imageLoader: ImageLoader,
        ): CoilImagesMarkwonPlugin {
            return create(
                context,
                object : CoilStore {
                    override fun load(drawable: AsyncDrawable): ImageRequest {
                        return ImageRequest.Builder(context)
                            .data(drawable.destination)
                            .build()
                    }

                    override fun cancel(disposable: Disposable) {
                        disposable.dispose()
                    }
                },
                imageLoader,
            )
        }

        fun create(
            context: PlatformContext,
            coilStore: CoilStore,
            imageLoader: ImageLoader,
        ): CoilImagesMarkwonPlugin {
            return CoilImagesMarkwonPlugin(context, coilStore, imageLoader)
        }
    }
}
