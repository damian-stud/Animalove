package pl.devnowak.animalove.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import pl.devnowak.animalove.databinding.FragmentChatUsersBinding


class ChatUsersFragment : Fragment() {

    private var _binding: FragmentChatUsersBinding? = null
    private val binding get() = _binding!!
    val db = Firebase.firestore
    val users = mutableListOf<User?>()
    var userAdapter = UserAdapter(users, false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatUsersBinding.inflate(inflater, container, false)
        val view = binding.root

        showUsers()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chatUsersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.chatUsersRecyclerView.adapter = userAdapter

        showUsers()
    }

    private fun showUsers() = CoroutineScope(Dispatchers.IO).launch {
        try {
            var firebaseUser = FirebaseAuth.getInstance().currentUser
            users.clear()

            val roles = mutableListOf("employee", "volunteer", "admin")

            db.collection("users").whereIn("role", roles).orderBy("username")
                .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                    if (e != null) {
                        Log.w(ContentValues.TAG, "Listen error", e)
                        return@addSnapshotListener
                    }
                    for (change in querySnapshot!!.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            val user = change.document.toObject<User>()
                            val userAdded = users.find { it?.id == user.id }
                            val userId = user.id
                            val fUserId = firebaseUser?.uid


                            if (userId != fUserId) {
                                if (userAdded == null) {
                                    users.add(user)
                                }
                            }
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    userAdapter.users = users
                                    userAdapter.isChat = true
                                    userAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                        if (change.type == DocumentChange.Type.MODIFIED) {
                            val user = change.document.toObject<User>()
                            val userId = user.id
                            val fUserId = firebaseUser?.uid

                            val userAdded = users.find { it?.id == user.id }
                            if (userId != fUserId) {
                                if (userAdded != null) {
                                    users[querySnapshot.documents.indexOf(change.document)] = user
                                }
                            }
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    userAdapter.users = users
                                    userAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                        if (change.type == DocumentChange.Type.REMOVED) {
                            val user = change.document.toObject<User>()
                            val userFromList = users.find { it?.id == user.id}
                            users.remove(userFromList)

                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    userAdapter.users = users
                                    userAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}