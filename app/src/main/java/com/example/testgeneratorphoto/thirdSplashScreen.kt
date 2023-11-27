package com.example.testgeneratorphoto

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.appcompat.widget.AppCompatButton

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
interface OnDataPass {
    fun onDataPassed(data: String)
}
class thirdSplashScreen : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var videoView: VideoView
    private lateinit var continueButton: AppCompatButton
    private var dataPassListener: OnDataPass? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDataPass) {
            dataPassListener = context
        } else {
            throw RuntimeException("$context must implement OnDataPass")
        }
    }
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
        val view = inflater.inflate(R.layout.fragment_third_splash_screen, container, false)
        videoView = view.findViewById(R.id.videoView2)
        continueButton = view.findViewById(R.id.generate_select2)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            thirdSplashScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.third_preview

        videoView.setVideoPath(videoPath)

        videoView.setOnCompletionListener {
            videoView.start()
        }
        videoView.start()

        continueButton.setOnClickListener {

            // Получаем доступ к SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("tutorial_pref", Context.MODE_PRIVATE)

            // Записываем в SharedPreferences информацию о просмотре обучения
            val editor = sharedPreferences.edit()
            editor.putBoolean("hasWatchedTutorial", true)
            editor.apply()
            dataPassListener?.onDataPassed("true")
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.remove(this)
            transaction.commit()
        }
    }

}

