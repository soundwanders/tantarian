package com.soundwanders.tantarian.filter

import android.widget.Filter
import com.soundwanders.tantarian.adapter.AdapterBookUser
import com.soundwanders.tantarian.models.ModelBook

class FilterPdfUser : Filter {
    private var filterList: ArrayList<ModelBook>

    var adapterBookUser: AdapterBookUser

    constructor(filterList: ArrayList<ModelBook>, adapterBookUser: AdapterBookUser) {
        this.filterList = filterList
        this.adapterBookUser = adapterBookUser
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
        adapterBookUser.pdfArrayList = results!!.values as ArrayList<ModelBook>
        adapterBookUser.notifyDataSetChanged()
    }
}
