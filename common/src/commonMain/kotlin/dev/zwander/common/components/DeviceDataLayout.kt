@file:OptIn(ExperimentalObjCRefinement::class)

package dev.zwander.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.model.MainModel
import dev.zwander.common.util.addAll
import dev.zwander.common.util.buildItemList
import dev.zwander.resources.common.MR
import korlibs.math.toIntFloor
import korlibs.time.TimeFormat
import korlibs.time.days
import korlibs.time.milliseconds
import korlibs.util.format
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@Composable
@HiddenFromObjC
fun DeviceDataLayout(
    modifier: Modifier = Modifier,
) {
    val data by MainModel.currentMainData.collectAsState()
    val deviceData = data?.device

    val timeFormat = remember {
        TimeFormat("HH'h' mm'm' ss's'")
    }

    val items = remember(deviceData, data) {
        buildItemList {
            addAll(
                MR.strings.friendly_name to deviceData?.friendlyName,
                MR.strings.name to deviceData?.name,
                MR.strings.softwareVersion to deviceData?.softwareVersion,
                MR.strings.hardware_version to deviceData?.hardwareVersion,
                MR.strings.mac to deviceData?.macId,
                MR.strings.serial to deviceData?.serial,
                MR.strings.update_state to deviceData?.updateState,
                MR.strings.mesh_supported to deviceData?.isMeshSupported,
                MR.strings.enabled to deviceData?.isEnabled,
                MR.strings.role to deviceData?.role,
                MR.strings.type to deviceData?.type,
                MR.strings.manufacturer to deviceData?.manufacturer,
                MR.strings.model to deviceData?.model,
                MR.strings.uptime to data?.time?.upTime?.let { upTime ->
                    val milliseconds = (upTime * 1000).milliseconds

                    val days = milliseconds.days.toIntFloor()
                    val rest = milliseconds - days.days

                    "${"%2".format(days)}d ${timeFormat.format(rest)}"
                },
            )
        }
    }

    EmptiableContent(
        content = {
            InfoRow(
                items = items,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        emptyContent = {
            Text(
                text = stringResource(MR.strings.unavailable),
            )
        },
        isEmpty = items.isEmpty(),
        modifier = modifier,
    )
}
