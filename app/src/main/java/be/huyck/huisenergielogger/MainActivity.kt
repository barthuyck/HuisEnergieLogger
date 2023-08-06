package be.huyck.huisenergielogger

import android.R.string
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.AlarmClock
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import be.huyck.huisenergielogger.ViewModel.DataViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel : DataViewModel
    private lateinit var sharedPreferences : SharedPreferences
    // lateinit var pad : File
    private val TAGJE = "huisenergielogger.Mainactivity"

    // [START auth_fui_create_launcher]
    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }
    // [END auth_fui_create_launcher]

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
            createSignInIntent()
            true
        }
        R.id.action_logout -> {
            signOut()
            true
        }

        R.id.action_settings -> {
            CreateSettingsIntent()
            true
        }

        R.id.action_export ->{
            CreateActionExportDataIntent()
            true
        }

        R.id.action_alarm ->{
            createAlarm("Registreer je meterstanden", 21, 30)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun createAlarm(message: String, hour: Int, minutes: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
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

    private fun CreateSettingsIntent()
    {
        //startActivity(Intent(Settings.ACTION_SETTINGS)); --> fout, zijn android settings
        startActivity(Intent(this@MainActivity,SettingsActivity::class.java))
        //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.backupOpDitToestel = sharedPreferences.getBoolean("backup_op_dit_toestel",true)
    }

    // firebase functies
    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            //AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())


        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
        // [END auth_fui_create_intent]
    }

    // [START auth_fui_result]
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        Log.d(TAGJE,"resultcode: $response")
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            viewModel.loaddata(true)
            val user = FirebaseAuth.getInstance().currentUser
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }
    // [END auth_fui_result]

    private fun signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                viewModel.cleardata()
            }
        // [END auth_fui_signout]
    }

    private fun CreateActionExportDataIntent(){
        val nu = LocalDateTime.now()
        val formatterdatumtime = DateTimeFormatter.ofPattern("YMd_Hmmss")
        val bestandsnaam = "Export" + nu.format(formatterdatumtime) + ".csv"

        viewModel.exportToFile(File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), bestandsnaam))
        val pad = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + bestandsnaam
        Snackbar.make(getWindow().getDecorView(), getString(R.string.snackbar_export) + pad, Snackbar.LENGTH_LONG)
            .show()
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, viewModel.exportToString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    /*
        private fun schrijfDataWegNaarBestand(uri: Uri){
            viewModel.exportToFile(uri.toFile())
        }

        val slaBestandOpLauncher =  registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/*")
        ) {
                uri: Uri? ->
                this.onSlaBestandOpResult(uri)
        }

        private fun createSlaBestandOpIntent() {
            val nu = LocalDateTime.now()
            val formatterdatumtime = DateTimeFormatter.ofPattern("YMd_Hmmss")
            val bestandsnaam = "Export" + nu.format(formatterdatumtime) + ".csv"

            viewModel.exportToFile(File(getExternalFilesDir("DIRECTORY_DOCUMENTS"),bestandsnaam))

            val SlaOpIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/csv"
                putExtra(Intent.EXTRA_TITLE, bestandsnaam)
            }
            //slaBestandOpLauncher.launch(bestandsnaam)

        }

        private fun onSlaBestandOpResult(uri: Uri? ) {

            uri?.path?.let {
                Log.d(TAGJE,"uridata.path: ${uri?.path}")
                //viewModel.exportToFile(File(uri?.path))
            }

            /*if (resultaat.resultCode == RESULT_OK) {
                // Successfully signed in
                val Intentwaarde = resultaat.data
                Log.d(TAGJE,"Intentwaarde: ${Intentwaarde}")
                val uridata = Intentwaarde?.data
                Log.d(TAGJE,"uridata.path: ${uridata?.path}")

            }*/
        }
        */
        */


}
