package com.aryan.veena.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aryan.veena.databinding.SearchSuggestionsItemBinding
import com.aryan.veena.ui.adapters.SearchSuggestionsAdapter.SearchSuggestionsViewHolder

class SearchSuggestionsAdapter(private var searchSuggestions: List<String>) :
    RecyclerView.Adapter<SearchSuggestionsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchSuggestionsViewHolder {
        val binding = SearchSuggestionsItemBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return SearchSuggestionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchSuggestionsViewHolder, position: Int) {
        val suggestion = searchSuggestions[position]
        Log.i("BIND", suggestion)
        holder.bind(suggestion)
    }

    override fun getItemCount(): Int {
        return searchSuggestions.size
    }

    fun updateSuggestions(newSuggestions: List<String>) {
        searchSuggestions = newSuggestions
        Log.i("ADAPTER", "Updated suggestions: $searchSuggestions")
        notifyDataSetChanged()
    }

    class SearchSuggestionsViewHolder(private val binding: SearchSuggestionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(suggestion: String) {
                Log.i("ViewHolder", "Binding suggestion: $suggestion")
                    binding.suggestionName.text = suggestion
            }
    }
}
