package pl.devnowak.animalove.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import pl.devnowak.animalove.adapter.AnimalAdapter
import pl.devnowak.animalove.databinding.FragmentHomeBinding
import pl.devnowak.animalove.model.Animal

class HomeFragment : Fragment() {

    val db = Firebase.firestore
    var animals = mutableListOf<Animal?>()
    val animalsCopy = mutableListOf<Animal?>()
    var animalAdapter = AnimalAdapter(animals, this)
    var dogListener: ListenerRegistration? = null
    var catListener: ListenerRegistration? = null
    var allListener: ListenerRegistration? = null
    var selectedIndex = 0
    val filterOptions = arrayOf("Domyślne", "Od najstarszych", "Od najnowszych")


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewAnimals.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAnimals.adapter = animalAdapter

        binding.imageViewHomeFilters.setOnClickListener {
            selectedItem()
        }

        binding.chipHomeDogs.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onlyDogs()
            }
        }

        binding.chipHomeCats.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onlyCats()
            }
        }

        binding.chipHomeAllAnimals.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAllAnimals()
            }
        }

        binding.searchViewHome.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(qString: String): Boolean {
                filter(qString)
                return true
            }
            override fun onQueryTextSubmit(qString: String): Boolean {
                return true
            }
        })

    }

    fun selectedItem() {
        var selectItem = filterOptions[selectedIndex]
        val filterDialog = MaterialAlertDialogBuilder(requireContext())
        filterDialog.setTitle("Filtrowanie")
        filterDialog.setSingleChoiceItems(filterOptions, selectedIndex) {
            dialog, which ->
            selectedIndex = which
            selectItem = filterOptions[which]
        }
        filterDialog.setPositiveButton("Ok") {
            dialog, which ->

            if (selectedIndex == 0) {
                sortByDefault()
            }
            if (selectedIndex == 1) {
                sortByNewestDate()
            }
            if (selectedIndex == 2) {
                sortByOldestDate()
            }
        }
        filterDialog.setNeutralButton("Anuluj") {
            dialog, which ->
            dialog.dismiss()
        }
        filterDialog.show()
    }

    private fun sortByDefault() {
        animals = animalsCopy.toMutableList()
        animalAdapter.animals = animals
        animalAdapter.notifyDataSetChanged()
    }

    private fun sortByNewestDate() {
        animals.sortBy { it!!.since }
        animalAdapter.notifyDataSetChanged()
    }

    private fun sortByOldestDate() {
        animals.sortByDescending { it!!.since }
        animalAdapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        if (!binding.chipHomeAllAnimals.isChecked && !binding.chipHomeDogs.isChecked && !binding.chipHomeCats.isChecked) {
            binding.chipHomeAllAnimals.isChecked = true
        }
    }


    override fun onStop() {
        super.onStop()
        dogListener?.remove()
        catListener?.remove()
        allListener?.remove()


    }

    fun filter(text: String) {
        var filterText = text
        animals.clear()
        if (text.isEmpty()) {
            animals = animalsCopy.toMutableList()
        } else {
            filterText = filterText.toLowerCase()
            for (item in animalsCopy) {
                if (item!!.name.toLowerCase().contains(filterText)) {
                    animals.add(item)
                }
            }
        }
        animalAdapter.animals = animals
        animalAdapter.notifyDataSetChanged()
    }


    private fun onlyDogs() = CoroutineScope(Dispatchers.IO).launch {
        try {
            animals.clear()
            animalsCopy.clear()
            dogListener = db.collection("animals").whereEqualTo("type", "dog")
                    .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen error", e)
                            return@addSnapshotListener
                        }
                        for (change in querySnapshot!!.documentChanges) {
                            if (change.type == DocumentChange.Type.ADDED) {
                                val animal = change.document.toObject<Animal>()
                                animals.add(animal)
                                animalsCopy.add(animal)
                                animalAdapter.animals = animals
                                animalAdapter.notifyDataSetChanged()
                            }
                            if (change.type == DocumentChange.Type.MODIFIED) {
                                Log.d(TAG, "Modified: ${change.document.data}")
                                val animal = change.document.toObject<Animal>()
                                animals[querySnapshot.documents.indexOf(change.document)] = animal
                                animalsCopy[querySnapshot.documents.indexOf(change.document)] = animal
                                animalAdapter.animals = animals
                                animalAdapter.notifyDataSetChanged()
                            }
                            if (change.type == DocumentChange.Type.REMOVED) {
                                val animal = change.document.toObject<Animal>()
                                animals.remove(animal)
                                animalsCopy.remove(animal)
                                animalAdapter.animals = animals
                                animalAdapter.notifyDataSetChanged()
                            }
                        }
                    }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onlyCats() = CoroutineScope(Dispatchers.IO).launch {
        try {
            animals.clear()
            animalsCopy.clear()
            catListener = db.collection("animals").whereEqualTo("type", "cat")
                    .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen error", e)
                            return@addSnapshotListener
                        }
                        for (change in querySnapshot!!.documentChanges) {
                            if (change.type == DocumentChange.Type.ADDED) {
                                val animal = change.document.toObject<Animal>()
                                animals.add(animal)
                                animalsCopy.add(animal)
                                animalAdapter.animals = animals
                                animalAdapter.notifyDataSetChanged()
                            }
                            if (change.type == DocumentChange.Type.MODIFIED) {
                                val animal = change.document.toObject<Animal>()
                                animals[querySnapshot.documents.indexOf(change.document)] = animal
                                animalsCopy[querySnapshot.documents.indexOf(change.document)] = animal
                                animalAdapter.animals = animals
                                animalAdapter.notifyDataSetChanged()
                            }
                            if (change.type == DocumentChange.Type.REMOVED) {
                                val animal = change.document.toObject<Animal>()
                                animals.remove(animal)
                                animalsCopy.remove(animal)
                                animalAdapter.animals = animals
                                animalAdapter.notifyDataSetChanged()
                            }
                        }
                    }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showAllAnimals() = CoroutineScope(Dispatchers.IO).launch {
        try {
            animals.clear()
            allListener = db.collection("animals")
                    .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen error", e)
                            return@addSnapshotListener
                        }
                        for (change in querySnapshot!!.documentChanges) {
                            if (change.type == DocumentChange.Type.ADDED) {
                                val animal = change.document.toObject<Animal>()
                                animals.add(animal)
                                animalsCopy.add(animal)
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        animalAdapter.animals = animals
                                        animalAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                            if (change.type == DocumentChange.Type.MODIFIED) {
                                val animal = change.document.toObject<Animal>()
                                animals[querySnapshot.documents.indexOf(change.document)] = animal
                                animalsCopy[querySnapshot.documents.indexOf(change.document)] = animal
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        animalAdapter.animals = animals
                                        animalAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                            if (change.type == DocumentChange.Type.REMOVED) {
                                val animal = change.document.toObject<Animal>()
                                val animalFromList = animals.find { it?.number == animal.number}
                                animals.remove(animalFromList)
                                animalsCopy.remove(animalFromList)
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        animalAdapter.animals = animals
                                        animalAdapter.notifyDataSetChanged()
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