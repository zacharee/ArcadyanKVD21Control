package dev.zwander.android

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.App
import dev.zwander.common.model.SettingsModel
import dev.zwander.common.widget.ConnectionStatusWidgetReceiver
import dev.zwander.resources.common.MR
import dev.zwander.resources.common.close_app
import dev.zwander.resources.common.grant
import dev.zwander.resources.common.local_network_permission
import dev.zwander.resources.common.local_network_permission_desc
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : AppCompatActivity() {
    private var showingLocalNetworkAccessDialog by mutableStateOf(false)
    private val appWidgetManager by lazy { getSystemService(APPWIDGET_SERVICE) as AppWidgetManager }
    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            finish()
        } else {
            showingLocalNetworkAccessDialog = false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            @Suppress("DEPRECATION")
            window.isStatusBarContrastEnforced = false
        }
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.TRANSPARENT
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        supportActionBar?.hide()

        setContent {
            val widgetRefresh by SettingsModel.widgetRefresh.collectAsState()

            LaunchedEffect(widgetRefresh) {
                updateWidgetRefresh()
            }

            LaunchedEffect(null) {
                showingLocalNetworkAccessDialog = !hasLocalNetworkAccess()
            }

            App(
                modifier = Modifier.imePadding(),
                fullPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
            )

            if (showingLocalNetworkAccessDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showingLocalNetworkAccessDialog = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    sheetGesturesEnabled = false,
                    dragHandle = null,
                    properties = ModalBottomSheetProperties(
                        shouldDismissOnBackPress = false,
                        shouldDismissOnClickOutside = false,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            top = 32.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        ).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(MR.strings.local_network_permission),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(
                            text = stringResource(MR.strings.local_network_permission_desc),
                        )

                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                onClick = {
                                    finish()
                                },
                            ) {
                                Text(text = stringResource(MR.strings.close_app))
                            }

                            TextButton(
                                onClick = {
                                    requestPermissionsLauncher.launch(
                                        @SuppressLint("InlinedApi")
                                        android.Manifest.permission.ACCESS_LOCAL_NETWORK,
                                    )
                                },
                            ) {
                                Text(text = stringResource(MR.strings.grant))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateWidgetRefresh() {
        App.instance.cancelWidgetRefresh()

        if (appWidgetManager.getAppWidgetIds(ComponentName(this, ConnectionStatusWidgetReceiver::class.java)).isNotEmpty()) {
            App.instance.scheduleWidgetRefresh()
        }
    }

    private fun hasLocalNetworkAccess(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN ||
                checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_LOCAL_NETWORK) ==
                PackageManager.PERMISSION_GRANTED
    }
}
