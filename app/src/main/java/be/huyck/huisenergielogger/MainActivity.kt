package be.huyck.huisenergielogger

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider

import be.huyck.huisenergielogger.ViewModel.DataViewModel
import java.util.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

import android.os.Environment

import androidx.preference.PreferenceManager

import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    lateinit var viewModel : DataViewModel
    lateinit var sharedPreferences : SharedPreferences
    lateinit var pad : File
    val TAGJE = "huisenergielogger.Mainactivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[DataViewModel::class.java]
        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        //val currentUser = auth.getCurrentUser()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.backupOpDitToestel = sharedPreferences.getBoolean("backup_op_dit_toestel",true)
        Log.d(TAGJE,"sharedpref: ${viewModel.backupOpDitToestel}")


        if (auth.currentUser != null) {
            val allesdownloaden = sharedPreferences.getBoolean("datadownload",false)
            viewModel.loaddata(allesdownloaden)
        }
        else{
            createSignInIntent()
        }


        viewModel.bestand = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ,"LocalStoredData.csv")

    }

    // Menu stuff
    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_hel, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_login -> {
            // User chose the "Settings" item, show the app settings UI...
            createSignInIntent()
            true
        }
        R.id.action_logout -> {
            signOut()
            true
        }

        R.id.action_settings -> {
            CreateSettingsIntent()
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
             //val navController = findNavController(R.id.nav_hel)
            //navController.navigate(R.id.toonDataFragment)
            //Navigation.findNavController(this,R.id.nav_hel).navigate(R.id.toonDataFragment)
            //      NavHostFragment.findNavController(GeefDataInFragment())
            true
        }

        R.id.action_export ->{
            //viewModel.ImportDataFromFile(File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "meetdata1.csv"))

            val nu = LocalDateTime.now()
            val formatterdatumtime = DateTimeFormatter.ofPattern("YMd_Hmmss")
            val bestandsnaam = "Export" + nu.format(formatterdatumtime) + ".csv"
            viewModel.exportToFile(File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), bestandsnaam))
            val pad = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + bestandsnaam

                Snackbar.make(getWindow().getDecorView(), getString(R.string.snackbar_export) + pad, Snackbar.LENGTH_LONG)
                //.setAction(getString(R.string.snackbar_openfile), SnackBarListenerExport(this))
                .show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val currentUser = auth.getCurrentUser()
        if (currentUser != null) {
            menu!!.findItem(R.id.action_login).setVisible(false)
            menu.findItem(R.id.action_logout).setVisible(true)
        }
        else{
            menu!!.findItem(R.id.action_login).setVisible(true)
            menu.findItem(R.id.action_logout).setVisible(false)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    fun CreateSettingsIntent()
    {
        //startActivity(Intent(Settings.ACTION_SETTINGS)); --> fout, zijn android settings
        startActivity(Intent(this@MainActivity,SettingsActivity::class.java))
        //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.backupOpDitToestel = sharedPreferences.getBoolean("backup_op_dit_toestel",true)
    }

    // firebase functies
    fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = Arrays.asList(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
        // [END auth_fui_create_intent]
    }

    // [START auth_fui_result]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAGJE,"resultcode: $resultCode")
        Log.d(TAGJE,"requestCode: $requestCode")

        if (requestCode == RC_SIGN_IN) {
            //val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                viewModel.loaddata(true)
                //NavHostFragment.findNavController(ToonDataFragment()).navigate(R.id.toonDataFragment)
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    // [END auth_fui_result]

    fun signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                viewModel.cleardata()
            }


        // [END auth_fui_signout]
    }

    companion object {
        private const val RC_SIGN_IN = 45263
    }




}
