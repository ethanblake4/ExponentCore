package io.ethanblake4.exponentdemo

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import io.ethanblake4.exponentcore.hl.ExAuthFlow
import io.ethanblake4.exponentcore.hl.ExponentAccounts
import kotlinx.android.synthetic.main.activity_spooky.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spooky)


        // Launch the custom Auth activity
        customStart.setOnClickListener {
            startActivity(Intent(this, CustomLoginActivity::class.java))
        }

        // All the code needed for AuthFlow is here
        authFlowStart.setOnClickListener {
            ExAuthFlow.start(this, ExAuthFlow.Format.GOOGLE, { loginState ->
                if(loginState == ExAuthFlow.State.COMPLETE) {
                    // Auth complete. Do something!
                    maybeShowAccountDetails()
                    //signOut.isEnabled = true
                }
            }, { throwable ->
                throwable.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error logging in", Toast.LENGTH_LONG).show()
                }
            })
        }



        /*signOut.setOnClickListener { _ ->
            ExponentAccounts.currentAccount?.let {
                ExponentAccounts.logout(it.email)
                accountHeader.setText(R.string.not_signed_in)
                accountDetail.setText(R.string.select_for_signin)
                signOut.isEnabled = false
            } ?: Toast.makeText(this, "Not signed in", Toast.LENGTH_LONG).show()
        }*/
    }


    override fun onResume() {
        super.onResume()
        maybeShowAccountDetails()
    }

    private fun maybeShowAccountDetails() {
        ExponentAccounts.currentAccount?.let {
            runOnUiThread {
                Toast.makeText(this, "Hacker signed in as ${it.firstName} ${it.lastName}", Toast.LENGTH_LONG).show()
                Handler().postDelayed({
                    runOnUiThread {
                        Toast.makeText(this, "Hacker has access to ${ExponentAccounts.master!!.services?.size ?: 0} Google services", Toast.LENGTH_LONG).show();
                    }
                }, 1500)

            }
            /*accountHeader.text = "Signed in as ${ExponentAccounts.master!!.Email ?: "Unknown"}"
            accountDetail.text = "This account has access to " +
                    "${ExponentAccounts.master!!.services?.size ?: 0} Google services"
            signOut.isEnabled = true*/
        }
    }

}