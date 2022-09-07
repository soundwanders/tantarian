package com.soundwanders.tantarian.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.soundwanders.tantarian.models.ModelBook
import com.soundwanders.tantarian.TantarianApplication
import com.soundwanders.tantarian.books.BookDetailsActivity
import com.soundwanders.tantarian.books.BookViewActivity
import com.soundwanders.tantarian.databinding.RowBookUserBinding
import com.soundwanders.tantarian.filter.FilterPdfUser

class AdapterBookUser: RecyclerView.Adapter<AdapterBookUser.HolderPdfUser>, Filterable {
    // bind row_pdf_user.xml --> RowPdfUserBinding
    private lateinit var binding: RowBookUserBinding

    private val filterList: ArrayList<ModelBook>
    private var context: Context
    private var filter: FilterPdfUser? = null

    var pdfArrayList: ArrayList<ModelBook>

    constructor(context: Context, pdfArrayList: ArrayList<ModelBook>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderPdfUser {
        binding = RowBookUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        val formattedDate = TantarianApplication.formatTimeStamp(timestamp)

        // set data to holder view
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        // page number irrelevant in this context, so we pass it as null
        TantarianApplication.loadFromUrlSinglePage(
            url,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        TantarianApplication.loadCategory(categoryId, holder.categoryTv)

        TantarianApplication.loadPdfSize(url, title, holder.sizeTv)

        holder.itemView.setOnClickListener {
            // pass bookId with intent, used to retrieve book data
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }

        binding.readBookBtn.setOnClickListener {
            // touch book image to read directly from dashboard list
            // added to create one-step process, instead of having to go to Details -> read btn
            val intent = Intent(context, BookViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() : Int {
        return pdfArrayList.size
    }

    override fun getFilter() : Filter {
        if (filter == null) {
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }

    // ViewHolder class row_pdf_user.xml
    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pdfView = binding.pdfView
        var progressBar  = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }
}