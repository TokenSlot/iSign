package com.example.isign.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.isign.databinding.RowDataBinding
import com.google.android.material.imageview.ShapeableImageView

class SignListAdapter (
    private val signNames: Array<String>,
    private val signImages: IntArray
) : RecyclerView.Adapter<SignListAdapter.SignViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {

        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignViewHolder {
        val binding = RowDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SignViewHolder(binding, mListener)
    }

    override fun getItemCount() = signNames.size

    override fun onBindViewHolder(holder: SignViewHolder, position: Int) {
        holder.binding.signs.text = signNames[position]
        holder.binding.images.setImageResource(signImages[position])
    }

    class SignViewHolder(val binding: RowDataBinding, listener: onItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        val image: ImageView = binding.images
        val text: TextView = binding.signs
        init {
            binding.root.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}