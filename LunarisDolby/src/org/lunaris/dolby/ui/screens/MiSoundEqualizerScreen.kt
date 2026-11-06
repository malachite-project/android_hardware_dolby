/*
 * Copyright (C) 2026 The Malachite Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lunaris.dolby.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Locale
import org.lunaris.dolby.DolbyConstants
import org.lunaris.dolby.R
import org.lunaris.dolby.domain.models.DolbyUiState
import org.lunaris.dolby.domain.models.MiSoundOutput
import org.lunaris.dolby.ui.components.*
import org.lunaris.dolby.ui.viewmodel.DolbyViewModel

// Takes the Dolby equalizer's place while Dolby is off, as on HyperOS.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiSoundEqualizerScreen(
    viewModel: DolbyViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val output by viewModel.miSoundOutput.collectAsState()
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(null)
    val miSoundEnabled = (uiState as? DolbyUiState.Success)?.settings?.miSoundEnabled ?: true
    val gains = remember(output) { viewModel.getMiSoundEqGains(output).toMutableStateList() }
    val showBands = miSoundEnabled && output != MiSoundOutput.OTHER

    val layoutDirection = LocalLayoutDirection.current
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.misound_eq_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    if (showBands) {
                        IconButton(
                            onClick = {
                                gains.indices.forEach { gains[it] = 0f }
                                viewModel.setMiSoundEqGains(output, gains.toList())
                            }
                        ) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = stringResource(R.string.misound_eq_reset),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    if (showBands) {
                        ModernSettingsCard(
                            title = stringResource(
                                if (output == MiSoundOutput.SPEAKER) R.string.misound_eq_speaker
                                else R.string.misound_eq_headphones
                            ),
                            icon = Icons.Default.GraphicEq
                        ) {
                            output.bands.forEachIndexed { index, hz ->
                                MiSoundBandRow(
                                    hz = hz,
                                    gain = gains[index],
                                    onGainChange = { gains[index] = it },
                                    onGainChangeFinished = {
                                        viewModel.setMiSoundEqGains(output, gains.toList())
                                    }
                                )
                            }
                        }
                    } else {
                        ModernSettingsCard(
                            title = stringResource(R.string.misound_eq_title),
                            icon = Icons.Default.GraphicEq
                        ) {
                            Text(
                                text = stringResource(
                                    if (miSoundEnabled) R.string.misound_eq_no_output
                                    else R.string.misound_eq_turn_on
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = cutoutInsets.calculateStartPadding(layoutDirection),
                        end = cutoutInsets.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding()
                    ),
                contentAlignment = Alignment.Center
            ) {
                FloatingNavToolbar(
                    currentRoute = currentRoute?.destination?.route ?: "equalizer",
                    onNavigate = { route ->
                        if (currentRoute?.destination?.route != route) {
                            navController.navigate(route) {
                                popUpTo("settings") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MiSoundBandRow(
    hz: Int,
    gain: Float,
    onGainChange: (Float) -> Unit,
    onGainChangeFinished: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (hz >= 1000) {
                    String.format(Locale.ROOT, "%.1f kHz", hz / 1000f)
                } else {
                    "$hz Hz"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format(Locale.ROOT, "%+.1f dB", gain),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Sends only when the finger lifts: every send reaches the audio HAL.
        Slider(
            value = gain,
            onValueChange = onGainChange,
            onValueChangeFinished = onGainChangeFinished,
            valueRange = DolbyConstants.MISOUND_EQ_MIN_DB..DolbyConstants.MISOUND_EQ_MAX_DB,
            steps = 23
        )
    }
}
