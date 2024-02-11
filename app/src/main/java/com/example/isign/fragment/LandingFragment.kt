package com.example.isign.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.isign.MainActivity
import com.example.isign.R
import com.example.isign.databinding.FragmentLandingBinding
import com.example.isign.presentation.sign_in.GoogleAuthUiClient
import com.example.isign.presentation.sign_in.SignInViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
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
            viewModel.state.collect {
                    state ->
                if (state.isSignInSuccessful) {
                    Toast.makeText(requireContext().applicationContext, "Sign In Success", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext().applicationContext, state.signInError, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onSignInClick() {
        lifecycleScope.launch {
            launcher.launch(googleSignInClient.signInIntent)
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val signInResult = googleAuthUiClient.signInWithIntent(
                    intent = result.data ?: return@launch
                )
                viewModel.onSignInResult(signInResult)
            }
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            Toast.makeText(requireContext(), "SIGN IN COMPLETE", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT).show()
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