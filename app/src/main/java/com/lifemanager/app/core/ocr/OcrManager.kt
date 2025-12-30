package com.lifemanager.app.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OCR文字识别管理器
 * 使用ML Kit进行中文文字识别
 */
@Singleton
class OcrManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 中文文字识别器
    private val recognizer: TextRecognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    /**
     * 从Bitmap识别文字
     * @param bitmap 图片Bitmap
     * @return 识别到的文字
     */
    suspend fun recognizeFromBitmap(bitmap: Bitmap): Result<OcrResult> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val text = processImage(inputImage)
            Result.success(OcrResult(
                rawText = text,
                lines = text.lines().filter { it.isNotBlank() },
                blocks = emptyList(),
                confidence = 0.9f
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从URI识别文字
     * 优先尝试通过Bitmap方式加载，以支持相机拍摄的临时URI
     * @param uri 图片URI
     * @return 识别到的文字
     */
    suspend fun recognizeFromUri(uri: Uri): Result<OcrResult> {
        return try {
            // 先尝试通过ContentResolver加载为Bitmap（更可靠）
            val bitmap = loadBitmapFromUri(uri)
            if (bitmap != null) {
                return recognizeFromBitmap(bitmap)
            }

            // 如果Bitmap加载失败，尝试直接使用URI
            val inputImage = InputImage.fromFilePath(context, uri)
            val text = processImage(inputImage)
            Result.success(OcrResult(
                rawText = text,
                lines = text.lines().filter { it.isNotBlank() },
                blocks = emptyList(),
                confidence = 0.9f
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从URI加载Bitmap
     * 处理相机拍摄的临时文件和相册选择的内容URI
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // 先读取到字节数组，避免流被关闭后无法再次读取
                val bytes = inputStream.readBytes()

                // 获取图片尺寸
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                // 计算采样率，限制最大尺寸为2048
                val maxSize = 2048
                var sampleSize = 1
                while (options.outWidth / sampleSize > maxSize ||
                    options.outHeight / sampleSize > maxSize) {
                    sampleSize *= 2
                }

                // 解码图片
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                })
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 处理图片并提取文字
     */
    private suspend fun processImage(inputImage: InputImage): String {
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    /**
     * 释放资源
     */
    fun close() {
        recognizer.close()
    }
}
