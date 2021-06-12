package pl.devnowak.animalove.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import pl.devnowak.animalove.R
import pl.devnowak.animalove.adapter.AnimalAdapter
import pl.devnowak.animalove.databinding.FragmentFavouriteBinding
import pl.devnowak.animalove.model.Animal

class FavouriteFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!
    val animals = mutableListOf<Animal?>()
    var animalAdapter = AnimalAdapter(animals, this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            findNavController().navigate(R.id.logInBeforeFragment)
        } else {
            showFavouriteAnimals()
        }

        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewAnimalsFav.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAnimalsFav.adapter = animalAdapter


    }

    private fun showFavouriteAnimals()  {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = firebaseUser!!.uid
            animals.clear()

            GlobalScope.launch(Dispatchers.IO) {
                db.collection("favouriteAnimals")
                        .whereArrayContains("users", currentUserId)
                        .get().addOnSuccessListener {
                            val virtualAdoptedAnimals = mutableListOf<Long>()
                            if (it.isEmpty) {
                                Toast.makeText(context, "Brak ulubionych zwierząt.", Toast.LENGTH_LONG).show()
                            } else {
                                for (document in it.documents) {
                                    virtualAdoptedAnimals.add(document.data?.get("number") as Long)
                                }
                                db.collection("animals").whereIn("number", virtualAdoptedAnimals)
                                        .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->

                                            if (e != null) {
                                                Log.w(ContentValues.TAG, "Listen error", e)
                                                return@addSnapshotListener
                                            }
                                            for (change in querySnapshot!!.documentChanges) {
                                                if (change.type == DocumentChange.Type.ADDED) {
                                                    val animal = change.document.toObject<Animal>()
                                                    animals.add(animal)
                                                    GlobalScope.launch {
                                                        withContext(Dispatchers.Main) {
                                                            animalAdapter.animals = animals
                                                            animalAdapter.notifyDataSetChanged()
                                                        }
                                                    }
                                                }
                                                if (change.type == DocumentChange.Type.MODIFIED) {
                                                    val animal = change.document.toObject<Animal>()
                                                    animals[querySnapshot.documents.indexOf(change.document)] =
                                                            animal
                                                    GlobalScope.launch {
                                                        withContext(Dispatchers.Main) {
                                                            animalAdapter.animals = animals
                                                            animalAdapter.notifyDataSetChanged()
                                                        }
                                                    }
                                                }
                                                if (change.type == DocumentChange.Type.REMOVED) {
                                                    val animal = change.document.toObject<Animal>()
                                                    val animalFromList =
                                                            animals.find { it?.number == animal.number }
                                                    animals.remove(animalFromList)
                                                    GlobalScope.launch {
                                                        withContext(Dispatchers.Main) {
                                                            animalAdapter.animals = animals
                                                            animalAdapter.notifyDataSetChanged()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                            }
                        }
            }

        } catch(e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}