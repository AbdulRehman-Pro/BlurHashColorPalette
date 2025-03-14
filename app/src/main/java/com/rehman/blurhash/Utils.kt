package com.rehman.blurhash

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.ImageView
import androidx.palette.graphics.Palette
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nguyenhoanglam.imagepicker.model.CustomColor
import com.nguyenhoanglam.imagepicker.model.CustomMessage
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.model.getHexColorFromResId
import com.vanniktech.blurhash.BlurHash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object Utils {

    private const val APP_NAME = "BlurHashApplication"
    private const val KEY_IMAGE_LIST = "saved_images"


    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
    }


    fun saveBlurDataList(context: Context, blurHashDataList: MutableList<BlurHashData>) {
        CoroutineScope(Dispatchers.IO).launch { // Run in background thread
            val jsonString = Gson().toJson(blurHashDataList)
            getSharedPreferences(context).edit().putString(KEY_IMAGE_LIST, jsonString).apply()
        }
    }

    suspend fun getBlurDataList(context: Context): MutableList<BlurHashData> {
        return withContext(Dispatchers.IO) { // Run in background thread
            val jsonString = getSharedPreferences(context).getString(KEY_IMAGE_LIST, null)
            if (jsonString != null) {
                Gson().fromJson(jsonString, object : TypeToken<List<BlurHashData>>() {}.type)
            } else {
                mutableListOf()
            }
        }
    }


    fun String.decodeBase64(): Bitmap {
        val decodedBytes = Base64.decode(this, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun Bitmap.encodeBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun imagePickerConfig(context: Context): ImagePickerConfig {
        return ImagePickerConfig(
            isFolderMode = false,
            isShowCamera = false,
            isSingleSelectMode = true,
            imageTitle = "Select Image",
            snackBarButtonTitle = "Allow",
            isAlwaysShowDoneButton = false,
            customColor = CustomColor(
                background = context.getHexColorFromResId(R.color.dark_grey),
                statusBar = context.getHexColorFromResId(R.color.dark_grey),
                toolbar = context.getHexColorFromResId(R.color.dark_grey),
                toolbarTitle = context.getHexColorFromResId(R.color.white),
                toolbarIcon = context.getHexColorFromResId(R.color.white),
                snackBarBackground = context.getHexColorFromResId(R.color.light_red),
                snackBarMessage = context.getHexColorFromResId(R.color.white),
                snackBarButtonTitle = context.getHexColorFromResId(R.color.white),
                loadingIndicator = context.getHexColorFromResId(R.color.white),
            ),
            customMessage = CustomMessage(
                noImage = "No image found.",
                noPhotoAccessPermission = "Please allow permission to access photos and media.",
            ),
        )
    }



    fun generateBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API 28+
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE) // Avoid HARDWARE config
            }.copy(Bitmap.Config.ARGB_8888, true) // Ensure mutable ARGB_8888 format
        } else { // For API 24 to 27
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
            bitmap?.copy(Bitmap.Config.ARGB_8888, true) // Ensure mutable ARGB_8888 format
        }
    }

    private fun encodeBlurHash(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        return BlurHash.encode(resizedBitmap, 5, 5)
    }

    private fun decodeBlurHash(blurHash: String): Bitmap? {
        return BlurHash.decode(blurHash, 500, 500) // Decode to larger size
    }

    fun Bitmap.blurredBase64(): String? {
        val encodeBlurHashString = encodeBlurHash(this)
        val decodeBlurHashBitmap = decodeBlurHash(encodeBlurHashString)
        return decodeBlurHashBitmap?.encodeBase64()
    }

    fun Bitmap.getDarkMutedColor(): Int {
        return Palette.from(this).generate().getDarkMutedColor(Color.BLACK)
    }

    fun Bitmap.getLightVibrantColor(): Int {
        return Palette.from(this).generate().getLightVibrantColor(Color.WHITE)
    }

    fun Bitmap.getVibrantColor(): Int {
        return Palette.from(this).generate().getVibrantColor(Color.BLACK)
    }


    fun ImageView.setMorphImageBitmap(bitmap: Bitmap?, duration: Long = 300) {
        if (bitmap == null) {
            this.setImageDrawable(null)
            return
        }
        val oldDrawable = this.drawable
            ?: ColorDrawable(Color.TRANSPARENT) // Use a transparent placeholder if null

        val newDrawable = BitmapDrawable(resources, bitmap)

        val crossFade = TransitionDrawable(arrayOf(oldDrawable, newDrawable)).apply {
            isCrossFadeEnabled = true // Smooth blending effect
        }

        this.setImageDrawable(crossFade)
        crossFade.startTransition(duration.toInt()) // Start smooth transition
    }


}