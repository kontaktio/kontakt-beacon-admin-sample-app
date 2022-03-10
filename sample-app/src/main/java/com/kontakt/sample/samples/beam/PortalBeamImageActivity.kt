package com.kontakt.sample.samples.beam

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kontakt.sample.R
import com.kontakt.sample.samples.common.Scanner
import com.kontakt.sample.samples.common.util.ColorMapUtils
import com.kontakt.sdk.android.ble.image_streaming.RecognitionBox
import com.kontakt.sdk.android.cloud.KontaktCloudFactory
import kotlinx.android.synthetic.main.activity_portal_beam_image.*
import kotlin.math.max

class PortalBeamImageActivity : AppCompatActivity() {

	private val kontaktCloud = KontaktCloudFactory.create()

	private lateinit var imageFetcher: ImageFetcher

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_portal_beam_image)
		imageFetcher = ImageFetcher(kontaktCloud, Scanner(this, kontaktCloud), this)
		setButtonOnStartStreamingClickedState()
	}

	private fun showInputUniqueIdDialog() {
		val input = EditText(this).apply { inputType =   InputType.TYPE_CLASS_TEXT }
		AlertDialog.Builder(this)
				.setTitle("Beam's uniqueId")
				.setView(input)
				.setPositiveButton("Ok") { _, _ -> startStreaming(input.text.toString()) }
				.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
				.show()
	}

	private fun startStreaming(uniqueId: String) {
		setButtonOnStopStreamingClickedState()
		imageFetcher.init(uniqueId, object: ImageFetcher.Listener {
			override fun onImage(pixels: Array<IntArray>, boxes: List<RecognitionBox>) {
				runOnUiThread {
					displayImagePixels(pixelsToBitmap(pixels))
					setButtonOnStartStreamingClickedState()
				}
			}

			override fun onError(errorMsg: String) {
				runOnUiThread {
					Log.e(TAG, "Error fetching image: $errorMsg")
					Toast.makeText(this@PortalBeamImageActivity, "Error fetching image", Toast.LENGTH_LONG).show()
					setButtonOnStartStreamingClickedState()
					imageFetcher.stopStreaming()
				}
			}

		})
		imageFetcher.startStreaming()
	}



	private fun displayImagePixels(bitmap: Bitmap){
		val width = resources.getDimension(R.dimen.beam_image_width).toInt()
		val height = resources.getDimension(R.dimen.beam_image_height).toInt()
		beamImagesView.setImageBitmap(
				createScaledBitmap(
						bitmap, width, height
				)
		)
	}

	private fun pixelsToBitmap(pixels: Array<IntArray>): Bitmap {
		val min = findMin(pixels).toFloat()
		val max = findMax(pixels).toFloat()
		val height = pixels.size
		val width = pixels[0].size
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		for(y in 0 until height){
			for(x in 0 until width) {
				val lutIndex = ((pixels[y][x] - min) / (max - min) * 255).toInt()
				val lutRow = ColorMapUtils.COOLWARM_COLOR_MAP[lutIndex]
				val red = lutRow[0].toInt()
				val green = lutRow[1].toInt()
				val blue = lutRow[2].toInt()
				val color = ((0xff) shl 24) or ((red and 0xff) shl 16) or ((green and 0xff) shl 8) or (blue and 0xff)
				bitmap.setPixel(x, y, color)
			}
		}
		return bitmap
	}

	private fun findMin(arr: Array<IntArray>): Int {
		val height = arr.size
		val width = arr[0].size
		var min = Int.MAX_VALUE
		for(i in 0 until height) {
			for(j in 0 until width){
				if(arr[i][j] < min) {
					min = arr[i][j]
				}
			}
		}
		return min
	}

	private fun findMax(arr: Array<IntArray>): Int {
		val height = arr.size
		val width = arr[0].size
		var max = Int.MIN_VALUE
		for(i in 0 until height) {
			for(j in 0 until width){
				if(arr[i][j] > max) {
					max = arr[i][j]
				}
			}
		}
		return max
	}

	private fun createScaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
		val ratio = max(
				bitmap.width.toDouble() / width,
				bitmap.height.toDouble() / height)
		val scaledWidth = (bitmap.width / ratio).toInt()
		val scaledHeight = (bitmap.height / ratio).toInt()
		val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
		val background = Bitmap.createBitmap(scaledWidth, scaledHeight, scaledBitmap.config)
		background.eraseColor(Color.WHITE)
		val canvas = Canvas(background)
		canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
		scaledBitmap.recycle()
		return background
	}

	private fun setButtonOnStopStreamingClickedState() {
		streamingButton.text = "stop streaming"
		streamingButton.setOnClickListener { setButtonOnStartStreamingClickedState() }
	}

	private fun setButtonOnStartStreamingClickedState() {
		streamingButton.text = "start streaming"
		streamingButton.setOnClickListener { showInputUniqueIdDialog() }
	}

	override fun onDestroy() {
		imageFetcher.onDestroy()
		super.onDestroy()
	}

	companion object {

		@JvmStatic
		fun createIntent(context: Context): Intent {
			return Intent(context, PortalBeamImageActivity::class.java)
		}

		val TAG = PortalBeamImageActivity::javaClass.name
	}
}