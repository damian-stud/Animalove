package pl.devnowak.animalove.fragments

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.FragmentUserProfileBinding
import pl.devnowak.animalove.model.User
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class UserProfileFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    val storageRef = Firebase.storage.reference
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private var isImageAdded = false

    val image = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.imageViewProfileUserImage.setImageURI(uri)
            isImageAdded = true
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonProfileAddImage.setOnClickListener {
            image.launch("image/*")
        }

        binding.buttonProfileSave.setOnClickListener {
            if (isImageAdded) {
                val image: ImageView = binding.imageViewProfileUserImage
                val drawable: BitmapDrawable? = image.drawable as BitmapDrawable

                if (drawable != null) {
                    val bitmap = drawable.bitmap
                    GlobalScope.launch {

                        val os = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 1, os)
                        val compressedImage = os.toByteArray()

                        val date = Date(System.currentTimeMillis())

                        val formatter = SimpleDateFormat("yyMMddHHmmssSSS", Locale.getDefault())
                        val timestamp = formatter.format(date)

                        val myImagesRef = storageRef.child("/images/users/" + timestamp + ".jpg")
                        var uploadTask = myImagesRef.putBytes(compressedImage)
                        uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            myImagesRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                saveUserProfile(downloadUri.toString())
                            } else {
                                Snackbar.make(
                                        requireView(),
                                        "Nie można dodać zdjęcia do bazy danych!",
                                        Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    saveUserProfile(null)
                }
            } else {
                saveUserProfile(null)
            }
        }

        return view
    }

    private fun saveUserProfile(path: String?) = CoroutineScope(Dispatchers.IO).launch {
        val newUserEmail = binding.editTextProfileEmail.editText?.text.toString()
        val newUserLogin = binding.editTextProfileLogin.editText?.text.toString()
        val newUserPassword = binding.editTextProfilePassword.editText?.text.toString()
        val changePassword = binding.checkBoxProfilePasswordChange.isChecked
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val user = auth.currentUser
            db = Firebase.firestore
            val userRef = db.collection("users").document(user!!.uid)
            val userDetails = userRef.get().await()
            val userEmail = userDetails.get("email")
            val userLogin = userDetails.get("login")
            val emailRegex = Regex("([A-Za-z0-9]+@[A-Za-z0-9]+.[A-Za-z]+)")


            if (changePassword && newUserPassword.isNotBlank()) {
                user.updatePassword(newUserPassword)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pomyślnie zaktualizowano hasło! $newUserPassword", Toast.LENGTH_LONG).show()
                }
            }
            if (path != null) {
                db.collection("users").document(user.uid).update( "profileImagePath", path)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pomyślnie zaktualizowano zdjęcie profilowe!", Toast.LENGTH_LONG).show()
                }
            }
            if (newUserEmail.isNotBlank() && !newUserEmail.matches(emailRegex)) {
                withContext(Dispatchers.Main) {
                    binding.editTextProfileEmail.editText?.error = "Błędny format email!"
                }
            } else if (userEmail != newUserEmail && userLogin != newUserLogin
                    && newUserEmail.isNotBlank() && newUserLogin.isNotBlank()
                    && newUserEmail.matches(emailRegex)) {
                db.collection("users").document(user.uid).update("email", newUserEmail, "login", newUserLogin)
                user.updateEmail(newUserEmail)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pomyślnie zaktualizowano dane!", Toast.LENGTH_LONG).show()
                }
            } else if (userEmail != newUserEmail && newUserEmail.isNotBlank()
                    && newUserEmail.matches(emailRegex)) {
                db.collection("users").document(user.uid).update("email", newUserEmail)
                user.updateEmail(newUserEmail)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pomyślnie zaktualizowano email!", Toast.LENGTH_LONG).show()
                }
            } else if (userLogin != newUserLogin && newUserLogin.isNotBlank()) {
                db.collection("users").document(user.uid).update( "login", newUserLogin)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Pomyślnie zaktualizowano login!", Toast.LENGTH_LONG).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Nie zaktualizowano danych, puste pola!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findUserData()
    }

    private fun findUserData() = CoroutineScope(Dispatchers.IO).launch {
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val user = auth.currentUser
            db = Firebase.firestore
            val userRef = db.collection("users").document(user!!.uid)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                val userData = documentSnapshot.toObject<User>()

                val userRole = userData?.role
                val userName = userData?.username
                val userEmail = userData?.email
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        if (userRole.equals("user")) {
                            val userRoleTextInfo = binding.textViewProfileRole.text.toString() + " użytkownik"
                            binding.textViewProfileRole.text = userRoleTextInfo
                        }
                        if (userRole.equals("employee")) {
                            val userRoleTextInfo = binding.textViewProfileRole.text.toString() + " pracownik"
                            binding.textViewProfileRole.text = userRoleTextInfo
                        }
                        if (userRole.equals("volunteer")) {
                            val userRoleTextInfo = binding.textViewProfileRole.text.toString() + " wolontariusz"
                            binding.textViewProfileRole.text = userRoleTextInfo
                        }
                        if (userRole.equals("admin")) {
                            val userRoleTextInfo = binding.textViewProfileRole.text.toString() + " administrator"
                            binding.textViewProfileRole.text = userRoleTextInfo
                        }

                        val userNameTextInfo = binding.textViewProfileUsername.text.toString() + " " + userName.toString()
                        binding.textViewProfileUsername.text = userNameTextInfo

                        val userEmailTextInfo = binding.textViewProfileEmail.text.toString() + " " + userEmail.toString()
                        binding.textViewProfileEmail.text = userEmailTextInfo
                    }
                }

                val profileImagePath = userData!!.profileImagePath

                if (profileImagePath == "default") {
                    binding.imageViewProfileUserImage.setImageResource(R.mipmap.ic_launcher)
                }
                if (profileImagePath != "default") {
                    Glide.with(requireView()).load(profileImagePath).fitCenter().centerCrop().into(
                            binding.imageViewProfileUserImage
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}