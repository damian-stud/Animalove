package pl.devnowak.animalove.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import pl.devnowak.animalove.adapter.AnimalAdapter
import pl.devnowak.animalove.databinding.FragmentFundraisingBinding
import pl.devnowak.animalove.model.Animal

class FundraisingFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    private var _binding: FragmentFundraisingBinding? = null
    private val binding get() = _binding!!
    val animals = mutableListOf<Animal?>()
    var animalAdapter = AnimalAdapter(animals, this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentFundraisingBinding.inflate(inflater, container, false)
        val view = binding.root

        showFundraisings()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewAnimalsFund.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAnimalsFund.adapter = animalAdapter
    }

    private fun showFundraisings() = CoroutineScope(Dispatchers.IO).launch {
        try {
            animals.clear()
            db.collection("fundraising")
                .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                    if (e != null) {
                        Log.w(ContentValues.TAG, "Listen error", e)
                        return@addSnapshotListener
                    }
                    if(querySnapshot!!.isEmpty) {
                        Toast.makeText(context, "Brak aktywnych zbiórek.", Toast.LENGTH_LONG).show()
                    }
                    for (change in querySnapshot!!.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            val number = change.document.get("number")
                            db.collection("animals").whereEqualTo("number", number).get().addOnSuccessListener {
                                for (document in it.documents) {
                                    val animal = document.toObject<Animal>()
                                    animals.add(animal)
                                    GlobalScope.launch {
                                        withContext(Dispatchers.Main) {
                                            animalAdapter.animals = animals
                                            animalAdapter.notifyDataSetChanged()

                                            if (animals.isNotEmpty()) {
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        binding.textViewFundEmpty.visibility = View.GONE
                                                    }
                                                }
                                            } else {
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        binding.textViewFundEmpty.visibility = View.VISIBLE
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (change.type == DocumentChange.Type.MODIFIED) {
                            val number = change.document.get("number")
                            db.collection("animals").whereEqualTo("number", number).get().addOnSuccessListener {
                                for (document in it.documents) {
                                    val animal = document.toObject<Animal>()
                                    animals[querySnapshot.documents.indexOf(change.document)] = animal
                                    GlobalScope.launch {
                                        withContext(Dispatchers.Main) {
                                            animalAdapter.animals = animals
                                            animalAdapter.notifyDataSetChanged()

                                            if (animals.isNotEmpty()) {
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        binding.textViewFundEmpty.visibility = View.GONE
                                                    }
                                                }
                                            } else {
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        binding.textViewFundEmpty.visibility = View.VISIBLE
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (change.type == DocumentChange.Type.REMOVED) {
                            val number = change.document.get("number")
                            db.collection("animals").whereEqualTo("number", number).get().addOnSuccessListener {
                                for (document in it.documents) {
                                    val animal = document.toObject<Animal>()
                                    val animalFromList = animals.find { it?.number == animal?.number}
                                    animals.remove(animalFromList)
                                    GlobalScope.launch {
                                        withContext(Dispatchers.Main) {
                                            animalAdapter.animals = animals
                                            animalAdapter.notifyDataSetChanged()

                                            if (animals.isNotEmpty()) {
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        binding.textViewFundEmpty.visibility = View.GONE
                                                    }
                                                }
                                            } else {
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        binding.textViewFundEmpty.visibility = View.VISIBLE
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}