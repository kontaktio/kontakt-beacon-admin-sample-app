package com.kontakt.sample.samples.beam

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kontakt.sample.samples.common.Scanner
import com.kontakt.sample.samples.common.connection.Connection
import com.kontakt.sdk.android.ble.image_streaming.ImageMetadata
import com.kontakt.sdk.android.ble.image_streaming.ImageStreamingListener
import com.kontakt.sdk.android.ble.image_streaming.RecognitionBox
import com.kontakt.sdk.android.ble.image_streaming.event.StreamingEvent
import com.kontakt.sdk.android.ble.util.BluetoothUtils
import com.kontakt.sdk.android.cloud.KontaktCloud
import com.kontakt.sdk.android.common.log.Logger
import com.kontakt.sdk.android.common.util.SecureProfileUtils
import java.util.concurrent.TimeUnit

class ImageFetcher(private val kontaktCloud: KontaktCloud, private val deviceDetailsScanner: Scanner, private val context: Context) {

	private var deviceConnection: Connection? = null

	private lateinit var uniqueId: String
	private lateinit var listener: Listener

	private val scanTimeoutHandler = Handler(Looper.getMainLooper())

	fun init(uniqueId: String, listener: Listener) {
		this.uniqueId = uniqueId
		this.listener = listener
	}

	fun startStreaming(){
		if (!BluetoothUtils.isBluetoothEnabled()) {
			listener.onError("You should have your Bluetooth turned on before proceeding")
			return
		}
		scanTimeoutHandler.postDelayed(onScanTimeoutRunnable, TimeUnit.SECONDS.toMillis(15))
		deviceDetailsScanner.startScan(uniqueId) { profile ->
			stopScanAndConnection()
			deviceConnection = Connection(
					context,
					this@ImageFetcher::onConnectionEstablishingTimeout
			).also { it.startImageStreaming(SecureProfileUtils.asRemoteBluetoothDevice(profile), kontaktCloud, imageStreamingListener) }
		}
	}

	private val onScanTimeoutRunnable: Runnable = Runnable {
		stopScan()
		listener.onError("Timeout while scanning for your Portal Beam - is it nearby you?")
	}

	private fun onConnectionEstablishingTimeout(){
		stopScanAndConnection()
		listener.onError("Timeout establishing connection")
	}

	private val imageStreamingListener = object: ImageStreamingListener {

		override fun onImage(pixels: Array<IntArray>, imageMetadata: ImageMetadata) {
			if(isNonZeroMatrix(pixels)){
				listener.onImage(pixels, imageMetadata.recognitionBoxes)
				stopStreaming()
			} else {
				Log.d(TAG, "Parsed image is a zero matrix, ignoring")
			}
		}

		override fun onEvent(event: StreamingEvent) {
			Log.d(TAG, "Started streaming")
		}

		override fun onError(message: String) {
			listener.onError(message)
		}
	}

	private fun isNonZeroMatrix(pixels: Array<IntArray>): Boolean {
		return pixels.any { row -> row.any { pixel -> pixel != 0 } }
	}

	fun stopStreaming() {
		Logger.enableAllLoggerLevels(false)
		stopScanAndConnection()
	}

	fun onDestroy(){
		deviceDetailsScanner.onDestroy()
		scanTimeoutHandler.removeCallbacksAndMessages(null)
		deviceConnection?.onDestroy()
	}

	private fun stopScanAndConnection() {
		stopScan()
		stopConnection()
	}

	private fun stopScan() {
		scanTimeoutHandler.removeCallbacksAndMessages(null)
		deviceDetailsScanner.stopScan()
	}

	private fun stopConnection(){
		deviceConnection?.closeConnection()
	}

	companion object {
		private val TAG = ImageFetcher::class.java.simpleName
	}

	interface Listener {
		fun onImage(pixels: Array<IntArray>, boxes: List<RecognitionBox>)
		fun onError(errorMsg: String)
	}
}
