package com.example.kerr.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kerr.R

@Composable
fun FennecFoxFace(
    isListening: Boolean,
    voiceAmplitude: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Crop out the body (keep top 62%) & make the white background transparent once
    val faceOnlyBitmap = remember {
        val raw = BitmapFactory.decodeResource(context.resources, R.drawable.fennec_fox)
        val croppedHeight = (raw.height * 0.62f).toInt()
        val cropped = Bitmap.createBitmap(raw, 0, 0, raw.width, croppedHeight)
        removeWhiteBackground(cropped)
    }

    // 1. Gentle perk-up scale + live voice amplitude bounce when listening
    val headScale by animateFloatAsState(
        targetValue = if (isListening) 1.05f + (voiceAmplitude * 0.08f) else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "headScale"
    )

    // 2. Smooth, slow curious tilt & sound-wave pulse while listening
    val infiniteTransition = rememberInfiniteTransition(label = "listeningAnim")

    val curiousTilt by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "curiousTilt"
    )

    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "soundWave"
    )

    val waveColor = Color(0xFFE87A3E)

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Face & Ears only (transparent background, body removed)
        Image(
            bitmap = faceOnlyBitmap,
            contentDescription = "Fennec Fox Face",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Pivot from the chin/neck bottom edge
                    transformOrigin = TransformOrigin(0.5f, 0.92f)
                    scaleX = headScale
                    scaleY = headScale
                    rotationZ = if (isListening) curiousTilt * (1f + voiceAmplitude * 0.8f) else 0f
                }
        )

        // Glowing sound-wave arcs near both ears that pulse with voiceAmplitude
        if (isListening) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val waveAlpha = ((1f - waveProgress) * (0.55f + voiceAmplitude * 0.45f)).coerceIn(0f, 1f)
                val spread = w * (0.09f + voiceAmplitude * 0.08f) * waveProgress
                val dynamicStroke = w * (0.015f + voiceAmplitude * 0.015f)

                // Left ear sound arc
                drawArc(
                    color = waveColor.copy(alpha = waveAlpha),
                    startAngle = 145f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(w * 0.02f - spread, h * 0.08f - spread * 0.4f),
                    size = Size(w * 0.24f + spread * 2, h * 0.36f + spread),
                    style = Stroke(width = dynamicStroke, cap = StrokeCap.Round)
                )

                // Right ear sound arc
                drawArc(
                    color = waveColor.copy(alpha = waveAlpha),
                    startAngle = -25f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(w * 0.74f - spread, h * 0.08f - spread * 0.4f),
                    size = Size(w * 0.24f + spread * 2, h * 0.36f + spread),
                    style = Stroke(width = dynamicStroke, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// Helper: Converts white background pixels to transparent with soft edge feathering
private fun removeWhiteBackground(source: Bitmap) = run {
    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in pixels.indices) {
        val c = pixels[i]
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF

        // Pure/near white -> fully transparent
        if (r > 245 && g > 245 && b > 245) {
            pixels[i] = android.graphics.Color.TRANSPARENT
        } else if (r > 225 && g > 225 && b > 225) {
            // Soft anti-aliased edge around fur tips so there are no jagged white pixels
            val brightness = (r + g + b) / 3
            val alpha = ((245 - brightness) * (255 / 20)).coerceIn(0, 255)
            pixels[i] = (alpha shl 24) or (c and 0x00FFFFFF)
        }
    }

    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    result.setPixels(pixels, 0, width, 0, 0, width, height)
    result.asImageBitmap()
}