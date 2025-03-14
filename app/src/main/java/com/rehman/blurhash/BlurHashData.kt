package com.rehman.blurhash

data class BlurHashData(
    val base64: String,    // Encoded image string
    val blurHashBase64: String?,  // Precomputed BlurHash
    val darkMutedColor: Int, // Extracted Dark Muted Color
    val lightVibrantColor: Int, // Extracted Light Vibrant Color
    val vibrantColor: Int,     // Extracted Vibrant Color
)
