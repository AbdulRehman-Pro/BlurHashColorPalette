package com.rehman.blurhash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rehman.blurhash.Utils.decodeBase64
import com.rehman.blurhash.databinding.ItemAddImageBinding
import com.rehman.blurhash.databinding.ItemImageBinding

class ImagePagerAdapter(
    private val blurHashData: MutableList<BlurHashData>,
    private val onAddImageClicked: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_ADD_IMAGE = 1
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == blurHashData.size) TYPE_ADD_IMAGE else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val binding =
                ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ImageViewHolder(binding)
        } else {
            val binding =
                ItemAddImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AddImageViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = blurHashData.size + 1 // Last item is Add Image

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            holder.bind(blurHashData[position].base64)
        } else if (holder is AddImageViewHolder) {
            holder.bind()
        }
    }

    inner class ImageViewHolder(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(base64: String) {
            binding.cardImageView.setImageBitmap(base64.decodeBase64())
        }
    }

    inner class AddImageViewHolder(private val binding: ItemAddImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.addImageCard.setOnClickListener { onAddImageClicked() }
        }
    }
}
