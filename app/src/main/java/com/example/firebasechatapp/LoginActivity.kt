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
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        auth = Firebase.auth



    }



    fun loginClicked(view: View){

        val email = emailTextView.text.toString()
        val password = passwordTextView.text.toString()

        if(email.isNotEmpty()&&password.isNotEmpty()) {

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d("Login", "Succesfully logged in with email:$email")
                        val intent = Intent(this, LatestMessages::class.java)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Log.w("Login", "Failed to login: ${it.message}")
                    Toast.makeText(this,"Failed to login: ${it.message}",Toast.LENGTH_LONG).show()
                }
        }
    }
}