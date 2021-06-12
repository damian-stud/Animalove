package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import pl.devnowak.animalove.MainActivity
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonSignInSignUp.setOnClickListener { Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_signUpFragment) }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.buttonSignIn.setOnClickListener {
            val email = binding.editTextEmailSignIn.editText?.text.toString()
            val password = binding.editTextPasswordLogIn.editText?.text.toString()
            val emailRegex = Regex("([A-Za-z0-9]+@[A-Za-z0-9]+.[A-Za-z]+)")

            if (email.isEmpty()) {
                binding.editTextEmailSignIn.error = "Pole nie może być puste!"
            }
            if (password.isEmpty()) {
                binding.editTextPasswordLogIn.error = "Pole nie może być puste!"
            }
            if (email.isNotEmpty() && !email.matches(emailRegex)) {
                binding.editTextEmailSignIn.error = "Wprowadź poprawny email!"
            } else if (email.isNotEmpty() && password.isNotEmpty()) {
                signInUser(it, email, password)
            }
        }
    }

    private fun signInUser(view: View, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }
                        val token = task.result
                        val registrationTokens = mapOf("registrationTokens" to token)

                        db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
                            val oldToken = it["deviceToken"]

                            if (oldToken != token) {
                                val data = hashMapOf("deviceToken" to token)
                                db.collection("users").document(auth.currentUser!!.uid).update(data as Map<String, Any>)
                            }

                            Navigation.findNavController(view).navigate(R.id.action_signInFragment_to_settFragment)

                            val mainActivity = MainActivity()
                            mainActivity.setStatus("online")
                        }
                    })
                }.addOnFailureListener {
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}