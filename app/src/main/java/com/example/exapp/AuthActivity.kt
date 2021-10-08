package com.example.exapp
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity(){
    private lateinit var remindCheckBox: Switch
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var googleButton: Button
    private val GOOGLE_SIGN_IN =100

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //Analytics Event
        setContentView(R.layout.activity_auth)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginButton = findViewById(R.id.loginButton)
        googleButton = findViewById(R.id.googleButton)
        remindCheckBox = findViewById(R.id.remindCheckBox)
        val loaderPreferences = getSharedPreferences("credentials", Context.MODE_PRIVATE)
        if (loaderPreferences.getBoolean("remindCheckBox", false)) {
            startActivity(Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("email", loaderPreferences.getString("email", ""))
                putExtra("password", loaderPreferences.getString("password", ""))
            })
        }
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integration of Firebase complet")
        analytics.logEvent("InitScreen", bundle)

        // Setup
        setup()

    }



    private fun setup() {
        title = "Autentication"
        signUpButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        emailEditText.text.toString(),
                        passwordEditText.text.toString()
                    )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            saveSession(applicationContext)
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert("Sea producido en un error autenticando al usuario")

                        }
                    }

            } else {
                showAlert("Campos requeridos vacios")
            }
        }
        loginButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(
                        emailEditText.text.toString(),
                        passwordEditText.text.toString()
                    )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            saveSession(applicationContext)
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert("Sea producido en un error autenticando al usuario")
                        }
                    }
            } else {
                showAlert("Campos requeridos vacios")
            }
        }
        googleButton.setOnClickListener {
            // Configuration
            val googleConf =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }


    }

    private fun saveSession(context: Context) {
        val edit = context.getSharedPreferences("credentials", Context.MODE_PRIVATE).edit()
        edit.putString("email", emailEditText.text.toString())
        edit.putString("password", passwordEditText.text.toString())
        edit.putBoolean("remindCheckBox", remindCheckBox.isChecked)
        edit.apply()
        Toast.makeText(
            applicationContext,
            "Credentials successfully saved!",
            Toast.LENGTH_SHORT
        )
            .show()
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("$message")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, password: ProviderType) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showHome(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert("Sea producido en un error autenticando al usuario")
                            }
                        }
                }

            } catch (e: ApiException) {
                showAlert("Sea producido un error")
            }

        }

    }

}