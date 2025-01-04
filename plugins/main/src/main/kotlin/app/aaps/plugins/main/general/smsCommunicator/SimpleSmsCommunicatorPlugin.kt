package app.aaps.plugins.main.general.smsCommunicator

import android.content.Context
import android.telephony.SmsManager
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
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
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.StringKey
import app.aaps.core.validators.DefaultEditTextValidator
import app.aaps.core.validators.EditTextValidator
import app.aaps.core.validators.preferences.AdaptiveStringPreference
import app.aaps.core.validators.preferences.AdaptiveSwitchPreference
import app.aaps.plugins.main.R
import app.aaps.plugins.main.general.smsCommunicator.events.EventSmsCommunicatorUpdateGui
import dagger.android.HasAndroidInjector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleSmsCommunicatorPlugin @Inject constructor(
    private val injector: HasAndroidInjector, // TODO: share? remove?
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
        .alwaysVisible(true) // TODO remove it
        .alwaysEnabled(true) // TODO remove it
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

    override fun addPreferenceScreen(preferenceManager: PreferenceManager, parent: PreferenceScreen, context: Context, requiredKey: String?) {
        // if (requiredKey != null) return // TODO what's that? Why is it used in original plugin, it's null
        val category = PreferenceCategory(context)
        parent.addPreference(category)
        category.apply {
            key = "smscommunicator_settings"
            title = rh.gs(R.string.smscommunicator)
            initialExpandedChildrenCount = 0
            // addPreference(AdaptiveIntPreference(ctx = context, intKey = IntKey.SmsRemoteBolusDistance, summary = R.string.smscommunicator_remote_bolus_min_distance_summary, title = R.string.smscommunicator_remote_bolus_min_distance))
            addPreference(AdaptiveSwitchPreference(ctx = context, booleanKey = BooleanKey.SmsAllowRemoteCommands, summary = R.string.client_allow_sms_warning, title = R.string.client_allow_sms))
            addPreference(
                AdaptiveStringPreference(
                    ctx = context, stringKey = StringKey.SmsReceiverNumber, dialogMessage = R.string.sms_receiver_number_dialog, title = app.aaps.core.ui.R.string.sms_receiver_number,
                    validatorParams = DefaultEditTextValidator.Parameters(testType = EditTextValidator.TEST_PHONE)
                )
            )

        }
    }

    override fun processSms(receivedSms: Sms) {
        if (!isEnabled()) return
        messages.add(receivedSms)
        aapsLogger.debug(LTag.SMS, receivedSms.toString())
        rxBus.send(EventSmsCommunicatorUpdateGui())
    }

}