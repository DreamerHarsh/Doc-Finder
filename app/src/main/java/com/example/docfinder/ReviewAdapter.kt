package com.example.docfinder

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private val reviewsList: List<ReviewDetail>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val reviewDetail = reviewsList[position]
        holder.bind(reviewDetail)
    }

    override fun getItemCount(): Int = reviewsList.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val patientPhotoImageView: ImageView = itemView.findViewById(R.id.patientPhotoImageView)
        private val patientNameTextView: TextView = itemView.findViewById(R.id.patientNameTextView)
        private val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)

        fun bind(reviewDetail: ReviewDetail) {
            // Set the patient name and review text
            patientNameTextView.text = reviewDetail.patientName
            reviewTextView.text = reviewDetail.reviewText

            // Set the patient photo if available, or use a placeholder if null
            reviewDetail.patientPhoto?.let { photo ->
                val bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.size)
                patientPhotoImageView.setImageBitmap(bitmap)
            } ?: run {
                patientPhotoImageView.setImageResource(R.drawable.ic_profile) // Use default placeholder
            }
        }


    }
}
