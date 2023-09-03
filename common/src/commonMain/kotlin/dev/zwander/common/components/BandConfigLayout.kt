@file:OptIn(ExperimentalObjCRefinement::class)

package dev.zwander.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.flow.compose.collectAsMutableState
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.model.MainModel
import dev.zwander.resources.common.MR
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@Composable
@HiddenFromObjC
fun BandConfigLayout(
    modifier: Modifier = Modifier,
) {
    var tempState by MainModel.tempWifiState.collectAsMutableState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TextSwitch(
            text = stringResource(MR.strings.twoGig_radio),
            checked = (tempState?.twoGig?.isRadioEnabled ?: false),
            onCheckedChange = { checked ->
                tempState = tempState?.copy(
                    twoGig = tempState?.twoGig?.copy(
                        isRadioEnabled = checked,
                    )
                )
            },
        )

        TextSwitch(
            text = stringResource(MR.strings.fiveGig_radio),
            checked = (tempState?.fiveGig?.isRadioEnabled ?: false),
            onCheckedChange = { checked ->
                tempState = tempState?.copy(
                    fiveGig = tempState?.fiveGig?.copy(
                        isRadioEnabled = checked,
                    )
                )
            },
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
        )

        if (tempState?.twoGig?.channel != null && tempState?.fiveGig?.channel != null) {
            ChannelSelector(
                whichBand = Band.TwoGig,
                currentValue = tempState?.twoGig?.channel ?: "Auto",
                onValueChange = {
                    tempState = tempState?.copy(
                        twoGig = tempState?.twoGig?.copy(
                            channel = it,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            ChannelSelector(
                whichBand = Band.FiveGig,
                currentValue = tempState?.fiveGig?.channel ?: "Auto",
                onValueChange = {
                    tempState = tempState?.copy(
                        fiveGig = tempState?.fiveGig?.copy(
                            channel = it,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
