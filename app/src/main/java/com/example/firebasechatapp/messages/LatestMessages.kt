package com.example.firebasechatapp.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.firebasechatapp.MainActivity
import com.example.firebasechatapp.R
import com.example.firebasechatapp.models.UserClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessages : AppCompatActivity() {

    private lateinit var db : FirebaseFirestore
    private var usersList = mutableListOf<UserClass>()
    private lateinit var currentUserUid : String
    private val adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var textToShow : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)


        db = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().uid.toString()

        val currentUser = Firebase.auth.currentUser
        if(currentUser==null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        latest_messages_recyclerView.adapter = adapter
        //listenForLatestMessages()
        getUsers()
        createRows()

    }


    private fun getUsers() {

        val usersFromFirestore = db.collection("users")
        usersFromFirestore.addSnapshotListener { snapshot, exception ->

            if (exception != null) {
                Toast.makeText(this, exception.localizedMessage.toString(), Toast.LENGTH_LONG)
                    .show()
            } else {

                if (snapshot != null) {

                    if (!snapshot.isEmpty) {
                        //usersList.clear()

                        val usersSnapshot = snapshot.documents
                        for (user in usersSnapshot) {
                            val username = user.get("username") as String
                            val userEmail = user.get("email") as String
                            val uid = user.get("uid") as String
                            val newUser = UserClass(username, userEmail, uid)
                            if (uid != FirebaseAuth.getInstance().uid) {
                                usersList.add(newUser)
                            }
                            Log.d("Success in fetching data.", "Username:$username")
                        }

                    }

                }
            }

        }
    }


    private fun createRows(){

        for (user in usersList){

            db.collection("user-messages").document(currentUserUid).collection(user.uid).orderBy("timeStamp",Query.Direction.DESCENDING).limit(1).get()
                .addOnCompleteListener {
                    if (it.isSuccessful){

                        for (data in it.result!!){

                            val receiverId = data.getString("receiverId")
                            val text = data.getString("text")
                            println("$receiverId   $text")

                        }

                    }
                }

        }


    }






    private fun listenForLatestMessages(){

        val users = db.collection("users")
        users.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Toast.makeText(this, exception.localizedMessage.toString(), Toast.LENGTH_LONG)
                    .show()
            } else {

                if (snapshot != null) {

                    if (!snapshot.isEmpty) {

                        val usersSnapshot = snapshot.documents
                        for (user in usersSnapshot) {
                            val username = user.get("username") as String
                            val userEmail = user.get("email") as String
                            val uid = user.get("uid") as String
                            val newUser = UserClass(username, userEmail, uid)

                            Log.d("Success in fetching data.", "Username:$username")
                            db.collection("/user-messages/${newUser.uid}/${currentUserUid}").orderBy("timeStamp",Query.Direction.DESCENDING).limit(1).addSnapshotListener{ snapshot, exception ->

                                if (exception != null){
                                    Toast.makeText(this, exception.localizedMessage.toString(), Toast.LENGTH_LONG).show()
                                } else {

                                    if(snapshot != null){

                                        if (!snapshot.isEmpty){

                                            val userMessagesSnapshot = snapshot.documents
                                            for(message in userMessagesSnapshot) {
                                                textToShow = message.get("text") as String
                                                Log.d("LatestMessageRow","Created latest message row with name:${newUser.username}")

                                            }
                                            adapter.add(LatestMessageRow(textToShow, newUser))
                                            adapter.setOnItemClickListener{item, view ->

                                                val latestMessageRow = item as LatestMessageRow

                                                val intent = Intent(view.context,ChatLogActivity::class.java)
                                                Log.d("Clicked:", latestMessageRow.user.username)
                                                intent.putExtra("USER_KEY",latestMessageRow.user)
                                                startActivity(intent)

                                            }

                                        }

                                    }

                                }

                            }

                        }



                    }

                }

            }
        }

    }


    class LatestMessageRow (val text : String, val user : UserClass) : Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            viewHolder.itemView.latestMessageUsername.text = user.username
            viewHolder.itemView.latestMessageText.text = text

        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item?.itemId){
            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            R.id.new_message ->{

                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)

            }
        }


        return super.onOptionsItemSelected(item)
    }


}

