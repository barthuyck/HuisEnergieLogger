package be.huyck.huisenergielogger.recycler

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import be.huyck.huisenergielogger.R
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
//import kotlinx.android.synthetic.main.item_toon_data.view.*
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.math.RoundingMode

class RecyclerAdapter(mmonGegevensitemListener: OnGegevensitemListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var lijst : List<RegistratieGegevens> = ArrayList()
    val TAG = "huisenergielogger.recycleradapter"
    private var monGegevensitemListener : OnGegevensitemListener
    lateinit var myOnBottomReachedListener: OnBottomReachedListener // einde van de recyclervieuwer

    init {
        this.monGegevensitemListener = mmonGegevensitemListener
    }

    fun setOnBottomReachedListener(onBottomReachedListener: OnBottomReachedListener) {
        this.myOnBottomReachedListener = onBottomReachedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RijGegevensViewHolder(
            LayoutInflater.from(parent.context).inflate(
                be.huyck.huisenergielogger.R.layout.item_toon_data,
                parent,
                false
            ),
            monGegevensitemListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is RijGegevensViewHolder -> {
                holder.bind(lijst.get(position))
            }
        }
        if (position == lijst.size - 1){
            myOnBottomReachedListener.onBottomReached(position);
        }
    }

    override fun getItemCount(): Int {
        return lijst.size
    }

    public fun submitList(blogList: List<RegistratieGegevens>) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        lijst = blogList
        val listIterator = lijst.listIterator()
        var huidige: RegistratieGegevens
        var vorige: RegistratieGegevens
        vorige = RegistratieGegevens(
            LocalDateTime.parse("19/08/2019 22:01", formatter),
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            "0"
        )
        if (blogList.size > 0) {
            while (listIterator.hasNext()) {
                Log.d(TAG, "iterator: ${listIterator}")

                huidige = listIterator.next()
                Log.d(TAG, "huidige: ${huidige}")
                Log.d(TAG, "vorige: ${vorige}")
                if (listIterator.hasPrevious()) {
                    var ruwewaarde = vorige.meterwaardeElekImport - huidige.meterwaardeElekImport
                    vorige.verschilElekImport = ruwewaarde.toBigDecimal().setScale(1,RoundingMode.HALF_EVEN).toDouble()
                    ruwewaarde = vorige.meterwaardeElekExport - huidige.meterwaardeElekExport
                    vorige.verschilElekExport = ruwewaarde.toBigDecimal().setScale(1,RoundingMode.HALF_EVEN).toDouble()
                    var tmp1 = vorige.meterwaardeGas - huidige.meterwaardeGas
                    //Log.d(TAG, "vorige: ${tmp1}")
                    vorige.verschilGas = tmp1 //df.format(tmp1)
                    ruwewaarde = vorige.meterwaardeWater - huidige.meterwaardeWater
                    vorige.verschilWater = ruwewaarde.toBigDecimal().setScale(4,RoundingMode.HALF_EVEN).toDouble()
                    ruwewaarde = vorige.meterwaardePv1 - huidige.meterwaardePv1
                    vorige.verschilPv1 = ruwewaarde.toBigDecimal().setScale(1,RoundingMode.HALF_EVEN).toDouble()
                    ruwewaarde = vorige.meterwaardePv2 - huidige.meterwaardePv2
                    vorige.verschilPv2 = ruwewaarde.toBigDecimal().setScale(1,RoundingMode.HALF_EVEN).toDouble()
                }
                else{
                    huidige.verschilElekImport = 0.0
                    huidige.verschilElekExport = 0.0
                    huidige.verschilGas = 0.0
                    huidige.verschilWater = 0.0
                    huidige.verschilPv1 = 0.0
                    huidige.verschilPv1 = 0.0

                }
                vorige = huidige
            }
        }
        //lijst = lijst.reversed()

    }

    class RijGegevensViewHolder constructor(
        itemView: View,
        onGegevensitemListener: OnGegevensitemListener
    ): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var mListener : OnGegevensitemListener
        private val rgdatum = itemView.findViewById<TextView>(R.id.tvdatum)
        private val rgel = itemView.findViewById<TextView>(R.id.tvel)
        private val rggas = itemView.findViewById<TextView>(R.id.tvgas)
        private val rgwater = itemView.findViewById<TextView>(R.id.tvwater)
        private val rgzon = itemView.findViewById<TextView>(R.id.tvpv)
        private val rghuis = itemView.findViewById<TextView>(R.id.tvhuis)

        private val formatter = DateTimeFormatter.ofPattern("EE dd/MM/yyyy HH:mm")
        private val weekdagformatter = DateTimeFormatter.ofPattern("e")

        init {
            this.mListener = onGegevensitemListener
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            mListener.onGegevensitemClick(view, adapterPosition)
        }

        fun bind(rijGegevens: RegistratieGegevens){
            rgdatum.setText(rijGegevens.registratiedatum.format(formatter))
            val dagvandeweek = rijGegevens.registratiedatum.format(weekdagformatter)

            if(dagvandeweek.toInt()==7) {
                rgdatum.setTextColor(Color.RED)
            }
            else
                rgdatum.setTextColor(Color.BLUE)
            val huisverbruik = rijGegevens.verschilPv1.toBigDecimal() +
                    rijGegevens.verschilPv2.toBigDecimal() +
                    rijGegevens.verschilElekImport.toBigDecimal() -
                    rijGegevens.verschilElekExport.toBigDecimal()

            val df_ga = DecimalFormat("###.###")
            val df_el = DecimalFormat("###.###")
            val df_wa = DecimalFormat("###.####")
            val df_pv = DecimalFormat("###.#")

            val rgeltxt = "Som: " + df_el.format(rijGegevens.meterwaardeElekImport -
                    rijGegevens.meterwaardeElekExport) + " kWh\n" +
                    "Import: " + df_el.format(rijGegevens.meterwaardeElekImport) + " kWh\n(Δ: " +
                    df_el.format(rijGegevens.verschilElekImport) + " kWh)" +
                    "\nExport: " + df_el.format(rijGegevens.meterwaardeElekExport) + " kWh\n(Δ: " +
                    df_el.format(rijGegevens.verschilElekExport) + " kWh)"
            rgel.setText(rgeltxt)

            val rggastxt = df_ga.format(rijGegevens.meterwaardeGas) + " m³\n(Δ: " +
                    df_ga.format(rijGegevens.verschilGas) + " m³)"
            rggas.setText(rggastxt)

            val rgwatertxt = df_wa.format(rijGegevens.meterwaardeWater) + " m³\n(Δ: " +
                    df_wa.format(rijGegevens.verschilWater) + " m³)"
            rgwater.setText(rgwatertxt)

            val rgzontxt = "Huis: " + df_pv.format(rijGegevens.meterwaardePv1) +  " kWh\n(Δ: " +
                    df_pv.format(rijGegevens.verschilPv1) + " kWh) \n" +
                    "Garage: " + df_pv.format(rijGegevens.meterwaardePv2) + " kWh\n(Δ: " +
                    df_pv.format(rijGegevens.verschilPv2) + " kWh)"
            rgzon.setText(rgzontxt)

            val huisverbruiktxt = df_el.format(huisverbruik) + " kWh"// + dagvandeweek.toString()
            rghuis.setText(huisverbruiktxt)
        }
    }

    public interface OnGegevensitemListener{
        fun onGegevensitemClick(v: View, position: Int)
    }


}

interface OnBottomReachedListener {
    // interface om aan te geven dat einde van de recycleviewer bereikt is
    fun onBottomReached(position: Int)
}
