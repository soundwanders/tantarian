package com.soundwanders.tantarian
import android.widget.Filter

class FilterCategory: Filter {
    private var filterList: ArrayList<ModelCategory>

    private var adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?) : FilterResults {
        var charConstraint = constraint
        val results = FilterResults()

        if (charConstraint != null && charConstraint.isNotEmpty()) {
            charConstraint = charConstraint.toString().uppercase()

            val filteredModels:ArrayList<ModelCategory> = ArrayList()
            // eliminate case sensitivity
            for (i in 0 until filterList.size) {
                if (filterList[i].category.uppercase().contains(charConstraint)) {
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

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        // it will always be more efficient to use specific change events where possible,
        // Rely on `notifyDataSetChanged` as a last resort
        adapterCategory.notifyDataSetChanged()
    }
}