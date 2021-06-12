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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.devnowak.animalove.databinding.FragmentAdminAddAnimalBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AdminAddAnimalFragment : Fragment() {

    lateinit var db: FirebaseFirestore
    private var _binding: FragmentAdminAddAnimalBinding? = null
    private val binding get() = _binding!!
    lateinit var animalSize: String
    lateinit var animalSex: String
    lateinit var animalType: String
    val storage = Firebase.storage
    val image = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.imageViewAnimal.setImageURI(uri)
            binding.imageViewAnimal.visibility = View.VISIBLE
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAddAnimalBinding.inflate(inflater, container, false)
        val view = binding.root

        var date = Date(0)

        binding.buttonAddAnimalPickDate.setOnClickListener {
            val datePickerDialog = MaterialDatePicker.Builder.datePicker().setTitleText("Wybierz datę").build()
            datePickerDialog.show(requireActivity().supportFragmentManager, "fragment_tag")
            datePickerDialog.addOnPositiveButtonClickListener {
                date = Date(it)
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pl", "PL"))
                binding.editTextAdminAddAnimalSince.text = formatter.format(date)
            }
        }

        binding.buttonAdminAddAnimalImage.setOnClickListener {
            image.launch("image/*")
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

        binding.buttonAdminAddAnimalSend.setOnClickListener {
            val number = binding.editTextAdminAddAnimalNumber.editText?.text.toString()
            val radioGroupCheckedId = binding.radioGroupAddAnimal.checkedRadioButtonId
            val radioGroupTypeCheckedId = binding.radioGroupAddAnimalType.checkedRadioButtonId
            val chipGroupCheckedId = binding.chipGroupAddAnimal.checkedChipId

            if (number.isEmpty()) {
                binding.editTextAdminAddAnimalNumber.error = "Pole nie może być puste!"
            }
            if (date <= Date(0)) {
                Toast.makeText(context, "Wybierz datę!", Toast.LENGTH_LONG).show()
            } else if (radioGroupCheckedId < 0) {
                Toast.makeText(context, "Wybierz płeć!", Toast.LENGTH_LONG).show()
            } else if (radioGroupTypeCheckedId < 0) {
                Toast.makeText(context, "Wybierz typ pies/kot!", Toast.LENGTH_LONG).show()
            } else if (chipGroupCheckedId < 0) {
                Toast.makeText(context, "Wybierz rozmiar!", Toast.LENGTH_LONG).show()
            } else if (number.isEmpty()) {
                Toast.makeText(context, "Pole numer nie może być puste!", Toast.LENGTH_LONG).show()
            } else if (number.isNotEmpty() && date > Date(0)
                    && radioGroupCheckedId >= 0 && chipGroupCheckedId >= 0 && radioGroupTypeCheckedId >= 0) {
                val image: ImageView = binding.imageViewAnimal
                val drawable: BitmapDrawable? = image.drawable as BitmapDrawable
                if (drawable != null) {
                    val bitmap = drawable.bitmap
                    GlobalScope.launch {

                        val os = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 1, os)
                        val compressedImage = os.toByteArray()

                        val storageRef = storage.reference
                        val date = Date(System.currentTimeMillis())

                        val formatter = SimpleDateFormat("yyMMddHHmmssSSS", Locale.getDefault())
                        val timestamp = formatter.format(date)

                        val myImagesRef = storageRef.child("/images/animals/" + timestamp + ".webp")
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
                                saveAnimal(date, downloadUri.toString())
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
                    saveAnimal(date, null)
                }
            }
        }
        return view
    }

    private fun saveAnimal(date: Date, path: String?) {
        db = Firebase.firestore
        val number = binding.editTextAdminAddAnimalNumber.editText?.text.toString().toInt()
        val name = binding.editTextAdminAddAnimalName.editText?.text.toString()
        val age = binding.editTextAdminAddAnimalAge.editText?.text.toString().toInt()

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

        val newAnimalRef = db.collection("animals").document()

        val data: HashMap<String, Any?>

        if (path != null) {
            data = hashMapOf(
                    "id" to newAnimalRef.id,
                    "number" to number,
                    "name" to name,
                    "image" to path,
                    "since" to date,
                    "sex" to animalSex,
                    "age" to age,
                    "size" to animalSize,
                    "type" to animalType
            )
        } else {
            data = hashMapOf(
                    "id" to newAnimalRef.id,
                    "number" to number,
                    "name" to name,
                    "image" to "-",
                    "since" to date,
                    "sex" to animalSex,
                    "age" to age,
                    "size" to animalSize,
                    "type" to animalType
            )
        }

        newAnimalRef.set(data).addOnSuccessListener {
            Snackbar.make(
                    requireView(),
                    "Poprawnie zapisano zwierzę!",
                    Snackbar.LENGTH_SHORT
            ).show()
        }.addOnFailureListener {
            Snackbar.make(
                    requireView(),
                    "Błąd zapisu do bazy danych!",
                    Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}