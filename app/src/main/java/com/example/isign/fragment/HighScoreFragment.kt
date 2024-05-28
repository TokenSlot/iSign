package com.example.isign.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.isign.MainViewModel
import com.example.isign.R
import com.example.isign.databinding.FragmentHighScoreBinding
import com.example.isign.presentation.sign_in.GoogleAuthUiClient
import com.example.isign.presentation.sign_in.UserData
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.firestore.FirebaseFirestore

class HighScoreFragment : Fragment() {

    private var _fragmentHighScoreBinding: FragmentHighScoreBinding? = null
    private val fragmentHighScoreBinding
        get() = _fragmentHighScoreBinding!!

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext().applicationContext,
            oneTapClient = Identity.getSignInClient(requireContext().applicationContext)
        )
    }

    private val db = FirebaseFirestore.getInstance()
    private val viewModel: MainViewModel by activityViewModels()
    private var userData: UserData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentHighScoreBinding =
            FragmentHighScoreBinding.inflate(inflater, container, false)

        userData = googleAuthUiClient.getSignedInUser()

        retrieveHighScore()

        return fragmentHighScoreBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentHighScoreBinding.btnPlayAgain.setOnClickListener {
            it.findNavController().navigate(R.id.action_highScoreFragment_to_cameraFragment)
        }
        fragmentHighScoreBinding.btnBackHome.setOnClickListener {
            it.findNavController().navigate(R.id.action_highScoreFragment_to_welcomeFragment)
        }
    }

    private fun retrieveHighScore() {
        Log.d("Leaderboard", "Im working on it")
        val docRef = db.collection("leaderboard").document(userData?.userId!!)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val highScore = document.getLong("score") ?: 0
                    val currentScore = viewModel.currentScore

                    updateUI(highScore, currentScore)
                } else {
                    Log.d("Leaderboard", "User Not Found")
                    addUserToLeaderboard()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Leaderboard", "get failed with ", exception)
            }
    }

    private fun updateUI(highScore: Long, currentScore: Long) {
        fragmentHighScoreBinding.tvHighScore.text = "$highScore"
        fragmentHighScoreBinding.tvScore.text = "$currentScore"

        var comparisonMsg= "Keep playing to beat your high score!"
        if (currentScore > highScore) {
            if (highScore == 0L) {
                fragmentHighScoreBinding.tvHighScore.text = ""
                comparisonMsg = "Your new high score!"
            } else {
                comparisonMsg = "Congratulations! You beat your high score."
            }

            updateUserData(currentScore)
        }

        fragmentHighScoreBinding.tvHighScoreLabel.text = comparisonMsg
    }

    private fun updateUserData(currentScore: Long) {
        val docRef = db.collection("leaderboard").document(userData?.userId!!)

        val name = userData?.username

        val userInput = mapOf(
            "username" to name!!.split(" ")[0],
            "score" to currentScore
        )

        docRef.update(userInput).addOnFailureListener {
            addUserToLeaderboard()
        }
    }

    private fun addUserToLeaderboard() {
        Log.d("Leaderboard", "Creating new user...")
        val initScore = viewModel.currentScore
        val userInput = hashMapOf(
            "username" to userData?.username,
            "score" to initScore
        )

        db.collection("leaderboard").document(userData?.userId!!)
            .set(userInput)
            .addOnSuccessListener {
                Log.d("Leaderboard", "Successfully Added")
                updateUI(0, initScore)
            }.addOnFailureListener { exception ->
                Log.d("Leaderboard", "get failed with ", exception)
            }
    }
}