package be.huyck.huisenergielogger.recycler

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import be.huyck.huisenergielogger.R
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import kotlinx.android.synthetic.main.item_toon_data.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter








class RecyclerAdapter(mmonGegevensitemListener : OnGegevensitemListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var lijst : List<RegistratieGegevens> = ArrayList()
    val TAG = "huisenergielogger.recycleradapter"
    private var monGegevensitemListener : OnGegevensitemListener

    init {
        this.monGegevensitemListener = mmonGegevensitemListener
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
        /*holder.itemView.setOnClickListener {
            Log.d(TAG, "onClick: clicked on: " + lijst.get(position))
            //Toast.makeText(mContext, mNames.get(position), Toast.LENGTH_SHORT).show()
        };*/
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

    class RijGegevensViewHolder constructor( itemView: View, onGegevensitemListener : OnGegevensitemListener ): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var mListener : OnGegevensitemListener
        private val rgdatum = itemView.tvdatum
        private val rgel = itemView.tvel
        private val rggas = itemView.tvgas
        private val rgwater = itemView.tvwater
        private val rgzon = itemView.tvpv
        private val rghuis = itemView.tvhuis
        private val formatter = DateTimeFormatter.ofPattern("E dd/MM/yyyy HH:mm")

        init {
            this.mListener = onGegevensitemListener
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            mListener.onGegevensitemClick(view, adapterPosition)
        }

        fun bind (rijGegevens: RegistratieGegevens){
            rgdatum.setText(rijGegevens.registratiedatum.format(formatter))
            val huisverbruik = rijGegevens.verschil_pv.toBigDecimal() + rijGegevens.verschil_el.toBigDecimal()
            rgel.setText(rijGegevens.meterwaarde_el.toString() + " kWh \n (" + rijGegevens.verschil_el.toString() + " kWh)")
            rggas.setText(rijGegevens.meterwaarde_ga.toString() + " m³ \n(" +  rijGegevens.verschil_ga.toString() + " m³)")
            rgwater.setText(rijGegevens.meterwaarde_wa.toString() + " m³ \n(" + rijGegevens.verschil_wa.toString() + " m³)")
            rgzon.setText(rijGegevens.meterwaarde_pv.toString() + " kWh \n(" +  rijGegevens.verschil_pv.toString() + " kWh)")
            rghuis.setText(huisverbruik.setScale(1).toString() + " kWh")
        }
    }

    public interface OnGegevensitemListener{
        fun onGegevensitemClick(v : View, position: Int)
    }


}