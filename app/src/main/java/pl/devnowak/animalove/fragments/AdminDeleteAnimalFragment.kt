package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pl.devnowak.animalove.databinding.FragmentAdminDeleteAnimalBinding

class AdminDeleteAnimalFragment : Fragment() {

    lateinit var db: FirebaseFirestore
    private var _binding: FragmentAdminDeleteAnimalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminDeleteAnimalBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.buttonAdminDeleteAnimalSend.setOnClickListener {
            val number = binding.editTextAdminDeleteAnimalNumber.editText?.text.toString()
            if (number.isEmpty()) {
                binding.editTextAdminDeleteAnimalNumber.error = "Pole nie może być puste!"
            } else {
                deleteAnimal()
            }
        }
        return view
    }

    private fun deleteAnimal() = CoroutineScope(Dispatchers.IO).launch {
        db = Firebase.firestore
        val number = binding.editTextAdminDeleteAnimalNumber.editText?.text.toString().toInt()
        val animalQuery = db.collection("animals").whereEqualTo("number", number).get().await()
        if (animalQuery.documents.isNotEmpty()) {
            for (document in animalQuery) {
                db.collection("animals").document(document.id).delete().await()
                Snackbar.make(
                        requireView(),
                        "Pomyślnie usunięto zwierzę z bazy danych!",
                        Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            Snackbar.make(
                    requireView(),
                    "Nie ma zwierzęcia o takim numerze w bazie danych!",
                    Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}