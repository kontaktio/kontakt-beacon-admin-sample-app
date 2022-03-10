package com.kontakt.sample.samples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kontakt.sample.R
import com.kontakt.sdk.android.cloud.KontaktCloudFactory
import com.kontakt.sdk.android.common.model.DeviceType
import kotlinx.android.synthetic.main.activity_kontakt_cloud_with_coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class KontaktCloudWithCoroutinesActivity : AppCompatActivity(), View.OnClickListener {

	private val kontaktCloud = KontaktCloudFactory.create()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_kontakt_cloud_with_coroutines)
		setupToolbar()
		setupButtons()
	}

	private fun setupToolbar() {
		val supportActionBar = supportActionBar
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	private fun setupButtons() {
		getDevicesButton.setOnClickListener(this)
		getConfigsButton.setOnClickListener(this)
		getManagersButton.setOnClickListener(this)
	}

	private suspend fun fetchDevices() {
		onRequestInProgress()
		// Request list of all devices. Max results are set to 50 by default. If there are more than 50 devices on your account results will be paginated.
		// Note that you do not need coroutineScope and withContext() builders for this example - the executeSuspending invocation is main-safe already
		try {
			val response = kontaktCloud.devices().fetch().executeSuspending()
			enableButtons()
			progressBar.visibility = View.GONE
			if (response != null && response.content != null) {
				//Do something with your devices list
				statusText.text = "Devices fetched:\n\n"
				for (device in response.content) {
					statusText.append(
						String.format(
							"ID: %s,  Model: %s\n",
							device.uniqueId,
							device.model.toString()
						)
					)
				}
			}
		} catch (e: CancellationException) {
			throw e
		} catch(error: Exception) {
			onRequestError(error)
		}
	}

	private suspend fun fetchConfigs() {
		onRequestInProgress()
		// Fetch list of all pending configurations.
		// Note that you do not need coroutineScope and withContext() builders for this example - the executeSuspending invocation is main-safe already
		try {
			val response = kontaktCloud.configs().fetch().type(DeviceType.BEACON).executeSuspending()
			enableButtons()
			progressBar.visibility = View.GONE
			if (response != null && response.content != null) {
				//Do something with your configs list
				statusText.text = String.format(
					"Configurations fetched! There are %d pending configurations.",
					response.content.size
				)
			}
		} catch (e: CancellationException) {
			throw e
		} catch(error: Exception) {
			onRequestError(error)
		}
	}

	private suspend fun fetchManagers() {
		onRequestInProgress()
		//Fetch list of all account's managers.
		// Note that you do not need coroutineScope and withContext() builders for this example - the executeSuspending invocation is main-safe already
		try {
			val response = kontaktCloud.managers().fetch().executeSuspending()
			enableButtons()
			progressBar.visibility = View.GONE
			if (response != null && response.content != null) {
				statusText.text = "Managers fetched:\n\n"
				for (manager in response.content) {
					statusText.append(
						String.format(
							"Name: %s, Surname: %s, Role: %s\n",
							manager.firstName,
							manager.lastName,
							manager.role.toString()
						)
					)
				}
			}
		} catch (e: CancellationException) {
			throw e
		} catch(error: Exception) {
			onRequestError(error)
		}
	}

	private fun onRequestInProgress() {
		disableButtons()
		progressBar.visibility = View.VISIBLE
		statusText.text = ""
	}

	private fun onRequestError(error: Exception) {
		enableButtons()
		progressBar.visibility = View.GONE
		Toast.makeText(this, "Error: " + error.message, Toast.LENGTH_SHORT).show()
	}

	private fun enableButtons() {
		getDevicesButton.isEnabled = true
		getConfigsButton.isEnabled = true
		getManagersButton.isEnabled = true
	}

	private fun disableButtons() {
		getDevicesButton.isEnabled = false
		getConfigsButton.isEnabled = false
		getManagersButton.isEnabled = false
	}

	override fun onClick(view: View) {
		when (view.id) {
			R.id.getDevicesButton -> lifecycleScope.launch { fetchDevices() }
			R.id.getConfigsButton -> lifecycleScope.launch { fetchConfigs() }
			R.id.getManagersButton -> lifecycleScope.launch { fetchManagers() }
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			android.R.id.home -> {
				onBackPressed()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	companion object {

		@JvmStatic
		fun createIntent(context: Context): Intent {
			return Intent(context, KontaktCloudWithCoroutinesActivity::class.java)
		}

	}
}
