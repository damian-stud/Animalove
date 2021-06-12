package pl.devnowak.animalove.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import pl.devnowak.animalove.R
import pl.devnowak.animalove.databinding.ItemChatBinding
import pl.devnowak.animalove.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
        var messageList: List<Message?>,
) : RecyclerView.Adapter<MessageAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.binding.textViewMessageText.text = messageList[position]?.message
        setTimeText(holder, position)
        setMessageRootGravity(holder, position)
    }

    private fun setMessageRootGravity(holder: ChatViewHolder, position: Int) {
        if (messageList[position]!!.sender == FirebaseAuth.getInstance().currentUser?.uid) {
            holder.binding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_white)
                val lParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END)
                this.layoutParams = lParams
            }
        } else {
            holder.binding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_primary_color)
                val lParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START)
                this.layoutParams = lParams
            }
        }
    }

    private fun setTimeText(holder: ChatViewHolder, position: Int) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.binding.textViewMessageTime.text = formatter.format(messageList[position]!!.time)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}