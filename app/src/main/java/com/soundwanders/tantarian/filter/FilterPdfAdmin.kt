package com.soundwanders.tantarian.filter

import android.widget.Filter
import com.soundwanders.tantarian.adapter.AdapterBookAdmin
import com.soundwanders.tantarian.models.ModelBook

class FilterPdfAdmin : Filter {
    private var filterList: ArrayList<ModelBook>

    var adapterBookAdmin: AdapterBookAdmin

    constructor(filterList: ArrayList<ModelBook>, adapterBookAdmin: AdapterBookAdmin) {
        this.filterList = filterList
        this.adapterBookAdmin = adapterBookAdmin
    }

    override fun performFiltering(constraint: CharSequence?) : FilterResults {
        var charConstraint:CharSequence? = constraint
        val results = FilterResults()

        if (charConstraint != null && charConstraint.isNotEmpty()) {
            charConstraint = charConstraint.toString().lowercase()

            var filteredModels = ArrayList<ModelBook>()
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
        adapterBookAdmin.pdfArrayList = results!!.values as ArrayList<ModelBook>

        // it will always be more efficient to use specific change events where possible,
        // Rely on `notifyDataSetChanged` as a last resort
        adapterBookAdmin.notifyDataSetChanged()
    }
}
