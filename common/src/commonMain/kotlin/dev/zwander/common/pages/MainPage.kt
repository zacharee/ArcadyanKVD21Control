@file:OptIn(ExperimentalObjCRefinement::class)

package dev.zwander.common.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.components.CellBars
import dev.zwander.common.components.CellDataLayout
import dev.zwander.common.components.DeviceDataLayout
import dev.zwander.common.components.InfoRow
import dev.zwander.common.components.MainDataLayout
import dev.zwander.common.components.PageGrid
import dev.zwander.common.components.SnapshotChart
import dev.zwander.common.components.dialog.InWindowAlertDialog
import dev.zwander.common.model.GlobalModel
import dev.zwander.common.model.MainModel
import dev.zwander.common.model.SettingsModel
import dev.zwander.common.model.UserModel
import dev.zwander.common.util.Storage
import dev.zwander.common.util.addAll
import dev.zwander.common.util.buildItemList
import dev.zwander.resources.common.MR
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

private data class ItemInfo(
    val title: StringResource,
    val render: @Composable (Modifier) -> Unit,
    val titleAccessory: (@Composable (Modifier) -> Unit)? = null,
    val selectable: Boolean = true,
    val visible: @Composable () -> Boolean = { true },
)

@Composable
@HiddenFromObjC
fun MainPage(
    modifier: Modifier = Modifier,
) {
    val httpClient by GlobalModel.httpClient.collectAsState()
    val scope = rememberCoroutineScope()

    val items = remember {
        listOf(
            ItemInfo(
                title = MR.strings.lte,
                render = {
                    val basicData by MainModel.currentMainData.collectAsState()
                    val advancedData by MainModel.currentCellData.collectAsState()

                    CellDataLayout(
                        data = basicData?.signal?.fourG,
                        advancedData = advancedData?.cell?.fourG,
                        expandedKey = "lte_cell_data_expanded",
                        modifier = it,
                    )
                },
                titleAccessory = {
                    val basicData by MainModel.currentMainData.collectAsState()

                    CellBars(
                        bars = basicData?.signal?.fourG?.bars?.toInt(),
                        modifier = it,
                    )
                },
                selectable = false,
            ),
            ItemInfo(
                title = MR.strings.five_g,
                render = {
                    val basicData by MainModel.currentMainData.collectAsState()
                    val advancedData by MainModel.currentCellData.collectAsState()

                    CellDataLayout(
                        data = basicData?.signal?.fiveG,
                        advancedData = advancedData?.cell?.fiveG,
                        expandedKey = "5g_cell_data_expanded",
                        modifier = it,
                    )
                },
                titleAccessory = {
                    val basicData by MainModel.currentMainData.collectAsState()

                    CellBars(
                        bars = basicData?.signal?.fiveG?.bars?.toInt(),
                        modifier = it,
                    )
                },
                selectable = false,
            ),
            ItemInfo(
                title = MR.strings.device,
                render = { DeviceDataLayout(it) }
            ),
            ItemInfo(
                title = MR.strings.general,
                render = { MainDataLayout(it) },
            ),
            ItemInfo(
                title = MR.strings.sim,
                render = {
                    val simData by MainModel.currentSimData.collectAsState()

                    val items = remember(simData) {
                        buildItemList {
                            addAll(
                                MR.strings.iccid to simData?.sim?.iccId,
                                MR.strings.imei to simData?.sim?.imei,
                                MR.strings.imsi to simData?.sim?.imsi,
                                MR.strings.msisdn to simData?.sim?.msisdn,
                                MR.strings.status to simData?.sim?.status,
                            )
                        }
                    }

                    InfoRow(
                        items = items,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ),
            ItemInfo(
                title = MR.strings.snapshots,
                render = {
                    SnapshotChart(it)
                },
                selectable = false,
                visible = {
                    SettingsModel.recordSnapshots.value &&
                            Storage.snapshots.updates.collectAsState(listOf()).value?.isNotEmpty() == true
                },
            ),
        )
    }

    var showingRebootConfirmation by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
    ) {
        PageGrid(
            id = "MainPage",
            items = items.filter { it.visible() },
            modifier = Modifier.fillMaxSize(),
            renderItemTitle = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(it.title),
                        modifier = Modifier.weight(1f),
                    )

                    it.titleAccessory?.invoke(Modifier.fillMaxHeight().aspectRatio(1f))
                }
            },
            renderItem = {
                it.render(Modifier.fillMaxWidth())
            },
            bottomBarContents = {
                Button(
                    onClick = {
                        scope.launch {
                            UserModel.logOut()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(MR.strings.log_out),
                    )
                }

                Button(
                    onClick = {
                        showingRebootConfirmation = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.error),
                    ),
                ) {
                    Text(
                        text = stringResource(MR.strings.reboot),
                    )
                }
            },
            itemIsSelectable = {
                selectable
            },
        )
    }

    InWindowAlertDialog(
        showing = showingRebootConfirmation,
        onDismissRequest = { showingRebootConfirmation = false },
        title = {
            Text(
                text = stringResource(MR.strings.reboot),
            )
        },
        text = {
            Text(
                text = stringResource(MR.strings.reboot_confirmation),
            )
        },
        buttons = {
            TextButton(
                onClick = {
                    showingRebootConfirmation = false
                },
            ) {
                Text(
                    text = stringResource(MR.strings.no),
                )
            }

            TextButton(
                onClick = {
                    showingRebootConfirmation = false

                    scope.launch {
                        httpClient?.reboot()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(
                    text = stringResource(MR.strings.yes),
                )
            }
        },
    )
}
