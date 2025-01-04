package app.aaps.plugins.main.di

import app.aaps.plugins.main.general.smsCommunicator.AuthRequest
import app.aaps.plugins.main.general.smsCommunicator.BaseSmsCommunicatorPlugin
import app.aaps.plugins.main.general.smsCommunicator.SimpleSmsCommunicatorFragment
import app.aaps.plugins.main.general.smsCommunicator.SmsCommunicatorFragment
import app.aaps.plugins.main.general.smsCommunicator.activities.SmsCommunicatorOtpActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("unused")
abstract class SMSCommunicatorModule {

    @ContributesAndroidInjector abstract fun authRequestInjector(): AuthRequest
    @ContributesAndroidInjector abstract fun contributesSmsCommunicatorOtpActivity(): SmsCommunicatorOtpActivity
    @ContributesAndroidInjector abstract fun contributesSimpleSmsCommunicatorFragment(): SimpleSmsCommunicatorFragment
    @ContributesAndroidInjector abstract fun contributesSmsCommunicatorFragment(): SmsCommunicatorFragment
    @ContributesAndroidInjector abstract fun contributesSmsCommunicatorWorker(): BaseSmsCommunicatorPlugin.SmsCommunicatorWorker
}