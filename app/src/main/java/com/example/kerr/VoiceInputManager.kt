package com.example.kerr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale


class VoiceInputManager (private val context: Context){
    var isListening by mutableStateOf(false)

        private set

    var rmsDb by mutableFloatStateOf(0f)
        private set

    var spokenText by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var speechRecognizer: SpeechRecognizer?=(null)

    private val recognitionListener= object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            errorMessage = null
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {
            rmsDb = rmsdB.coerceAtLeast(0f)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            isListening = false
            rmsDb = 0f

        }

        override fun onError(error: Int) {
            isListening = false
            rmsDb = 0f
            errorMessage = "Speech error code: $error"
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            rmsDb = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            spokenText = matches?.firstOrNull().orEmpty()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial= partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!partial.isNullOrEmpty()){
                spokenText=partial[0]
        }
    }

        override fun onEvent(eventType: Int, params: Bundle?) {}

    }
    fun startListening(){
        if (!SpeechRecognizer.isRecognitionAvailable(context)){
            errorMessage="Speech Recogniton is not available on this device"
            return
        }
        if (speechRecognizer==null){
            speechRecognizer= SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        }
        val intent= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true)

        }
        spokenText=""
        speechRecognizer?.startListening(intent)
    }
    fun stopListening(){
        speechRecognizer?.stopListening()
        isListening=false
        rmsDb=0f

    }
    fun destroy(){
        speechRecognizer?.destroy()
        speechRecognizer=null
    }
}