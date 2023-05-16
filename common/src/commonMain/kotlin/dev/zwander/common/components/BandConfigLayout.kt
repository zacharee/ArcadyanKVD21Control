package dev.zwander.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.icerock.moko.mvvm.flow.compose.collectAsMutableState
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.model.GlobalModel
import dev.zwander.common.model.MainModel
import dev.zwander.resources.common.MR

@Composable
fun BandConfigLayout(
    modifier: Modifier = Modifier,
) {
    val isLoading by GlobalModel.isLoading.collectAsState()

    var tempState by MainModel.tempWifiState.collectAsMutableState()

    Column(
        modifier = modifier,
    ) {
        TextSwitch(
            text = stringResource(MR.strings.twoGig_radio),
            checked = tempState?.twoGig?.isRadioEnabled ?: false,
            onCheckedChange = { checked ->
                tempState = tempState?.copy(
                    twoGig = tempState?.twoGig?.copy(
                        isRadioEnabled = checked
                    )
                )
            },
            enabled = !isLoading,
        )

        TextSwitch(
            text = stringResource(MR.strings.fiveGig_radio),
            checked = tempState?.fiveGig?.isRadioEnabled ?: false,
            onCheckedChange = { checked ->
                tempState = tempState?.copy(
                    fiveGig = tempState?.fiveGig?.copy(
                        isRadioEnabled = checked
                    )
                )
            },
            enabled = !isLoading,
        )

        TextSwitch(
            text = stringResource(MR.strings.band_steering),
            checked = tempState?.bandSteering?.isEnabled ?: false,
            onCheckedChange = { checked ->
                tempState = tempState?.copy(
                    bandSteering = tempState?.bandSteering?.copy(
                        isEnabled = checked
                    )
                )
            },
            enabled = !isLoading,
        )
    }
}