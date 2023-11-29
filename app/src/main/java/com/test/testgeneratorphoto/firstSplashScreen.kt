package com.test.testgeneratorphoto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.appcompat.widget.AppCompatButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class FirstSplashScreen : Fragment() {

    private lateinit var videoView: VideoView
    private lateinit var continueButton: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_splash_screen, container, false)
        videoView = view.findViewById(R.id.videoView2)
        continueButton = view.findViewById(R.id.generate_select2)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.preview_splash

        videoView.setVideoPath(videoPath)

        videoView.setOnCompletionListener {
            videoView.start()
        }
        videoView.start()

        continueButton.setOnClickListener {
            val newFragment = second_splash_screen()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, newFragment)
            transaction.commit()
        }


    }

}