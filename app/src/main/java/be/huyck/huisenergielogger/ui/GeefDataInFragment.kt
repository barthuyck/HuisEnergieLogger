package be.huyck.huisenergielogger.ui


import android.app.DatePickerDialog
import android.app.TimePickerDialog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController

import be.huyck.huisenergielogger.R
//import kotlinx.android.synthetic.main.fragment_geef_data_in.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import be.huyck.huisenergielogger.ViewModel.DataViewModel
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import com.google.android.material.snackbar.Snackbar
//import kotlinx.android.synthetic.main.item_toon_data.*

/**
 * A simple [Fragment] subclass.
 */
class GeefDataInFragment : Fragment() {
    lateinit var viewModel : DataViewModel
    val TAGJE = "huisenergielogger.GeefDataInFragment"

    lateinit var nu : LocalDateTime

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_geef_data_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        this.viewModel = activity?.run {
            ViewModelProvider(this)[DataViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        nu = LocalDateTime.now()
        UpdateDatumEnTijd()

        val buttonSave: Button = view.findViewById(R.id.buttonsave)
        buttonSave.setOnClickListener(View.OnClickListener {
            val el = with(view.findViewById<TextView>(R.id.EdtEL)) { text.toString() }
            Log.d(TAGJE,"el: $el")
            val ga = with(view.findViewById<TextView>(R.id.EdtGas)) { text.toString().toDouble() }
            val wa = with(view.findViewById<TextView>(R.id.EdtWat)) { text.toString().toDouble() }
            val pv = with(view.findViewById<TextView>(R.id.EdtPV)) { text.toString().toDouble() }



            Log.d(TAGJE,"ga: $ga")
            Log.d(TAGJE,"wa: $wa")
            Log.d(TAGJE,"pv: $pv")

            val ingevoerdeGegevens = RegistratieGegevens(
                nu,
                el.toDouble(),
                ga,
                wa,
                pv,
                0.0,
                0.0,
                0.0,
                0.0,
                "0")
            Log.d(TAGJE,"gegevens die worden ingevoerd:")
            Log.d(TAGJE,ingevoerdeGegevens.toString())
            viewModel.addData(ingevoerdeGegevens)
            Snackbar.make(view, getString(R.string.snackbar_toegevoegd), Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
            val navController = it.findNavController()
            navController.navigate(R.id.toonDataFragment)
        })

        val buttonCancel: Button = view.findViewById(R.id.buttoncancel)
        buttonCancel.setOnClickListener(View.OnClickListener {
            val navController = it.findNavController()
            navController.navigate(R.id.toonDataFragment)
        })

        val buttonPopulate: Button = view.findViewById(R.id.buttonpopulate)
        buttonPopulate.setOnClickListener(View.OnClickListener {
            val ingevoerdeGegevens = RegistratieGegevens(
                nu,
                1.0,
                2.0,
                3.0,
                4.0,
                0.0,
                0.0,
                0.0,
                0.0,
                "0")
            Log.d(TAGJE,"gegevens die worden ingevoerd:")
            Log.d(TAGJE,ingevoerdeGegevens.toString())
            viewModel.addData(ingevoerdeGegevens)
            Snackbar.make(view, getString(R.string.snackbar_toegevoegd), Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
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




}
