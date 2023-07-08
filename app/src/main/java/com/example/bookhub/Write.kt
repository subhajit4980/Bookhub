package com.example.bookhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bookhub.databinding.FragmentStorylistBinding

class Write() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStorylistBinding.inflate(inflater, container, false)
        return binding.root
    }
}
