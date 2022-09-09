package com.soundwanders.tantarian.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.soundwanders.tantarian.TantarianApplication
import com.soundwanders.tantarian.books.BookDetailsActivity
import com.soundwanders.tantarian.databinding.RowFavoritesBinding
import com.soundwanders.tantarian.models.ModelBook

class AdapterFavorites : RecyclerView.Adapter<AdapterFavorites.HolderBookFavorites> {
    private val context: Context
    private lateinit var binding: RowFavoritesBinding
    var booksArrayList: ArrayList<ModelBook>

    constructor(context: Context, booksArrayList: ArrayList<ModelBook>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderBookFavorites {
        binding = RowFavoritesBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderBookFavorites(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterFavorites.HolderBookFavorites, position: Int) {
        val model = booksArrayList[position]

        loadBookDetails(model, holder)

        // on click, open book details. identify book using id
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }

        holder.removeFavorite.setOnClickListener {
            TantarianApplication.removeFavorite(context, model.id)
        }
    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }

    private fun loadBookDetails(model: ModelBook, holder: AdapterFavorites.HolderBookFavorites) {
        val bookId = model.id
        Log.d("BOOK_ID", bookId)

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    // get book data
                    val title = "${snapshot.child("title").value}"
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    val date = TantarianApplication.formatTimeStamp(timestamp.toLong())

                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadsCount = downloadsCount.toLong()

                    TantarianApplication.loadCategory("$categoryId", holder.categoryTv)
                    TantarianApplication.loadFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar, null)
                    TantarianApplication.loadPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    inner class HolderBookFavorites(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
        var removeFavorite = binding.removeFavoriteBtn
    }

}