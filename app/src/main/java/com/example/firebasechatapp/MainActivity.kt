package com.example.firebasechatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.firebasechatapp.messages.LatestMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            //What will happen if a user is already logged in
            val intent = Intent(baseContext, LatestMessages::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = Firebase.firestore
        auth = Firebase.auth

    }


    fun registerClicked(view : View){

        val username = usernameTextView.text.toString()
        val email = emailTextView.text.toString()
        val password = passwordTextView.text.toString()
        if (email!=null && password != null){
            Log.d("Main Activity", "Email is: $email" )
            Log.d("Main Activity", "Password: $password")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {
                    if(it.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("Main","Successfully created user with UID: ${it.result!!.user!!.uid}")
                        saveUserToFirebase(email, password, username,FirebaseAuth.getInstance().uid!!)
                        //Update UI in case of successful registration
                        val intent = Intent(baseContext, LatestMessages::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }.addOnFailureListener {
                    Log.w("Main","Failed to create user: ${it.message}")
                    Toast.makeText(baseContext,"Failed to create user: ${it.message}",Toast.LENGTH_LONG).show()
                }


        } else {
            Toast.makeText(baseContext,"Please don't leave password or email blank.",Toast.LENGTH_LONG).show()
        }

    }

    fun asiClicked(view: View){

        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)


    }

    private fun saveUserToFirebase(email:String, password:String, username:String, uid:String){

        val userMap = hashMapOf<String,String>(
            "email" to email,
            "password" to password,
            "username" to username,
            "uid" to uid
        )
        db.collection("users").add(userMap).addOnCompleteListener {
            if(it.isSuccessful&&it.isComplete){
                Log.d("User Saved","Success!")
            }
        }.addOnFailureListener {
            Log.w("Error","Error adding user",it)
        }


    }

}