package be.huyck.huisenergielogger.ViewModel


import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class DataViewModel : ViewModel() {
    val TAGJE = "huisenergielogger.viewmodel"
    var backupOpDitToestel = false
    var TeUpdatenRegistratieGegevens = 0
    lateinit var bestand : File
    private var auth : FirebaseAuth

    init {
        //Log.d(TAGJE, "init doorlopen")
        auth = FirebaseAuth.getInstance()

    }

    private var lijst = ArrayList<RegistratieGegevens>()

    val lijstRegistratieGegevens: MutableLiveData<List<RegistratieGegevens>> by lazy {
        MutableLiveData<List<RegistratieGegevens>>()
    }

    fun addData(registratieGegevens: RegistratieGegevens){
        //Log.d(TAGJE, "Data ingeven in viewmodel!")

        lijstRegistratieGegevens.postValue(lijst)

        val user = auth.currentUser
        if (user != null) {
            Log.d(TAGJE, "gebruiker ingelogd, ...")
            val db = FirebaseFirestore.getInstance()
            val db_useruid = user.uid.toString()

            val docRef = db.collection("users").document(db_useruid).collection("meetgegevens")
                .document()

            registratieGegevens.firebaseid = docRef.id
            lijst.add(0,registratieGegevens)

            val docData = hashMapOf(
                "datum" to Timestamp(
                    registratieGegevens.registratiedatum.toEpochSecond(ZoneOffset.UTC),
                    0
                ),
                "meterwaardeEL" to registratieGegevens.meterwaarde_el,
                "meterwaardeGA" to registratieGegevens.meterwaarde_ga,
                "meterwaardeWA" to registratieGegevens.meterwaarde_wa,
                "meterwaardePV" to registratieGegevens.meterwaarde_pv
            )

            docRef.set(docData)
                .addOnSuccessListener { Log.d(TAGJE, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAGJE, "Error writing document", e) }
        }
        else{
            Log.d(TAGJE, "Geen gebruiker ingelogd, niets geschreven!")
        }
        if (backupOpDitToestel){
            SaveDataLocally(bestand,registratieGegevens)
            Log.d(TAGJE, "Data local opgeslagen!")
        }
    }

    fun deleteData(registratieGegevens: RegistratieGegevens){
        Log.d(TAGJE, "Data updaten in viewmodel!")
        lijst.remove(registratieGegevens)
        lijstRegistratieGegevens.postValue(lijst)

        val user = auth.currentUser
        if (user != null) {
            Log.d(TAGJE, "gebruiker ingelogd, ...")
            val db = FirebaseFirestore.getInstance()
            val db_useruid = user.uid.toString()

            val docRef = db.collection("users").document(db_useruid).collection("meetgegevens")
                .document(registratieGegevens.firebaseid).delete()
                    .addOnSuccessListener { Log.d(TAGJE, "DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(TAGJE, "Error deleting document", e) }

        }
        else{
            Log.d(TAGJE, "Geen gebruiker ingelogd, niets geschreven!")
        }

    }

    fun updateData(registratieGegevens: RegistratieGegevens){
        Log.d(TAGJE, "Data updaten in viewmodel!")
        lijst[TeUpdatenRegistratieGegevens] = registratieGegevens
        lijstRegistratieGegevens.postValue(lijst)

        val user = auth.currentUser
        if (user != null) {
            Log.d(TAGJE, "gebruiker ingelogd, ...")
            val db = FirebaseFirestore.getInstance()
            val db_useruid = user.uid.toString()

            val updatesdata = hashMapOf<String, Any>(
                "datum" to Timestamp(
                    registratieGegevens.registratiedatum.toEpochSecond(ZoneOffset.UTC),
                    0
                ),
                "meterwaardeEL" to registratieGegevens.meterwaarde_el,
                "meterwaardeGA" to registratieGegevens.meterwaarde_ga,
                "meterwaardeWA" to registratieGegevens.meterwaarde_wa,
                "meterwaardePV" to registratieGegevens.meterwaarde_pv
            )

            val docRef = db.collection("users").document(db_useruid).collection("meetgegevens")
                .document(registratieGegevens.firebaseid).update(updatesdata)
                .addOnSuccessListener { Log.d(TAGJE, "DocumentSnapshot successfully updatet!") }
                .addOnFailureListener { e -> Log.w(TAGJE, "Error updating document", e) }
        }
        else{
            Log.d(TAGJE, "Geen gebruiker ingelogd, niets geschreven!")
        }
        if (backupOpDitToestel){
            SaveDataLocally(bestand,registratieGegevens)
            Log.d(TAGJE, "Data local opgeslagen!")
        }
    }

    fun getTeUpdatenRegistratieGegevens(): RegistratieGegevens {
        return lijst[TeUpdatenRegistratieGegevens]
    }

    fun getLijstRegistratieGegevens(): LiveData<List<RegistratieGegevens>> {
        return lijstRegistratieGegevens
    }

    // login stuff
    fun isThereAUserLoggedIn(): Boolean{
        val user = auth.currentUser
        return user != null
    }

    fun loaddata(Allesdownloaden : Boolean){
        val user = auth.currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val db_useruid = user.uid.toString()
            lijst.clear()

            val docRef = db.collection("users").document(db_useruid).collection("meetgegevens")

            if (Allesdownloaden) {
                docRef.orderBy("datum", Query.Direction.DESCENDING).get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            Log.d(TAGJE, "${document.id} => ${document.data}")

                            val dat = document.get("datum") as Timestamp
                            val el = document.get("meterwaardeEL") as Double
                            val ga = document.get("meterwaardeGA") as Double
                            val wa = document.get("meterwaardeWA") as Double
                            val pv = document.get("meterwaardePV") as Double


                            val reggeg = RegistratieGegevens(
                                LocalDateTime.ofEpochSecond(dat.seconds, 0, ZoneOffset.UTC),
                                el,
                                ga,
                                wa,
                                pv,
                                0.0,
                                0.0,
                                0.0,
                                0.0,
                                document.id
                            )
                            Log.d(TAGJE, "reggegevens : ${reggeg}")
                            lijst.add(reggeg)
                        }
                        lijstRegistratieGegevens.postValue(lijst)
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAGJE, "Error getting documents: ", exception)
                    }
            }
            else{
                docRef.orderBy("datum", Query.Direction.DESCENDING).limit(31).get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            Log.d(TAGJE, "${document.id} => ${document.data}")

                            val dat = document.get("datum") as Timestamp
                            val el = document.get("meterwaardeEL") as Double
                            val ga = document.get("meterwaardeGA") as Double
                            val wa = document.get("meterwaardeWA") as Double
                            val pv = document.get("meterwaardePV") as Double


                            val reggeg = RegistratieGegevens(
                                LocalDateTime.ofEpochSecond(dat.seconds, 0, ZoneOffset.UTC),
                                el,
                                ga,
                                wa,
                                pv,
                                0.0,
                                0.0,
                                0.0,
                                0.0,
                                document.id
                            )
                            Log.d(TAGJE, "reggegevens : ${reggeg}")
                            lijst.add(reggeg)
                        }
                        lijstRegistratieGegevens.postValue(lijst)
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAGJE, "Error getting documents: ", exception)
                    }
            }

        }
    }

    fun cleardata(){
        lijst.clear()
        lijstRegistratieGegevens.postValue(lijst)

    }

    // export
    fun exportToFile(file : File) {
        //loaddata(true)
        // probleem met delay... volgende code moet even wachten
        var fileWriter: FileWriter? = null
        try {
            val bestand = file
            var aanvullen = false
            if (bestand.exists()) {
                aanvullen = true
                Log.i(TAGJE, "gegevens aanvullen in bestaand bestand")
            } else {
                Log.i(TAGJE, "nieuw bestand maken")
            }

            fileWriter = FileWriter(bestand, aanvullen)
            Log.i(TAGJE, bestand.absolutePath)
            // Log.i(TAGJE, bestand.length().toString())
            if (bestand.length() < 50) {
                val CSV_HEADER = "Meetmoment,Elektriciteit,Gas,Water,PV"
                fileWriter.append(CSV_HEADER)
                fileWriter.append('\n')
                Log.i(TAGJE, "header toegevoegd")
            }
            val lijstiterator = lijst.iterator()
            lijstiterator.forEach {
                val formatted = it.registratiedatum.format(DateTimeFormatter.ISO_DATE_TIME)
                fileWriter.append(formatted)
                fileWriter.append(',')
                fileWriter.append(it.meterwaarde_el.toString())
                fileWriter.append(',')
                fileWriter.append(it.meterwaarde_ga.toString())
                fileWriter.append(',')
                fileWriter.append(it.meterwaarde_wa.toString())
                fileWriter.append(',')
                fileWriter.append(it.meterwaarde_pv.toString())
                fileWriter.append('\n')
                Log.i(TAGJE, "gegevens aangevuld")
            }
        } catch (e: Exception) {
            Log.e(TAGJE, "Writing CSV error")
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: Exception) {
                Log.e(TAGJE, "file closing error")
            }
        }
    }

    // schrijf 1 waarde
    fun SaveDataLocally(file : File, registratieGegevens:RegistratieGegevens ) {
        var fileWriter: FileWriter? = null
        try {
            val bestand = file
            var aanvullen = false
            if (bestand.exists()) {
                aanvullen = true
                Log.i(TAGJE, "gegevens aanvullen in bestaand bestand")
            } else {
                Log.i(TAGJE, "nieuw bestand maken")
            }
            fileWriter = FileWriter(bestand, aanvullen)
            Log.i(TAGJE, bestand.absolutePath)
            // Log.i(TAGJE, bestand.length().toString())
            if (bestand.length() < 50) {
                val CSV_HEADER = "Meetmoment,Elektriciteit,Gas,Water,PV"
                fileWriter.append(CSV_HEADER)
                fileWriter.append('\n')
                Log.i(TAGJE, "header toegevoegd")
            }
            //val lijstiterator = lijst.iterator()
            //lijstiterator.forEach {
                val formatted = registratieGegevens.registratiedatum.format(DateTimeFormatter.ISO_DATE_TIME)
                fileWriter.append(formatted)
                fileWriter.append(',')
                fileWriter.append(registratieGegevens.meterwaarde_el.toString())
                fileWriter.append(',')
                fileWriter.append(registratieGegevens.meterwaarde_ga.toString())
                fileWriter.append(',')
                fileWriter.append(registratieGegevens.meterwaarde_wa.toString())
                fileWriter.append(',')
                fileWriter.append(registratieGegevens.meterwaarde_pv.toString())
                fileWriter.append('\n')
                Log.i(TAGJE, "gegevens aangevuld")
            //}
        } catch (e: Exception) {
            Log.e(TAGJE, "Writing CSV error")
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: Exception) {
                Log.e(TAGJE, "file closing error")
            }
        }
    }

    fun ImportDataFromFile(file: File){

        var line: String?
        //var fileReader: BufferedReader? = null

        var fileReader = file.bufferedReader()


        // Read CSV header
        fileReader.readLine()

        // Read the file line by line starting from the second line
        line = fileReader.readLine()
        while (line != null) {
            val tokens = line.split(",")
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            if (tokens.size > 0) {
                val regdata = RegistratieGegevens(
                    LocalDateTime.parse(tokens[0], formatter),
                    tokens[1].run { toDouble() },
                    tokens[2].run { toDouble() },
                    tokens[3].run { toDouble() },
                    tokens[4].run { toDouble() },
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "0")
                addData(regdata)
            }
            line = fileReader.readLine()
        }


    }
}

