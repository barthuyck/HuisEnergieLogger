package be.huyck.huisenergielogger.recycler

import android.R
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import kotlinx.android.synthetic.main.item_toon_data.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class RecyclerAdapter(mmonGegevensitemListener : OnGegevensitemListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            LayoutInflater.from(parent.context).inflate(be.huyck.huisenergielogger.R.layout.item_toon_data, parent, false),
            monGegevensitemListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is RijGegevensViewHolder -> {
                holder.bind(lijst.get(position))
            }
        }
        if (position == lijst.size - 3){
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
            "0"
        )
        if (blogList.size > 0) {
            while (listIterator.hasNext()) {
                Log.d(TAG,"iterator: ${listIterator}")

                huidige = listIterator.next()
                Log.d(TAG,"huidige: ${huidige}")
                Log.d(TAG,"vorige: ${vorige}")
                if (listIterator.hasPrevious()) {
                    var tmp =
                        vorige.meterwaarde_el.toBigDecimal() - huidige.meterwaarde_el.toBigDecimal()
                    vorige.verschil_el = tmp.setScale(1).toDouble()
                    tmp =
                        vorige.meterwaarde_ga.toBigDecimal() - huidige.meterwaarde_ga.toBigDecimal()
                    vorige.verschil_ga = tmp.setScale(3).toDouble()
                    tmp =
                        vorige.meterwaarde_wa.toBigDecimal() - huidige.meterwaarde_wa.toBigDecimal()
                    vorige.verschil_wa = tmp.setScale(4).toDouble()
                    tmp =
                        vorige.meterwaarde_pv.toBigDecimal() - huidige.meterwaarde_pv.toBigDecimal()
                    vorige.verschil_pv = tmp.setScale(1).toDouble()
                }
                else{
                    huidige.verschil_el = 0.1
                    huidige.verschil_ga = 0.1
                    huidige.verschil_wa = 0.1
                    huidige.verschil_pv = 0.1

                }
                vorige = huidige
            }
        }
        //lijst = lijst.reversed()

    }

    class RijGegevensViewHolder constructor( itemView: View, onGegevensitemListener : OnGegevensitemListener): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var mListener : OnGegevensitemListener
        private val rgdatum = itemView.tvdatum
        private val rgel = itemView.tvel
        private val rggas = itemView.tvgas
        private val rgwater = itemView.tvwater
        private val rgzon = itemView.tvpv
        private val rghuis = itemView.tvhuis
        private val formatter = DateTimeFormatter.ofPattern("EE dd/MM/yyyy HH:mm")
        private val weekdagformatter = DateTimeFormatter.ofPattern("e")

        init {
            this.mListener = onGegevensitemListener
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            mListener.onGegevensitemClick(view, adapterPosition)
        }

        fun bind (rijGegevens: RegistratieGegevens){
            rgdatum.setText(rijGegevens.registratiedatum.format(formatter))
            val dagvandeweek = rijGegevens.registratiedatum.format(weekdagformatter)

            if(dagvandeweek.toInt()==7) {
                rgdatum.setTextColor(Color.RED)
            }
            else
                rgdatum.setTextColor(Color.BLUE)
            val huisverbruik = rijGegevens.verschil_pv.toBigDecimal() + rijGegevens.verschil_el.toBigDecimal()

            val rgeltxt = rijGegevens.meterwaarde_el.toString() + " kWh \n(Δ: " + rijGegevens.verschil_el.toString() + " kWh)"
            rgel.setText(rgeltxt)
            val rggastxt = rijGegevens.meterwaarde_ga.toString() + " m³ \n(Δ: " +  rijGegevens.verschil_ga.toString() + " m³)"
            rggas.setText(rggastxt)
            val rgwatertxt = rijGegevens.meterwaarde_wa.toString() + " m³ \n(Δ: " + rijGegevens.verschil_wa.toString() + " m³)"
            rgwater.setText(rgwatertxt)
            val rgzontxt = rijGegevens.meterwaarde_pv.toString() + " kWh \n(Δ: " +  rijGegevens.verschil_pv.toString() + " kWh)"
            rgzon.setText(rgzontxt)

            val huisverbruiktxt = huisverbruik.setScale(1).toString() + " kWh"// + dagvandeweek.toString()
            rghuis.setText(huisverbruiktxt)
        }
    }

    public interface OnGegevensitemListener{
        fun onGegevensitemClick(v : View, position: Int)
    }


}

interface OnBottomReachedListener {
    // interface om aan te geven dat einde van de recycleviewer bereikt is
    fun onBottomReached(position: Int)
}
