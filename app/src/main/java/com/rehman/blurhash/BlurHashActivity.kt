package com.rehman.blurhash

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import com.rehman.blurhash.Utils.blurredBase64
import com.rehman.blurhash.Utils.decodeBase64
import com.rehman.blurhash.Utils.encodeBase64
import com.rehman.blurhash.Utils.getDarkMutedColor
import com.rehman.blurhash.Utils.getLightVibrantColor
import com.rehman.blurhash.Utils.getVibrantColor
import com.rehman.blurhash.Utils.setMorphImageBitmap
import com.rehman.blurhash.databinding.ActivityBlurHashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class BlurHashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlurHashBinding
    private lateinit var adapter: ImagePagerAdapter
    private var blurHashData = mutableListOf<BlurHashData>()

    private val paddingPx = 150
    private val minScale = 0.8f
    private val maxScale = 1f

    // Page Transformer for ViewPager2 to create a scaling, fading, and rotation effect
    private val transformer = ViewPager2.PageTransformer { page, position ->
        val scaleFactor = minScale + (1 - abs(position)) * (maxScale - minScale)
        val rotation = position * 15f  // Rotate pages slightly
        val alpha = 0.5f + (1 - abs(position)) * 0.5f // Fading effect

        page.scaleX = scaleFactor
        page.scaleY = scaleFactor
        page.alpha = alpha
        page.rotationY = rotation

    }

    private val launcher = registerImagePicker { pickedImages ->
        pickedImages.forEach {
            val bitmap = Utils.generateBitmap(contentResolver, it.uri)
            bitmap?.let { bmp ->

                CoroutineScope(Dispatchers.IO).launch {
                    val base64 = bmp.encodeBase64()
                    val blurredBase64 = bmp.blurredBase64()
                    val darkMutedColor = bmp.getDarkMutedColor()
                    val lightVibrantColor = bmp.getLightVibrantColor()
                    val vibrantColor = bmp.getVibrantColor()

                    blurHashData.add(
                        0,
                        BlurHashData(
                            base64,
                            blurredBase64,
                            darkMutedColor,
                            lightVibrantColor,
                            vibrantColor
                        )
                    )

                    Utils.saveBlurDataList(this@BlurHashActivity, blurHashData)

                    withContext(Dispatchers.Main) {
                        adapter.notifyItemInserted(0)
                        binding.viewPager.post {
                            binding.viewPager.setCurrentItem(0, true)
                        }
                    }
                }


            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )
        binding = ActivityBlurHashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for update
        Utils.checkForUpdate(this)

        // Initialize adapter for ViewPager2
        adapter = ImagePagerAdapter(blurHashData) {
            launcher.launch(Utils.imagePickerConfig(this))
        }
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)

        // Load saved images asynchronously
        lifecycleScope.launch {
            blurHashData.clear()
            Utils.getBlurDataList(this@BlurHashActivity).forEach {
                blurHashData.add(it)
            }

            adapter.notifyItemRangeChanged(0, blurHashData.size)
            binding.viewPager.adapter = adapter

        }


        // Configure ViewPager2 properties
        binding.viewPager.clipToPadding = false
        binding.viewPager.setClipChildren(false)
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.setPadding(paddingPx, 0, paddingPx, 0)
        binding.viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.viewPager.setPageTransformer(transformer)


        // Page change listener to update UI based on selected image
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)


                if (position < blurHashData.size) { // Ignore the "Add Image" item
                    val blurHashData = blurHashData[position]
                    setContent(isAddImage = false, blurHashData)
                } else {
                    setContent(isAddImage = true)
                }

            }
        })


    }

    // Update UI based on selected page
    private fun setContent(
        isAddImage: Boolean,
        blurHashData: BlurHashData? = null,
    ) {
        if (isAddImage) {
            binding.descriptionText.text = getString(R.string.image_add_description)
            binding.blurredImageView.setMorphImageBitmap(null)
            binding.descriptionCard.setCardBackgroundColor(getColor(R.color.white))
            binding.descriptionText.setTextColor(getColor(R.color.black))

            binding.dotsIndicator.selectedDotColor = getColor(R.color.white)
            binding.dotsIndicator.dotsColor = getColor(R.color.black)

        } else {
            binding.descriptionText.text = getString(R.string.image_added_description)
            binding.blurredImageView.setMorphImageBitmap(blurHashData?.blurHashBase64?.decodeBase64()!!)
            binding.descriptionCard.setCardBackgroundColor(blurHashData.lightVibrantColor)
            binding.descriptionText.setTextColor(blurHashData.vibrantColor)

            binding.dotsIndicator.selectedDotColor = blurHashData.vibrantColor
            binding.dotsIndicator.dotsColor = blurHashData.darkMutedColor
        }


    }








}