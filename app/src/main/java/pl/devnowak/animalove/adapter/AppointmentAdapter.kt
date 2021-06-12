package pl.devnowak.animalove.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.ItemAppointmentBinding
import pl.devnowak.animalove.model.Appointment
import java.text.SimpleDateFormat
import java.util.*


class AppointmentAdapter(
        var appointments: List<Appointment?>,
        var fragment: Fragment
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(val binding: ItemAppointmentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {

        return AppointmentViewHolder(
                ItemAppointmentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val animalName = appointments[position]!!.animalName
        val username = appointments[position]!!.username

        setDateText(holder, position)
        holder.binding.textViewDogName.text = "Imię zwierzęcia:\n" + animalName
        holder.binding.textViewUsername.text = "Wizytę umówił:\n" + username
        val url = appointments[position]?.animalImage
        if (url != "-") {
            Glide.with(holder.itemView).load(url).fitCenter().centerCrop().into(
                    holder.itemView.findViewById(
                            R.id.animal_image
                    )
            )
        } else {
            Glide.with(holder.itemView).load(R.mipmap.ic_launcher).fitCenter().centerCrop().into(
                    holder.itemView.findViewById(
                            R.id.animal_image
                    )
            )
        }
    }

    private fun setDateText(holder: AppointmentViewHolder, position: Int) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.binding.textViewDateApp.text = "Data zaplanowanej wizyty:\n" + formatter.format(appointments[position]!!.date)
    }

    override fun getItemCount(): Int {
        return appointments.size
    }
}