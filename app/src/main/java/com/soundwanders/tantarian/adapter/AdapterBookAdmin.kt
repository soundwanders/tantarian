package com.soundwanders.tantarian.adapter

import android.app.AlertDialog
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
import com.soundwanders.tantarian.books.BookEditActivity
import com.soundwanders.tantarian.databinding.RowBookAdminBinding
import com.soundwanders.tantarian.filter.FilterPdfAdmin

class AdapterBookAdmin : RecyclerView.Adapter<AdapterBookAdmin.HolderPdfAdmin>, Filterable{
    private lateinit var binding:RowBookAdminBinding

    private var context: Context
    private var filter: FilterPdfAdmin? = null
    private val filterList: ArrayList<ModelBook>

    // set pdfArrayList as public to allow access by FilterPdfAdmin Activity
    var pdfArrayList: ArrayList<ModelBook>

    constructor(context: Context, pdfArrayList: ArrayList<ModelBook>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : HolderPdfAdmin {
        binding = RowBookAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val formattedDate = TantarianApplication.formatTimeStamp(timestamp)

        // set data to holder view
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        // get category id
        TantarianApplication.loadCategory(categoryId, holder.categoryTv)

        // page number irrelevant in this context, so we will pass page number parameter as NULL
        TantarianApplication.loadFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        // get pdf size
        TantarianApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: ModelBook, holder: HolderPdfAdmin) {
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        // dialog options
        val options = arrayOf("Edit", "Delete")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Option")
            .setItems(options){ dialog, position ->
                if (position == 0) {
                    // bookId will be used to edit the selected book
                    val intent = Intent(context, BookEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                }
                else if (position == 1) {
                    TantarianApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }

    override fun getItemCount() : Int {
        return pdfArrayList.size
    }

    override fun getFilter() : Filter {
        if (filter == null) {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // PDF UI
        val pdfView = binding.pdfView
        val progressBar  = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }
}