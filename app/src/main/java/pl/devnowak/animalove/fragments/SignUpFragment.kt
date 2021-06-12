package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    var db = Firebase.firestore
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonSignUpSignIn.setOnClickListener { Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signInFragment) }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.buttonSignUp.setOnClickListener {
            val username = binding.editTextLoginSignUp.editText?.text.toString()
            val email = binding.editTextEmailSignUp.editText?.text.toString()
            val password = binding.editTextPasswordSignUp.editText?.text.toString()
            val emailRegex = Regex("([A-Za-z0-9]+@[A-Za-z0-9]+.[A-Za-z]+)")

            if (username.isEmpty()) {
                binding.editTextLoginSignUp.error = "Pole nie może być puste!"
            }
            if (email.isEmpty()) {
                binding.editTextEmailSignUp.error = "Pole nie może być puste!"
            }
            if (password.isEmpty()) {
                binding.editTextPasswordSignUp.error = "Pole nie może być puste!"
            }
            if (email.isNotEmpty() && !email.matches(emailRegex)) {
                binding.editTextEmailSignUp.error = "Wprowadź poprawny email!"
            } else if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signUpUser(it, email, password, username)
            }
        }
    }

    private fun signUpUser(view: View, email: String, password: String, username: String, role: String = "user", userProfileImage: String = "default") {
        CoroutineScope(Dispatchers.IO).launch {
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                val firebaseUser = auth.currentUser
                val userId = firebaseUser.uid

                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result
                    val data = hashMapOf("id" to userId, "deviceToken" to token, "username" to username, "email" to email,  "role" to role, "profileImagePath" to userProfileImage)
                    db.collection("users").document(userId).set(data)
                    Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_destination_settings)
                })
            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}