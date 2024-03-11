package com.graduation.vitlog_android.presentation.edit

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.graduation.vitlog_android.databinding.FragmentEditBinding
import com.graduation.vitlog_android.presentation.MainActivity
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException


@AndroidEntryPoint
class EditFragment : Fragment(), TextureView.SurfaceTextureListener,
    MediaPlayer.OnPreparedListener {


    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private var getUri: Uri? = null
    private val editViewModel by viewModels<EditViewModel>()
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var frameSeekBar: SeekBar

    private var isBlurModeSelected: Boolean = false
    private var isSubtitleModeSelected: Boolean = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(layoutInflater, container, false)
        frameSeekBar = binding.editTimelineSv

        getUri = arguments?.getString("videoUri")?.toUri()

        activity?.runOnUiThread {
            binding.editProgressbar.visibility = View.INVISIBLE
        }
        mediaPlayer = MediaPlayer()
        binding.video.surfaceTextureListener = this
        binding.backBtn.setOnClickListener {
            mediaPlayer.release()
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }
        binding.editBlurBtn.setOnClickListener {
            isBlurModeSelected = true
        }
        binding.editSubtitlesBtn.setOnClickListener {
            isSubtitleModeSelected = true
        }
        binding.editSaveBtn.setOnClickListener {
            editViewModel.getPresignedUrl()
        }
        getUri?.let {
            setupMediaRetrieverAndSeekBar(it)
            setPutVideoToPresignedUrlStateObserver()
            setGetPresignedUrlStateObserver()
            setGetMosaicedVideoStateObserver()
            setGetSubtitleStateObserver()
        }

        buttonActions()

        return binding.root
    }

    private fun buttonActions() {
        // 수동 블러 버튼 클릭
        binding.editBlurSelfBtn.setOnClickListener {
            binding.blurSelfLayout.visibility = View.VISIBLE
        }

        // 수동 블러 rectangle 드래그
        dragBlurRectangle()
    }

    private fun setupMediaRetrieverAndSeekBar(uri: Uri) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, uri)

        val videoLength =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()
        if (videoLength != null) {
            frameSeekBar.max = videoLength.toInt()
        }

        // SeekBar의 드래그 이벤트 처리
        frameSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        getUri?.let { editViewModel.loadFrames(requireContext(), it, videoLength!!) }

    }

    private fun uriToRequestBody() {
        viewLifecycleOwner.lifecycleScope.launch {
            getUri?.let { editViewModel.uriToRequestBody(requireContext(), it) }
        }
    }


    private fun setGetPresignedUrlStateObserver() {
        editViewModel.getPresignedUrlState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Timber.tag("Success").d(state.data.data.url)
                        uriToRequestBody()
                        editViewModel.setVideoFileName(state.data.data.fileName)
                        editViewModel.setPresignedUrl(state.data.data.url)
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").e(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setPutVideoToPresignedUrlStateObserver() {
        editViewModel.putVideoToPresignedUrlState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Timber.tag("Success").d(state.data.toString())
                        if (isBlurModeSelected) {
                            editViewModel.videoFileName.value?.let {
                                editViewModel.getMosaicedVideo(
                                    UID,
                                    it
                                )
                            }
                        }
                        if (isSubtitleModeSelected) {
                            editViewModel.videoFileName.value?.let { fileName ->
                                editViewModel.getSubtitle(
                                    uid = UID,
                                    fileName = fileName
                                )
                            }
                        }
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").e(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setGetSubtitleStateObserver() {
        editViewModel.getSubtitleState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        isSubtitleModeSelected = false
                        Timber.tag("SuccessSubTitle").d(state.data.data.subtitle.toString())
                    }

                    is UiState.Failure -> {
                        Log.d("Fail", state.msg)
                        Timber.tag("Failure").e(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }


    private fun setGetMosaicedVideoStateObserver() {
        editViewModel.getMosaicedVideoState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        isBlurModeSelected = false
                        editViewModel.saveFile(requireContext(), state.data)
                        Timber.tag("Success").d(state.data.toString())
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").d(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        try {
            if (getUri != null) {
                mediaPlayer.setDataSource(requireContext(), getUri!!)
                mediaPlayer.apply {
                    setSurface(Surface(surface))
                    setOnPreparedListener(this@EditFragment)
                    prepareAsync()  // mediaPlayer가 준비되었음을 알림
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        // 재생 버튼
        binding.videoPlayBtn.setOnClickListener {
            mp!!.start()
            binding.videoPlayBtn.visibility = View.GONE
            binding.videoPauseBtn.visibility = View.VISIBLE
        }

        // 일시정지 버튼
        binding.videoPauseBtn.setOnClickListener {
            mp!!.pause()
            binding.videoPlayBtn.visibility = View.VISIBLE
            binding.videoPauseBtn.visibility = View.GONE
        }
    }

    // 수동 블러 rectangle 드래그
    @SuppressLint("ClickableViewAccessibility")
    private fun dragBlurRectangle() {
        var dX: Float = 0F
        var dY: Float = 0F
        binding.blurSelfLayout.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 터치 시작 위치 저장
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    // ImageView 위치 업데이트
                    binding.blurSelfLayout.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }

                MotionEvent.ACTION_UP -> {
                    // 사용자가 뷰를 눌렀다가 뗐을 때 performClick() 메서드 호출
                    view.performClick()
                    getCoordinates()
                }

                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }

        binding.blurSelfLayout.setOnClickListener {
            // 클릭 시
        }

        binding.blurRectangleX.setOnClickListener {
            binding.blurSelfLayout.visibility = View.GONE
        }

//        var nX: Float = 0F
//        var nY: Float = 0F
//        val constraintSet = ConstraintSet()
//        binding.blurRectangleResize.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    // 터치 시작 위치 저장
//                    nX = view.x - event.rawX
//                    nY = view.y - event.rawY
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    // blur_self_rectangle의 너비 제약조건을 변경합니다.
//                    constraintSet.clone(binding.blurSelfLayout)
//                    constraintSet.constrainWidth(R.id.blur_self_rectangle, (event.rawX - binding.blurSelfRectangle.x).toInt())
//                    constraintSet.constrainHeight(R.id.blur_self_rectangle, (event.rawY - binding.blurSelfRectangle.y).toInt())
//                    constraintSet.applyTo(binding.blurSelfLayout)
//                }
//            }
//            true
//        }
    }

    private var rectangleX = 0F
    private var rectangleY = 0F

    // 블러 rectangle 좌측상단 좌표 저장
    private fun getCoordinates() {
        val density = resources.displayMetrics.density
        val paddingInPx = 10 * density
        rectangleX = binding.blurSelfLayout.x + paddingInPx
        rectangleY = binding.blurSelfLayout.y + paddingInPx
        Log.d("blur rectangle width", binding.blurSelfRectangle.width.toString())
        Log.d("blur rectangle height", binding.blurSelfRectangle.height.toString())
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    companion object {
        private const val UID = 3
    }
}