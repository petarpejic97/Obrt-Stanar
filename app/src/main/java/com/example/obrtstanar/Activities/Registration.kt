package com.example.obrtstanar.Activities

//import com.google.firebase.database.FirebaseDatabase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.obrtstanar.Klase.Controllers.AlertController
import com.example.obrtstanar.Klase.Controllers.PasswordController
import com.example.obrtstanar.Klase.ProgressDialog
import com.example.obrtstanar.Klase.FirebaseClass.User
import com.example.obrtstanar.R
import com.example.obrtstanar.RegistrationViewModel
import com.example.obrtstanar.databinding.ActivityRegistrationBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_registration.*


class Registration : AppCompatActivity() {
    private lateinit var viewModel : RegistrationViewModel
    private lateinit var binding : ActivityRegistrationBinding

    lateinit var progress : ProgressDialog

    private var alertController : AlertController = AlertController()

    var emptyflag : Boolean = true;
    var passrequerments : Boolean = true;
    var passequality : Boolean = true;

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        initializateVariable()
        setUpUI()
        backListener()
        btnRegListener()
    }
    private fun initializateVariable() {
        // Initialize Firebase Auth
        auth = Firebase.auth

        binding = DataBindingUtil.setContentView(this,R.layout.activity_registration)

        viewModel = ViewModelProviders.of(this).get(RegistrationViewModel::class.java)

        progress = ProgressDialog(this, "Prijava", "Molimo pričekate...")
    }

    private fun setUpUI(){
        binding.apply {
            registration = viewModel
            binding.registration?.name?.value = viewModel.name.value
            binding.registration?.lastname?.value = viewModel.lastname.value
            binding.registration?.phoneNumber?.value = viewModel.phoneNumber.value
            binding.registration?.address?.value = viewModel.address.value
            binding.registration?.email?.value = viewModel.email.value
            binding.registration?.password?.value = viewModel.password.value
            binding.registration?.confPass?.value = viewModel.confPass.value
        }
    }
    private fun backListener() {
        binding.imgBack.setOnClickListener {
            goOnLoginActivity()
        }
    }
    private fun goOnLoginActivity() {
        val intent = Intent(this, LoginUser::class.java)
        startActivity(intent)
        finish()
    }
    private fun btnRegListener() {
        binding.btnRegistration.setOnClickListener {
            evaluateEditTexts()
        }
    }
    private fun evaluateEditTexts() {
        emptyflag = true
        var passwordControler = PasswordController(viewModel.email.value.toString(),
            viewModel.email.value.toString())
        checkEmptyED()
        if(emptyflag){
            passrequerments = passwordControler.checkPasswordRequirments()
            if(!passrequerments){
                alertController.oneEditTextAlert(edPassword,"Zaporka mora imati najmanje 6 znakova i sadržavati barem jedan broj.")
            }
        }

        if(passrequerments && emptyflag){
            passequality = passwordControler.checkPasswordEquality()
            if(!passequality){
                alertController.twoEditTextAlert(edPassword,edConfPass,"Zaporka nisu iste.")
            }
        }
        if(emptyflag && passrequerments && passequality){
            registration()
        }
    }
    private fun checkEmptyED() {
        if(viewModel.name.value==null) {
            alertController.oneEditTextAlert(edName,"Polje ne smije biti prazno.")
            emptyflag = false
        }
        if(viewModel.lastname.value==null){
            alertController.oneEditTextAlert(edLastname,"Polje ne smije biti prazno.")
            emptyflag = false
        }
        if(viewModel.phoneNumber.value==null){
            alertController.oneEditTextAlert(edPhoneNumber,"Polje ne smije biti prazno.")
            emptyflag = false
        }
        if(viewModel.address.value==null){
            alertController.oneEditTextAlert(edAddress,"Polje ne smije biti prazno.")
            emptyflag = false
        }
        if(viewModel.email.value==null){
            alertController.oneEditTextAlert(edEmail,"Polje ne smije biti prazno.")
            emptyflag = false
        }
        if(viewModel.password.value==null){
            alertController.oneEditTextAlert(edPassword,"Polje ne smije biti prazno.")
            emptyflag = false
        }
        if(viewModel.confPass.value==null){
            alertController.oneEditTextAlert(edConfPass,"Polje ne smije biti prazno.")
            emptyflag = false
        }
    }
    private fun registration() {
        progress.showDialog()

        auth.createUserWithEmailAndPassword(binding.registration?.email?.value!!, binding.registration?.password?.value!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener(OnCompleteListener<Void?> { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this,"Molimo Vas potvrdite svoj email!",Toast.LENGTH_LONG).show()
                                saveInDatabase()
                                goOnLoginActivity()
                            }
                        })

                } else {
                    progress.progresDismis()
                    Toast.makeText(baseContext, "Authentication failed.",Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun saveInDatabase(){
        val user = User(
            binding.registration?.name?.value!!,
            binding.registration?.lastname?.value!!,
            binding.registration?.phoneNumber?.value!!,
            binding.registration?.address?.value!!,
            binding.registration?.email?.value!!
        )

        val database = FirebaseDatabase.getInstance()
        val key = database.getReference("users").push().key
        val myRef = key?.let { database.getReference().child("users").child(it).setValue(user) }

    }
}