package de.fisch981.minimallauncher.ui.components

import android.os.SystemClock
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.ui.theme.DarkSurface
import de.fisch981.minimallauncher.ui.theme.PureWhite
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

@Composable
fun WaveScroller(
    isRightHanded: Boolean = true,
    availableLetters: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    onScrubFinished: () -> Unit = {},
    onTouchStateChanged: (Boolean) -> Unit = {},
    idleAlphaPercent: Int = 40,
    modifier: Modifier = Modifier
) {
    val alphabet = remember {
        listOf('★') + ('A'..'Z').toList() + listOf('#')
    }

    val view = LocalView.current
    val density = LocalDensity.current
    val currentOnLetterSelected by rememberUpdatedState(onLetterSelected)
    val currentOnScrubFinished by rememberUpdatedState(onScrubFinished)
    val currentOnTouchStateChanged by rememberUpdatedState(onTouchStateChanged)

    var isTouching by remember { mutableStateOf(false) }
    var touchY by remember { mutableFloatStateOf(-1f) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }

    val idleAlpha = (idleAlphaPercent.coerceIn(0, 100)) / 100f
    var currentFadeAlpha by remember { mutableFloatStateOf(idleAlpha) }
    var wasTouching by remember { mutableStateOf(false) }

    LaunchedEffect(isTouching, idleAlpha) {
        if (isTouching) {
            wasTouching = true
            currentFadeAlpha = 1f
        } else if (wasTouching) {
            wasTouching = false
            currentFadeAlpha = 1f
            delay(1000L)
            val startAlpha = 1f
            val durationMs = 1800f
            val startTimeMs = SystemClock.uptimeMillis()
            while (true) {
                withFrameNanos { }
                val elapsedMs = (SystemClock.uptimeMillis() - startTimeMs).coerceAtLeast(0L)
                val t = (elapsedMs.toFloat() / durationMs).coerceIn(0f, 1f)
                val smoothT = t * t * (3f - 2f * t)
                currentFadeAlpha = startAlpha + (idleAlpha - startAlpha) * smoothT
                if (t >= 1f) break
            }
            currentFadeAlpha = idleAlpha
        } else {
            currentFadeAlpha = idleAlpha
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer { clip = false }
            .padding(
                top = 40.dp,
                bottom = 40.dp,
                end = if (isRightHanded) 10.dp else 0.dp,
                start = if (!isRightHanded) 10.dp else 0.dp
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .width(52.dp)
                .graphicsLayer { clip = false }
                .pointerInput(Unit) {
                    fun selectAt(y: Float, heightPx: Float) {
                        if (heightPx <= 0f) return
                        val clampedY = y.coerceIn(0f, heightPx - 0.01f)
                        val slotPx = heightPx / alphabet.size
                        val index = (clampedY / slotPx).toInt().coerceIn(0, alphabet.lastIndex)
                        val letter = alphabet[index]
                        if (letter != activeLetter) {
                            activeLetter = letter
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            currentOnLetterSelected(letter)
                        }
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isTouching = true
                        currentOnTouchStateChanged(true)
                        touchY = down.position.y
                        down.consume()
                        try {
                            selectAt(down.position.y, size.height.toFloat())

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                if (change == null || !change.pressed) {
                                    break
                                }
                                change.consume()
                                touchY = change.position.y
                                selectAt(change.position.y, size.height.toFloat())
                            }
                        } finally {
                            isTouching = false
                            touchY = -1f
                            activeLetter = null
                            currentOnTouchStateChanged(false)
                            currentOnScrubFinished()
                        }
                    }
                },
            contentAlignment = if (isRightHanded) Alignment.TopEnd else Alignment.TopStart
        ) {
            val totalHeightPx = with(density) { maxHeight.toPx() }
            val slotHeightPx = if (alphabet.isNotEmpty()) totalHeightPx / alphabet.size else 1f
            val slotHeightDp = with(density) { slotHeightPx.toDp() }

            val sigma = with(density) { 44.dp.toPx() }
            val maxHorizontalOffset = with(density) { 54.dp.toPx() }
            val maxScale = 1.55f

            alphabet.forEachIndexed { index, letter ->
                val letterCenterY = index * slotHeightPx + slotHeightPx / 2f
                val isAvailable = letter == '★' || availableLetters.contains(letter)
                val baseColor = if (isAvailable) PureWhite else PureWhite.copy(alpha = 0.35f)
                val fadeAlpha = currentFadeAlpha.coerceIn(0f, 1f)
                val fadedColor = baseColor.copy(alpha = (baseColor.alpha * fadeAlpha).coerceIn(0f, 1f))

                val (horizontalOffsetPx, scaleFactor, textColor) = if (isTouching && touchY >= 0f) {
                    val distance = abs(touchY - letterCenterY)
                    val gaussian = exp(-(distance * distance) / (2f * sigma * sigma))

                    val offsetDir = if (isRightHanded) -1f else 1f
                    val hOffset = offsetDir * (maxHorizontalOffset * gaussian)
                    val scale = 1.0f + (maxScale - 1.0f) * gaussian
                    Triple(hOffset, scale, fadedColor)
                } else {
                    Triple(0f, 1f, fadedColor)
                }

                val topOffsetPx = (index * slotHeightPx).roundToInt()

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = horizontalOffsetPx.roundToInt(),
                                y = topOffsetPx
                            )
                        }
                        .scale(scaleFactor)
                        .size(width = 24.dp, height = slotHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 11.sp,
                        fontWeight = if (isTouching && scaleFactor > 1.25f) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }

            // Dunkler, monochromer Buchstaben-Indikator neben dem Wellenkamm
            val currentLetter = activeLetter
            if (isTouching && currentLetter != null && touchY >= 0f) {
                val bubbleSizeDp = 44.dp
                val bubbleSizePx = with(density) { bubbleSizeDp.toPx() }
                val bubbleY = (touchY - bubbleSizePx / 2f).coerceIn(0f, (totalHeightPx - bubbleSizePx).coerceAtLeast(0f))
                val bubbleX = if (isRightHanded) {
                    with(density) { (-84).dp.toPx() }
                } else {
                    with(density) { 84.dp.toPx() }
                }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(bubbleX.roundToInt(), bubbleY.roundToInt())
                        }
                        .size(bubbleSizeDp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(1.dp, PureWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentLetter.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        }
    }
}
