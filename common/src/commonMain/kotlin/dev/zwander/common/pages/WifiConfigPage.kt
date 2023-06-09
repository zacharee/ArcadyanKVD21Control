@file:OptIn(ExperimentalObjCRefinement::class)

package dev.zwander.common.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.flow.compose.collectAsMutableState
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.components.BandConfigLayout
import dev.zwander.common.components.PageGrid
import dev.zwander.common.components.SSIDListLayout
import dev.zwander.common.components.dialog.AlertDialogDef
import dev.zwander.common.model.GlobalModel
import dev.zwander.common.model.MainModel
import dev.zwander.resources.common.MR
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

private data class ItemData(
    val title: StringResource,
    val render: @Composable (Modifier) -> Unit,
    val description: (@Composable () -> Unit)? = null,
)

@Composable
@HiddenFromObjC
fun WifiConfigPage(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val data by MainModel.currentWifiData.collectAsState()

    var tempState by MainModel.tempWifiState.collectAsMutableState()
    var showingRadioWarning by remember {
        mutableStateOf(false)
    }

    fun save() {
        scope.launch {
            tempState?.let {
                GlobalModel.httpClient.value?.setWifiData(it)
            }
            MainModel.currentWifiData.value = GlobalModel.httpClient.value?.getWifiData()
        }
    }

    LaunchedEffect(data) {
        tempState = data
    }

    val items: List<ItemData> = remember(data) {
        val list = mutableListOf<ItemData>()

        if (data?.twoGig != null && data?.fiveGig != null) {
            list.add(
                ItemData(
                    title = MR.strings.band_mgmnt,
                    render = {
                        BandConfigLayout(it)
                    },
                    description = {
                        Text(
                            text = stringResource(MR.strings.radio_warning),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            )
        }

        list.add(
            ItemData(
                title = MR.strings.ssids,
                render = {
                    SSIDListLayout(it)
                },
            )
        )

        list
    }

    PageGrid(
        items = items,
        modifier = modifier,
        renderItemTitle = {
            Text(
                text = stringResource(it.title),
            )
        },
        renderItem = {
            it.render(Modifier.fillMaxWidth())
        },
        renderItemDescription = {
            it.description?.invoke()
        },
        bottomBarContents = {
            Button(
                onClick = {
                    if ((tempState?.twoGig?.isRadioEnabled == false && tempState?.twoGig?.isRadioEnabled != data?.twoGig?.isRadioEnabled) ||
                        (tempState?.fiveGig?.isRadioEnabled == false && tempState?.fiveGig?.isRadioEnabled != data?.fiveGig?.isRadioEnabled)
                    ) {
                        showingRadioWarning = true
                    } else {
                        save()
                    }
                },
                enabled = tempState != data && tempState != null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.12f)
                        .compositeOver(MaterialTheme.colorScheme.background),
                ),
            ) {
                Text(
                    text = stringResource(MR.strings.save)
                )
            }
        },
        showBottomBarExpander = false,
    )

    AlertDialogDef(
        showing = showingRadioWarning,
        title = {
            Text(text = stringResource(MR.strings.save))
        },
        text = {
            Text(text = stringResource(MR.strings.radio_disable_confirm))
        },
        onDismissRequest = { showingRadioWarning = false },
        buttons = {
            TextButton(
                onClick = {
                    showingRadioWarning = false
                },
            ) {
                Text(text = stringResource(MR.strings.no))
            }

            TextButton(
                onClick = {
                    showingRadioWarning = false
                    save()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(text = stringResource(MR.strings.yes))
            }
        },
    )
}
