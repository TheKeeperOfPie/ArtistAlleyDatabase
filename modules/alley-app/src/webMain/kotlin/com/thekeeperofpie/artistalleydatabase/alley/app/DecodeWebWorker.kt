package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toInt8Array
import org.w3c.dom.MessageEvent
import org.w3c.dom.Worker
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.js
import kotlin.js.set
import kotlin.js.unsafeCast
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val WebWorkerJs = """
let canvas = null;
let context = null;
let cw = 0, ch = 0;

function ensureCanvas(w, h) {
  if (!canvas || w > cw || h > ch) {
    cw = Math.max(cw, w);
    ch = Math.max(ch, h);
    canvas = new OffscreenCanvas(cw, ch);
    context = canvas.getContext("2d", { willReadFrequently: true });
  }
  return context;
}

self.onmessage = async (e) => {
    const { id, data, w, h } = e.data;
    let bmp = null;
    try {
        const blob = new Blob([data]);
        bmp = await createImageBitmap(blob, {
            resizeWidth: w,
            resizeHeight: h,
            resizeQuality: 'high'
        });
        const ctx = ensureCanvas(w, h);
        ctx.clearRect(0, 0, w, h);
        ctx.drawImage(bmp, 0, 0);

        const imgData = ctx.getImageData(0, 0, w, h);
        const rawBuffer = imgData.data.buffer;
        self.postMessage(
            { kind: "result", id: id, buffer: rawBuffer },
            [rawBuffer]
        );
    } catch (err) {
        self.postMessage(
            { kind: "error", id: id, message: err?.message ?? String(err), }
        );
    } finally {
        if (bmp) bmp.close();
    }
};
"""

private fun blob(code: String): Blob = js("new Blob([code], { type: 'application/javascript' })")

private fun startWorker(code: String): Worker {
    val url = URL.createObjectURL(blob(code))
    val worker = Worker(url)
    URL.revokeObjectURL(url)
    return worker
}

private val worker by lazy { startWorker(WebWorkerJs) }

@OptIn(ExperimentalSkikoApi::class)
internal suspend fun decodeImageAsync(
    bytes: ByteArray,
    width: Int,
    height: Int,
): Bitmap {
    // async decodes an image to a bitmap on a special web worker. doesn't block UI thread :)
    val webBitmap = decodeBytesToBitmap(bytes, width, height)

    // pass bitmap ArrayBuffer to the skiko memory
    val skikoData = webBitmap.passToSkiko()
    return try {
        val colorInfo = ColorInfo(
            ColorType.RGBA_8888,
            ColorAlphaType.UNPREMUL,
            ColorSpace.sRGB,
        )
        val imageInfo = ImageInfo(colorInfo, width, height)
        val image = Image.makeRaster(imageInfo, skikoData, imageInfo.minRowBytes)
        try {
            Bitmap.makeFromImage(image)
        } finally {
            image.close()
        }
    } finally {
        skikoData.close()
    }
}

private suspend fun ArrayBuffer.passToSkiko(): Data {
    val data = Data.makeUninitialized(byteLength)
    val skikoMemory = getSkikoMemory(awaitSkiko())
    skikoMemory.set(this, data.writableData())
    return data
}

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("INVISIBLE_MEMBER", "OPT_IN_USAGE_ERROR")
internal suspend fun awaitSkiko(): JsAny =
    org.jetbrains.skiko.wasm.awaitSkiko.await()

private fun getSkikoMemory(skikoWasm: JsAny): ArrayBuffer =
    js("skikoWasm.wasmExports.memory.buffer")

private fun ArrayBuffer.set(data: ArrayBuffer, offset: NativePointer) {
    Int8Array(this).set(Int8Array(data), offset)
}

@OptIn(ExperimentalUuidApi::class)
private suspend fun decodeBytesToBitmap(
    bytes: ByteArray,
    width: Int,
    height: Int,
): ArrayBuffer = suspendCancellableCoroutine { continuation ->
    val id = Uuid.random().toString()
    var responseListener: ((Event) -> Unit)? = null
    responseListener = { event ->
        val data = (event as? MessageEvent)?.data?.unsafeCast<WebWorkerMessage>()
        if (data != null && data.id == id) {
            when (data.kind) {
                "result" -> {
                    worker.removeEventListener("message", responseListener)
                    continuation.resume(data.unsafeCast<WebWorkerResponse>().buffer)
                }
                "error" -> {
                    worker.removeEventListener("message", responseListener)
                    val message = data.unsafeCast<WebWorkerError>().message
                    continuation.resumeWithException(Error("WebWorker error: $message"))
                }
            }
        }
    }
    worker.addEventListener("message", responseListener)

    val buffer = bytes.toInt8Array().buffer
    val transfer = JsArray<JsAny>().apply { set(0, buffer) }
    worker.postMessage(WebWorkerRequest(id, buffer, width, height), transfer)

    continuation.invokeOnCancellation {
        worker.removeEventListener("message", responseListener)
    }
}

private fun WebWorkerRequest(
    id: String,
    buffer: ArrayBuffer,
    width: Int,
    height: Int,
): JsAny = js("({ id: id, data: buffer, w: width, h: height })")

internal external interface WebWorkerMessage : JsAny {
    val id: String
    val kind: String
}

internal external interface WebWorkerResponse : WebWorkerMessage {
    val buffer: ArrayBuffer
}

internal external interface WebWorkerError : WebWorkerMessage {
    val message: String
}
