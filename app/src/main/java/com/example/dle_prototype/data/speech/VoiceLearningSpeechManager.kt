package com.example.dle_prototype.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * State representing current speech recognition status and audio amplitude.
 */
data class SpeechRecordingState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val transcript: String = "",
    val partialText: String = "",
    val rmsDecibels: Float = 0f,
    val error: String? = null,
    val isSpeechAvailable: Boolean = true
)

/**
 * Manages device microphone speech recognition using Android's SpeechRecognizer
 * with real-time audio amplitude telemetry and fallback sample summaries.
 */
class VoiceLearningSpeechManager {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _recordingState = MutableStateFlow(SpeechRecordingState())
    val recordingState: StateFlow<SpeechRecordingState> = _recordingState.asStateFlow()

    companion object {
        val SAMPLE_LEARNING_SUMMARIES = listOf(
            "Today I learned how Coroutines and TFLite neural inference run on background worker threads without freezing the Compose UI.",
            "I discovered how C language pointers and malloc memory allocation directly impact Big-O space complexity and system performance.",
            "I explored how HTML DOM hierarchy and CSS Grid layouts correlate with declarative UI composables in Jetpack Compose.",
            "I studied how Spaced Repetition (SM-2) memory forgetting curves can be calibrated using TFLite neural weight predictions.",
            "I analyzed how MySQL database indexes use B-Tree binary search trees to optimize query execution in Algorithms."
        )
    }

    fun isSpeechRecognitionSupported(context: Context): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(context: Context, onTranscriptUpdated: ((String) -> Unit)? = null) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _recordingState.value = _recordingState.value.copy(
                isListening = false,
                isSpeechAvailable = false,
                error = "Speech recognition service not active on device. You can type or pick a quick preset summary below."
            )
            return
        }

        stopListening()

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _recordingState.value = _recordingState.value.copy(
                        isListening = true,
                        isProcessing = false,
                        error = null
                    )
                }

                override fun onBeginningOfSpeech() {
                    _recordingState.value = _recordingState.value.copy(
                        isListening = true,
                        error = null
                    )
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _recordingState.value = _recordingState.value.copy(
                        rmsDecibels = rmsdB.coerceIn(0f, 15f)
                    )
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _recordingState.value = _recordingState.value.copy(
                        isListening = false,
                        isProcessing = true
                    )
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error for speech service"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking clearly or use presets."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Speech server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech recognition error ($error)"
                    }
                    _recordingState.value = _recordingState.value.copy(
                        isListening = false,
                        isProcessing = false,
                        error = message
                    )
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        val current = _recordingState.value.transcript
                        val updated = if (current.isBlank()) text else "$current $text"
                        _recordingState.value = _recordingState.value.copy(
                            isListening = false,
                            isProcessing = false,
                            transcript = updated,
                            partialText = ""
                        )
                        onTranscriptUpdated?.invoke(updated)
                    } else {
                        _recordingState.value = _recordingState.value.copy(
                            isListening = false,
                            isProcessing = false
                        )
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull() ?: ""
                    if (partial.isNotBlank()) {
                        _recordingState.value = _recordingState.value.copy(partialText = partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
            _recordingState.value = _recordingState.value.copy(
                isListening = true,
                isProcessing = false,
                error = null
            )
        } catch (e: Exception) {
            _recordingState.value = _recordingState.value.copy(
                isListening = false,
                error = "Failed to launch speech recognizer: ${e.message}"
            )
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        _recordingState.value = _recordingState.value.copy(
            isListening = false,
            rmsDecibels = 0f
        )
    }

    fun setTranscript(text: String) {
        _recordingState.value = _recordingState.value.copy(
            transcript = text,
            partialText = "",
            error = null
        )
    }

    fun appendTranscript(text: String) {
        val current = _recordingState.value.transcript
        val updated = if (current.isBlank()) text else "$current $text"
        _recordingState.value = _recordingState.value.copy(
            transcript = updated,
            partialText = "",
            error = null
        )
    }

    fun clear() {
        stopListening()
        _recordingState.value = SpeechRecordingState()
    }
}
