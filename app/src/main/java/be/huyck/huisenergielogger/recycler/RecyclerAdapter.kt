package be.huyck.huisenergielogger.recycler

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import kotlinx.android.synthetic.main.item_toon_data.view.*
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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
            "0"
        )
        if (blogList.size > 0) {
            while (listIterator.hasNext()) {
                Log.d(TAG, "iterator: ${listIterator}")

                huidige = listIterator.next()
                Log.d(TAG, "huidige: ${huidige}")
                Log.d(TAG, "vorige: ${vorige}")
                if (listIterator.hasPrevious()) {
                    var tmp =
                        vorige.meterwaarde_el.toBigDecimal() - huidige.meterwaarde_el.toBigDecimal()
                    vorige.verschil_el = tmp.setScale(1).toDouble()
                    var tmp1 = vorige.meterwaarde_ga - huidige.meterwaarde_ga
                    Log.d(TAG, "vorige: ${tmp1}")

                    vorige.verschil_ga = tmp1 //df.format(tmp1)
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

    class RijGegevensViewHolder constructor(
        itemView: View,
        onGegevensitemListener: OnGegevensitemListener
    ): RecyclerView.ViewHolder(itemView), View.OnClickListener {

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

        fun bind(rijGegevens: RegistratieGegevens){
            rgdatum.setText(rijGegevens.registratiedatum.format(formatter))
            val dagvandeweek = rijGegevens.registratiedatum.format(weekdagformatter)

            if(dagvandeweek.toInt()==7) {
                rgdatum.setTextColor(Color.RED)
            }
            else
                rgdatum.setTextColor(Color.BLUE)
            val huisverbruik = rijGegevens.verschil_pv.toBigDecimal() + rijGegevens.verschil_el.toBigDecimal()

            val df_ga = DecimalFormat("###.###")

            val df_el = DecimalFormat("###.###")
            val df_wa = DecimalFormat("###.###")
            val df_pv = DecimalFormat("###.#")
            val rgeltxt = df_el.format(rijGegevens.meterwaarde_el) + " kWh \n(Δ: " + df_el.format(rijGegevens.verschil_el) + " kWh)"
            rgel.setText(rgeltxt)
            val rggastxt = df_ga.format(rijGegevens.meterwaarde_ga) + " m³ \n(Δ: " +  df_ga.format(rijGegevens.verschil_ga) + " m³)"
            rggas.setText(rggastxt)

            val rgwatertxt = df_wa.format(rijGegevens.meterwaarde_wa) + " m³ \n(Δ: " + df_wa.format(rijGegevens.verschil_wa) + " m³)"
            rgwater.setText(rgwatertxt)
            val rgzontxt = df_pv.format(rijGegevens.meterwaarde_pv) + " kWh \n(Δ: " +  df_pv.format(rijGegevens.verschil_pv) + " kWh)"
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
