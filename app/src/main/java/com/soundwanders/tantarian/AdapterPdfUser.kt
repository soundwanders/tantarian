package com.soundwanders.tantarian

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.soundwanders.tantarian.databinding.RowPdfUserBinding

class AdapterPdfUser: RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {
    private var context: Context
    var pdfArrayList: ArrayList<ModelPdf>
    var filterList: ArrayList<ModelPdf>
    private lateinit var binding: RowPdfUserBinding
    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderPdfUser {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterPdfUser.HolderPdfUser, position: Int) {
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        val date = TantarianApplication.formatTimeStamp(timestamp)

        // set data to holder
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        // pass null into pages TextView because page numbers are not applicable here
        TantarianApplication.loadFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        TantarianApplication.loadCategory(categoryId, holder.categoryTv)
        TantarianApplication.loadPdfSize(url, title, holder.sizeTv)

        holder.itemView.setOnClickListener {
            // pass bookId with intent, used to retrieve unique book data
            val intent = Intent(context, PdfDetailsActivity::class.java)
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
        val pdfView = binding.pdfView
        val progressBar  = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
    }
}