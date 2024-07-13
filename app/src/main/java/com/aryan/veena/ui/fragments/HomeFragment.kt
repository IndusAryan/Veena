package com.aryan.veena.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aryan.veena.R
import com.aryan.veena.databinding.FragmentHomeBinding
import com.aryan.veena.helpers.ThemeHelper.setAppName
import com.aryan.veena.repository.Provider
import com.aryan.veena.ui.adapters.SearchSuggestionsAdapter
import com.aryan.veena.ui.adapters.SearchSuggestionsAdapter.OnSuggestionClickListener
import com.aryan.veena.ui.adapters.SongsAdapter
import com.aryan.veena.utils.CoroutineUtils.ioScope
import com.aryan.veena.utils.ToastUtil.showToast
import com.aryan.veena.viewmodels.HomeViewModel
import com.aryan.veena.viewmodels.HomeViewModel.Resource
import org.schabi.newpipe.extractor.ServiceList.YouTube

class HomeFragment : Fragment(), OnSuggestionClickListener {

    private var _binding : FragmentHomeBinding? = null
    private var homeViewModel : HomeViewModel? = null
    private val binding get() = _binding
    private val songsAdapter  by lazy { SongsAdapter(emptyList()) }
    private val searchSuggestionsAdapter by lazy { SearchSuggestionsAdapter(emptyList(), this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): CoordinatorLayout? {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChipGroup()
        observeHomeViewModel()
        setupRecyclerView()
        binding?.toolbar?.title = setAppName(context ?: return)

        binding?.searchSuggestions?.apply {
            adapter = searchSuggestionsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding?.searchView?.editText?.apply {
            doOnTextChanged { _, _, _, _ ->
                if (text.length >= 3) {
                    try {
                        ioScope {
                            val suggestionsYT =
                                YouTube?.suggestionExtractor?.suggestionList(text?.toString())
                            Log.i("SUGGESTIONS", "$suggestionsYT")
                            activity?.runOnUiThread {
                                searchSuggestionsAdapter.updateSuggestions(suggestionsYT ?: emptyList())
                            }
                        }
                        //addAll(SoundCloud.suggestionExtractor.suggestionList(editText.text.toString()))
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }

            setOnEditorActionListener { textView, _, _ ->
                val query = textView.text.toString()
                Log.i("SearchView", "${homeViewModel?.searchQuery?.value}")

                fun search() {
                    // Only call search if the new query is different from current text
                    if (query != binding?.searchBar?.text.toString() && query.length >= 3) {
                        //showToast(requireContext(), )
                        homeViewModel?.search(query)
                        binding?.searchBar?.setText(query)
                        binding?.searchView?.hide()// Update search bar text
                    }
                }
                search()
                true
            }
        }
    }

    override fun onSuggestionClick(suggestion: String) {
        homeViewModel?.search(suggestion)
        binding?.searchBar?.setText(suggestion)
        binding?.searchView?.hide()
    }

    override fun onDrawableClick(suggestion: String) {
       // homeViewModel?.searchQuery? = suggestion
    }

    private fun setupChipGroup() {
        binding?.providerChips?.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val provider = when (checkedIds[0]) {
                    R.id.jiosaavn -> Provider.SAAVN
                    R.id.ytmusic -> Provider.YTMUSIC
                    R.id.newpipe -> Provider.NEWPIPE
                    R.id.piped -> Provider.PIPED
                    R.id.wapking -> Provider.WAPKING
                    else -> null
                }
                provider?.let { homeViewModel?.selectProvider(it) }
            }
        }
    }

    private fun setupRecyclerView() {
        binding?.songsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songsAdapter
        }
    }

    private fun observeHomeViewModel() {
        homeViewModel?.apply {
            selectedProvider.observe(viewLifecycleOwner) { provider ->
                if (providerResults.containsKey(provider)) {
                    providerResults[provider]?.let { _searchedSong.postValue(it) }
                } else {
                    ioScope {
                        if (!searchQuery.value.isNullOrEmpty()) {
                            search(searchQuery.value.toString())
                        }
                    }
                }
            }

            status.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        shimmer(showShimmer = true, showRecyclerView = false)
                    }

                    is Resource.Success -> {
                        shimmer(showShimmer = false, showRecyclerView = true)
                    }

                    is Resource.Error -> {
                        shimmer(showShimmer = false, showRecyclerView = false)
                        showToast(
                            context ?: return@observe,
                            resource.message ?: R.string.unknown_error
                        )
                    }
                }
            }

            searchedSong.observe(viewLifecycleOwner) { songs ->
                val selectedProvider = homeViewModel?.selectedProvider?.value
                if (selectedProvider != null && selectedProvider == homeViewModel?.currentProvider) {
                    Log.d("HomeFragment", "Received searched songs: $songs")
                     songsAdapter.updateSongs(songs.filterNotNull())
                }
            }
        }
    }

    private fun shimmer(showShimmer: Boolean, showRecyclerView: Boolean? = null) {
        binding?.apply {
            if (showShimmer) {
                shimmerView.startShimmer()
                shimmerView.isVisible = true
            } else {
                shimmerView.stopShimmer()
                shimmerView.isGone = true
            }

            showRecyclerView?.let { isVisible ->
                songsRecyclerView.isGone = !isVisible
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}