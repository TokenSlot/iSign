package com.example.isign.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.isign.LeaderboardEntry
import com.example.isign.databinding.RowLeaderboardEntryBinding

class LeaderboardAdapter(
    private val entries: MutableList<LeaderboardEntry>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LeaderboardHolder {
        val binding = RowLeaderboardEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return LeaderboardHolder(binding)
    }

    override fun getItemCount() = entries.size

    override fun onBindViewHolder(holder: LeaderboardHolder, position: Int) {
        val pos: Int = position + 1
        holder.binding.positionTextView.text = pos.toString()
        holder.binding.nameTextView.text = entries[position].userName
        holder.binding.scoreTextView.text  = entries[position].score.toString()
    }

    class LeaderboardHolder(val binding: RowLeaderboardEntryBinding): RecyclerView.ViewHolder(binding.root) {
        val pos: TextView = binding.positionTextView
        val name: TextView = binding.nameTextView
        val score: TextView = binding.scoreTextView
    }

}