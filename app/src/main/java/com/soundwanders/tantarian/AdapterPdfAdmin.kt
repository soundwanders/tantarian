package com.soundwanders.tantarian

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.soundwanders.tantarian.databinding.RowPdfAdminBinding

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable{
    private var context: Context

    // pdfArrayList is public
    var pdfArrayList: ArrayList<ModelPdf>

    private val filterList: ArrayList<ModelPdf>
    private lateinit var binding:RowPdfAdminBinding
    private var filter: FilterPdfAdmin? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

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
        TantarianApplication.loadFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        // get pdf size
        TantarianApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {
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
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                }
                else if (position == 1) {
                    TantarianApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
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