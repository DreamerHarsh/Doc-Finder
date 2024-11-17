package com.example.docfinder

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(
    private val context: Context,
    private var photos: List<ByteArray>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    // ViewHolder class for photo items
    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoBytes = photos[position]
        val bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
        holder.photoImageView.setImageBitmap(bitmap)
    }

    override fun getItemCount(): Int = photos.size

    // Function to update the photos list
    fun updatePhotos(newPhotos: List<ByteArray>) {
        photos = newPhotos  // Replace the photos list entirely
        notifyDataSetChanged()
    }
}
