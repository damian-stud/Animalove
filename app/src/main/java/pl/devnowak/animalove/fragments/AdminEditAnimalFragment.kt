package pl.devnowak.animalove.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import pl.devnowak.animalove.databinding.FragmentAdminEditAnimalBinding
import java.text.SimpleDateFormat
import java.util.*

class AdminEditAnimalFragment : Fragment() {

    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private var _binding: FragmentAdminEditAnimalBinding? = null
    private val binding get() = _binding!!
    lateinit var animalSize: String
    lateinit var animalSex: String
    lateinit var animalType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEditAnimalBinding.inflate(inflater, container, false)
        val view = binding.root

        var date = Date(0)

        binding.buttonEditAnimalPickDate.setOnClickListener {
            val datePickerDialog = MaterialDatePicker.Builder.datePicker().setTitleText("Wybierz datę").build()
            datePickerDialog.show(requireActivity().supportFragmentManager, "fragment_tag")
            datePickerDialog.addOnPositiveButtonClickListener {
                date = Date(it)
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pl", "PL"))
                binding.textViewAdminEditAnimalSince.text = formatter.format(date)
            }
        }

        binding.buttonAdminEditAnimalSend.setOnClickListener {
            val number = binding.editTextAdminEditAnimalNumber.editText?.text.toString()
            if (number.isEmpty()) {
                binding.editTextAdminEditAnimalNumber.error = "Pole nie może być puste!"
                Toast.makeText(context, "Pole numer nie może być puste!", Toast.LENGTH_SHORT).show()
            } else {
                editAnimal()
            }

        }

        binding.buttonAdminEditAnimalEnableFundrising.setOnClickListener {
            val number = binding.editTextFundrisingAnimalNumber.editText?.text.toString()
            if (number.isEmpty()) {
                binding.editTextFundrisingAnimalNumber.error = "Pole nie może być puste!"
            } else {
                enableFundrising()
            }
        }

        binding.buttonAdminEditAnimalDisableFundrising.setOnClickListener {
            val number = binding.editTextFundrisingAnimalNumber.editText?.text.toString()
            if (number.isEmpty()) {
                binding.editTextFundrisingAnimalNumber.error = "Pole nie może być puste!"
            } else {
                disableFundrising()
            }

        }

        binding.chipSmall.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                animalSize = "mały"
            }
        }

        binding.chipMedium.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                animalSize = "średni"
            }
        }

        binding.chipBig.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                animalSize = "duży"
            }
        }

        binding.chipVeryBig.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                animalSize = "bardzo duży"
            }
        }

        return view
    }


    private fun enableFundrising() = CoroutineScope(Dispatchers.IO).launch {
        db = Firebase.firestore
        if (binding.editTextFundrisingAnimalNumber.editText?.text!!.isNotBlank() && binding.editTextFundDescription.editText?.text!!.isNotBlank()
            && binding.editTextFundAccount.editText?.text!!.isNotBlank()) {
            val number = binding.editTextFundrisingAnimalNumber.editText?.text.toString().toInt()
            val desc = binding.editTextFundDescription.editText?.text.toString()
            val bankAccountNumber = binding.editTextFundAccount.editText?.text.toString()

            val animalQuery = db.collection("animals").whereEqualTo("number", number).get().await()
            if (animalQuery.documents.isNotEmpty()) {
                for (document in animalQuery) {
                    val data = hashMapOf("number" to document["number"], "name" to document["name"], "description" to desc, "bankAccountNumber" to bankAccountNumber)
                    db.collection("fundraising").document(document.id)
                        .get().addOnSuccessListener {
                            if (!it.exists()) {
                                db.collection("fundraising").document(document.id)
                                    .set(data, SetOptions.merge()).addOnSuccessListener {
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Zbiórka aktywowana poprawnie!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                            } else {
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Zwierzę o tym numerze ma juź aktywną zbiórkę!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Musisz wypełnić wszystkie pola!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disableFundrising() = CoroutineScope(Dispatchers.IO).launch {
        db = Firebase.firestore
        if (binding.editTextFundrisingAnimalNumber.editText?.text!!.isNotBlank()) {
            val number = binding.editTextFundrisingAnimalNumber.editText?.text.toString().toInt()

            val animalQuery = db.collection("animals").whereEqualTo("number", number).get().await()
            if (animalQuery.documents.isNotEmpty()) {
                for (document in animalQuery) {
                    db.collection("fundraising").document(document.id)
                            .get().addOnSuccessListener {
                                if (!it.exists()) {
                                    GlobalScope.launch {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Zwierzę o tym numerze nie ma aktywnej zbiórki!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    db.collection("fundraising").document(document.id)
                                            .delete().addOnSuccessListener {
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Zbiórka usunięta!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                }
                            }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Musisz podać numer zwierzęcia!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editAnimal() = CoroutineScope(Dispatchers.IO).launch {
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        if (auth.currentUser != null) {
            val user = auth.currentUser
            val userDetails = db.collection("users").document(user!!.uid).get().await()
            val userRole = userDetails.get("role")
            val radioGroupCheckedId = binding.radioGroupEditAnimal.checkedRadioButtonId
            val radioGroupTypeCheckedId = binding.radioGroupEditAnimalAnimalType.checkedRadioButtonId
            val chipGroupCheckedId = binding.chipGroupEditAnimal.checkedChipId

                if (userRole == "admin" || userRole == "employee") {
                    if (binding.editTextAdminEditAnimalNumber.editText?.text!!.isNotBlank() && binding.editTextAdminEditAnimalName.editText?.text!!.isNotBlank()
                            && binding.textViewAdminEditAnimalSince.text.isNotBlank() && binding.editTextAdminEditAnimalAge.editText?.text!!.isNotBlank()
                            && radioGroupCheckedId >= 0 && radioGroupTypeCheckedId >= 0 && chipGroupCheckedId >= 0) {
                        val number = binding.editTextAdminEditAnimalNumber.editText?.text.toString().toInt()
                        val name = binding.editTextAdminEditAnimalName.editText?.text.toString()
                        val since = binding.textViewAdminEditAnimalSince.text.toString()
                        val age = binding.editTextAdminEditAnimalAge.editText?.text.toString().toInt()

                        if (binding.radioButtonSamiec.isChecked) {
                            animalSex = "samiec"
                        }
                        if (binding.radioButtonSamica.isChecked) {
                            animalSex = "samica"
                        }
                        if (binding.radioButtonDog.isChecked) {
                            animalType = "dog"
                        }
                        if (binding.radioButtonSamica.isChecked) {
                            animalType = "cat"
                        }

                        val animalQuery = db.collection("animals").whereEqualTo("number", number).get().await()
                        if (animalQuery.documents.isNotEmpty()) {
                            for (document in animalQuery) {
                                db.collection("animals").document(document.id)
                                        .update("number", number, "name", name, "since",
                                                since, "age", age, "sex", animalSex, "type", animalType, "size", animalSize).await()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Edycja zakończona poprawnie!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Wszystkie pola muszą być wypełnione!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    } else if (userRole == "volunteer") {
                        withContext(Dispatchers.Main) {
                           Toast.makeText(context, "Nie możesz edytować zwierzęcia!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                withContext(Dispatchers.Main) {
                 Toast.makeText(context, "Musisz być zalogowany!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}