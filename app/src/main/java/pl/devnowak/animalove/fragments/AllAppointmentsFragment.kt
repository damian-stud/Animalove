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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import pl.devnowak.animalove.model.Appointment
import pl.devnowak.animalove.adapter.AppointmentAdapter
import pl.devnowak.animalove.databinding.FragmentAllAppointmentsBinding
import java.util.*

class AllAppointmentsFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    private var _binding: FragmentAllAppointmentsBinding? = null
    private val binding get() = _binding!!
    var appointments = mutableListOf<Appointment?>()
    var appointmentAdapter = AppointmentAdapter(appointments, this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllAppointmentsBinding.inflate(inflater, container, false)
        val view = binding.root

        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewAppointments.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewAppointments.adapter = appointmentAdapter

        showAllAppointments()
    }

    private fun showAllAppointments() = CoroutineScope(Dispatchers.IO).launch {
        try {
            auth = FirebaseAuth.getInstance()
            val firebaseUser = auth.currentUser

            if (auth.currentUser != null) {
                val userDetails = db.collection("users").document(firebaseUser!!.uid).get().await()
                val userRole = userDetails.get("role")
                if (userRole == "employee" || userRole == "volunteer" || userRole == "admin") {
                    try {
                        appointments.clear()
                        db.collection("appointments")
                                .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                                    if (e != null) {
                                        Log.w(ContentValues.TAG, "Listen error", e)
                                        return@addSnapshotListener
                                    }
                                    for (change in querySnapshot!!.documentChanges) {
                                        if (change.type == DocumentChange.Type.ADDED) {
                                            val appointment = change.document.toObject<Appointment>()
                                            appointments.add(appointment)
                                            appointments.sortBy { it!!.date }
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    appointmentAdapter.appointments = appointments
                                                    appointmentAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                        if (change.type == DocumentChange.Type.MODIFIED) {
                                            val appointment = change.document.toObject<Appointment>()
                                            appointments[querySnapshot.documents.indexOf(change.document)] = appointment
                                            appointments.sortBy { it!!.date }
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    appointmentAdapter.appointments = appointments
                                                    appointmentAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                        if (change.type == DocumentChange.Type.REMOVED) {
                                            val appointment = change.document.toObject<Appointment>()
                                            val animalFromList = appointments.find { it?.animalNumber == appointment.animalNumber }
                                            appointments.remove(animalFromList)
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    appointmentAdapter.appointments = appointments
                                                    appointmentAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                    }
                                    if (appointments.isNotEmpty()) {
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                binding.textViewAppointments.text = "Wszystkie spotkania"
                                            }
                                        }
                                    } else {
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                binding.textViewAppointments.text = "Brak spotkań do wyświetlenia"
                                            }
                                        }
                                    }
                                }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    try {
                        appointments.clear()
                        db.collection("appointments").whereEqualTo("userId", firebaseUser.uid)
                                .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                                    if (e != null) {
                                        Log.w(ContentValues.TAG, "Listen error", e)
                                        return@addSnapshotListener
                                    }
                                    if (querySnapshot!!.size() == 0) {
                                        appointments.clear()
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                appointmentAdapter.appointments = appointments
                                                appointmentAdapter.notifyDataSetChanged()
                                                binding.textViewAppointments.text = "Brak umówionych wizyt"
                                            }
                                        }
                                        return@addSnapshotListener
                                    }
                                    for (change in querySnapshot!!.documentChanges) {
                                        if (change.type == DocumentChange.Type.ADDED) {
                                            val appointment = change.document.toObject<Appointment>()
                                            appointments.add(appointment)
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    appointmentAdapter.appointments = appointments
                                                    appointmentAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                        if (change.type == DocumentChange.Type.MODIFIED) {
                                            val appointment = change.document.toObject<Appointment>()
                                            appointments[querySnapshot.documents.indexOf(change.document)] = appointment
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    appointmentAdapter.appointments = appointments
                                                    appointmentAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                        if (change.type == DocumentChange.Type.REMOVED) {
                                            val appointment = change.document.toObject<Appointment>()
                                            val animalFromList = appointments.find { it?.animalNumber == appointment.animalNumber }
                                            appointments.remove(animalFromList)
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    appointmentAdapter.appointments = appointments
                                                    appointmentAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                    }

                                    if (appointments.isNotEmpty()) {
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                binding.textViewAppointments.text = "Twoja umówiona wizyta"
                                            }
                                        }
                                    } else {
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                binding.textViewAppointments.text = "Brak umówionych wizyt"
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

        } catch (e: Exception) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}