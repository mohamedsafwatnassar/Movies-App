package com.example.moviesapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesapp.MainActivity
import com.example.moviesapp.R
import com.example.moviesapp.databinding.FragmentTopRatedBinding
import com.example.moviesapp.handleData.HandleError
import com.example.moviesapp.ui.adapter.HomeAdapter
import com.example.moviesapp.ui.models.ItemDetails
import com.example.moviesapp.ui.viewmodel.HomeViewModel

class TopRatedFragment : Fragment() {

    private lateinit var viewRef: FragmentTopRatedBinding

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var homeAdapter: HomeAdapter

    private val onFavouriteCallback: (movie: ItemDetails, position: Int) -> Unit = { movie, pos ->
        if (movie.isFavourite) viewModel.removeFromFavourite(movie)
        else viewModel.addToFavourite(movie)
        movie.isFavourite = !movie.isFavourite
        homeAdapter.notifyItemChanged(pos)
    }

    private val onMovieCallback: (movie: ItemDetails) -> Unit = { movie ->
        viewModel.selectedMovie = movie
        findNavController().navigate(R.id.action_topRatedFragment_to_movieDetailsFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewRef = FragmentTopRatedBinding.inflate(inflater, container, false)

        setBtnListeners()

        viewModel.getTopRated()

        subscribeToHandleData()

        return viewRef.root
    }

    private fun subscribeToHandleData() {
        viewModel.handleError.observe(viewLifecycleOwner, {
            it.let {
                when (it.getStatus()) {
                    HandleError.DataStatus.SUCCESS -> {
                        (requireActivity() as MainActivity?)!!.hideProgressBar()
                        viewRef.txtConnectionFailure.visibility = View.GONE
                        initRecycler(it.getData())
                    }
                    HandleError.DataStatus.ERROR -> {
                        (requireActivity() as MainActivity?)!!.hideProgressBar()
                        Toast.makeText(requireContext(), it.getError(), Toast.LENGTH_SHORT).show()
                    }
                    HandleError.DataStatus.CONNECTIONERROR -> {
                        viewRef.txtConnectionFailure.visibility = View.VISIBLE
                        (requireActivity() as MainActivity?)!!.hideProgressBar()
                        Toast.makeText(
                            requireContext(),
                            it.getConnectionError(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun setBtnListeners() {
        viewRef.refreshTopRated.apply {
            viewModel.getTopRated()
            viewRef.refreshTopRated.isRefreshing = false
        }

        viewRef.searchViewTopRated.BaseSearchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                this@TopRatedFragment.homeAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun initRecycler(data: ArrayList<ItemDetails>?) {
        viewRef.recyclerTopRated.apply {
            layoutManager = LinearLayoutManager(requireContext())
            homeAdapter = HomeAdapter(data!!, onFavouriteCallback, onMovieCallback)
            adapter = homeAdapter
        }
    }

}