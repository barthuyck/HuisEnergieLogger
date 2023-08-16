package be.huyck.huisenergielogger.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import be.huyck.huisenergielogger.R
import be.huyck.huisenergielogger.ViewModel.DataViewModel
import be.huyck.huisenergielogger.modellen.RegistratieGegevens
import be.huyck.huisenergielogger.recycler.OnBottomReachedListener
import be.huyck.huisenergielogger.recycler.RecyclerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton


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

        //initRecyclerView()

        var myLayoutManager = LinearLayoutManager(activity)
        var dataRecyclerV = view.findViewById(R.id.dataRecycler) as androidx.recyclerview.widget.RecyclerView
        dataRecyclerV.layoutManager = myLayoutManager

        gegevensadapter = RecyclerAdapter(this)
        dataRecyclerV.adapter = gegevensadapter

        gegevensadapter.setOnBottomReachedListener(object : OnBottomReachedListener {
            override fun onBottomReached(position: Int) {
                Toast.makeText(context, R.string.snacckbar_eindofrecycleviewer, Toast.LENGTH_SHORT).show()
                viewModel.loadnextdata()
            }
        })

        //viewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        this.viewModel = activity?.run {
            ViewModelProvider(this)[DataViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        // Use the ViewModel
        // Create the observer which updates the UI.

        viewModel.getLijstRegistratieGegevens().observe(this, Observer<List<RegistratieGegevens>> { geg ->
            // update UI
            //Log.d(TAGJE,"nieuwe gegevens geladen via observer")
            gegevensadapter.submitList(geg)
            gegevensadapter.notifyDataSetChanged()

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

    /*private fun initRecyclerView(){
        var myLayoutManager = LinearLayoutManager(activity)
        var dataRecyclerV = View.findViewById(R.id.dataRecycler)
        dataRecycler.layoutManager = myLayoutManager

        gegevensadapter = RecyclerAdapter(this)
        dataRecycler.adapter = gegevensadapter

        gegevensadapter.setOnBottomReachedListener(object : OnBottomReachedListener {
            override fun onBottomReached(position: Int) {
                Toast.makeText(context, R.string.snacckbar_eindofrecycleviewer, Toast.LENGTH_SHORT).show()
                viewModel.loadnextdata()
            }
        })

        /*dataRecycler.apply {
            layoutManager = LinearLayoutManager(activity)
            //val topSpacingDecorator = SpacingItemDecoration(10)
            //addItemDecoration(topSpacingDecorator)
            gegevensadapter = RecyclerAdapter(this)
            adapter = gegevensadapter
        }*/

        /*var onScrollListener = RecyclerOnscrollListener(myLayoutManager)
        //dataRecycler.addOnItemTouchListener()
        dataRecycler.addOnScrollListener(onScrollListener)
        Log.d("onscrollistener","op het einde toegevogd")*/

    }*/

    override fun onGegevensitemClick(v : View, position: Int){
        //viewModel.lijstRegistratieGegevens.value[position]
        //Toast.makeText(context, "geklikt", Toast.LENGTH_SHORT).show()
        viewModel.TeUpdatenRegistratieGegevens = position
        val navController = v.findNavController()
        navController.navigate(R.id.action_toonDataFragment_to_updateDataFragment)
    }


}

