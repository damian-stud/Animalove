package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pl.devnowak.animalove.R

class AdminFragment : PreferenceFragmentCompat() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_admin, rootKey)
        checkRole()

        val preferenceAddAnimal: Preference? = findPreference("add_animal")

        preferenceAddAnimal?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.adminAddAnimalFragment)
            true
        }

        val preferenceEditAnimal: Preference? = findPreference("edit_animal")

        preferenceEditAnimal?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.adminEditAnimalFragment)
            true
        }

        val preferenceDeleteAnimal: Preference? = findPreference("delete_animal")

        preferenceDeleteAnimal?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.adminDeleteAnimalFragment)
            true
        }

        val preferenceEditUser: Preference? = findPreference("edit_user")

        preferenceEditUser?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.adminEditUserFragment)
            true
        }
    }

    private fun checkRole() = CoroutineScope(Dispatchers.IO).launch {
        val addPreference: Preference? = findPreference("add_animal")
        val editPreference: Preference? = findPreference("edit_animal")
        val deletePreference: Preference? = findPreference("delete_animal")
        val editUserPreference: Preference? = findPreference("edit_user")

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        if (auth.currentUser != null) {
            val user = auth.currentUser
            val userDetails = db.collection("users").document(user!!.uid).get().await()
            val userRole = userDetails.get("role")
            withContext(Dispatchers.Main) {
                if (userRole == "admin") {
                    addPreference?.isVisible = true
                    editPreference?.isVisible = true
                    deletePreference?.isVisible = true
                    editUserPreference?.isVisible = true
                } else if (userRole == "employee") {
                    addPreference?.isVisible = true
                    editPreference?.isVisible = true
                    deletePreference?.isVisible = false
                    editUserPreference?.isVisible = false
                } else if (userRole == "volunteer") {
                    addPreference?.isVisible = false
                    editPreference?.isVisible = true
                    deletePreference?.isVisible = false
                    editUserPreference?.isVisible = false
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                addPreference?.isVisible = false
                editPreference?.isVisible = false
                deletePreference?.isVisible = false
                editUserPreference?.isVisible = false
            }
        }
    }

}