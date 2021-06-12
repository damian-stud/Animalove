package pl.devnowak.animalove.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import pl.devnowak.animalove.model.User
import pl.devnowak.animalove.adapter.UserAdapter
import pl.devnowak.animalove.databinding.FragmentAllMessagesBinding

class AllMessagesFragment : Fragment() {

    private var _binding: FragmentAllMessagesBinding? = null
    private val binding get() = _binding!!
    val db = Firebase.firestore
    val users = mutableListOf<User?>()
    var userAdapter = UserAdapter(users, false)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentAllMessagesBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.allMessagesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.allMessagesRecyclerView.adapter = userAdapter

        showStartedChats()
    }

    private fun showStartedChats() = CoroutineScope(Dispatchers.IO).launch {
        try {
            var firebaseUser = FirebaseAuth.getInstance().currentUser
            users.clear()
            var otherParticipantsIDs = mutableListOf("", "")

            db.collection("chats").whereArrayContains("participants", firebaseUser.uid)
                .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                    if (e != null) {
                        Log.w(ContentValues.TAG, "Listen error", e)
                        return@addSnapshotListener
                    }

                    for (document in querySnapshot!!.documents) {
                        val userIds: ArrayList<String> = document.get("participants") as ArrayList<String>
                        for (userId in userIds) {
                            if (userId != firebaseUser?.uid) {
                                otherParticipantsIDs.add(userId)
                            }
                        }
                    }

                    db.collection("users").whereIn("id", otherParticipantsIDs)
                        .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                            if (e != null) {
                                Log.w(ContentValues.TAG, "Listen error", e)
                                return@addSnapshotListener
                            }
                            for (change in querySnapshot!!.documentChanges) {
                                if (change.type == DocumentChange.Type.ADDED) {
                                    val user = change.document.toObject<User>()
                                    val userAdded = users.find { it?.id == user?.id }
                                    if (userAdded == null) {
                                        users.add(user)
                                    }
                                    userAdapter.users = users
                                    userAdapter.isChat = true
                                    userAdapter.notifyDataSetChanged()
                                }
                                if (change.type == DocumentChange.Type.MODIFIED) {
                                    val user = change.document.toObject<User>()
                                    users[querySnapshot.documents.indexOf(change.document)] = user

                                    userAdapter.users = users
                                    userAdapter.notifyDataSetChanged()

                                }
                                if (change.type == DocumentChange.Type.REMOVED) {
                                    val user = change.document.toObject<User>()
                                    users.remove(user)

                                    userAdapter.users = users
                                    userAdapter.notifyDataSetChanged()

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