package pl.devnowak.animalove.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.devnowak.animalove.model.User
import pl.devnowak.animalove.databinding.FragmentCreateAppointmentBinding
import pl.devnowak.animalove.model.Animal
import pl.devnowak.animalove.model.Appointment
import java.text.SimpleDateFormat
import java.util.*

class CreateAppointmentFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    private var _binding: FragmentCreateAppointmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAppointmentBinding.inflate(inflater, container, false)
        val view = binding.root

        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        var date = Date(0)

        binding.buttonAppointmentDate.setOnClickListener {
            val datePickerDialog = MaterialDatePicker.Builder.datePicker().setTitleText("Wybierz datę").build()
            datePickerDialog.show(requireActivity().supportFragmentManager, "fragment_tag")
            datePickerDialog.addOnPositiveButtonClickListener {
                date = Date(it)
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pl", "PL"))
                binding.textViewAppointmentDate.text = formatter.format(date)
            }
        }

        binding.buttonAppointmentSend.setOnClickListener {
            val number = binding.editTextAppointmentAnimalNumber.editText?.text.toString()

            if (number.isEmpty()) {
                binding.editTextAppointmentAnimalNumber.error = "Pole nie może być puste!"
            } else if (date <= Date(0)) {
                Toast.makeText(context, "Wybierz datę!", Toast.LENGTH_LONG).show()
            } else {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        if (date >= Date(0) && binding.editTextAppointmentAnimalNumber.editText?.text!!.isNotBlank()) {
                            val numberRegex = Regex("([0-9]+)")
                            val animalNumberString = binding.editTextAppointmentAnimalNumber.editText?.text.toString()
                            val animalNumber = animalNumberString.toLong()
                            db.collection("animals").whereEqualTo("number", animalNumber).get().addOnSuccessListener { querySnapshot ->
                                for (document in querySnapshot.documents) {
                                    val animal = document.toObject<Animal>()
                                    val animalImage = animal!!.image
                                    val animalName = animal!!.name

                                    if (!animalNumberString.matches(numberRegex)) {
                                        Snackbar.make(
                                                requireView(),
                                                "Pole numer może zawierać tylko cyfry!",
                                                Snackbar.LENGTH_SHORT
                                        ).show()
                                    } else if (date > Date(0)) {
                                        val currentTime = Calendar.getInstance()
                                        val oneWeekForward = Calendar.getInstance()
                                        currentTime.timeInMillis = System.currentTimeMillis()
                                        oneWeekForward.timeInMillis = currentTime.timeInMillis
                                        oneWeekForward.add(Calendar.DATE, 7)
                                        val oneWeekForwardDate = oneWeekForward.time

                                        if (date > oneWeekForwardDate) {
                                            Snackbar.make(
                                                    requireView(),
                                                    "Możesz umówić wizytę na tydzień w przód!",
                                                    Snackbar.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            db.collection("users").document(auth.currentUser!!.uid)
                                                    .get().addOnSuccessListener { documentSnapshot ->
                                                        val user = documentSnapshot.toObject<User>()
                                                        if (user?.role == "user") {
                                                            val username = user?.username
                                                            val data = hashMapOf("date" to date, "userId" to auth.currentUser!!.uid,
                                                                    "username" to username, "animalNumber" to animalNumber,
                                                                    "animalImage" to animalImage, "animalName" to animalName)
                                                            db.collection("appointments").whereEqualTo("animalNumber", animalNumber)
                                                                    .get().addOnSuccessListener {
                                                                        if (it.isEmpty) {
                                                                            db.collection("appointments").add(data).addOnSuccessListener {
                                                                                Snackbar.make(
                                                                                        requireView(),
                                                                                        "Poprawnie dodano wizytę!",
                                                                                        Snackbar.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        } else {
                                                                            var isReserved = false
                                                                            for (document in it.documents) {
                                                                                val appointment = document.toObject<Appointment>()
                                                                                val time = Date(System.currentTimeMillis())
                                                                                if (appointment!!.date > time) {
                                                                                    isReserved = true
                                                                                }
                                                                            }
                                                                            if (isReserved) {
                                                                                Snackbar.make(
                                                                                        requireView(),
                                                                                        "Zwierzę jest już zarezerwowane przez kogoś innego! Spróbuj ponownie później!",
                                                                                        Snackbar.LENGTH_SHORT
                                                                                ).show()
                                                                            } else {
                                                                                db.collection("appointments").add(data).addOnSuccessListener {
                                                                                    Snackbar.make(
                                                                                            requireView(),
                                                                                            "Poprawnie dodano wizytę!",
                                                                                            Snackbar.LENGTH_SHORT
                                                                                    ).show()
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                        } else {
                                                            Snackbar.make(
                                                                    requireView(),
                                                                    "Wizyty mogą umawiać tylko zwykli użytkownicy!",
                                                                    Snackbar.LENGTH_SHORT
                                                            ).show()
                                                        }

                                                    }
                                        }
                                    } else {
                                        Toast.makeText(context, "Wybierz datę!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Snackbar.make(
                                    requireView(),
                                    "Data i numer nie mogą być puste!",
                                    Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        }
    }
}