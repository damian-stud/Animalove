package pl.devnowak.animalove.adapter

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.devnowak.animalove.*
import pl.devnowak.animalove.databinding.ItemAnimalBinding
import pl.devnowak.animalove.fragments.*
import pl.devnowak.animalove.model.Animal
import java.text.SimpleDateFormat
import java.util.*


class AnimalAdapter(
        var animals: List<Animal?>,
        var fragment: Fragment
) : RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    inner class AnimalViewHolder(val binding: ItemAnimalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        return AnimalViewHolder(
                ItemAnimalBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val id = animals[position]!!.id
        val number = animals[position]!!.number
        val name = animals[position]!!.name
        val since = animals[position]!!.since
        val imageURL = animals[position]!!.image
        val age = animals[position]!!.age
        val sex = animals[position]!!.sex
        val type = animals[position]!!.type
        val size = animals[position]!!.size

        holder.binding.recyclerViewOneItem.setOnClickListener { view ->
            try {
                val image: ImageView = holder.binding.animalImage
                val drawable = image.drawable as BitmapDrawable
                val bitmap = drawable.bitmap
                val animal = Animal(id, number, name, since, bitmap, imageURL, age, sex, type, size)

                if (fragment is HomeFragment) {
                    val action = HomeFragmentDirections.actionDestinationHomeToAnimalDetailsFragment(
                            animal
                    )
                    view.findNavController().navigate(action)
                } else if (fragment is FavouriteFragment) {
                    val action = FavouriteFragmentDirections.actionDestinationFavouriteToAnimalDetailsFragment(
                            animal
                    )
                    view.findNavController().navigate(action)
                } else if (fragment is VirtualAdoptionFragment) {
                    val action = VirtualAdoptionFragmentDirections.actionVirtualAdoptionFragmentToAnimalDetailsFragment(
                            animal
                    )
                    view.findNavController().navigate(action)
                } else if (fragment is FundraisingFragment) {
                    val action =
                            FundraisingFragmentDirections.actionFundraisingFragmentToAnimalDetailsFragment(
                                    animal
                            )
                    view.findNavController().navigate(action)
                }
            } catch (e: Exception) {
                val animal = Animal(id, number, name, since, imageURL, age, sex, type, size)
                if (fragment is HomeFragment) {
                    val action = FavouriteFragmentDirections.actionDestinationFavouriteToAnimalDetailsFragment(
                            animal
                    )
                    view.findNavController().navigate(action)
                } else if (fragment is FavouriteFragment) {
                    val action = FavouriteFragmentDirections.actionDestinationFavouriteToAnimalDetailsFragment(
                            animal
                    )
                    view.findNavController().navigate(action)
                }
            }
        }
        holder.binding.animalName.text = "Imię: " + name
        setSinceText(holder, position)
        holder.binding.animalAge.text = "Wiek w latach: " + age.toString()
        holder.binding.animalSex.text = "Płeć: " + sex
        holder.binding.animalSize.text = "Rozmiar: " + size

        val url = animals[position]?.image
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

    private fun setSinceText(holder: AnimalViewHolder, position: Int) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.binding.animalSince.text = "W schronisku od: " + formatter.format(animals[position]!!.since)
    }

    override fun getItemCount(): Int {
        return animals.size
    }
}