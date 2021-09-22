package com.coprotect.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.coprotect.myapplication.constants.IntentStrings
import com.hbb20.CountryCodePicker

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val countryCodePicker = findViewById<CountryCodePicker>(R.id.ccp)
        val phoneNumberEditText = findViewById<EditText>(R.id.loginPhoneEditText)
        val nextBtn = findViewById<Button>(R.id.loginNextBtn)

        nextBtn.setOnClickListener {

            val phoneNumber = countryCodePicker.selectedCountryCodeWithPlus + phoneNumberEditText.text.toString()
            val country = countryCodePicker.selectedCountryName

            if (phoneNumber != ""){
                redirectToEnterOtp(phoneNumber, country)
            }else{
                Toast.makeText(this, "Empty Fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redirectToEnterOtp(phone:String, country: String){
        val intent = Intent(this, VerifyCodeActivity::class.java)
        intent.putExtra(IntentStrings.phoneNumber, phone)
        intent.putExtra(IntentStrings.countryName, country)
        startActivity(intent)
    }

}