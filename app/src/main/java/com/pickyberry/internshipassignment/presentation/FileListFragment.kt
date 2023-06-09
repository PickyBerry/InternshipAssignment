package com.pickyberry.internshipassignment.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.pickyberry.internshipassignment.MainActivity
import com.pickyberry.internshipassignment.R
import com.pickyberry.internshipassignment.databinding.FragmentFileListBinding
import com.pickyberry.internshipassignment.domain.SortTypes
import kotlinx.coroutines.launch
import javax.inject.Inject

//Main screen to display list of files
class FileListFragment : Fragment() {

    private lateinit var binding: FragmentFileListBinding
    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>
    private lateinit var recyclerFilesAdapter: FilesAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private val viewModel: FileListViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[FileListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFileListBinding.inflate(layoutInflater)

        //Inject dagger2 dependencies
        (activity as MainActivity).repositoryComponent.inject(this)



        setViewmodelObservers()
        setupRecycler()
        setupSpinner()
        overrideOnBackPressed()


        //Switch between showing all files and updated files
        binding.btnSwitch.setOnClickListener {
            viewModel.switchBetweenAllAndUpdated()
            binding.btnSwitch.text =
                if (viewModel.showingUpdatedFiles) resources.getString(R.string.see_all)
                else resources.getString(R.string.see_updated)
        }

        //Get files from clicked directory
        recyclerFilesAdapter.folderClicked.observe(viewLifecycleOwner) { folderPath ->
            viewModel.getFiles(folderPath)
        }


        return binding.root
    }

    private fun setViewmodelObservers() {
        //Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.sortSpinner.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.sortSpinner.visibility = View.VISIBLE
            }
        }

        //Observe list updates
        viewModel.currentFiles.observe(viewLifecycleOwner) {
            if (binding.sortSpinner.onItemSelectedListener == null) {
                binding.sortSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            itemSelected: View?, selectedItemPosition: Int, selectedId: Long,
                        ) {
                            lifecycleScope.launch {
                                viewModel.sort(type = SortTypes.from(selectedItemPosition)!!)
                            }
                            binding.recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                                binding.recyclerView.scrollToPosition(0)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            }
            recyclerFilesAdapter.setData(it)
        }
    }


    private fun setupRecycler() {
        recyclerFilesAdapter = FilesAdapter(requireContext())
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = recyclerFilesAdapter
        val dividerItemDecoration =
            DividerItemDecoration(requireContext(), layoutManager.orientation)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun setupSpinner() {
        spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.sorts, R.layout.custom_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.sortSpinner.adapter = spinnerAdapter
    }

    //Go to previous folder on back button pressed, if can't - exit app
    private fun overrideOnBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    if (isEnabled && !viewModel.goBack()) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
            )
    }


}