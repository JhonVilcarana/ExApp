package com.example.exapp


import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class ProviderType{
    BASIC,
    GOOGLE
}
class MainActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var emailEdit: TextView
    private lateinit var passwordEdit: TextView
    private lateinit var logOut: Button
    private lateinit var saveButton: Button
    private lateinit var recButton: Button
    private lateinit var deleteButton: Button
    private lateinit var addressTextView: TextView
    private lateinit var phoneTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Analytics Event
        setContentView(R.layout.activity_main)
        emailEdit = findViewById(R.id.emailEdit)
        passwordEdit = findViewById(R.id.passwordEdit)
        logOut = findViewById(R.id.logOut)
        saveButton = findViewById(R.id.saveButton)
        recButton = findViewById(R.id.recButton)
        deleteButton = findViewById(R.id.deleteButton)
        addressTextView = findViewById(R.id.addressTextView)
        phoneTextView = findViewById(R.id.phoneTextView)


        //setup
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val password = bundle?.getString("password")
        setup(email ?: "", password ?: "")
    }

    private fun setup(email: String, password: String) {
        title = "Inicio"
        emailEdit.text = email
        passwordEdit.text = password

        logOut.setOnClickListener {
            val clean = getSharedPreferences("credentials", Context.MODE_PRIVATE)
                .edit()
            clean.remove("email")
            clean.remove("password")
            clean.remove("remindCheckBox")
            clean.apply()
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
        saveButton.setOnClickListener {
            db.collection("users").document(email).set(
                hashMapOf("password" to password,
                "address" to addressTextView.text.toString(),
                "phone" to phoneTextView.text.toString())
            )
        }
        recButton.setOnClickListener {
            db.collection("users").document(email).get().addOnSuccessListener {
                addressTextView.setText(it.get("address") as String?)
                phoneTextView.setText(it.get("phone") as String?)
            }
        }
        deleteButton.setOnClickListener {
            db.collection("users").document(email).delete()

        }
    }
}