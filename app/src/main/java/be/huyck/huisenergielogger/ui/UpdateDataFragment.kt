package be.huyck.huisenergielogger.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import be.huyck.huisenergielogger.R
import be.huyck.huisenergielogger.ViewModel.DataViewModel
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId


class UpdateDataFragment : Fragment() {
    // TODO: Rename and change types of parameters
    lateinit var viewModel : DataViewModel
    val TAGJE = "huisenergielogger.UpdateDataFragment"
    lateinit var nu : LocalDateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        this.viewModel = activity?.run {
            ViewModelProvider(this)[DataViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        nu = LocalDateTime.now()
        val TeUpdaten = viewModel.getTeUpdatenRegistratieGegevens()
        //Log.d(TAGJE,"te updaten: ${TeUpdaten}")

        val EdtELV = view.findViewById<TextView>(R.id.EdtEL)
        EdtELV.setText(TeUpdaten.meterwaardeElekImport.toString())
        val EdtEL2V = view.findViewById<TextView>(R.id.EdtELEx)
        EdtEL2V.setText(TeUpdaten.meterwaardeElekExport.toString())
        val EdtGasV = view.findViewById<TextView>(R.id.EdtGas)
        EdtGasV.setText(TeUpdaten.meterwaardeGas.toString())
        val EdtWatV = view.findViewById<TextView>(R.id.EdtWat)
        EdtWatV.setText(TeUpdaten.meterwaardeWater.toString())
        val EdtPVV = view.findViewById<TextView>(R.id.EdtPV)
        EdtPVV.setText(TeUpdaten.meterwaardePv1.toString())
        val EdtPV2V = view.findViewById<TextView>(R.id.EdtPV2)
        EdtPV2V.setText(TeUpdaten.meterwaardePv2.toString())
        nu = TeUpdaten.registratiedatum
        val zoneId = ZoneId.of("Europe/Paris")
        nu.atZone(zoneId)
        UpdateDatumEnTijd()

        val buttonUpdate: Button = view.findViewById(R.id.buttonupdate)
        buttonUpdate.setOnClickListener(View.OnClickListener {
            val el = with(EdtELV) { text.toString().toDouble() }
            val el2 = with(EdtEL2V) { text.toString().toDouble() }
            val ga = with(EdtGasV) { text.toString().toDouble() }
            val wa = with(EdtWatV) { text.toString().toDouble() }
            val pv = with(EdtPVV) { text.toString().toDouble() }
            val pv2 = with(EdtPV2V) { text.toString().toDouble() }

            //Log.d(TAGJE,"el: $el")
            //Log.d(TAGJE,"ga: $ga")
            //Log.d(TAGJE,"wa: $wa")
            //Log.d(TAGJE,"pv: $pv")

            val ingevoerdeGegevens = RegistratieGegevens(
                nu,
                el,
                el2,
                ga,
                wa,
                pv,
                pv2,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                TeUpdaten.firebaseid)
            //Log.d(TAGJE,"gegevens die worden geupdate:")
            //Log.d(TAGJE,ingevoerdeGegevens.toString())
            viewModel.updateData(ingevoerdeGegevens)
            val navController = it.findNavController()
            navController.navigate(R.id.toonDataFragment)
            Snackbar.make(view, getString(R.string.snackbar_update), Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        })

        class MyUndoListener : View.OnClickListener {
            override fun onClick(v: View) {
                // Code to undo the user's last action.
                viewModel.addData(TeUpdaten)
                Snackbar.make(view, getString(R.string.snackbar_undo_delete), Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
            }
        }

        val buttonDelete: Button = view.findViewById(R.id.buttondelete)
        buttonDelete.setOnClickListener(View.OnClickListener {
            //Log.d(TAGJE,"gegevens die worden gedelete:")
            //Log.d(TAGJE,TeUpdaten.toString())
            viewModel.deleteData(TeUpdaten)
            val mySnackbar = Snackbar.make(view, getString(R.string.snackbar_delete), Snackbar.LENGTH_LONG)
            mySnackbar.setAction(R.string.snackbar_undo, MyUndoListener())
            mySnackbar.show()
            val navController = it.findNavController()
            navController.navigate(R.id.toonDataFragment)
        })


        val buttonCancel: Button = view.findViewById(R.id.buttoncancel)
        buttonCancel.setOnClickListener(View.OnClickListener {
            val navController = it.findNavController()
            navController.navigate(R.id.toonDataFragment)
        })

        val buttonDate: TextView = view.findViewById(R.id.RegistratieDatum)
        buttonDate.setOnClickListener(View.OnClickListener {
            val dag = nu.dayOfMonth
            val maand = nu.monthValue-1
            val jaar = nu.year

            val dpd = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                var nieuwedag : LocalDateTime = LocalDateTime.of(year.toInt(),monthOfYear.toInt()+1,dayOfMonth.toInt(),nu.hour.toInt(),nu.minute.toInt())
                if (nieuwedag > LocalDateTime.now()) {
                    nieuwedag = LocalDateTime.now()
                }
                nu = nieuwedag
                UpdateDatumEnTijd()
            }, jaar, maand, dag)
            dpd.show()
        })

        val buttonTime: TextView = view.findViewById(R.id.RegistratieTijd)
        buttonTime.setOnClickListener(View.OnClickListener {
            val minuut = nu.minute
            val uur = nu.hour
            val dpd = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                var nieuwedag : LocalDateTime = LocalDateTime.of(nu.year.toInt(),nu.monthValue.toInt(),nu.dayOfMonth.toInt(),hourOfDay,minute)
                if (nieuwedag > LocalDateTime.now()) {
                    nieuwedag = LocalDateTime.now()
                }
                nu = nieuwedag
                UpdateDatumEnTijd()

            }, uur, minuut,true)
            dpd.show()
        })

    }

    fun UpdateDatumEnTijd(){
        val formatterdatum = DateTimeFormatter.ofPattern("d/M/Y")
        val formattertijd = DateTimeFormatter.ofPattern("H:mm")
        val RegistratieDatumV = view?.findViewById<TextView>(R.id.RegistratieDatum)
        val RegistratieTijdV = view?.findViewById<TextView>(R.id.RegistratieTijd)
        RegistratieDatumV?.text = nu.format(formatterdatum)
        RegistratieTijdV?.text = nu.format(formattertijd)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }


}
