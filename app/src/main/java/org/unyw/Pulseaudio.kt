package org.unyw

import android.media.AudioFormat
import android.media.AudioTrack

import android.media.AudioManager
import java.io.BufferedInputStream
import java.net.Socket


fun startPulseaudio(){

    val sock = Socket("localhost", 12333)
    val audioData = BufferedInputStream(sock.getInputStream())

    val sampleRate = 44100

    val musicLength = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_CONFIGURATION_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
        AudioFormat.ENCODING_PCM_16BIT, musicLength,
        AudioTrack.MODE_STREAM
    )
    audioTrack.play()

    val audioBuffer = ByteArray(musicLength * 50)

    while (true) {
            val sizeRead: Int = audioData.read(audioBuffer, 0, musicLength * 50)
            audioTrack.write(audioBuffer, 0, sizeRead)
    }

}