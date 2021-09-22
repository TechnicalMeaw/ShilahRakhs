package com.coprotect.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserReference
import com.coprotect.myapplication.constants.IntentStrings
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit


class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var phoneNumber : String
    private lateinit var countryName : String


//    private lateinit var notificationToken: String

    private lateinit var resendBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        phoneNumber = intent.getStringExtra(IntentStrings.phoneNumber).toString()
        countryName = intent.getStringExtra(IntentStrings.countryName).toString()


        val phoneNumberTextView = findViewById<TextView>(R.id.verifyPhoneNumberTextView)
        val verifyBtn = findViewById<Button>(R.id.verifyOtpBtn)
        val otpEditText = findViewById<EditText>(R.id.verifyOtpEditText)
        resendBtn = findViewById<TextView>(R.id.resendCodeTextView)
        val notYouBtn = findViewById<TextView>(R.id.notYouBtn)
//        val otpAnimation = findViewById<LottieAnimationView>(R.id.otpAnimation)
//        otpAnimation.playAnimation()

        //Show number details
        phoneNumberTextView.text = "Code sent to: " + phoneNumber.substring(0,phoneNumber.length - 10) + "-" + phoneNumber.substring(phoneNumber.length-10,phoneNumber.length) + " | "

        //Send OTP
        sendCode(phoneNumber)

        //Verify Otp
        verifyBtn.setOnClickListener {
            val otp = otpEditText.text.toString()
            if (otp != ""){
                verifyVerificationCode(otp)
            }
        }

        //Prompt to Change phone number
        notYouBtn.setOnClickListener { finish() }

        //Resend Code
        resendBtn.setOnClickListener {
            try{
                resendVerificationCode(phoneNumber, resendToken!!)
            }catch (e: Exception){
                e.stackTrace
            }
        }

    }


    private fun sendCode(phoneNumber: String){

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        // Start the countdown
        startCountdown()
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this,  // Activity (for callback binding)
            callbacks,  // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks

        Toast.makeText(this, "Sending New Code...", Toast.LENGTH_SHORT).show()
        // Start the countdown
        startCountdown()
    }



    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("VerifyCodeActivity", "onVerificationCompleted:$credential")

            Toast.makeText(this@VerifyCodeActivity, "Success", Toast.LENGTH_SHORT).show()

            // Upload Image to fireStorage
            // val user = FirebaseAuth.getInstance().currentUser
            // updateUI(user)
            signInWithPhoneAuthCredential(credential)

        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w("VerifyCodeActivity", "onVerificationFailed", e)

            // If sign in fails, display a message to the user.
            // updateUI(null)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(this@VerifyCodeActivity, e.message, Toast.LENGTH_LONG).show()
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(this@VerifyCodeActivity, e.message, Toast.LENGTH_LONG).show()
                // ...
            }

            // Show a message and update the UI
            // ...
            Toast.makeText(this@VerifyCodeActivity, "Failed", Toast.LENGTH_SHORT).show()

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("VerifyCodeActivity", "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token

            // ...
            Toast.makeText(this@VerifyCodeActivity, "Code Sent.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun verifyVerificationCode(code: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)

        // signing the user
        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("VerifyCodeActivity", "signInWithCredential:success")
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()

                    val user = task.result?.user
                    registerToDatabase(user)
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("VerifyCodeActivity", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun registerToDatabase(user: FirebaseUser?) {

        val ref = getUserReference(user?.uid.toString())
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    redirectToMainActivity()
                }else{
                    redirectToRegisterNewUser()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun redirectToRegisterNewUser() {
        val intent = Intent(this, RegisterUserActivity::class.java)
        intent.putExtra(IntentStrings.phoneNumber, phoneNumber)
        intent.putExtra(IntentStrings.countryName, countryName)
        startActivity(intent)
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    private fun startCountdown(){
        resendBtn.isClickable = false

        val timer = object: CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendBtn.text = "Wait until: ${(millisUntilFinished/1000)} sec"
            }

            override fun onFinish() {
                val content = SpannableString("Resend")
                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                resendBtn.text = content
                resendBtn.isClickable = true
            }
        }
        timer.start()
    }


}