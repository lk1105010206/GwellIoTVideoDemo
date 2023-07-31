package com.tencentcs.iotvideodemo.videoplayer

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tencentcs.iotvideo.IPropertySettingListener
import com.tencentcs.iotvideo.iotvideoplayer.*
import com.tencentcs.iotvideo.iotvideoplayer.player.PlaybackFileDownloader
import com.tencentcs.iotvideo.iotvideoplayer.player.PlaybackPlayer
import com.tencentcs.iotvideo.iotvideoplayer.player.ThumbnailDownloader
import com.tencentcs.iotvideo.messagemgr.*
import com.tencentcs.iotvideo.messagemgr.InnerMessageSender.delPlaybackByFiles
import com.tencentcs.iotvideo.utils.FileIOUtils
import com.tencentcs.iotvideo.utils.LogUtils
import com.tencentcs.iotvideo.utils.Utils
import com.tencentcs.iotvideo.utils.rxjava.IDelFileResultListener
import com.tencentcs.iotvideo.utils.rxjava.IResultListener
import com.tencentcs.iotvideodemo.R
import com.tencentcs.iotvideodemo.entity.PlaybackDownloadFileEntity
import com.tencentcs.iotvideodemo.kt.base.BaseActivity
import com.tencentcs.iotvideodemo.kt.function.click
import com.tencentcs.iotvideodemo.kt.ui.ListItemDecoration
import com.tencentcs.iotvideodemo.kt.ui.adapter.*
import com.tencentcs.iotvideodemo.kt.ui.adapter.SimpleAdapter
import com.tencentcs.iotvideodemo.kt.utils.ViewUtils
import com.tencentcs.iotvideodemo.messagemgr.PlaybackFileDownloadManager
import com.tencentcs.iotvideodemo.utils.StorageManager
import kotlinx.android.synthetic.main.activity_playback_player.*
import kotlinx.android.synthetic.main.item_playback_node.*
import kotlinx.android.synthetic.main.item_playback_node.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlaybackPlayerActivity : BaseActivity(), PlaybackFileDownloadManager.DownloadItemStateListener {
    private val mSimpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")

    private lateinit var mAdapter: SimpleAdapter<PlaybackMessage.PlaybackNode>

    var data: ArrayList<PlaybackMessage.PlaybackNode> = ArrayList()

    var thumbCacheDirect = ""

    private var mDeviceId = ""

    private var mPlaybackPlayer = PlaybackPlayer()

    private var mCurrentPageIndex = 0

    private var mPageCount = -1

    private var selectCalendar: Calendar = Calendar.getInstance()

    private var mPlayStrategy = PlaybackPlayStrategy.PLAY_STRATEGY_ASCENDING

    private var mLastPlayNode: PlaybackMessage.PlaybackNode? = null;

    private var thumbnailDownloader = ThumbnailDownloader()

    override fun getResId() = R.layout.activity_playback_player

    override fun init(savedInstanceState: Bundle?) {
        thumbCacheDirect = StorageManager.getSdThumbPath()

        intent.getStringExtra("deviceID")?.apply {
            mDeviceId = this
        }

        mPlaybackPlayer.setPlaybackInnerUserDataLister(object : IPlaybackInnerUserDataLister {
            override fun onPlayFileFinished(playFileStartTime: Long) {
                LogUtils.i(TAG, "onPlayFileFinished playFileStartTime:" + Utils.timeFormat(playFileStartTime))
            }

            override fun onViewerNumberChanged(viewerNumber: Byte) {

            }

            override fun onSeekRet(isSuccessful: Boolean, seekTime: Long) {
                LogUtils.i(TAG, "isSuccessful:$isSuccessful; seekTime:$seekTime")
            }

            override fun onPlayFileStart(playFileStartTime: Long) {
                LogUtils.i(TAG, "onPlayFileStart playFileStartTime:" + Utils.timeFormat(playFileStartTime))
            }

        })

        val recyclerView = rv_playback_list
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(ListItemDecoration(ViewUtils.dip2px(1f), Color.parseColor("#eeeeee")))

        val itemHolder = ItemHolder<PlaybackMessage.PlaybackNode>(R.layout.item_playback_node, 0)
                .bindData { data, position ->
                    setText(itemView.tv_start, data.startTimeDisplay)
                    setText(itemView.tv_end, data.endTimeDisplay)
                    setText(itemView.tv_type, data.recordType)
                    loadCachePicture(data.startTime, itemView.iv_event_thumb)
                }
                .bindEvent { data, position ->
                    onClick(itemView) {
                        if (mPlaybackPlayer.isConnectedDevice) {
                            mPlaybackPlayer.seek(it.startTime, it)
                        } else {
                            //设置播放器数据源
                            if (mPlaybackPlayer.playState == PlayerStateEnum.STATE_IDLE ||
                                    mPlaybackPlayer.playState == PlayerStateEnum.STATE_STOP) {
                                mPlaybackPlayer.setDataResource(mDeviceId, it.startTime, it)
                            }
                            mPlaybackPlayer.play()
                        }
                        mLastPlayNode = it
                    }
                    onClick(itemView.iv_download_file) {
                        downloadPlaybackFile(it)
                    }
                    onClick(itemView.iv_event_thumb) {
                        var nodes = ArrayList<PlaybackMessage.PlaybackNode>()
                        nodes.add(it)
                        loadThumb(itemView.iv_event_thumb, nodes)
                    }
                    onClick(itemView.tv_del_item) {
                        LogUtils.i(TAG, "onSuccess: position = $position")
                        PlaybackPlayer.delPlaybackByTimeRange(mDeviceId, it.startTime, it.endTime, object : IDelFileResultListener {
                            override fun onStart() {
                            }

                            override fun onProgress(delPlaybackData: DelPlaybackData?) {
                            }

                            override fun onComplete() {
                                mAdapter.data.removeAt(position)
                                mAdapter.notifyItemRemoved(position)
                                mAdapter.notifyItemRangeChanged(position, mAdapter.data.size - position)
                            }

                            override fun onError(errorCode: Int, errorMsg: String) {
                                LogUtils.i(TAG, "onError: errorCode = $errorCode errorMsg = $errorMsg")
                            }
                        })

                    }
                }

        mAdapter = SimpleAdapter<PlaybackMessage.PlaybackNode>(data, itemHolder) { data, postion -> 0 }

        recyclerView.adapter = mAdapter

        tv_get_playback_first.click {
            getPlaybackList(tv_get_playback_first, 0)
        }

        tv_get_playback_previous.click {
            getPlaybackListPrevious()
        }

        tv_get_playback_next.click {
            getPlaybackListNext()
        }

        tv_get_playback_last.click {
            if (mPageCount > 0) {
                getPlaybackList(tv_get_playback_last, mPageCount - 1)
            } else {
                getPlaybackList(tv_get_playback_last, 0)
            }
        }

        tv_start_record.click {
            deviceRecord(true)
        }

        tv_stop_record.click {
            deviceRecord(false)
        }

        tv_fast_play.click {
            if (!TextUtils.isEmpty(speed_et.text)) {
                val speed = speed_et.text.toString().toFloat();
                mPlaybackPlayer.setPlaybackSpeed(speed, object : IPropertySettingListener {
                    override fun onResult(code: Int, errorReason: String?) {
                        if (0 == code) {
                            tv_play_speed_hint.run {
                                visibility = View.VISIBLE
                                text = "${resources.getString(R.string.play_speed)}$speed"
                            }
                        }
                        LogUtils.d(TAG, "setPlaybackSpeed onSuccess");

                    }

                })
            }
        }

        record_btn.click { recordVideoFromDevice() }

        snap_btn.click { snap() }

        mute_btn.click { mPlaybackPlayer.mute(!mPlaybackPlayer.isMute) }

        stop_btn.click { mPlaybackPlayer.stop() }

        pause_btn.click {
            if (pause_btn.isSelected) {
                mPlaybackPlayer.resume { code, errorReason ->
                    if (0 == code) {
                        pause_btn.isSelected = false
                        pause_btn.text = "暂停"
                    }
                }
            } else {
                mPlaybackPlayer.pause { code, errorReason ->
                    if (0 == code) {
                        pause_btn.isSelected = true
                        pause_btn.text = "恢复"
                    }
                }
            }
        }

        // 删除回放列表
        tv_del_playback_list.click { view ->
            val delFiles = arrayListOf<Long>()
            mAdapter.data.forEach {
                delFiles.add(it.startTime)
            }
            PlaybackPlayer.deletePlaybackByFiles(mDeviceId, delFiles, object : IDelFileResultListener {
                override fun onStart() {
                    showLoadingDialog("loading..", true)
                }

                override fun onProgress(delPlaybackData: DelPlaybackData) {
                    LogUtils.i(TAG, "onProgress: delPlaybackData = $delPlaybackData")
                    delPlaybackData.failList.forEach {
                        LogUtils.i(TAG, "onProgress: delPlaybackData fileTime = ${it.fileTime} errCode = ${it.errCode}")
                    }
                }

                override fun onComplete() {
                    Snackbar.make(view, getString(R.string.toast_del_playback_success), Snackbar.LENGTH_LONG)
                    hideLoadingDialog()
                    // 刷新列表数据
                    getPlaybackList(tv_del_playback_list, 0)
                }

                override fun onError(errorCode: Int, errorMsg: String?) {
                    hideLoadingDialog()
                    Snackbar.make(view, "删除错误  errCode${errorCode}", Snackbar.LENGTH_LONG)
                }
            })
        }

        // 取消删除回放
        tv_cancel_del_playback.click {
            PlaybackPlayer.cancelDeletePlaybackFiles(mDeviceId, object : IResultListener<DataMessage> {
                override fun onStart() {

                }

                override fun onSuccess(msg: DataMessage?) {
                    Snackbar.make(it, "取消删除成功", Snackbar.LENGTH_LONG)
                }

                override fun onError(errorCode: Int, errorMsg: String?) {
                    Snackbar.make(it, "取消失败  errCode${errorCode}", Snackbar.LENGTH_LONG)
                }

            })
        }

        initPlaybackPlayer()

        getPlaybackList(tv_get_playback_previous, 0)
        setSelectedDateText()
        PlaybackFileDownloadManager.getInstance().setContext(applicationContext)

        ArrayAdapter.createFromResource(
                this,
                R.array.play_back_play_strategy,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sn_play_strategy.adapter = adapter
        }

        sn_play_strategy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                setPlayStrategy(p2)
            }
        }

        tv_test.click {
            if (null == mLastPlayNode) {
                Snackbar.make(tv_get_playback_previous, "请先点击文件进行播放", Snackbar.LENGTH_LONG).show()
                return@click
            }
            if (mPlaybackPlayer.isPlaying) {
                mPlaybackPlayer.seek(mLastPlayNode!!.startTime + 5000, mLastPlayNode)
            } else {
                Snackbar.make(tv_get_playback_previous, "请先与设备建立连接", Snackbar.LENGTH_LONG).show()
            }
        }

        thumbnailDownloader.setDataSource(mDeviceId)
    }

    private fun getExistRecordDateList() {
        var endTime: Long = System.currentTimeMillis();
        var startTime: Long = 0;//endTime - 24 * 60 * 60 * 1000 * 30L;
        PlaybackPlayer.getExistRecordDateList(mDeviceId, startTime, endTime, 0, 1000, object : IResultListener<PlaybackExistDateMessage> {
            override fun onStart() {
                Snackbar.make(tv_get_playback_previous, "开始查询存在录像日期列表...", Snackbar.LENGTH_LONG).show()
            }

            override fun onSuccess(msg: PlaybackExistDateMessage?) {
                LogUtils.i(TAG, "getExistRecordDateList onSuccess:${msg.toString()}")
                runOnUiThread {
                    if (msg?.type == -1 && msg?.error == -1 && msg?.id.toInt() == -1) {
                        LogUtils.i(TAG, "录像存在日期列表为空")
                    } else {
                        showExistDateListDialog(msg!!.dateListString)
                    }
                }
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                LogUtils.i(TAG, "getExistRecordDateList onError,errorCode:$errorCode ; errorMsg:$errorMsg")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        tencentcs_gl_surface_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        tencentcs_gl_surface_view.onPause()
    }

    override fun onStop() {
        super.onStop()
        mPlaybackPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlaybackPlayer.release()
        thumbnailDownloader?.stop()
    }

    private fun initPlaybackPlayer() {
        mPlaybackPlayer.setVideoView(tencentcs_gl_surface_view)
        mPlaybackPlayer.setPreparedListener {
            LogUtils.i(TAG, "onPrepared")
//            playback_status.text = "开始准备"
        }
        mPlaybackPlayer.setStatusListener {
            LogUtils.i(TAG, "onStatus changed ${getPlayStatus(it)}")
            playback_status.text = getPlayStatus(it)
            if (!mPlaybackPlayer.isConnectedDevice) {
                mPlayStrategy = PlaybackPlayStrategy.PLAY_STRATEGY_ASCENDING
                tv_play_speed_hint.visibility = View.GONE
            }
        }
        mPlaybackPlayer.setTimeListener {
            LogUtils.d(TAG, "setTimeListener onTime:$it")
        }
        mPlaybackPlayer.setErrorListener {
            playback_status.text = "播放错误：$it"
        }
        mPlaybackPlayer.setUserDataListener {
//            playback_status.text = "收到数据：$data"
            LogUtils.i(TAG, "收到数据：$data")
        }

    }

    private fun getPlayStatus(status: Int): String {
        var playStatus = ""
        when (status) {
            PlayerStateEnum.STATE_IDLE -> playStatus = "未初始化"
            PlayerStateEnum.STATE_INITIALIZED -> playStatus = "已初始化"
            PlayerStateEnum.STATE_PREPARING -> playStatus = "准备中..."
            PlayerStateEnum.STATE_READY -> playStatus = "准备完成"
            PlayerStateEnum.STATE_LOADING -> playStatus = "加载中"
            PlayerStateEnum.STATE_PLAY -> playStatus = "播放中"
            PlayerStateEnum.STATE_PAUSE -> {
                playStatus = "暂停"
            }
            PlayerStateEnum.STATE_STOP -> playStatus = "停止播放"
            PlayerStateEnum.STATE_SEEKING -> playStatus = "快进中..."
        }
        return playStatus
    }

    private fun getPlaybackList(view: View, pageIndex: Int) {
        val startTime: Long;
        val endTime: Long;
        val recordType = ""

        selectCalendar.set(Calendar.HOUR_OF_DAY, 0)
        selectCalendar.set(Calendar.MINUTE, 0)
        selectCalendar.set(Calendar.SECOND, 0)
        selectCalendar.set(Calendar.MILLISECOND, 0)
        startTime = selectCalendar.timeInMillis

        selectCalendar.set(Calendar.HOUR_OF_DAY, 23)
        selectCalendar.set(Calendar.MINUTE, 59)
        selectCalendar.set(Calendar.SECOND, 59)
        selectCalendar.set(Calendar.MILLISECOND, 999)
        endTime = selectCalendar.timeInMillis

        LogUtils.i(TAG, "startTime:$startTime; endTime:$endTime")
        PlaybackPlayer.getPlaybackListV2(mDeviceId, startTime, endTime,
                pageIndex, 600, recordType, object : IResultListener<PlaybackMessage> {
            override fun onStart() {
                LogUtils.d(TAG, "请求中...")
                playback_status.text = "正在获取回放列表${pageIndex + 1}..."
            }

            override fun onSuccess(msg: PlaybackMessage?) {
                if (msg?.type == -1 && msg?.error == -1 && msg?.id.toInt() == -1) {
                    playback_status.text = "回放列表为空"
                    LogUtils.i(TAG, "回放列表为空")
                    return
                }
                mCurrentPageIndex = msg?.currentPage!!
                mPageCount = msg?.pageCount!!
                val logStr = "获取成功 : 当前页 ${mCurrentPageIndex + 1}, 总页数 $mPageCount"
                LogUtils.d(TAG, logStr)
                LogUtils.d(TAG, "获取成功 ${msg.toString()}")
                runOnUiThread {
                    playback_status.text = "获取回放列表成功"
                    data.clear()
                    msg.playbackList?.let {
                        data.addAll(it)
                        mAdapter.notifyDataSetChanged()
                    }
                    loadThumb(null, data)
                    Snackbar.make(view, logStr, Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                val logStr = "getPlaybackList error code $errorCode,  $errorMsg"
                runOnUiThread {
                    playback_status.text = "获取回放列表失败 $errorCode, $errorMsg"
                    LogUtils.d(TAG, logStr)
                    Snackbar.make(view, logStr, Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun getPlaybackListPrevious() {
        if (mPageCount == -1) {
            //未成功获取到回放列表，默认从第一页获取
            getPlaybackList(tv_get_playback_previous, 0)
            return
        } else if (mCurrentPageIndex == 0) {
            Snackbar.make(tv_get_playback_previous, "已是第一页", Snackbar.LENGTH_SHORT).show()
            return
        }
        getPlaybackList(tv_get_playback_previous, mCurrentPageIndex - 1)
    }

    private fun getPlaybackListNext() {
        if (mPageCount == -1) {
            //未成功获取到回放列表，默认从第一页获取
            getPlaybackList(tv_get_playback_next, 0)
            return
        } else if (mCurrentPageIndex == mPageCount - 1) {
            Snackbar.make(tv_get_playback_next, "已是最后页", Snackbar.LENGTH_SHORT).show()
            return
        }
        getPlaybackList(tv_get_playback_next, mCurrentPageIndex + 1)
    }

    private fun deviceRecord(on: Boolean) {
        val charset = Charsets.UTF_8
        val byteArray = (if (on) "record_start" else "record_stop").toByteArray(charset)
        MessageMgr.getInstance().sendDataToDeviceWithoutResponse(mDeviceId, byteArray, object : IResultListener<DataMessage> {
            override fun onStart() {
            }

            override fun onSuccess(msg: DataMessage?) {
                LogUtils.d(TAG, "deviceRecord $on")
                Snackbar.make(tv_start_record, (if (on) "打开录像成功" else "关闭录像成功"), Snackbar.LENGTH_LONG).show()
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                LogUtils.d(TAG, "deviceRecord $on error code $errorCode,  $errorMsg")
                Snackbar.make(tv_start_record, "deviceRecord $on error code $errorCode,  $errorMsg", Snackbar.LENGTH_LONG).show()
            }

        })
    }

    private fun recordVideoFromDevice() {
        if (!StorageManager.isVideoPathAvailable()) {
            Toast.makeText(this, "storage is not available", Toast.LENGTH_LONG).show()
            return
        }
        if (mPlaybackPlayer.isRecording) {
            record_btn.text = "录像"
            mPlaybackPlayer.stopRecord()
        } else {
            record_btn.text = "停止录像"
            val recordFile = File(StorageManager.getVideoPath() + File.separator + mDeviceId)
            if (!recordFile.exists() && !recordFile.mkdirs()) {
                LogUtils.e(TAG, "can not create file")
                return
            }
            mPlaybackPlayer.startRecord(recordFile.absolutePath, mSimpleDateFormat.format(Date()) + ".mp4", object : IRecordListener {
                override fun onResult(code: Int, path: String?) {
                    Toast.makeText(baseContext, "code:$code path:$path", Toast.LENGTH_LONG).show()
                    if (code != 0) {
                        record_btn.text = "录像"
                    }
                }

                override fun onStartRecord() {
                    TODO("Not yet implemented")
                }

                override fun onPositionUpdated(videoDuration: Long, audioDuration: Long) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun snap() {
        if (!StorageManager.isPicPathAvailable()) {
            Toast.makeText(this, "storage is not available", Toast.LENGTH_LONG).show()
            return
        }
        val snapFile = File(StorageManager.getPicPath() + File.separator + mDeviceId)
        if (!snapFile.exists() && !snapFile.mkdirs()) {
            LogUtils.e(TAG, "can not create file")
            return
        }
        mPlaybackPlayer.snapShot(snapFile.absolutePath + File.separator + mSimpleDateFormat.format(Date()) + ".jpeg",
                ISnapShotListener { code, path ->
                    Toast.makeText(this, "code:$code path:$path", Toast.LENGTH_LONG).show()
                })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == R.id.action_menu_select_more -> {
                showPopupMenu(playback_status)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.play_back_menu, menu)
        return true
    }

    private fun showDatePickerDialog(listener: OnDateSetListener) {
        val datePickerDialog = DatePickerDialog(this, listener,
                selectCalendar[Calendar.YEAR],
                selectCalendar[Calendar.MONTH],
                selectCalendar[Calendar.DAY_OF_MONTH])
        datePickerDialog.show()
    }

    private fun onCalendarClick() {
        Snackbar.make(tv_get_playback_next, "calendar click", Snackbar.LENGTH_SHORT).show()
        showDatePickerDialog(object : OnDateSetListener {
            override fun onDateSet(datePicker: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                var realMonth = monthOfYear + 1;
                var dateStr = "onCalendarClick:$year-$realMonth-$dayOfMonth";
                LogUtils.i(TAG, "onCalendarClick:$year-$monthOfYear-$dayOfMonth")

                selectCalendar.set(Calendar.YEAR, year)
                selectCalendar.set(Calendar.MONTH, monthOfYear)
                selectCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectCalendar.set(Calendar.HOUR_OF_DAY, 0)
                setSelectedDateText()
                getPlaybackList(tv_get_playback_previous, 0)
            }
        })
    }

    private fun setSelectedDateText() {
        var year = selectCalendar.get(Calendar.YEAR)
        var month = selectCalendar.get(Calendar.MONTH) + 1
        var day = selectCalendar.get(Calendar.DAY_OF_MONTH)
        playback_selected_calendar.setText("查询日期：$year/$month/$day")
    }

    private fun showExistDateListDialog(dateList: String) {
        AlertDialog.Builder(this)
                .setMessage(dateList)
                .setTitle("录像存在日期")
                .setPositiveButton("确定", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                .create()
                .show()
    }

    private fun showPopupMenu(anchor: View) {
        if (this == null) {
            return
        }
        val popupMenu = PopupMenu(this, anchor, Gravity.RIGHT)
        popupMenu.menuInflater.inflate(R.menu.playback_sub_menu, popupMenu.menu)
        if (com.tencentcs.iotvideodemo.utils.Utils.isOemVersion()) {
            popupMenu.menu.findItem(R.id.action_menu_share).isVisible = true
        }
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            if (this == null) {
                return@OnMenuItemClickListener false
            }
            when (item.itemId) {
                R.id.action_menu_date_select -> {
                    onCalendarClick();
                    true
                }
                R.id.action_menu_query_exist_date -> {
                    getExistRecordDateList();
                    true
                }
                R.id.action_menu_download_center -> {
                    goToDownloadFilePage()
                    true
                }
            }
            false
        })
        popupMenu.show()
    }

    private fun downloadPlaybackFile(fileInfo: PlaybackMessage.PlaybackNode) {
        Snackbar.make(tv_get_playback_next, "下载文件${fileInfo.startTime}", Snackbar.LENGTH_SHORT).show()
        PlaybackFileDownloadManager.getInstance().setDownloadItemStateListener(this)
        PlaybackFileDownloadManager.getInstance().addDownloadNote(mDeviceId, 0, fileInfo)

    }

    private fun goToDownloadFilePage() {
        startActivity(Intent(this, PlaybackDownloadActivity::class.java))
    }

    private fun setPlayStrategy(selectedIndex: Int) {
        if (selectedIndex == mPlayStrategy.toInt()) {
            Snackbar.make(sn_play_strategy, getString(R.string.same_play_strategy_with_last), Snackbar.LENGTH_LONG)
            LogUtils.i(TAG, "select same strategy with last")
            return
        }
        if (!mPlaybackPlayer.isConnectedDevice) {
            LogUtils.i(TAG, "select same strategy with last")
            Snackbar.make(sn_play_strategy, getString(R.string.set_strategy_error_player_disconnected), Snackbar.LENGTH_LONG)
            sn_play_strategy.setSelection(mPlayStrategy.toInt())
            return
        }
        mPlaybackPlayer.setPlaybackStrategy(selectedIndex.toByte(), object : IResultListener<Boolean> {
            override fun onStart() {

            }

            override fun onSuccess(msg: Boolean?) {
                mPlayStrategy = selectedIndex.toByte();
                Snackbar.make(sn_play_strategy, getString(R.string.setting_successfully), Snackbar.LENGTH_LONG)
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                LogUtils.e(TAG, "setPlaybackStrategy onError, errorCode:${errorCode}; errorMsg:${errorMsg}")
                Snackbar.make(sn_play_strategy, getString(R.string.setting_failure), Snackbar.LENGTH_LONG)
                sn_play_strategy.setSelection(mPlayStrategy.toInt())
            }

        })
    }

    override fun onItemStateChange(changeEntity: PlaybackDownloadFileEntity) {
        LogUtils.d(TAG, "download state:" + changeEntity.downloadState + ",text:" + getDownloadState(changeEntity));
        Toast.makeText(this, getDownloadState(changeEntity), Toast.LENGTH_LONG).show()
    }

    private fun getDownloadState(downloadFileEntity: PlaybackDownloadFileEntity): String {
        var state = "";
        when (downloadFileEntity.downloadState) {
            PlaybackFileDownloader.DOWNLOAD_STATE_DOWNLOADING -> {
                state = getString(R.string.download_state_downloading)
            }
            PlaybackFileDownloader.DOWNLOAD_STATE_PAUSE -> {
                state = getString(R.string.download_state_pause)
            }
            PlaybackFileDownloader.DOWNLOAD_STATE_FINISHED -> {
                state = getString(R.string.download_state_downloaded)
            }
            PlaybackFileDownloader.DOWNLOAD_STATE_ERROR -> {
                state = PlaybackFileDownloadManager.getInstance().getDownloadErrorHint(downloadFileEntity.downloadErrorCode)
            }
        }
        return state
    }

    /**
     * 加载文件缩率图，如果本地已经缓存过，则直接使用本地的图片；
     * 如果还未缓存则直接从设备端下载
     * */
    private fun loadThumb(clickView: ImageButton?, playNodes: List<PlaybackMessage.PlaybackNode>) {
        var fileStartTimes = ArrayList<Long>()
        for (playNode: PlaybackMessage.PlaybackNode in playNodes) {
            val thumbCachePath = thumbCacheDirect + File.separator + mDeviceId + File.separator + playNode.startTime + ".png"
            var thumbFile = File(thumbCachePath)
            if (thumbFile.exists()) {
                continue
            }
            fileStartTimes.add(playNode.startTime)
        }
        if (fileStartTimes.isNullOrEmpty()) {
            return
        }

        thumbnailDownloader.downloadThumbnails(fileStartTimes, object : IThumbnailDownloadListener {
            override fun onFinished() {
                Toast.makeText(this@PlaybackPlayerActivity, "下载完成", Toast.LENGTH_SHORT).show()
                mAdapter.notifyDataSetChanged()
            }

            override fun onProgress(fileStartTime: Long, thumbnailData: ByteArray?) {
                val thumbDirect = thumbCacheDirect + File.separator + mDeviceId;
                val thumbSavePath = "$thumbDirect${File.separator}$fileStartTime.png"
                var directFile = File(thumbDirect)
                if (!directFile.exists()) {
                    directFile.mkdirs()
                }
                val thumbCachePath = thumbCacheDirect + File.separator + mDeviceId + File.separator + fileStartTime + ".png"
                var pictureFile = File(thumbCachePath)
                if (!pictureFile.exists()) {
                    pictureFile.createNewFile()
                }
                FileIOUtils.writeFileFromBytesByChannel(pictureFile, thumbnailData, true)

                clickView?.setImageURI(Uri.parse(thumbSavePath))
            }

            override fun onCanceled() {
                LogUtils.d(TAG, "onCanceled")
                mAdapter.notifyDataSetChanged()
                Toast.makeText(this@PlaybackPlayerActivity, "下载被取消", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: Int, errorReason: String?) {
                mAdapter.notifyDataSetChanged()
                Toast.makeText(this@PlaybackPlayerActivity, "下载发生异常，error:$error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCachePicture(fileStartTime: Long, imgView: ImageButton) {
        val thumbCachePath = thumbCacheDirect + File.separator + mDeviceId + File.separator + fileStartTime + ".png"
        var thumbFile = File(thumbCachePath)
        if (thumbFile.exists()) {
            imgView.setImageURI(Uri.fromFile(thumbFile))
        } else {
            imgView.setImageResource(R.drawable.ic_iot_refresh)
        }
    }
}
