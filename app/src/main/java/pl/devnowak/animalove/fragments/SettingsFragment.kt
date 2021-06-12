package pl.devnowak.animalove.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
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
import pl.devnowak.animalove.MainActivity
import pl.devnowak.animalove.R

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)

        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(false)

        checkIfSignIn()

        val preferenceAdmin: Preference? = findPreference("admin")

        preferenceAdmin?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.adminFragment)
            true
        }

        val preferenceVirtualAdoption: Preference? = findPreference("virtual_adoption")

        preferenceVirtualAdoption?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.virtualAdoptionFragment)
            true
        }

        val preferenceFundraising: Preference? = findPreference("fundraising")

        preferenceFundraising?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.fundraisingFragment)
            true
        }

        val preferenceProfile: Preference? = findPreference("profile")

        preferenceProfile?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.userProfileFragment)
            true
        }


        val preferenceSignIn: Preference? = findPreference("signIn")

        preferenceSignIn?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.signInFragment)
            true
        }

        val preferenceSignUp: Preference? = findPreference("signUp")

        preferenceSignUp?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.signUpFragment)
            true
        }

        val preferenceSignOut: Preference? = findPreference(getString(R.string.sign_out))

        preferenceSignOut?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            signOut()
            true
        }

        val preferenceAbout: Preference? = findPreference("about")

        preferenceAbout?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.aboutFragment)
            true
        }
    }

    private fun checkIfSignIn() = CoroutineScope(Dispatchers.IO).launch {
        val userPreference: Preference? = findPreference("currentUser")
        val adminPreference: Preference? = findPreference("admin")
        val virtualAdoptionPreference: Preference? = findPreference("virtual_adoption")
        val fundraisingPreference: Preference? = findPreference("fundraising")
        val profilePreference: Preference? = findPreference("profile")
        val signInPreference: Preference? = findPreference("signIn")
        val signUpPreference: Preference? = findPreference("signUp")
        val aboutPreference: Preference? = findPreference("about")
        val signOutPreference: Preference? = findPreference(getString(R.string.sign_out))

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        if (auth.currentUser != null) {
            val user = auth.currentUser
            val userDetails = db.collection("users").document(user!!.uid).get().await()
            val userRole = userDetails.get("role")
            val userLogin = userDetails.get("username")
            withContext(Dispatchers.Main) {
                userPreference?.isVisible = true
                userPreference?.summary = userLogin.toString()
                virtualAdoptionPreference?.isVisible = true
                fundraisingPreference?.isVisible = true
                profilePreference?.isVisible = true
                signInPreference?.isVisible = false
                signUpPreference?.isVisible = false
                aboutPreference?.isVisible = true
                signOutPreference?.isVisible = true

                if (userRole == "employee" || userRole == "volunteer" || userRole == "admin") {
                    adminPreference?.isVisible = true
                } else {
                    adminPreference?.isVisible = false
                }
            }

        } else {
            adminPreference?.isVisible = false
            virtualAdoptionPreference?.isVisible = true
            fundraisingPreference?.isVisible = true
            profilePreference?.isVisible = false
            signInPreference?.isVisible = true
            signUpPreference?.isVisible = true
            userPreference?.isVisible = false
            aboutPreference?.isVisible = true
            signOutPreference?.isVisible = false
        }
    }


    private fun signOut() {
        val mainActivity = MainActivity()
        mainActivity.setStatus("offline")

        FirebaseAuth.getInstance().signOut()
        checkIfSignIn()
    }

}