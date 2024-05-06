package com.example.isign.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.isign.MainViewModel
import com.example.isign.R
import com.example.isign.databinding.FragmentListDataBinding

class ListDataFragment : Fragment() {

    private var _fragmentListDataBinding: FragmentListDataBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    private val fragmentListDataBinding
        get() = _fragmentListDataBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentListDataBinding =
            FragmentListDataBinding.inflate(inflater, container, false)

        return fragmentListDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = viewModel.dataText

        fragmentListDataBinding.dataText.text = viewModel.dataText
        fragmentListDataBinding.imageView.setImageResource(viewModel.dataImage)

        fragmentListDataBinding.btnLearn.setOnClickListener {
            it.findNavController().navigate(R.id.action_listDataFragment_to_tutPermFragment)
        }
    }
}