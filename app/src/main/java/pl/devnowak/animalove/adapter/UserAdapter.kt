package pl.devnowak.animalove.adapter

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.ItemUserBinding
import pl.devnowak.animalove.fragments.ChatFragmentDirections
import pl.devnowak.animalove.model.User

class UserAdapter(
        var users: List<User?>,
        var isChat: Boolean
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        return UserViewHolder(ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.binding.chatUsername.text = users[position]?.username
        val user =  users[position]

        if (user?.profileImagePath.equals("default")) {
            holder.binding.chatUserImage.setImageResource(R.mipmap.ic_launcher)

        } else {
            val url = user?.profileImagePath
            Glide.with(holder.itemView).load(url).fitCenter()
                .centerCrop().placeholder(R.mipmap.ic_launcher)
                .into(holder.itemView.findViewById(R.id.chat_userImage))
        }

        if (isChat) {
            if (user?.status.equals("online")) {
                holder.binding.imageViewChatStatusOnline.visibility = View.VISIBLE
                holder.binding.imageViewChatStatusOffline.visibility = View.GONE
            } else {
                holder.binding.imageViewChatStatusOnline.visibility = View.GONE
                holder.binding.imageViewChatStatusOffline.visibility = View.VISIBLE
            }
        } else {
            holder.binding.imageViewChatStatusOnline.visibility = View.GONE
            holder.binding.imageViewChatStatusOffline.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { view ->
            if (user?.profileImagePath != "default") {
                val image: ImageView = holder.binding.chatUserImage
                val drawable = image.drawable as BitmapDrawable
                val bitmap = drawable.bitmap
                user!!.profileImage = bitmap
            }
            val action = ChatFragmentDirections.actionDestinationChatToMessageFragment(user)
            view.findNavController().navigate(action)
        }

    }

    override fun getItemCount(): Int {
        return users.size
    }
}