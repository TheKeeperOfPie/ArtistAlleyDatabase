package android.media

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
import android.os.Handler
import android.view.KeyEvent
import java.util.concurrent.Executor

/**
 * Prevents anything from actually taking audio focus by dropping requests and automatically
 * returning [AUDIOFOCUS_REQUEST_GRANTED]. This prevents the app from overriding the user's
 * actively playing audio.
 */
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@SuppressLint("MissingPermission", "NewApi")
class AudioManagerIgnoreFocus(private val audioManager: AudioManager) : AudioManager() {

    override fun requestAudioFocus(
        l: OnAudioFocusChangeListener?,
        streamType: Int,
        durationHint: Int
    ) = AUDIOFOCUS_REQUEST_GRANTED

    override fun requestAudioFocus(focusRequest: AudioFocusRequest) = AUDIOFOCUS_REQUEST_GRANTED

    override fun dispatchMediaKeyEvent(keyEvent: KeyEvent?) {
        audioManager.dispatchMediaKeyEvent(keyEvent)
    }

    override fun isVolumeFixed(): Boolean {
        return audioManager.isVolumeFixed
    }

    override fun adjustStreamVolume(streamType: Int, direction: Int, flags: Int) {
        audioManager.adjustStreamVolume(streamType, direction, flags)
    }

    override fun adjustVolume(direction: Int, flags: Int) {
        audioManager.adjustVolume(direction, flags)
    }

    override fun adjustSuggestedStreamVolume(direction: Int, suggestedStreamType: Int, flags: Int) {
        audioManager.adjustSuggestedStreamVolume(direction, suggestedStreamType, flags)
    }

    override fun getRingerMode(): Int {
        return audioManager.ringerMode
    }

    override fun isRampingRingerEnabled(): Boolean {
        return audioManager.isRampingRingerEnabled
    }

    override fun getStreamMaxVolume(streamType: Int): Int {
        return audioManager.getStreamMaxVolume(streamType)
    }

    override fun getStreamMinVolume(streamType: Int): Int {
        return audioManager.getStreamMinVolume(streamType)
    }

    override fun getStreamVolume(streamType: Int): Int {
        return audioManager.getStreamVolume(streamType)
    }

    override fun getStreamVolumeDb(streamType: Int, index: Int, deviceType: Int): Float {
        return audioManager.getStreamVolumeDb(streamType, index, deviceType)
    }

    override fun setRingerMode(ringerMode: Int) {
        audioManager.ringerMode = ringerMode
    }

    override fun setStreamVolume(streamType: Int, index: Int, flags: Int) {
        audioManager.setStreamVolume(streamType, index, flags)
    }

    override fun getVolumeGroupIdForAttributes(attributes: AudioAttributes): Int {
        return audioManager.getVolumeGroupIdForAttributes(attributes)
    }

    override fun adjustVolumeGroupVolume(groupId: Int, direction: Int, flags: Int) {
        audioManager.adjustVolumeGroupVolume(groupId, direction, flags)
    }

    override fun isVolumeGroupMuted(groupId: Int): Boolean {
        return audioManager.isVolumeGroupMuted(groupId)
    }

    override fun setStreamSolo(streamType: Int, state: Boolean) {
        audioManager.setStreamSolo(streamType, state)
    }

    override fun setStreamMute(streamType: Int, state: Boolean) {
        audioManager.setStreamMute(streamType, state)
    }

    override fun isStreamMute(streamType: Int): Boolean {
        return audioManager.isStreamMute(streamType)
    }

    override fun shouldVibrate(vibrateType: Int): Boolean {
        return audioManager.shouldVibrate(vibrateType)
    }

    override fun getVibrateSetting(vibrateType: Int): Int {
        return audioManager.getVibrateSetting(vibrateType)
    }

    override fun setVibrateSetting(vibrateType: Int, vibrateSetting: Int) {
        audioManager.setVibrateSetting(vibrateType, vibrateSetting)
    }

    override fun setSpeakerphoneOn(on: Boolean) {
        audioManager.isSpeakerphoneOn = on
    }

    override fun isSpeakerphoneOn(): Boolean {
        return audioManager.isSpeakerphoneOn
    }

    override fun setAllowedCapturePolicy(capturePolicy: Int) {
        audioManager.allowedCapturePolicy = capturePolicy
    }

    override fun getAllowedCapturePolicy(): Int {
        return audioManager.allowedCapturePolicy
    }

    override fun getSpatializer(): Spatializer {
        return audioManager.spatializer
    }

    override fun isBluetoothScoAvailableOffCall(): Boolean {
        return audioManager.isBluetoothScoAvailableOffCall
    }

    override fun startBluetoothSco() {
        audioManager.startBluetoothSco()
    }

    override fun stopBluetoothSco() {
        audioManager.stopBluetoothSco()
    }

    override fun setBluetoothScoOn(on: Boolean) {
        audioManager.isBluetoothScoOn = on
    }

    override fun isBluetoothScoOn(): Boolean {
        return audioManager.isBluetoothScoOn
    }

    override fun setBluetoothA2dpOn(on: Boolean) {
        audioManager.isBluetoothA2dpOn = on
    }

    override fun isBluetoothA2dpOn(): Boolean {
        return audioManager.isBluetoothA2dpOn
    }

    override fun setWiredHeadsetOn(on: Boolean) {
        audioManager.isWiredHeadsetOn = on
    }

    override fun isWiredHeadsetOn(): Boolean {
        return audioManager.isWiredHeadsetOn
    }

    override fun setMicrophoneMute(on: Boolean) {
        audioManager.isMicrophoneMute = on
    }

    override fun isMicrophoneMute(): Boolean {
        return audioManager.isMicrophoneMute
    }

    override fun setMode(mode: Int) {
        audioManager.mode = mode
    }

    override fun getMode(): Int {
        return audioManager.mode
    }

    override fun addOnModeChangedListener(executor: Executor, listener: OnModeChangedListener) {
        audioManager.addOnModeChangedListener(executor, listener)
    }

    override fun removeOnModeChangedListener(listener: OnModeChangedListener) {
        audioManager.removeOnModeChangedListener(listener)
    }

    override fun isCallScreeningModeSupported(): Boolean {
        return audioManager.isCallScreeningModeSupported
    }

    override fun setRouting(mode: Int, routes: Int, mask: Int) {
        audioManager.setRouting(mode, routes, mask)
    }

    override fun getRouting(mode: Int): Int {
        return audioManager.getRouting(mode)
    }

    override fun isMusicActive(): Boolean {
        return audioManager.isMusicActive
    }

    override fun generateAudioSessionId(): Int {
        return audioManager.generateAudioSessionId()
    }

    override fun setParameters(keyValuePairs: String?) {
        audioManager.setParameters(keyValuePairs)
    }

    override fun getParameters(keys: String?): String {
        return audioManager.getParameters(keys)
    }

    override fun playSoundEffect(effectType: Int) {
        audioManager.playSoundEffect(effectType)
    }

    override fun playSoundEffect(effectType: Int, volume: Float) {
        audioManager.playSoundEffect(effectType, volume)
    }

    override fun loadSoundEffects() {
        audioManager.loadSoundEffects()
    }

    override fun unloadSoundEffects() {
        audioManager.unloadSoundEffects()
    }

    override fun abandonAudioFocusRequest(focusRequest: AudioFocusRequest): Int {
        return audioManager.abandonAudioFocusRequest(focusRequest)
    }

    override fun abandonAudioFocus(l: OnAudioFocusChangeListener?): Int {
        return audioManager.abandonAudioFocus(l)
    }

    override fun registerMediaButtonEventReceiver(eventReceiver: ComponentName?) {
        audioManager.registerMediaButtonEventReceiver(eventReceiver)
    }

    override fun registerMediaButtonEventReceiver(eventReceiver: PendingIntent?) {
        audioManager.registerMediaButtonEventReceiver(eventReceiver)
    }

    override fun unregisterMediaButtonEventReceiver(eventReceiver: ComponentName?) {
        audioManager.unregisterMediaButtonEventReceiver(eventReceiver)
    }

    override fun unregisterMediaButtonEventReceiver(eventReceiver: PendingIntent?) {
        audioManager.unregisterMediaButtonEventReceiver(eventReceiver)
    }

    override fun registerRemoteControlClient(rcClient: RemoteControlClient?) {
        audioManager.registerRemoteControlClient(rcClient)
    }

    override fun unregisterRemoteControlClient(rcClient: RemoteControlClient?) {
        audioManager.unregisterRemoteControlClient(rcClient)
    }

    override fun registerRemoteController(rctlr: RemoteController?): Boolean {
        return audioManager.registerRemoteController(rctlr)
    }

    override fun unregisterRemoteController(rctlr: RemoteController?) {
        audioManager.unregisterRemoteController(rctlr)
    }

    override fun registerAudioPlaybackCallback(cb: AudioPlaybackCallback, handler: Handler?) {
        audioManager.registerAudioPlaybackCallback(cb, handler)
    }

    override fun unregisterAudioPlaybackCallback(cb: AudioPlaybackCallback) {
        audioManager.unregisterAudioPlaybackCallback(cb)
    }

    override fun getActivePlaybackConfigurations(): MutableList<AudioPlaybackConfiguration> {
        return audioManager.activePlaybackConfigurations
    }

    override fun registerAudioRecordingCallback(cb: AudioRecordingCallback, handler: Handler?) {
        audioManager.registerAudioRecordingCallback(cb, handler)
    }

    override fun unregisterAudioRecordingCallback(cb: AudioRecordingCallback) {
        audioManager.unregisterAudioRecordingCallback(cb)
    }

    override fun getActiveRecordingConfigurations(): MutableList<AudioRecordingConfiguration> {
        return audioManager.activeRecordingConfigurations
    }

    override fun getAudioDevicesForAttributes(attributes: AudioAttributes): MutableList<AudioDeviceInfo> {
        return audioManager.getAudioDevicesForAttributes(attributes)
    }

    override fun getProperty(key: String?): String {
        return audioManager.getProperty(key)
    }

    override fun getDevices(flags: Int): Array<AudioDeviceInfo> {
        return audioManager.getDevices(flags)
    }

    override fun registerAudioDeviceCallback(callback: AudioDeviceCallback?, handler: Handler?) {
        audioManager.registerAudioDeviceCallback(callback, handler)
    }

    override fun unregisterAudioDeviceCallback(callback: AudioDeviceCallback?) {
        audioManager.unregisterAudioDeviceCallback(callback)
    }

    override fun getMicrophones(): MutableList<MicrophoneInfo> {
        return audioManager.microphones
    }

    override fun setEncodedSurroundMode(mode: Int): Boolean {
        return audioManager.setEncodedSurroundMode(mode)
    }

    override fun getEncodedSurroundMode(): Int {
        return audioManager.encodedSurroundMode
    }

    override fun setSurroundFormatEnabled(audioFormat: Int, enabled: Boolean): Boolean {
        return audioManager.setSurroundFormatEnabled(audioFormat, enabled)
    }

    override fun isSurroundFormatEnabled(audioFormat: Int): Boolean {
        return audioManager.isSurroundFormatEnabled(audioFormat)
    }

    override fun getAudioHwSyncForSession(sessionId: Int): Int {
        return audioManager.getAudioHwSyncForSession(sessionId)
    }

    override fun setCommunicationDevice(device: AudioDeviceInfo): Boolean {
        return audioManager.setCommunicationDevice(device)
    }

    override fun clearCommunicationDevice() {
        audioManager.clearCommunicationDevice()
    }

    override fun getCommunicationDevice(): AudioDeviceInfo? {
        return audioManager.communicationDevice
    }

    override fun getAvailableCommunicationDevices(): MutableList<AudioDeviceInfo> {
        return audioManager.availableCommunicationDevices
    }

    override fun getDirectProfilesForAttributes(attributes: AudioAttributes): MutableList<AudioProfile> {
        return audioManager.getDirectProfilesForAttributes(attributes)
    }

    override fun addOnCommunicationDeviceChangedListener(
        executor: Executor,
        listener: OnCommunicationDeviceChangedListener
    ) {
        audioManager.addOnCommunicationDeviceChangedListener(executor, listener)
    }

    override fun removeOnCommunicationDeviceChangedListener(listener: OnCommunicationDeviceChangedListener) {
        audioManager.removeOnCommunicationDeviceChangedListener(listener)
    }

    override fun getSupportedMixerAttributes(device: AudioDeviceInfo): MutableList<AudioMixerAttributes> {
        return audioManager.getSupportedMixerAttributes(device)
    }

    override fun setPreferredMixerAttributes(
        attributes: AudioAttributes,
        device: AudioDeviceInfo,
        mixerAttributes: AudioMixerAttributes
    ): Boolean {
        return audioManager.setPreferredMixerAttributes(attributes, device, mixerAttributes)
    }

    override fun getPreferredMixerAttributes(
        attributes: AudioAttributes,
        device: AudioDeviceInfo
    ): AudioMixerAttributes? {
        return audioManager.getPreferredMixerAttributes(attributes, device)
    }

    override fun clearPreferredMixerAttributes(
        attributes: AudioAttributes,
        device: AudioDeviceInfo
    ): Boolean {
        return audioManager.clearPreferredMixerAttributes(attributes, device)
    }

    override fun addOnPreferredMixerAttributesChangedListener(
        executor: Executor,
        listener: OnPreferredMixerAttributesChangedListener
    ) {
        audioManager.addOnPreferredMixerAttributesChangedListener(executor, listener)
    }

    override fun removeOnPreferredMixerAttributesChangedListener(listener: OnPreferredMixerAttributesChangedListener) {
        audioManager.removeOnPreferredMixerAttributesChangedListener(listener)
    }
}
