package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import pl.devnowak.animalove.databinding.FragmentLogInBeforeBinding

class LogInBeforeFragment : Fragment() {

    private var _binding: FragmentLogInBeforeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLogInBeforeBinding.inflate(inflater, container, false)
        val view = binding.root

        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(false)

        binding.pleaseLogIn.setOnClickListener {
            val action = LogInBeforeFragmentDirections.actionLogInBeforeFragmentToSignInFragment()
            view.findNavController().navigate(action)
        }

        return view
    }

}