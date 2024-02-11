package com.example.isign.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.isign.MainViewModel
import com.example.isign.R
import com.example.isign.databinding.FragmentSignListBinding

class SignListFragment : Fragment() {

    private var _fragmentSignListBinding: FragmentSignListBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    private var _adapter: SignListAdapter? = null
    private val signNames = arrayOf(
        "FSL Letter A", "FSL Letter B","FSL Letter C","FSL Letter D",
        "FSL Letter E","FSL Letter F","FSL Letter G","FSL Letter H",
        "FSL Letter I","FSL Letter J","FSL Letter K","FSL Letter L",
        "FSL Letter M","FSL Letter N","FSL Letter O","FSL Letter P",
        "FSL Letter Q","FSL Letter R","FSL Letter S","FSL Letter T",
        "FSL Letter U","FSL Letter V","FSL Letter W","FSL Letter X",
        "FSL Letter Y","FSL Letter Z")

    private val signImages = intArrayOf(
        R.drawable.fsl_letter_a,
        R.drawable.fsl_letter_b,
        R.drawable.fsl_letter_c,
        R.drawable.fsl_letter_d,
        R.drawable.fsl_letter_e,
        R.drawable.fsl_letter_f,
        R.drawable.fsl_letter_g,
        R.drawable.fsl_letter_h,
        R.drawable.fsl_letter_i,
        R.drawable.fsl_letter_j,
        R.drawable.fsl_letter_k,
        R.drawable.fsl_letter_l,
        R.drawable.fsl_letter_m,
        R.drawable.fsl_letter_n,
        R.drawable.fsl_letter_o,
        R.drawable.fsl_letter_p,
        R.drawable.fsl_letter_q,
        R.drawable.fsl_letter_r,
        R.drawable.fsl_letter_s,
        R.drawable.fsl_letter_t,
        R.drawable.fsl_letter_u,
        R.drawable.fsl_letter_v,
        R.drawable.fsl_letter_w,
        R.drawable.fsl_letter_x,
        R.drawable.fsl_letter_y,
        R.drawable.fsl_letter_z,
    )

    private val fragmentSignListBinding
        get() = _fragmentSignListBinding!!

    private val adapter
        get() = _adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSignListBinding =
            FragmentSignListBinding.inflate(inflater, container, false)

        val manager = LinearLayoutManager(requireContext())
        fragmentSignListBinding.signRecyclerView.layoutManager = manager

        _adapter = SignListAdapter(signNames, signImages)
        fragmentSignListBinding.signRecyclerView.adapter = adapter

        return fragmentSignListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter?.setOnItemClickListener(object: SignListAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                viewModel.setListData(signNames[position], signImages[position])
                view.findNavController().navigate(R.id.action_signListFragment_to_listDataFragment)
            }
        })
    }
}