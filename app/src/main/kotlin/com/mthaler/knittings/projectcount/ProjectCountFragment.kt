package com.mthaler.knittings.projectcount

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.database.ProjectsDataSource
import com.mthaler.knittings.databinding.FragmentProjectCountBinding

class ProjectCountFragment : Fragment() {

    private var _binding: FragmentProjectCountBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProjectCountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ProjectCountViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // get all knittings from database
        val ds = KnittingsDataSource as ProjectsDataSource
        val projects = ds.allProjects

        _binding = FragmentProjectCountBinding.inflate(inflater, container, false)
        val view = binding.root

        val years = viewModel.createYearsList(projects)
        val categoryNames = viewModel.createCategoryNamesList()

        val yearAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, years)
        binding.yearSpinner.adapter = yearAdapter
        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val year = if (position == 0) null else Integer.parseInt(years[position])
                val p = binding.categorySpinner.selectedItemPosition
                val categoryName = if (p == 0) null else categoryNames[p]
                val projectCount = viewModel.getProjectCount(projects, year, categoryName)
                binding.projectCount.text = Integer.toString(projectCount) + " / " + Integer.toString(projects.size)
                val percent = if (projects.size > 0) 100.0 * projectCount / projects.size else 0.0
                binding.progressBarCircle.progress = percent.toInt()
            }
        }

        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, categoryNames)
        binding.categorySpinner.adapter = categoryAdapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val p = binding.yearSpinner.selectedItemPosition
                val year = if (p == 0) null else Integer.parseInt(years[p])
                val categoryName = if (position == 0) null else categoryNames[position]
                val projectCount = viewModel.getProjectCount(projects, year, categoryName)
                binding.projectCount.text = Integer.toString(projectCount) + " / " + Integer.toString(projects.size)
                val percent = if (projects.size > 0) 100.0 * projectCount / projects.size else 0
                binding.progressBarCircle.progress = percent.toInt()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}