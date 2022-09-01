package com.soundwanders.tantarian

import android.widget.Filter

class FilterPdfAdmin : Filter {
    private var filterList: ArrayList<ModelPdf>

    var adapterPdfAdmin: AdapterPdfAdmin

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?) : FilterResults {
        var charConstraint:CharSequence? = constraint
        val results = FilterResults()

        if (charConstraint != null && charConstraint.isNotEmpty()) {
            charConstraint = charConstraint.toString().lowercase()
                var filteredModels = ArrayList<ModelPdf>()
                for (i in filterList.indices) {
                    if (filterList[i].title.lowercase().contains(charConstraint)) {
                        filteredModels.add(filterList[i])
                    }
                }
                results.count = filteredModels.size
                results.values = filteredModels
            }
        else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapterPdfAdmin.pdfArrayList = results!!.values as ArrayList<ModelPdf>

        // it will always be more efficient to use specific change events where possible,
        // Rely on `notifyDataSetChanged` as a last resort
        adapterPdfAdmin.notifyDataSetChanged()
    }
}
