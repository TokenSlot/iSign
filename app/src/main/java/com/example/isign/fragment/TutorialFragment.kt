package com.example.isign.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.isign.GestureRecognizerHelper
import com.example.isign.MainViewModel
import com.example.isign.R
import com.example.isign.TutorialManager
import com.example.isign.TutorialManagerFactory
import com.example.isign.databinding.FragmentTutorialBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TutorialFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    companion object {
        private const val TAG = "Tutorial gesture recognizer"
    }

    private var _fragmentTutorialBinding: FragmentTutorialBinding? = null

    private val fragmentTutorialBinding
        get() = _fragmentTutorialBinding!!

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var defaultNumResults = 1
    private val gestureRecognizerResultsAdapter: GestureRecognizerResultsAdapter by lazy {
        GestureRecognizerResultsAdapter().apply {
            updateAdapterSize(defaultNumResults)
            setGameMode(viewModel.isGameMode)
        }
    }
    private val tutorialManagerFactory by lazy {
        TutorialManagerFactory(
            context = requireContext()
        )
    }
    private val tutorialManager: TutorialManager by activityViewModels {
        tutorialManagerFactory
    }

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT
    private var letterToCheck: String? = null

    private val db = FirebaseFirestore.getInstance()

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()

        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragmentContainerView
            ).navigate(R.id.action_tutorialFragment_to_tutPermFragment)
        }

        tutorialManager.stopTutorial()

        backgroundExecutor.execute {
            if (gestureRecognizerHelper.isClosed()) {
                gestureRecognizerHelper.setupGestureRecognizer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        tutorialManager.stopTutorial()
        if (this::gestureRecognizerHelper.isInitialized) {
            viewModel.setMinHandDetectionConfidence(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)

            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }
    }

    override fun onDestroyView() {
        _fragmentTutorialBinding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        tutorialManager.stopTutorial()
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentTutorialBinding = FragmentTutorialBinding.inflate(inflater, container, false)

        return fragmentTutorialBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoTitle = viewModel.dataText


        val videoView = fragmentTutorialBinding.videoView
        videoView.setMediaController(MediaController(requireContext()))

        db.collection("tutorialVideos")
            .whereEqualTo("videoTitle", videoTitle)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val docSnapshot = querySnapshot.documents[0]
                    val videoURL = docSnapshot.getString("videoURL") ?: ""
                    Log.d(TAG, videoTitle)
                    videoView.setVideoURI(Uri.parse(videoURL))
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
            }



        tutorialManager.startTutorial()

        tutorialManager.gameState.observe(viewLifecycleOwner) {
                gameState ->
            when (gameState!!) {
                TutorialManager.TutorialState.IDLE -> {
                    videoView.stopPlayback()
                }
                TutorialManager.TutorialState.IN_PROGRESS -> {
                    videoView.start()
                }
            }
        }

        val icon = fragmentTutorialBinding.indicator
        val red = ContextCompat.getColor(this.requireContext(), R.color.mp_color_error)
        val green = ContextCompat.getColor(this.requireContext(), R.color.primary_color)

        tutorialManager.isCorrect.observe(viewLifecycleOwner) {
                isCorrect ->
            if (isCorrect) {
                icon.setImageResource(R.drawable.ic_correct)
                icon.setColorFilter(green, android.graphics.PorterDuff.Mode.SRC_IN)
            } else {
                icon.setImageResource(R.drawable.ic_wrong)
                icon.setColorFilter(red, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }

        backgroundExecutor = Executors.newSingleThreadExecutor()

        fragmentTutorialBinding.viewFinder.post {
            setUpCamera()
        }

        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                gestureRecognizerListener = this
            )
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        letterToCheck = viewModel.dataText.last().toString()
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentTutorialBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentTutorialBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        recognizeHand(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentTutorialBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureRecognizerHelper.recognizeLiveStream(
            imageProxy = imageProxy,
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentTutorialBinding.viewFinder.display.rotation
    }

    override fun onResults(
        resultBundle: GestureRecognizerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentTutorialBinding != null) {
                // Show result of recognized gesture
                val gestureCategories = resultBundle.results.first().gestures()
                if (gestureCategories.isNotEmpty()) {

                    gestureRecognizerResultsAdapter.updateResults(
                        gestureCategories.first()
                    )


                } else {
                    gestureRecognizerResultsAdapter.updateResults(emptyList())
                }


                letterToCheck?.let {
                    tutorialManager.handleUserInput(gestureRecognizerResultsAdapter.currentLetter,
                        it, this)
                }

                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentTutorialBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    cameraFacing,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentTutorialBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {

            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureRecognizerResultsAdapter.updateResults(emptyList())
        }
    }
}