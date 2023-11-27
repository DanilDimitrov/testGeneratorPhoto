package com.example.testgeneratorphoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.appcompat.widget.AppCompatButton

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class second_splash_screen : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var videoView: VideoView
    private lateinit var continueButton: AppCompatButton

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
        val view = inflater.inflate(R.layout.fragment_second_splash_screen, container, false)
        videoView = view.findViewById(R.id.videoView2)
        continueButton = view.findViewById(R.id.generate_select2)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.second_preview

        videoView.setVideoPath(videoPath)

        videoView.setOnCompletionListener {
            videoView.start()
        }
        videoView.start()

        continueButton.setOnClickListener {
            val newFragment = thirdSplashScreen()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, newFragment)

            transaction.commit()
        }


    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            second_splash_screen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}