package pl.devnowak.animalove.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.FragmentAnimalDetailsBinding
import java.text.SimpleDateFormat
import java.util.*


class AnimalDetailsFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    private val args by navArgs<AnimalDetailsFragmentArgs>()
    private var _binding: FragmentAnimalDetailsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentAnimalDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        auth = FirebaseAuth.getInstance()
        val animalId = args.currentAnimal.id
        val animalNumber = args.currentAnimal.number
        val animalName = args.currentAnimal.name
        val animalType = args.currentAnimal.type

        if (auth.currentUser != null) {
            checkFavourite(animalNumber)
            checkAdopted(animalNumber)
        }

        binding.imageViewAnimalDetailsFav.setOnClickListener {
            if (auth.currentUser == null) {
                Snackbar.make(
                    view,
                    "Musisz się zalogować, aby dodać do ulubionych!",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                addToFavourite(animalId, animalNumber, animalName, animalType)
            }
        }
        binding.buttonAdoptVirtual.setOnClickListener {
            if (auth.currentUser == null) {
                Snackbar.make(
                    view,
                    "Musisz się zalogować, aby adoptować wirtualnie!",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                adoptVirtual(animalId, animalNumber, animalName, animalType)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView: ImageView = binding.imageViewAnimalDetailsImage

        val number = args.currentAnimal.number
        val name = args.currentAnimal.name
        val since = args.currentAnimal.since
        val myImage = args.currentAnimal.bitmap
        val age = args.currentAnimal.age
        val sex = args.currentAnimal.sex
        val size = args.currentAnimal.size

        binding.textViewAnimalDetailsNumber.text = "Numer: " + number.toString()
        binding.textViewAnimalDetailsName.text = "Imię: " + name
        binding.textViewAnimalDetailsSince.text = setSinceText(since)
        imageView.setImageBitmap(myImage)
        binding.textViewAnimalDetailsAge.text = "Wiek w latach: " + age.toString()
        binding.textViewAnimalDetailsSex.text = "Płeć: " + sex
        binding.textViewAnimalDetailsSize.text = "Rozmiar: " + size

        checkIfActiveFund(number)
    }

    private fun checkIfActiveFund(animalNumber: Long) = CoroutineScope(Dispatchers.IO).launch {
        db = Firebase.firestore
        db.collection("fundraising").whereEqualTo("number", animalNumber).get().addOnSuccessListener {
            if (it.isEmpty) {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        binding.textViewFundActiveTitle.visibility = View.GONE
                        binding.textViewFundDesc.visibility = View.GONE
                        binding.textViewFundAccountNumber.visibility = View.GONE
                    }
                }
            } else {
                for (document in it.documents) {
                    val desc = document.get("description")
                    val bankAccountNumber = document.get("bankAccountNumber")
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            binding.textViewFundDesc.text = "Opis zbiórki:\n" + desc
                            binding.textViewFundAccountNumber.text = "Numer konta bankowego:\n" + bankAccountNumber

                            binding.textViewFundActiveTitle.visibility = View.VISIBLE
                            binding.textViewFundDesc.visibility = View.VISIBLE
                            binding.textViewFundAccountNumber.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

    }

    private fun setSinceText(date: Date): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    private fun checkFavourite(animalNumber: Long) {
        db = Firebase.firestore

        db.collection("favouriteAnimals")
            .whereEqualTo("number", animalNumber).whereArrayContains("users", auth.currentUser!!.uid)
            .get().addOnSuccessListener {
                if (it.isEmpty) {
                    val borderHeart = AppCompatResources.getDrawable(
                        requireContext(),
                            R.drawable.ic_baseline_favorite_border_24
                    )
                    binding.imageViewAnimalDetailsFav.setImageDrawable(borderHeart)
                } else {
                    val fillHeart = AppCompatResources.getDrawable(
                        requireContext(),
                            R.drawable.ic_baseline_favorite_24
                    )
                    binding.imageViewAnimalDetailsFav.setImageDrawable(fillHeart)
                }
            }
    }

    private fun addToFavourite(animalId: String, animalNumber: Long, animalName: String, animalType: String) {
        db = Firebase.firestore
        val currentUserId = auth.currentUser!!.uid
        val userList = mutableListOf(currentUserId)

        db.collection("favouriteAnimals")
            .whereEqualTo("number", animalNumber)
            .get().addOnSuccessListener {
                if (it.isEmpty) {
                    val data = hashMapOf("number" to animalNumber, "name" to animalName, "type" to animalType, "users" to userList)
                    db.collection("favouriteAnimals").document(animalId).set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            val fillHeart = AppCompatResources.getDrawable(
                                requireContext(),
                                    R.drawable.ic_baseline_favorite_24
                            )
                            binding.imageViewAnimalDetailsFav.setImageDrawable(fillHeart)
                        }
                    Toast.makeText(context, "Dodano do ulubionych!", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in it.documents) {
                        val usersList = document["users"] as MutableList<String>
                        if (usersList.contains(currentUserId)) {
                            db.collection("favouriteAnimals").document(document.id).update("users", FieldValue.arrayRemove(currentUserId))
                                    .addOnSuccessListener {
                                        val borderHeart = AppCompatResources.getDrawable(
                                                requireContext(),
                                                R.drawable.ic_baseline_favorite_border_24
                                        )
                                        binding.imageViewAnimalDetailsFav.setImageDrawable(borderHeart)
                                        Toast.makeText(context, "Usunięto z ulubionych!", Toast.LENGTH_SHORT).show()
                                    }
                        } else {
                            db.collection("favouriteAnimals").document(document.id).update("users", FieldValue.arrayUnion(currentUserId))
                                    .addOnSuccessListener {
                                        val borderHeart = AppCompatResources.getDrawable(
                                                requireContext(),
                                                R.drawable.ic_baseline_favorite_24
                                        )
                                        binding.imageViewAnimalDetailsFav.setImageDrawable(borderHeart)
                                        Toast.makeText(context, "Dodano do ulubionych!", Toast.LENGTH_SHORT).show()
                                    }
                        }

                    }
                }
            }
    }

    private fun checkAdopted(animalNumber: Long) {
        db = Firebase.firestore

        db.collection("virtualAdoptedAnimals")
            .whereEqualTo("number", animalNumber).whereArrayContains("users", auth.currentUser!!.uid)
            .get().addOnSuccessListener {
                if (it.isEmpty) {
                    binding.buttonAdoptVirtual.text = "Adoptuj wirtualnie"
                } else {
                    binding.buttonAdoptVirtual.text = "Usuń z adoptowanych wirtualnie"
                }
            }
    }

    private fun adoptVirtual(animalId: String, animalNumber: Long, animalName: String, animalType: String) {
        db = Firebase.firestore
        val currentUserId = auth.currentUser!!.uid
        val userList = mutableListOf(currentUserId)

        db.collection("virtualAdoptedAnimals")
            .whereEqualTo("number", animalNumber)
            .get().addOnSuccessListener {
                if (it.isEmpty) {
                    val data = hashMapOf("number" to animalNumber, "name" to animalName, "type" to animalType, "users" to userList)
                    db.collection("virtualAdoptedAnimals").document(animalId).set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            binding.buttonAdoptVirtual.text = "Usuń z adoptowanych wirtualnie"
                        }
                    Toast.makeText(context, "Dodano do wirtualnie adoptowanych!", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in it.documents) {
                        val usersList = document["users"] as MutableList<String>
                        if (usersList.contains(currentUserId)) {
                            db.collection("virtualAdoptedAnimals").document(document.id).update("users", FieldValue.arrayRemove(currentUserId))
                                    .addOnSuccessListener {
                                        binding.buttonAdoptVirtual.text = "Adoptuj wirtualnie"
                                        Toast.makeText(context, "Usunięto z wirtualnie adoptowanych", Toast.LENGTH_SHORT).show()
                                    }
                        } else {
                            db.collection("virtualAdoptedAnimals").document(document.id).update("users", FieldValue.arrayUnion(currentUserId))
                                    .addOnSuccessListener {
                                        binding.buttonAdoptVirtual.text = "Usuń z adoptowanych wirtualnie"
                                        Toast.makeText(context, "Dodano do wirtualnie adoptowanych!", Toast.LENGTH_SHORT).show()
                                    }
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