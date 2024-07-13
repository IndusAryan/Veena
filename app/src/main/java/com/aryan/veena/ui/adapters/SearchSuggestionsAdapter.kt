package com.aryan.veena.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.RecyclerView
import com.aryan.veena.databinding.SearchSuggestionsItemBinding
import com.aryan.veena.ui.adapters.SearchSuggestionsAdapter.SearchSuggestionsViewHolder
import com.aryan.veena.viewmodels.HomeViewModel

class SearchSuggestionsAdapter(
    private var searchSuggestions: List<String>,
    private val listener: OnSuggestionClickListener
) : RecyclerView.Adapter<SearchSuggestionsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchSuggestionsViewHolder {
        val binding = SearchSuggestionsItemBinding.inflate(LayoutInflater.from(parent.context), parent , false)

        return SearchSuggestionsViewHolder(binding, listener)
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

    class SearchSuggestionsViewHolder(
        private val binding: SearchSuggestionsItemBinding,
        private val listener: OnSuggestionClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestion: String) {
            //Log.i("ViewHolder", "Binding suggestion: $suggestion")
            binding.apply {
                suggestionName.apply {
                    text = suggestion
                    setOnClickListener {
                        listener.onSuggestionClick(suggestion)
                    }
                }
                inputHint.setOnClickListener {
                    Log.d("DrawableClick", suggestion)
                    listener.onDrawableClick(suggestion)
                }
            }
        }
    }

    interface OnSuggestionClickListener {
        fun onSuggestionClick(suggestion: String)
        fun onDrawableClick(suggestion: String)
    }
}
