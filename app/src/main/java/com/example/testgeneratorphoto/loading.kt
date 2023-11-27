package com.example.testgeneratorphoto

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.VideoView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

interface LoadingFragmentListener {
    fun onLoadingFragmentFinished()
}
class loading : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var progressBar2: ProgressBar
    private var loadingFragmentListener: LoadingFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loading, container, false)
        progressBar2 = view.findViewById(R.id.progressBar2)
        return view
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            loading().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val handler = Handler()
        var progress = 0

        val runnable = object : Runnable {
            override fun run() {
                if (progress <= 100) {
                    progress++
                    progressBar2.setProgress(progress, true)
                    handler.postDelayed(this, 30) //

                } else {
                    val sharedPreferences = requireContext().getSharedPreferences("tutorial_pref", Context.MODE_PRIVATE)
                    val hasWatchedTutorial = sharedPreferences.getBoolean("hasWatchedTutorial", false)

                    if(!hasWatchedTutorial){
                        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.container)
                        if (currentFragment != null) {
                            val transaction = requireActivity().supportFragmentManager.beginTransaction()
                            transaction.remove(currentFragment)
                            transaction.commit()
                        }

                        val newFragment = FirstSplashScreen()
                        val newTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        newTransaction.replace(R.id.container, newFragment)
                        newTransaction.addToBackStack(null)
                        newTransaction.commit()
                    }
                    else{
                        loadingFragmentListener?.onLoadingFragmentFinished()
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.remove(this@loading)
                        transaction.commit()
                    }
                }
            }
        }

        handler.post(runnable)
    }
    fun setOnLoadingFragmentListener(listener: LoadingFragmentListener) {
        loadingFragmentListener = listener
    }
}