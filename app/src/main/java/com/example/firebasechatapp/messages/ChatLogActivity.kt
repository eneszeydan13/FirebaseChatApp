package com.example.firebasechatapp.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.firebasechatapp.R
import com.example.firebasechatapp.models.UserClass
import com.example.firebasechatapp.models.ChatMessage
import com.example.firebasechatapp.models.ReceivedChatItem
import com.example.firebasechatapp.models.SentChatItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var user : UserClass
    private lateinit var db : FirebaseFirestore
    private lateinit var currentUserUid : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        user = intent.getParcelableExtra<UserClass>("USER_KEY")!!
        supportActionBar?.title = user?.username
        db = FirebaseFirestore.getInstance()
        currentUserUid = FirebaseAuth.getInstance().uid.toString()
        recyclerViewChat.adapter = adapter
        listenForMessages()
        recyclerViewChat.scrollToPosition((adapter.itemCount)-1)
    }

    private fun listenForMessages(){

        val messages = db.collection("user-messages/${currentUserUid}/${user.uid}").orderBy("timeStamp")
        messages.addSnapshotListener{snapshot, exception ->

            if (exception != null){
                Toast.makeText(this,exception.localizedMessage.toString(), Toast.LENGTH_LONG).show()
            }else{
                if(snapshot != null) {
                    if(!snapshot.isEmpty){
                        adapter.clear()
                        val messagesSnapshot = snapshot.documents

                        for(message in messagesSnapshot){

                            val text = message.get("text") as String
                            val receiverId = message.get("receiverId") as String
                            val userId = message.get("userId") as String
                            val timeStamp = message.get("timeStamp") as Long

                            if(userId == currentUserUid){
                                adapter.add(SentChatItem(text))
                            }else if(userId == user.uid){
                                adapter.add(ReceivedChatItem(text))
                            }

                        }



                    }
                }
            }

        }

    }

    fun sendButtonClicked(view: View){

        if(messageText.text != null){
            val fromId = FirebaseAuth.getInstance().uid
            val toId = user.uid
            Log.d("Chatlog","Attempt to send a message:"+messageText.text.toString())
            val text = messageText.text.toString()
            val chatMessage = ChatMessage(text, fromId!!,toId, System.currentTimeMillis()/1000 )
            performSendMessage(chatMessage)
            messageText.setText("")
            recyclerViewChat.scrollToPosition(adapter.itemCount - 1)
        }



    }
    companion object {
        const val TAG = "ChatLog"
    }
    private fun performSendMessage( chatMessage : ChatMessage){

        val chatMap = hashMapOf(
            "text" to chatMessage.text,
            "userId" to chatMessage.userId,
            "receiverId" to chatMessage.receiverId,
            "timeStamp" to chatMessage.timeStamp
        )

        //Saving the messages for both users so it can be shown on both users' screens.
        db.collection("/user-messages/${chatMessage.userId}/${chatMessage.receiverId}").add(chatMap).addOnCompleteListener {
            if(it.isSuccessful && it.isComplete){
                Log.d(TAG,"Success! Message sent:${chatMap.get("text")}")
            }
        }.addOnFailureListener{
            Log.w(TAG,"Message not sent!",it)
        }

        db.collection("/user-messages/${chatMessage.receiverId}/${chatMessage.userId}").add(chatMap).addOnCompleteListener {
            if(it.isSuccessful && it.isComplete){

                Log.d(TAG,"Success! Message sent:${chatMap.get("text")}")

            }
        }.addOnFailureListener {
            Log.w(TAG,"Message not sent!",it)
        }

        //Latest message is also saved so it can be viewed on LatestMessages activity.

        db.collection("latest-messages").document("${chatMessage.receiverId} to ${chatMessage.userId}").set(chatMap)
        db.collection("latest-messages").document("${chatMessage.userId} to ${chatMessage.receiverId}").set(chatMap)



    }
}







