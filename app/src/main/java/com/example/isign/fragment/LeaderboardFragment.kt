package com.example.isign.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.isign.LeaderboardEntry
import com.example.isign.MainViewModel
import com.example.isign.R
import com.example.isign.databinding.FragmentHighScoreBinding
import com.example.isign.databinding.FragmentLeaderboardBinding
import com.example.isign.presentation.sign_in.GoogleAuthUiClient
import com.example.isign.presentation.sign_in.UserData
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardFragment : Fragment() {

    private var _fragmentLeaderboardBinding: FragmentLeaderboardBinding? = null
    private val fragmentLeaderboardBinding
        get() = _fragmentLeaderboardBinding!!

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
        _fragmentLeaderboardBinding=
            FragmentLeaderboardBinding.inflate(inflater, container, false)

        userData = googleAuthUiClient.getSignedInUser()

        val manager = LinearLayoutManager(requireContext())
        fragmentLeaderboardBinding.boardRecyclerView.layoutManager = manager

        retrieveLeaderboard()

        return fragmentLeaderboardBinding.root
    }

    private fun retrieveLeaderboard() {
        val db = FirebaseFirestore.getInstance()
        val leaderboardRef = db.collection("leaderboard")

        leaderboardRef.orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<LeaderboardEntry>()

                for (document in documents) {
                    val userName = document.getString("username") ?: ""
                    val score = document.getLong("score") ?: 0
                    entries.add(LeaderboardEntry(userName, score))
                }
                val adapter = LeaderboardAdapter(entries)
                fragmentLeaderboardBinding.boardRecyclerView.adapter = adapter
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}