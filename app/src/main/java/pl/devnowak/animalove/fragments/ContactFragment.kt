package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.devnowak.animalove.databinding.FragmentContactBinding
import pl.devnowak.animalove.databinding.FragmentHomeBinding

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }
}