@file:OptIn(ExperimentalObjCRefinement::class)

package dev.zwander.common.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.components.*
import dev.zwander.common.model.MainModel
import dev.zwander.common.model.UserModel
import dev.zwander.common.util.SettingsManager
import dev.zwander.resources.common.MR
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

private data class ItemInfo(
    val title: StringResource,
    val render: @Composable (Modifier) -> Unit,
    val titleAccessory: (@Composable (Modifier) -> Unit)? = null,
)

@Composable
@HiddenFromObjC
fun MainPage(
    modifier: Modifier = Modifier,
) {
    val data by MainModel.currentMainData.collectAsState()

    val items = remember(data) {
        listOf(
            ItemInfo(
                title = MR.strings.device,
                render = { DeviceDataLayout(it) }
            ),
            ItemInfo(
                title = MR.strings.general,
                render = { MainDataLayout(it) },
            ),
            ItemInfo(
                title = MR.strings.lte,
                render = {
                    CellDataLayout(
                        data = data?.signal?.fourG,
                        modifier = it
                    )
                },
                titleAccessory = {
                    CellBars(
                        bars = data?.signal?.fourG?.bars?.toInt(),
                        modifier = it,
                    )
                },
            ),
            ItemInfo(
                title = MR.strings.five_g,
                render = {
                    CellDataLayout(
                        data = data?.signal?.fiveG,
                        modifier = it
                    )
                },
                titleAccessory = {
                    CellBars(
                        bars = data?.signal?.fiveG?.bars?.toInt(),
                        modifier = it,
                    )
                },
            )
        )
    }

    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            PageGrid(
                items = items,
                modifier = Modifier.fillMaxWidth().weight(1f),
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
                }
            )

            OutlinedButton(
                onClick = {
                    UserModel.token.value = null
                    UserModel.username.value = "admin"
                    UserModel.password.value = null

                    SettingsManager.username = "admin"
                    SettingsManager.password = null
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.log_out),
                )
            }
        }
    }
}
