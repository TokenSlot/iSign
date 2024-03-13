package com.example.isign.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.example.isign.MainActivity
import com.example.isign.R
import com.example.isign.databinding.FragmentLandingBinding
import com.example.isign.presentation.sign_in.GoogleAuthUiClient
import com.example.isign.presentation.sign_in.SignInViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LandingFragment : Fragment() {

    private var _fragmentLandingBinding: FragmentLandingBinding? = null
    private val viewModel: SignInViewModel by activityViewModels()
    private val fragmentLandingBinding
        get() = _fragmentLandingBinding!!

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext().applicationContext,
            oneTapClient = Identity.getSignInClient(requireContext().applicationContext)
        )
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentLandingBinding =
            FragmentLandingBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        return fragmentLandingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observer()

        fragmentLandingBinding.btnSignIn.setOnClickListener {
            onSignInClick()
        }
    }

    private fun observer() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                        state ->
                    if (state.isSignInSuccessful) {
                        Toast.makeText(requireContext(), "Sign In Success", Toast.LENGTH_LONG).show()
                        navigateToHome()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Navigation.findNavController(requireActivity(), R.id.fragmentContainerView).navigate(
                    R.id.action_landingFragment_to_welcomeFragment
                )
            }
        }
    }

    private fun onSignInClick() {
        fragmentLandingBinding.btnSignIn.isEnabled = false

        lifecycleScope.launch {
            val signInIntentSender = googleAuthUiClient.signIn()
            launcher.launch(
                IntentSenderRequest.Builder(
                    signInIntentSender ?: return@launch
                ).build()
            )
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val signInResult = googleAuthUiClient.signInWithIntent(
                    intent = result.data ?: return@launch
                )
                viewModel.onSignInResult(signInResult)
                fragmentLandingBinding.btnSignIn.isEnabled = true
            }
        } else {
            fragmentLandingBinding.btnSignIn.isEnabled = true
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