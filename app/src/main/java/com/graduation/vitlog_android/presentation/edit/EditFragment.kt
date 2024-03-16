package com.graduation.vitlog_android.presentation.edit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaExtractor
import android.media.MediaFormat
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
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentEditBinding
import com.graduation.vitlog_android.presentation.MainActivity
import com.graduation.vitlog_android.util.multipart.ContentUriRequestBody
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException
import java.security.InvalidParameterException


@AndroidEntryPoint
class EditFragment : Fragment(), TextureView.SurfaceTextureListener,
    MediaPlayer.OnPreparedListener {


    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private var getUri: Uri? = null
    private val editViewModel by viewModels<EditViewModel>()
    private lateinit var mediaPlayer: MediaPlayer

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(layoutInflater, container, false)

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
        binding.editSaveBtn.setOnClickListener {
            editViewModel.getPresignedUrl()
        }
        getUri?.let {
            setupMediaRetrieverAndSeekBar(it)
            //createAndSetVideoRequestBody(getUri!!)
            //setPostVideoStateObserver()
            setPutVideoToPresignedUrlStateObserver()
            setGetPresignedUrlStateObserver()
            setGetMosaicedVideoStateObserver()
        }

        buttonActions()
        timeLineRV()

        return binding.root
    }

    private fun timeLineRV() {

        Log.d("edit model", editViewModel.timeLineImages.toString())
        binding.editTimelineRv.adapter = TimeLineAdapter(editViewModel.timeLineImages)

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
            getUri?.let { editViewModel.loadFrames(requireContext(), it, videoLength) }
        }

        binding.editTimelineRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // RecyclerView가 스크롤될 때 호출됩니다.
                // dx와 dy 매개변수는 스크롤 이동량을 나타냅니다. (가로 및 세로 방향으로의 이동량)

                // 현재 스크롤 위치를 계산합니다.
                val scrollY = recyclerView.computeHorizontalScrollOffset()

                // 여기서 scrollY를 이용하여 mediaPlayer.seekTo(progress)와 같은 동작을 구현합니다.
                // 예를 들어, scrollY를 시간(초)으로 변환하여 mediaPlayer를 조정할 수 있습니다.
                // 예를 들어, 비디오의 총 길이를 알고 있다면 scrollY를 비디오의 전체 길이로 나누어 해당 위치로 이동시킬 수 있습니다.
                // mediaPlayer.seekTo(progress)를 호출하여 재생 위치를 조정합니다.
                val videoLengthInMilliseconds = mediaPlayer.duration // 밀리초 단위로 영상의 총 길이를 가져옵니다.
                val desiredPositionInMilliseconds = (scrollY / recyclerView.height.toFloat() * videoLengthInMilliseconds).toInt()
                mediaPlayer.seekTo(desiredPositionInMilliseconds)

                // 영상의 분,초 00:00 형태로 저장
                val minutes = (mediaPlayer.currentPosition / 1000) / 60
                val seconds = (mediaPlayer.currentPosition / 1000) % 60
                val milliseconds = (mediaPlayer.currentPosition % 1000) / 10
                val timeString = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
                binding.editTimeTv.text = timeString
            }
        })



    }

    private fun setPostVideoStateObserver() {
        editViewModel.postVideoState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("Success", "POST 완료")
                    }

                    is UiState.Failure -> {
                        Log.d("Failure", state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                    else -> {}
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
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
                        Log.d("Success", state.data.data.url)
                        uriToRequestBody()
                        editViewModel.setPresignedUrl(state.data.data.url)
                    }

                    is UiState.Failure -> {
                        Log.d("Failure", state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                    else -> {}
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setPutVideoToPresignedUrlStateObserver() {
        editViewModel.putVideoToPresignedUrlState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("Success", state.data.toString())
                        editViewModel.getMosaicedVideo(1,"hi")
                    }
                    is UiState.Failure -> {
                        Log.d("Failure", state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                    else -> {}
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }


    private fun setGetMosaicedVideoStateObserver() {
        editViewModel.getMosaicedVideoState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        editViewModel.saveFile(requireContext(),state.data)
                        Log.d("Success", state.data.toString())
                    }
                    is UiState.Failure -> {
                        Log.d("Failure", state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> Unit
                    else -> {}
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun createAndSetVideoRequestBody(uri: Uri) {
        val videoRequestBody = ContentUriRequestBody(requireContext(), uri)
        editViewModel.setVideoRequestBody(videoRequestBody)
    }

    // 선택한 영상의 트랙 추출
    private fun selectVideoTrack(
        extractor: MediaExtractor,
        prefix: String = "video/"
    ): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith(prefix) == true) {
                extractor.selectTrack(i)
                return format
            }
        }
        throw InvalidParameterException("File contains no video track")
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
    private var rectangleRightX = 0F
    private var rectangleRightY = 0F

    // 블러 rectangle 좌측상단 좌표 저장
    private fun getCoordinates() {
        val density = resources.displayMetrics.density
        val paddingInPx = 10 * density
        rectangleX = binding.blurSelfLayout.x + paddingInPx
        rectangleY = binding.blurSelfLayout.y + paddingInPx
        rectangleRightX = rectangleX + binding.blurSelfRectangle.width
        rectangleRightY = rectangleX + binding.blurSelfRectangle.height
//        Log.d("blur rectangle width", binding.blurSelfRectangle.width.toString())
//        Log.d("blur rectangle height", binding.blurSelfRectangle.height.toString())
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
        private const val TIMEOUT_US = 10000
    }
}