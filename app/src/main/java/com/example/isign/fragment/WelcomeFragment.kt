package com.example.isign.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.isign.MainActivity
import com.example.isign.MainViewModel
import com.example.isign.R
import com.example.isign.databinding.FragmentWelcomeBinding
import com.example.isign.presentation.sign_in.GoogleAuthUiClient
import com.example.isign.presentation.sign_in.UserData
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

    private var _fragmentWelcomeBinding: FragmentWelcomeBinding? = null
    private val fragmentWelcomeBinding
        get() = _fragmentWelcomeBinding!!

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext().applicationContext,
            oneTapClient = Identity.getSignInClient(requireContext().applicationContext)
        )
    }

    private val viewModel: MainViewModel by activityViewModels()
    private var userData: UserData? = null

    private fun observer() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (userData != null) {
                    fragmentWelcomeBinding.tvTitle.text = userData!!.username
                    Glide.with(this@WelcomeFragment)
                        .load(userData!!.profilePictureUrl)
                        .into(fragmentWelcomeBinding.profileUser)
                } else {
                    navigateToLanding()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentWelcomeBinding =
            FragmentWelcomeBinding.inflate(inflater, container, false)

        userData = googleAuthUiClient.getSignedInUser()

        return fragmentWelcomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observer()

        fragmentWelcomeBinding.btnStart.setOnClickListener {
            viewModel.setIsGameMode(false)
            it.findNavController().navigate(R.id.action_welcomeFragment_to_cameraFragment)
        }
        fragmentWelcomeBinding.btnGameStart.setOnClickListener {
            viewModel.setIsGameMode(true)
            it.findNavController().navigate(R.id.action_welcomeFragment_to_cameraFragment)
        }
        fragmentWelcomeBinding.btnListOfSign.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_signListFragment)
        }
        fragmentWelcomeBinding.btnLeaderboard.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_leaderboardFragment)
        }
        fragmentWelcomeBinding.btnLogout.setOnClickListener {
            signOut()
            navigateToLanding()
        }
    }

    private fun navigateToLanding() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Navigation.findNavController(requireActivity(), R.id.fragmentContainerView).navigate(
                    R.id.action_welcomeFragment_to_landingFragment
                )
            }
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            googleAuthUiClient.signOut()
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).supportActionBar!!.show()
    }
}