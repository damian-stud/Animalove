package pl.devnowak.animalove

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.devnowak.animalove.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController)
        setupBottomNavMenu(navController)

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val registrationToken = FirebaseMessaging.getInstance().token.result
                MyFirebaseMessagingService.addTokenToFirestore(registrationToken)

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navigated = NavigationUI.onNavDestinationSelected(item!!, navController)
        return navigated || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupBottomNavMenu(navController: NavController) {
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    fun setStatus(status: String) {
        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser != null) {
            val ref = Firebase.firestore.collection("users").document(fUser.uid)
            val hashMap = HashMap<String, Any>()
            hashMap["status"] = status
            ref.update(hashMap)
        }
    }

    override fun onResume() {
        super.onResume()
        setStatus("online")
    }

    override fun onPause() {
        super.onPause()
        setStatus("offline")
    }

    override fun onDestroy() {
        super.onDestroy()
        setStatus("offline")
    }
}