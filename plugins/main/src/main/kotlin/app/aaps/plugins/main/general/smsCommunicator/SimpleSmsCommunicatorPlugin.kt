package app.aaps.plugins.main.general.smsCommunicator

import android.content.Context
import android.telephony.SmsManager
import app.aaps.core.data.plugin.PluginType
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.plugin.PluginDescription
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventPreferenceChange
import app.aaps.core.interfaces.smsCommunicator.Sms
import app.aaps.core.interfaces.utils.fabric.FabricPrivacy
import app.aaps.plugins.main.R
import app.aaps.plugins.main.general.smsCommunicator.events.EventSmsCommunicatorUpdateGui
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleSmsCommunicatorPlugin @Inject constructor(
    aapsLogger: AAPSLogger,
    rh: ResourceHelper,
    smsManager: SmsManager?,
    aapsSchedulers: AapsSchedulers,
    rxBus: RxBus,
    fabricPrivacy: FabricPrivacy
) : BaseSmsCommunicatorPlugin(
    PluginDescription()
        .mainType(PluginType.GENERAL)
        .fragmentClass(SimpleSmsCommunicatorFragment::class.java.name) // TODO can both sms communicators share the same fragment?
        .pluginIcon(app.aaps.core.objects.R.drawable.ic_sms)
        .pluginName(R.string.smscommunicator_simple)
        .shortName(R.string.smscommunicator_simple_shortname)
        .preferencesId(PluginDescription.PREFERENCE_SCREEN)
        .description(R.string.description_sms_communicator_simple),
    aapsLogger, rh, smsManager, aapsSchedulers, rxBus, fabricPrivacy
) {

    override fun processSettings(ev: EventPreferenceChange?) {
        // Specific settings processing for SimpleSmsCommunicatorPlugin
        // TODO get number for aapscclient receiver
    }

    override fun sendNotificationToAllNumbers(text: String): Boolean {
        // TODO: remove?
        return false
    }

    override fun processSms(receivedSms: Sms) {
        if (!isEnabled()) return
        messages.add(receivedSms)
        aapsLogger.debug(LTag.SMS, receivedSms.toString())
        rxBus.send(EventSmsCommunicatorUpdateGui())
    }
}