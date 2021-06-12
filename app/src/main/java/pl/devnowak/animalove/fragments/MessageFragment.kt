package pl.devnowak.animalove.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import pl.devnowak.animalove.adapter.MessageAdapter
import pl.devnowak.animalove.databinding.FragmentMessageBinding
import pl.devnowak.animalove.model.Chat
import pl.devnowak.animalove.model.Message
import java.util.*

class MessageFragment : Fragment() {

    private val args by navArgs<MessageFragmentArgs>()
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    val db = Firebase.firestore
    val chatList = mutableListOf<Message?>()
    var messageAdapter = MessageAdapter(chatList)
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        val view = binding.root
        val user = args.user
        val currentUser = db.document("users/${firebaseUser.uid}").get()

        binding.messageSendButton.setOnClickListener {
            val msg = binding.messageSendEditText.text.toString()
            if (!msg.equals("")) {
                sendMessage(firebaseUser.uid, user.id, msg, currentUser.result!!.get("username") as String)
            } else {
                Toast.makeText(context, "Pusta wiadomość!", Toast.LENGTH_SHORT).show()
            }
            binding.messageSendEditText.text.clear()
        }
        showChat(user.id)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.messagesRecyclerView.adapter = messageAdapter
    }

    private fun showChat(id: String) {
        try {
            var firebaseUser = FirebaseAuth.getInstance().currentUser
            chatList.clear()

            GlobalScope.launch(Dispatchers.IO) {
                db.collection("users/${firebaseUser?.uid}/activeChats")
                    .document(id).get().addOnSuccessListener {
                        db.collection("chats/${it.get("channelId")}/messages").orderBy("time")
                            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                                if (e != null) {
                                    Log.w(ContentValues.TAG, "Listen error", e)
                                    return@addSnapshotListener
                                }

                                if (querySnapshot != null) {
                                    for (change in querySnapshot!!.documentChanges) {
                                        if (change.type == DocumentChange.Type.ADDED) {
                                            val chat = change.document.toObject<Message>()
                                            val chatAdded = chatList.find { it?.id == chat.id }

                                            if (chatAdded == null) {
                                                chatList.add(chat)
                                            }

                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    messageAdapter.messageList = chatList
                                                    messageAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                        if (change.type == DocumentChange.Type.MODIFIED) {
                                            val chat = change.document.toObject<Message>()
                                            chatList[querySnapshot.documents.indexOf(change.document)] = chat
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    messageAdapter.messageList = chatList
                                                    messageAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                        if (change.type == DocumentChange.Type.REMOVED) {

                                            val chat = change.document.toObject<Message>()

                                            val chatFromList = chatList.find { it?.message == chat.message }
                                            chatList.remove(chatFromList)
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    messageAdapter.messageList = chatList
                                                    messageAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                    }
                                    binding.messagesRecyclerView.scrollToPosition(chatList.size - 1)
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

    private fun sendMessage(sender: String, receiver: String, message: String, senderName: String) {
        val db = Firebase.firestore

        db.collection("users/${firebaseUser?.uid}/activeChats")
            .document(receiver).get().addOnSuccessListener {
                var newChatId: String? = null
                if (!it.exists()) {
                    val newChat = db.collection("chats").document()
                    newChat.set(Chat(mutableListOf(firebaseUser.uid, receiver)))

                    db.collection("users/${firebaseUser?.uid}/activeChats")
                        .document(receiver).set(mapOf("channelId" to newChat.id))

                    db.collection("users/${receiver}/activeChats")
                        .document(firebaseUser.uid).set(mapOf("channelId" to newChat.id))

                    newChatId = newChat.id
                }
                if (it.exists()) {
                    newChatId = it.get("channelId") as String
                }
                val ref = db.collection("chats/${newChatId}/messages")
                val messageId: String = UUID.randomUUID().toString()

                val date = Date(System.currentTimeMillis())
                val map = hashMapOf(
                    "id" to messageId,
                    "sender" to sender,
                    "receiver" to receiver,
                    "message" to message,
                    "time" to date,
                    "senderName" to senderName
                )

                ref.document(messageId).set(map).addOnSuccessListener {
                    if (chatList.isEmpty()) {
                        showChat(receiver)
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Błąd wysyłania!", Toast.LENGTH_LONG).show()
                }
                binding.messagesRecyclerView.scrollToPosition(chatList.size - 1)
            }
    }
}