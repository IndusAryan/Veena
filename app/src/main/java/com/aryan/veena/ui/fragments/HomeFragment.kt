package com.aryan.veena.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aryan.veena.R
import com.aryan.veena.databinding.FragmentHomeBinding
import com.aryan.veena.ui.adapters.SearchSuggestionsAdapter
import com.aryan.veena.ui.adapters.SongsAdapter
import com.aryan.veena.utils.CoroutineUtils.ioScope
import com.aryan.veena.utils.ToastUtil.showToast
import com.aryan.veena.viewmodels.HomeViewModel
import com.aryan.veena.viewmodels.HomeViewModel.Resource
import org.schabi.newpipe.extractor.ServiceList.YouTube


class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding? = null
    private var homeViewModel : HomeViewModel? = null
    private val binding get() = _binding
    private val songsAdapter  by lazy { SongsAdapter(emptyList()) }
    private val searchSuggestionsAdapter by lazy { SearchSuggestionsAdapter(emptyList())}
    //private var searchSuggestions = mutableListOf<String>()

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

        setupRecyclerView()
        observeHomeViewModel()
        setupChipGroup()
        binding?.toolbar?.setTitleTextColor(
            ContextCompat.getColor(
                context ?: return ,
                R.color.grayTextColor
            )
        )
        binding?.searchSuggestions?.apply {
            adapter = searchSuggestionsAdapter
            layoutManager = LinearLayoutManager(context)
            setOnClickListener {

            }
        }

        binding?.searchView?.apply {
            editText.doOnTextChanged { _, _, _, _ ->
                if (editText.text.length >= 3) {
                    ioScope {
                        try {
                            val suggestionsYT =
                                YouTube.suggestionExtractor.suggestionList(editText.text.toString())
                            Log.i("SUGGESTIONS", "$suggestionsYT")
                            activity?.runOnUiThread {
                                searchSuggestionsAdapter.updateSuggestions(suggestionsYT)
                            }
                            //addAll(SoundCloud.suggestionExtractor.suggestionList(editText.text.toString()))
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                }
            }


            /*binding?.searchSuggestions?.apply {
                adapter = searchSuggestionsAdapter
                layoutManager = LinearLayoutManager(context)

                setOnItemClickListener { _, _, position, _ ->
                    binding?.searchBar?.setText(searchSuggestions[position])
                }
            }*/

            editText.setOnEditorActionListener { textView, _, _ ->
                val query = textView.text.toString()
                Log.i("SearchView", "${homeViewModel?.searchQuery?.value}")

                fun search() {
                    // Only call search if the new query is different from current text
                    homeViewModel?.search(query)
                    binding?.searchBar?.setText(query) // Update search bar text
                }
                if (query != binding?.searchBar?.text.toString()) {
                    search()

                        // Only call search if the new query is different from current text
                        homeViewModel?.search(query)
                        binding?.searchBar?.setText(query) // Update search bar text
                   }
                    hide()
                    true
            }
        }
    }

    private fun setupChipGroup() {
        binding?.providerChips?.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.jiosaavn -> { homeViewModel?.selectProvider(HomeViewModel.Provider.JIO_SAAVN) }
                    R.id.yt -> { homeViewModel?.selectProvider(HomeViewModel.Provider.YOUTUBE) }
                }
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
            selectedProvider.observe(viewLifecycleOwner) {
                if (!searchQuery.value.isNullOrEmpty()) {
                 homeViewModel?.search(searchQuery.value.toString()) }
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
                songs?.let {
                    songsAdapter.updateSongs(it)
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