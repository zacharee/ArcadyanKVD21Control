@file:OptIn(ExperimentalObjCRefinement::class)

package dev.zwander.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

        tempState?.twoGig?.transmissionPower?.let { transmissionPower ->
            SliderWithTitle(
                title = stringResource(MR.strings.twoGig_transmission_power),
                minValue = 50,
                maxValue = 100,
                currentValue = transmissionPower.replace("%", "").toInt(),
                onValueChange = {
                    tempState = tempState?.copy(
                        twoGig = tempState?.twoGig?.copy(
                            transmissionPower = "${it.toInt()}%",
                        ),
                    )
                },
                unit = "%",
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
        }

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

        tempState?.fiveGig?.transmissionPower?.let { transmissionPower ->
            SliderWithTitle(
                title = stringResource(MR.strings.fiveGig_transmission_power),
                minValue = 50,
                maxValue = 100,
                currentValue = transmissionPower.replace("%", "").toInt(),
                onValueChange = {
                    tempState = tempState?.copy(
                        fiveGig = tempState?.fiveGig?.copy(
                            transmissionPower = "${it.toInt()}%",
                        ),
                    )
                },
                unit = "%",
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
        }

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
    }
}
