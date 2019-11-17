package be.huyck.huisenergielogger.ui


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager

import be.huyck.huisenergielogger.R
import be.huyck.huisenergielogger.ViewModel.DataViewModel
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import be.huyck.huisenergielogger.recycler.RecyclerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_toon_data.*


/**
 * A simple [Fragment] subclass.
 */

class ToonDataFragment : Fragment(), RecyclerAdapter.OnGegevensitemListener {
    private lateinit var gegevensadapter : RecyclerAdapter
    lateinit var viewModel : DataViewModel
    val TAGJE = "huisenergielogger.ToonDataFragment"

    companion object {
        fun newInstance() = ToonDataFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_toon_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        //viewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        this.viewModel = activity?.run {
            ViewModelProviders.of(this)[DataViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        // Use the ViewModel
        // Create the observer which updates the UI.

        viewModel.getLijstRegistratieGegevens().observe(this, Observer<List<RegistratieGegevens>> { geg ->
            // update UI
            Log.d(TAGJE,"nieuwe gegevens geladen via observer")
            if (geg !=null){
                gegevensadapter.submitList(geg)
                gegevensadapter.notifyDataSetChanged()
            }

        })

        val fab = view.findViewById(R.id.floatingActionButton) as FloatingActionButton
        fab.setOnClickListener { vview ->
            /*Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()*/
            val navController = vview.findNavController()
            navController.navigate(R.id.action_toonDataFragment_to_geefDataInFragment)
        }


        /*val button = view.findViewById(R.id.btn_geefdatain) as Button
        button.setOnClickListener(View.OnClickListener {
            val navController = it.findNavController()
            navController.navigate(R.id.action_toonDataFragment_to_geefDataInFragment)
        })*/

    }

    private fun initRecyclerView(){

        dataRecycler.layoutManager = LinearLayoutManager(activity)
        gegevensadapter = RecyclerAdapter(this)
        dataRecycler.adapter = gegevensadapter
        /*dataRecycler.apply {
            layoutManager = LinearLayoutManager(activity)
            //val topSpacingDecorator = SpacingItemDecoration(10)
            //addItemDecoration(topSpacingDecorator)
            gegevensadapter = RecyclerAdapter(this)
            adapter = gegevensadapter
        }*/
    }

    override fun onGegevensitemClick(v : View, position: Int){
        //viewModel.lijstRegistratieGegevens.value[position]
        //Toast.makeText(context, "geklikt", Toast.LENGTH_SHORT).show()
        viewModel.TeUpdatenRegistratieGegevens = position
        val navController = v.findNavController()
        navController.navigate(R.id.action_toonDataFragment_to_updateDataFragment)
    }

}
