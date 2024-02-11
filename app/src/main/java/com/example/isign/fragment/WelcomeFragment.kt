package com.example.isign.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.isign.R
import com.example.isign.databinding.FragmentWelcomeBinding
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

    private var _fragmentWelcomeBinding: FragmentWelcomeBinding? = null

    private val fragmentWelcomeBinding
        get() = _fragmentWelcomeBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentWelcomeBinding =
            FragmentWelcomeBinding.inflate(inflater, container, false)

        return fragmentWelcomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentWelcomeBinding.btnStart.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_cameraFragment)
        }
        fragmentWelcomeBinding.btnListOfSign.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_signListFragment)
        }
        fragmentWelcomeBinding.btnLogout.setOnClickListener {
            it.findNavController().navigate(R.id.landingFragment)
        }
    }

    private fun navigateToCamera() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Navigation.findNavController(requireActivity(), R.id.fragmentContainerView).navigate(
                    R.id.landingFragment
                )
            }
        }
    }
}