package app.aaps.plugins.main.general.smsCommunicator

import android.telephony.SmsManager
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.notifications.Notification
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginDescription
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventNewNotification
import app.aaps.core.interfaces.rx.events.EventPreferenceChange
import app.aaps.core.interfaces.smsCommunicator.Sms
import app.aaps.core.interfaces.smsCommunicator.SmsCommunicator
import app.aaps.core.interfaces.utils.fabric.FabricPrivacy
import app.aaps.plugins.main.R
import app.aaps.plugins.main.general.smsCommunicator.events.EventSmsCommunicatorUpdateGui
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.text.Normalizer

abstract class BaseSmsCommunicatorPlugin(
    pluginDescription: PluginDescription,
    aapsLogger: AAPSLogger,
    rh: ResourceHelper,
    protected val smsManager: SmsManager?,
    protected val aapsSchedulers: AapsSchedulers,
    protected val rxBus: RxBus,
    protected val fabricPrivacy: FabricPrivacy
) : PluginBase(pluginDescription, aapsLogger, rh), SmsCommunicator {

    protected val disposable = CompositeDisposable()
    override var messages = ArrayList<Sms>()

    override fun onStart() {
        processSettings(null)
        super.onStart()
        disposable += rxBus
            .toObservable(EventPreferenceChange::class.java)
            .observeOn(aapsSchedulers.io)
            .subscribe({ event: EventPreferenceChange? -> processSettings(event) }, fabricPrivacy::logException)
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    protected open fun processSettings(ev: EventPreferenceChange?) {
        // Common settings processing
    }

    override fun sendSMS(sms: Sms): Boolean {
        sms.text = stripAccents(sms.text)

        try {
            aapsLogger.debug(LTag.SMS, "Sending SMS to " + sms.phoneNumber + ": " + sms.text)
            if (sms.text.toByteArray().size <= 140) smsManager?.sendTextMessage(sms.phoneNumber, null, sms.text, null, null)
            else {
                val parts = smsManager?.divideMessage(sms.text)
                smsManager?.sendMultipartTextMessage(
                    sms.phoneNumber, null, parts,
                    null, null
                )
            }
            messages.add(sms)
        } catch (e: IllegalArgumentException) {
            return if (e.message == "Invalid message body") {
                val notification = Notification(Notification.INVALID_MESSAGE_BODY, rh.gs(R.string.smscommunicator_message_body), Notification.NORMAL)
                rxBus.send(EventNewNotification(notification))
                false
            } else {
                val notification = Notification(Notification.INVALID_PHONE_NUMBER, rh.gs(R.string.smscommunicator_invalid_phone_number), Notification.NORMAL)
                rxBus.send(EventNewNotification(notification))
                false
            }
        } catch (_: SecurityException) {
            val notification = Notification(Notification.MISSING_SMS_PERMISSION, rh.gs(app.aaps.core.ui.R.string.smscommunicator_missingsmspermission), Notification.NORMAL)
            rxBus.send(EventNewNotification(notification))
            return false
        }
        rxBus.send(EventSmsCommunicatorUpdateGui())
        return true
    }

    private fun stripAccents(str: String): String {
        var s = str
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
        s = s.replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
        s = s.replace("ł", "l") // hack for Polish language (bug in libs)
        return s
    }

    override fun getLatestMsg(phoneNumber: String): Sms? {
        return messages.lastOrNull { it.phoneNumber == phoneNumber }
    }
}