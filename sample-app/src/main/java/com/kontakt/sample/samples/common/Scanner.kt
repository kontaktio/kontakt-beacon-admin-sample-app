package com.kontakt.sample.samples.common


import android.content.Context
import com.kontakt.sdk.android.ble.configuration.ScanMode
import com.kontakt.sdk.android.ble.configuration.ScanPeriod
import com.kontakt.sdk.android.ble.manager.ProximityManager
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSecureProfileListener
import com.kontakt.sdk.android.cloud.KontaktCloud
import com.kontakt.sdk.android.common.model.Model
import com.kontakt.sdk.android.common.profile.ISecureProfile
import java.util.concurrent.TimeUnit

class Scanner (context: Context, kontaktCloud: KontaktCloud){

	private lateinit var uniqueId: String

	private var secureProfileConsumer: ((ISecureProfile) -> Unit)? = null

	private var proximityManagerDelegate: ProximityManager? =
			ProximityManagerFactory.create(context, kontaktCloud).apply {

				configuration()
						.deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(1))
						.scanMode(ScanMode.LOW_LATENCY)
						.scanPeriod(ScanPeriod.RANGING)

				setSecureProfileListener(
						object: SimpleSecureProfileListener() {
							override fun onProfileDiscovered(profile: ISecureProfile) {
								onSecureProfile(profile)
							}
							override fun onProfilesUpdated(profiles: List<ISecureProfile>) {
								for(secureProfile in profiles) {
									onSecureProfile(secureProfile)
								}
							}
						}
				)
			}


	private fun onSecureProfile(profile: ISecureProfile) {
		if (profile.model == Model.UNKNOWN) return
		if (uniqueId != profile.uniqueId) return
		secureProfileConsumer?.invoke(profile)
	}

	@Synchronized
	fun startScan(uniqueId: String, secureProfileConsumer: (ISecureProfile) -> Unit) {
		this.secureProfileConsumer = secureProfileConsumer
		this.uniqueId = uniqueId
		proximityManagerDelegate?.connect { proximityManagerDelegate?.startScanning() }
	}

	@Synchronized
	fun stopScan() {
		proximityManagerDelegate?.apply {
			if(isScanning) {
				stopScanning()
			}
		}
		secureProfileConsumer = null
	}

	@Synchronized
	fun onDestroy() {
		proximityManagerDelegate?.apply {
			if(isScanning) {
				disconnect()
			}
		}
		secureProfileConsumer = null
		proximityManagerDelegate = null
	}

}
