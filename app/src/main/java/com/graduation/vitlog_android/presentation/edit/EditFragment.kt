package com.graduation.vitlog_android.presentation.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.graduation.vitlog_android.R
import com.graduation.vitlog_android.databinding.FragmentEditBinding
import com.graduation.vitlog_android.model.entity.Subtitle
import com.graduation.vitlog_android.model.request.RequestBlurDto
import com.graduation.vitlog_android.util.binding.BindingFragment
import com.graduation.vitlog_android.util.preference.SharedPrefManager
import com.graduation.vitlog_android.util.preference.SharedPrefManager.uid
import com.graduation.vitlog_android.util.preference.SharedPrefManager.vid
import com.graduation.vitlog_android.util.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException


@AndroidEntryPoint
class EditFragment : BindingFragment<FragmentEditBinding>(R.layout.fragment_edit),
    TextureView.SurfaceTextureListener,
    MediaPlayer.OnPreparedListener {
    private var getUri: Uri? = null
    private val editViewModel by viewModels<EditViewModel>()
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var subtitleAdapter: SubtitleAdapter
    private lateinit var timeLineAdapter: TimeLineAdapter

    private var isBlurModeSelected: Boolean = false
    private var isSubtitleModeSelected: Boolean = false
    private var isManualBlurModeSelected: Boolean = false
    private var mediaPlayerOnPrepared: Boolean = false

    private var manualBlurData = mutableListOf<RequestBlurDto>()
    private var startTime: String = "00:00:00"
    private var endTime: String = "00:00:00"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUri = arguments?.getString("videoUri")?.toUri()

        activity?.runOnUiThread {
            binding.editProgressbar.visibility = View.INVISIBLE
        }
        mediaPlayer = MediaPlayer()
        initAdapter()
        setListener()
        setObserver()
        showEditDefaultMode()

        binding.tvVideo.surfaceTextureListener = this
        binding.backBtn.setOnClickListener {
            mediaPlayer.release()
            activity?.finish()
        }
        getUri?.let {
            setupMediaRetrieverAndSeekBar(it)
        }
        manageSubtitle()
        applySubtitleFont()
        applySubtitleColor()
    }

    private fun initAdapter() {
        timeLineAdapter = TimeLineAdapter(editViewModel.timeLineImages)
        binding.editTimelineRv.adapter = timeLineAdapter
        subtitleAdapter = SubtitleAdapter()
        binding.rvEditToolSubtitle.adapter = subtitleAdapter
    }

    private fun buttonActions() {
        // 수동 블러 버튼 클릭
        binding.btnEditBlurSelf.setOnClickListener {
            binding.blurSelfLayout.visibility = VISIBLE
            binding.timelineSectionIv.visibility = VISIBLE
            binding.editSaveBtn.text = "완료"
            binding.editSaveBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.main_pink
                )
            )
            setBlurPartOfBitmap()
        }

        // 수동 블러 rectangle 드래그
        dragBlurRectangle()
    }

    private fun setBlurPartOfBitmap() {
        val metaDataSource = MediaMetadataRetriever()
        metaDataSource.setDataSource(context, getUri)

        val context = this.context ?: return
        val density = context.resources.displayMetrics.density
        var px = (70 * density).toInt()
        var py = (70 * density).toInt()

        val bitmap = binding.tvVideo.getBitmap()

        val dx = (binding.blurSelfLayout.x + binding.blurSelfRectangle.x).coerceAtLeast(0F).toInt()
        val dy = (binding.blurSelfLayout.y + binding.blurSelfRectangle.y).coerceAtLeast(0F).toInt()

        if (dx >= bitmap!!.width - binding.blurSelfRectangle.width) {
            px = (bitmap.width - dx).coerceAtLeast(0)
        }
        if (dy >= bitmap.height - binding.blurSelfRectangle.height) {
            py = (bitmap.height - dy).coerceAtLeast(0)
        }
        val partialBitmap = Bitmap.createBitmap(
            bitmap,
            dx,
            dy,
            px,
            py
        )

        val blurEffect = RenderEffect.createBlurEffect(10F, 10F, Shader.TileMode.MIRROR)
        binding.blurSelfRectangle.setRenderEffect(blurEffect)
        binding.blurSelfRectangle.setImageBitmap(partialBitmap)
    }

    private fun setObserver() {
        setPutVideoToPresignedUrlStateObserver()
        setGetPresignedUrlStateObserver()
        setGetMosaicedVideoStateObserver()
        setGetSubtitleStateObserver()
        setPostManualBlurStateObserver()
        setSaveFileObserver()
    }


    private fun showEditDefaultMode() {
        binding.tvEditBlurAuto.visibility = INVISIBLE
        binding.tvEditBlurSelf.visibility = INVISIBLE
        binding.btnEditBlurAuto.visibility = INVISIBLE
        binding.btnEditBlurSelf.visibility = INVISIBLE
        binding.tvEditSubtitleToolFont.visibility = INVISIBLE
        binding.tvEditSubtitleToolColor.visibility = INVISIBLE
        binding.btnEditSubtitleToolFont.visibility = INVISIBLE
        binding.btnEditSubtitleToolColor.visibility = INVISIBLE
    }

    private fun showEditSubtitleMode() {
        binding.tvEditBlurAuto.visibility = INVISIBLE
        binding.tvEditBlurSelf.visibility = INVISIBLE
        binding.btnEditBlurAuto.visibility = INVISIBLE
        binding.btnEditBlurSelf.visibility = INVISIBLE
        binding.tvEditSubtitleToolFont.visibility = VISIBLE
        binding.tvEditSubtitleToolColor.visibility = VISIBLE
        binding.btnEditSubtitleToolFont.visibility = VISIBLE
        binding.btnEditSubtitleToolColor.visibility = VISIBLE
    }

    private fun showEditBlurMode() {
        binding.tvEditBlurAuto.visibility = VISIBLE
        binding.tvEditBlurSelf.visibility = VISIBLE
        binding.btnEditBlurAuto.visibility = VISIBLE
        binding.btnEditBlurSelf.visibility = VISIBLE
        binding.tvEditSubtitleToolFont.visibility = INVISIBLE
        binding.tvEditSubtitleToolColor.visibility = INVISIBLE
        binding.btnEditSubtitleToolFont.visibility = INVISIBLE
        binding.btnEditSubtitleToolColor.visibility = INVISIBLE
    }


    private fun setListener() {
        binding.btnEditBlur.setOnClickListener {
            showEditBlurMode()
            binding.btnEditBlur.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_edit_blur_clicked
                )
            )
            binding.btnEditSubtitle.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_edit_subtitles_unclicked
                )
            )
            isBlurModeSelected = true
        }
        binding.btnEditSubtitle.setOnClickListener {
            showEditSubtitleMode()
            binding.btnEditBlur.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_edit_blur_unclicked
                )
            )
            binding.btnEditSubtitle.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_edit_subtitles_clicked
                )
            )
            isSubtitleModeSelected = true
        }

        binding.editSaveBtn.setOnClickListener {
            if (binding.editSaveBtn.text == "저장") {
                editViewModel.getPresignedUrl()
            } else if (binding.editSaveBtn.text == "완료") {
                binding.editSaveBtn.text = "저장"
                binding.editSaveBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                startTime = (mediaPlayer.currentPosition / 1000).toString()
                endTime =
                    (mediaPlayer.currentPosition / 1000 + 2).toString()   // 끝나는 시간은 일단 2초 뒤로 고정

                // 수동블러
                manualBlurData.add(
                    RequestBlurDto(
                        startTime = startTime,
                        endTime = endTime,
                        x1 = rectangleX,
                        y1 = rectangleY,
                        x2 = rectangleRightX,
                        y2 = rectangleRightY
                    )
                )
                isManualBlurModeSelected = true
            }
        }

        binding.btnEditSubtitleToolFont.setOnClickListener {
            showSubtitleFontBar()
        }

        subtitleCompleteButtonListener()
        subtitleEditCompleteButtonListener()
        buttonActions()

        // 재생 완료 되었을 때
        mediaPlayer.setOnCompletionListener {
            binding.videoPlayBtn.visibility = VISIBLE
            binding.videoPauseBtn.visibility = View.GONE
        }
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
        mediaPlayerOnPrepared = true
//        editViewModel.getPresignedUrl()
        mediaPlayer.seekTo(0)   // 재생 전 첫 번쨰 프레임 보여주기

        // 재생 버튼
        binding.videoPlayBtn.setOnClickListener {
            mp!!.start()
            binding.videoPlayBtn.visibility = View.GONE
            binding.videoPauseBtn.visibility = VISIBLE
            handler.post(updateUiRunnable) // 여기를 수정
        }

        // 일시정지 버튼
        binding.videoPauseBtn.setOnClickListener {
            mp!!.pause()
            binding.videoPlayBtn.visibility = VISIBLE
            binding.videoPauseBtn.visibility = View.GONE
            handler.removeCallbacks(updateUiRunnable) // 여기를 수정
        }

        // 영상 -3초
        binding.playSkipBackBtn.setOnClickListener {
            val currentPosition = mediaPlayer.currentPosition
            val newPosition = if ((currentPosition - 3000) > 0) currentPosition - 3000 else 0
            mediaPlayer.seekTo(newPosition)
        }
        // 영상 +3초
        binding.playSkipForwardBtn.setOnClickListener {
            val currentPosition = mediaPlayer.currentPosition
            mediaPlayer.seekTo(currentPosition + 3000)
        }
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
                // 타임라인(리사이클러뷰)가 스크롤됨에 따라 가로 스크롤길이를 구한다
                val scrollY = recyclerView.computeHorizontalScrollOffset()
                val videoLengthInMilliseconds = mediaPlayer.duration
                // 전체 스크롤 가능한 범위 대비 스크롤한 비율을 구해서 이를 영상 전체 길이에 다시 대응하는 식으로 구현
                val desiredPositionInMilliseconds =
                    (scrollY / recyclerView.computeHorizontalScrollRange()
                        .toFloat() * videoLengthInMilliseconds).toInt()

                mediaPlayer.seekTo(desiredPositionInMilliseconds)

                // 타임라인 이동에 따른 자막 업데이트, 시간 흐름 업데이트
                updateSubtitle(mediaPlayer.currentPosition, editViewModel.subtitleList)
                updateTimeString()
                if (mediaPlayerOnPrepared) {
                    setBlurPartOfBitmap()
                }
            }
        })
    }

    val handler = Handler(Looper.getMainLooper())

    private fun updateTimeString() {
        // 영상의 분,초 00:00 형태로 저장
        val minutes = (mediaPlayer.currentPosition / 1000) / 60
        val seconds = (mediaPlayer.currentPosition / 1000) % 60
        val milliseconds = (mediaPlayer.currentPosition % 1000) / 10
        val timeString = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
        binding.editTimeTv.text = timeString
    }

    private val updateUiRunnable = object : Runnable {
        override fun run() {
            setBlurPartOfBitmap()
            val currentPositionInMilliseconds = mediaPlayer.currentPosition
            val videoLengthInMilliseconds = mediaPlayer.duration
            val scrollOffset =
                (currentPositionInMilliseconds.toFloat() / videoLengthInMilliseconds * timeLineAdapter.itemCount)
            Log.d("subtitle", editViewModel.subtitleList.toString())
            binding.editTimelineRv.smoothScrollToPosition(scrollOffset.toInt())
            updateTimeString(currentPositionInMilliseconds)
            handler.postDelayed(this, 1)
        }
    }

    private fun updateTimeString(currentPositionInMilliseconds: Int) {
        val minutes = (currentPositionInMilliseconds / 1000) / 60
        val seconds = (currentPositionInMilliseconds / 1000) % 60
        val milliseconds = (currentPositionInMilliseconds % 1000) / 10
        val timeString = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
        binding.editTimeTv.text = timeString
    }

    fun updateSubtitle(currentPosition: Int, subtitles: List<Subtitle>) {
        val currentSubtitle = subtitles.find { subtitle ->
            // startMill, endMill로  currentPosition이 자막의 시작과 종료 시간 사이에 있는지 확인
            subtitle.startMill <= currentPosition && currentPosition < subtitle.endMill
        }
        binding.tvSubtitle.text = currentSubtitle?.text ?: ""
    }

    // MediaPlayer가 재생 중일 때 currentPosition에 맞게 자막 업데이트
    private fun startSubtitleUpdater(subtitle: List<Subtitle>) {
        val handler = Handler(Looper.getMainLooper())
        val updateTask = object : Runnable {
            override fun run() {
                val currentPosition = mediaPlayer.currentPosition
                updateSubtitle(currentPosition, subtitle)
                handler.postDelayed(this, 1) // 1초마다 업데이트
            }
        }
        handler.post(updateTask)
    }

    private fun applyEditedSubtitle() {
        binding.tvSubtitle.setOnClickListener {
            it.visibility = View.GONE
            binding.etSubtitle.setText(binding.tvSubtitle.text)
            binding.etSubtitle.visibility = VISIBLE
            binding.etSubtitle.requestFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSubtitle, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun saveAndSwitchToTextView() {
        binding.tvSubtitle.text = binding.etSubtitle.text
        editViewModel.updateSubtitleText(
            mediaPlayer.currentPosition,
            binding.etSubtitle.text.toString()
        )
        binding.etSubtitle.visibility = View.GONE
        binding.tvSubtitle.visibility = VISIBLE
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSubtitle.windowToken, 0)
    }

    private fun manageSubtitle() {
        applyEditedSubtitle()
        binding.root.setOnClickListener {
            if (binding.etSubtitle.visibility == VISIBLE) {
                saveAndSwitchToTextView()
            }
        }

        binding.etSubtitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveAndSwitchToTextView()
            }
        }
    }

    private fun applySubtitleFont() {
        binding.ivEditSubtitlePlayfair.setOnClickListener {
            binding.tvSubtitle.setTextAppearance(R.style.TextAppearance_VitLog_FairDisplay_m_10)
        }
        binding.ivEditSubtitlePretendard.setOnClickListener {
            binding.tvSubtitle.setTextAppearance(R.style.TextAppearance_VitLog_Content_m_10)
        }
    }

    private fun applySubtitleColor() {
        binding.ivEditSubtitleColorGray.setOnClickListener {
            binding.tvSubtitle.setTextColor(Color.parseColor("#F5F5F5"))
        }
        binding.ivEditSubtitleColorYellow.setOnClickListener {
            binding.tvSubtitle.setTextColor(Color.parseColor("#FFF852"))
        }
        binding.ivEditSubtitleColorPink.setOnClickListener {
            binding.tvSubtitle.setTextColor(Color.parseColor("#FF485E"))
        }
        binding.ivEditSubtitleColorBlack.setOnClickListener {
            binding.tvSubtitle.setTextColor(Color.parseColor("#FF000000"))
        }
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
                        SharedPrefManager.save("vid", state.data.data.vid)
                        Log.d("vid", vid.toString())
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
                                    uid,
                                    it
                                )
                            }
                        }
                        if (isSubtitleModeSelected) {
                            editViewModel.videoFileName.value?.let { fileName ->
                                editViewModel.getSubtitle(
                                    uid = uid,
                                    fileName = fileName
                                )
                            }
                        }
                        if (isManualBlurModeSelected) {
                            editViewModel.videoFileName.value?.let {
                                editViewModel.postManualBlur(
                                    uid = uid,
                                    vid = vid.toString(),
                                    requestBlurDto = manualBlurData
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
                    is UiState.Loading -> {
                        binding.editProgressbar.visibility = VISIBLE
                    }

                    is UiState.Success -> {
                        binding.editProgressbar.visibility = INVISIBLE
                        showSubtitleEditBar()
                        editViewModel.saveSubtitleList(state.data)
                        startSubtitleUpdater(editViewModel.subtitleList)
                        subtitleAdapter.submitList(state.data)
                        editViewModel._getSubtitleState.value = UiState.Empty
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").e(state.msg)
                    }

                    is UiState.Empty -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setPostManualBlurStateObserver() {
        editViewModel.postManualBlurState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Loading -> {
                    }

                    is UiState.Success -> {
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").e(state.msg)
                    }

                    is UiState.Empty -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showEditToolBar() {
        binding.clEditTool.visibility = VISIBLE
        binding.clEditToolSubtitle.visibility = INVISIBLE
        binding.clEditSubtitle.visibility = INVISIBLE
    }

    private fun showSubtitleEditBar() {
        binding.clEditTool.visibility = INVISIBLE
        binding.clEditToolSubtitle.visibility = VISIBLE
        binding.clEditSubtitle.visibility = INVISIBLE
    }

    private fun showSubtitleFontBar() {
        binding.clEditTool.visibility = INVISIBLE
        binding.clEditSubtitle.visibility = VISIBLE
    }

    private fun subtitleCompleteButtonListener() {
        binding.tvEditToolComplete.setOnClickListener {
            showEditToolBar()
        }
    }

    private fun subtitleEditCompleteButtonListener() {
        binding.tvEditSubtitleComplete.setOnClickListener {
            showEditToolBar()
        }
    }

    private fun setGetMosaicedVideoStateObserver() {
        editViewModel.getMosaicedVideoState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("success","getMosaicedVideoState")
                        editViewModel._getMosaicedVideoState.value = UiState.Empty
                        editViewModel.saveFile(requireContext(), state.data)
                        Timber.tag("Success").d(state.data.toString())
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").d(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> {
                        binding.editProgressbar.visibility = VISIBLE
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setSaveFileObserver() {
        editViewModel.saveVideoState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is UiState.Success -> {
                        Log.d("success","saveVideoState")
                        binding.editProgressbar.visibility = INVISIBLE
                        editViewModel._saveVideoState.value = UiState.Empty
                        Timber.tag("Success").d(state.data.toString())
                    }

                    is UiState.Failure -> {
                        Timber.tag("Failure").d(state.msg)
                    }

                    is UiState.Empty -> Unit
                    is UiState.Loading -> {
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
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
                    setBlurPartOfBitmap()
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
            binding.blurSelfLayout.visibility = View.INVISIBLE
            binding.timelineSectionIv.visibility = View.GONE
        }
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

//     TODO : 혜선 1. 자동로그인 2. 자막 클릭 시, 편집 할 수 있도록 3. 내용 뿐만 아니라 폰트 및 색상 까지 4. 자막 서버 연결

}