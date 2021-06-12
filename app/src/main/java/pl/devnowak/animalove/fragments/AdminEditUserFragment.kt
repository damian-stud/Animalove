package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pl.devnowak.animalove.databinding.FragmentAdminEditUserBinding

class AdminEditUserFragment : Fragment() {

    lateinit var db: FirebaseFirestore
    private var _binding: FragmentAdminEditUserBinding? = null
    private val binding get() = _binding!!
    lateinit var newUserRole: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentAdminEditUserBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonAdminEditUserSend.setOnClickListener {
            val username = binding.editTextAdminEditUserLogin.editText?.text.toString()
            val radioGroupCheckedId = binding.radioGroupEditUserRole.checkedRadioButtonId
            if (username.isEmpty()) {
                binding.editTextAdminEditUserLogin.error = "Pole nie może być puste!"
            } else if (radioGroupCheckedId < 0) {
                Toast.makeText(context, "Wybierz rolę użytkownika!", Toast.LENGTH_LONG).show()
            } else {
                editUser()
            }
        }

        return view
    }

    private fun editUser() = CoroutineScope(Dispatchers.IO).launch {
        db = Firebase.firestore


        if (binding.radioButtonAdmin.isChecked) {
            newUserRole = "admin"
        }
        if (binding.radioButtonEmployee.isChecked) {
            newUserRole = "employee"
        }
        if (binding.radioButtonVolunteer.isChecked) {
            newUserRole = "volunteer"
        }
        if (binding.radioButtonUser.isChecked) {
            newUserRole = "user"
        }

        val username = binding.editTextAdminEditUserLogin.editText?.text.toString()
        val userQuery = db.collection("users").whereEqualTo("username", username).get().await()
        if (userQuery.documents.isNotEmpty()) {
            for (document in userQuery) {
                db.collection("users").document(document.id).update("role", newUserRole).addOnSuccessListener {
                    Snackbar.make(
                        requireView(),
                        "Poprawnie edytowano użytkownika!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}