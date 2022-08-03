package com.example.bookhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bookhub.databinding.SignupFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.signup_fragment.*

class signup_fragment(): Fragment() {
    var root:View?=null
    private lateinit var ref:DatabaseReference
    var mAurh:FirebaseAuth?=null
    var fstore:FirebaseFirestore?=null
    var userID:String?=null
    companion object{
        const val TAG="TAG"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.signup_fragment, container, false)
        register()
        return root
    }

@SuppressLint("UseRequireInsteadOfGet")
// create user for authentication
fun createuser(){
        val dateofbirth=root!!.findViewById<EditText>(R.id.dob)
        var ffullname:EditText=root!!.findViewById(R.id.fullname)
//        val mfullname=ffullname.text.toString()
        val phone:EditText=root!!.findViewById(R.id.phone)
        val email:EditText=root!!.findViewById(R.id.email)
        val password:EditText=root!!.findViewById(R.id.password)
        val DOB:EditText= root!!.findViewById(R.id.dob)
        val memail=email.text.toString()
        val mpassword=password.text.toString()
        val fAuth:FirebaseAuth= FirebaseAuth.getInstance()
        val progressbar:ProgressBar=root!!.findViewById(R.id.progress_bar)
        try {
            if (email.text.toString().isEmpty())
            {
                email.error="Email is required"
                return
            }
            if (password.text.toString().isEmpty())
            {
                password.error="Email is required"
                return
            }
            if (password.text.toString().length<6)
            {
                password.error="password must be greater then 6 characters"
                return
            }
            progressbar.visibility=View.VISIBLE
            //create account on app
            fAuth.createUserWithEmailAndPassword(memail, mpassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // send verification link
                    val fuser = fAuth.currentUser
                    fuser!!.sendEmailVerification().addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            "Verification Email Has been Sent.\n Verify Your Email to use this app",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent:Intent= Intent(activity,Loginhome::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)
                        activity!!.finish()
                    }.addOnFailureListener { e ->
                        Log.d(
                            TAG,
                            "onFailure: Email not sent " + e.message
                        )
                        progressbar.visibility=View.GONE
                    }
                    ref= FirebaseDatabase.getInstance().getReference("bookhub/users")
                    val getid=ref.push().key
                    val currenuser=fAuth.currentUser!!.uid
                    val User=User(fullname.text.toString(),email.text.toString(),password.text.toString(),phone.text.toString(),dob.text.toString(),"create your won bio",)
                    ref.child(currenuser).setValue(User).addOnSuccessListener {
                        ffullname.text.clear()
                        email.text.clear()
                        password.text.clear()
                        phone.text.clear()
                        DOB.text.clear()
                    }.addOnFailureListener{
                        Toast.makeText(
                            activity,
                            "something going wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        activity,
                        "Error ! " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    progressbar.visibility = View.GONE
                }
            }
        }
        catch (e:Exception)
        {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    //    add data on realtime database
//    val fAuth:FirebaseAuth= FirebaseAuth.getInstance()

    }
//    register function
    fun register(){
        val regbtn:Button=root!!.findViewById(R.id.signupbtn)
        regbtn.setOnClickListener{
            createuser()
        }
    }
}