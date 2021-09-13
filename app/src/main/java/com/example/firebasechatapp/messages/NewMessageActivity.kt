package com.example.firebasechatapp.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.firebasechatapp.R
import com.example.firebasechatapp.models.UserClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*


class NewMessageActivity : AppCompatActivity() {

    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"
        db = FirebaseFirestore.getInstance()

        val adapter = GroupAdapter<GroupieViewHolder>()

        recyclerView_newMessages.adapter = adapter

        fetchUsers()



    }
    private fun fetchUsers(){

        //Fetch users from firebase
        val usersFromFirestore = db.collection("users")
        usersFromFirestore.addSnapshotListener{snapshot, exception ->

            if (exception != null){
                Toast.makeText(this,exception.localizedMessage.toString(),Toast.LENGTH_LONG).show()
            }else {

                if(snapshot != null) {

                    if (!snapshot.isEmpty){
                        //usersList.clear()

                        val usersSnapshot = snapshot.documents
                        val adapter = GroupAdapter<GroupieViewHolder>()
                        for (user in usersSnapshot){
                            val username = user.get("username") as String
                            val userEmail = user.get("email") as String
                            val uid = user.get("uid") as String
                            val newUser = UserClass(username,userEmail, uid)
                            if(uid != FirebaseAuth.getInstance().uid){
                                adapter.add(UserItem(newUser))}
                            Log.d("Success in fetching data.","Username:$username")
                        }

                        adapter.setOnItemClickListener{item, view ->

                            val userItem = item as UserItem

                            val intent = Intent(view.context,ChatLogActivity::class.java)
                            intent.putExtra("USER_KEY",userItem.user)
                            startActivity(intent)
                            finish() //I use finish() because I don't wanna go back to NewMessageActivity, I wanna go to LatestMessages when I go back.

                        }

                        recyclerView_newMessages.adapter = adapter

                    }

                }

            }
        }
    }
}

class UserItem(val user: UserClass): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        //will be called in our list for each user
        viewHolder.itemView.usernameRowTextView.text = user.username

    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

}
