package com.aryan.veena.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aryan.veena.R
import com.aryan.veena.databinding.FragmentHomeBinding
import com.aryan.veena.ui.activities.QRActivity
import com.aryan.veena.ui.adapters.SongsAdapter
import com.aryan.veena.utils.ToastUtil.showToast
import com.aryan.veena.viewmodels.HomeViewModel
import com.aryan.veena.viewmodels.HomeViewModel.Resource
import com.aryan.veena.viewmodels.SharedViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val viewModel: HomeViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val songsRecyclerView: RecyclerView? = null
    private var songsAdapter : SongsAdapter? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val qrActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedText = result.data?.getStringExtra("SCANNED_TEXT")
            scannedText?.let {
                sharedViewModel.setScannedText(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        songsRecyclerView?.layoutManager = LinearLayoutManager(context)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        observeSharedViewModel()
        observeHomeViewModel()

        binding.cameraButton.setOnClickListener {
            val intent = Intent(context, QRActivity::class.java)
            qrActivityResultLauncher.launch(intent)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setTitleTextColor(
            ContextCompat.getColor(
                context ?: return ,
                R.color.grayTextColor
            )
        )

        val searchView = binding.searchView

        searchView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View) {
                // handle when SearchView is attached
            }

            override fun onViewDetachedFromWindow(p0: View) {
                // handle when SearchView is detached
            }
        })

        searchView.editText.setOnEditorActionListener { textView, _, _ ->
            val query = textView.text.toString()
            Log.i("SearchView", query)
            viewModel.searchSaavn(query)
            searchView.hide()
            //observeViewModel()
            true
        }
    }

    private fun setupRecyclerView() {
        songsAdapter = SongsAdapter(emptyList())
        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songsAdapter
        }
    }

    private fun observeSharedViewModel() {
        sharedViewModel.scannedText.observe(viewLifecycleOwner) { scannedText ->
            if (scannedText.isNotEmpty()) {
                //binding.searchView.editText.setText(scannedText)
                viewModel.searchSaavn(scannedText)
            }
        }
    }

    private fun observeHomeViewModel() {
        viewModel.status.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.shimmerView.startShimmer()
                    binding.shimmerView.isVisible = true
                    binding.songsRecyclerView.isGone = true
                }

                is Resource.Success -> {
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.isGone = true
                    binding.songsRecyclerView.isGone = false
                }

                is Resource.Error -> {
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.isGone = true
                    binding.songsRecyclerView.isGone = true
                    showToast(context ?: return@observe, resource.message ?: R.string.unknown_error)
                }
            }
        }

        viewModel.searchedSong.observe(viewLifecycleOwner) { songs ->
            songs?.let {
                songsAdapter?.updateSongs(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}