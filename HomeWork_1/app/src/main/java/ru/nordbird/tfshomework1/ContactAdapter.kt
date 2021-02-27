package ru.nordbird.tfshomework1

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.nordbird.tfshomework1.databinding.ItemContactBinding

class ContactAdapter :
    RecyclerView.Adapter<ContactAdapter.PhotoItemViewHolder>() {

    var items: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContactBinding.inflate(inflater, parent, false)

        return PhotoItemViewHolder(binding, parent.context)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PhotoItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(data: List<String>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean =
                items[oldPos] == data[newPos]

            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = data.size

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean =
                items[oldPos].hashCode() == data[newPos].hashCode()

        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items = data
        diffResult.dispatchUpdatesTo(this)
    }

    inner class PhotoItemViewHolder(viewBinding: ItemContactBinding, private val context: Context) :
        RecyclerView.ViewHolder(viewBinding.root) {
        private var binding: ItemContactBinding = viewBinding

        fun bind(item: String) {
            binding.tvName.text = item
        }
    }
}