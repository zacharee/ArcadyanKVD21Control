@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")
@file:JvmName("AlertDialogSkia")

package dev.zwander.common.components.dialog

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.zwander.common.locals.LocalMenuBarHeight
import kotlin.jvm.JvmName

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformAlertDialog(
    showing: Boolean,
    onDismissRequest: () -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    text: (@Composable ColumnScope.() -> Unit)?,
    shape: Shape,
    backgroundColor: Color,
    contentColor: Color,
    maxWidth: Dp,
) {
    val alpha by animateFloatAsState(
        if (showing) 1f else 0f,
        spring(stiffness = Spring.StiffnessMediumLow),
    )
    val showingForAnimation by derivedStateOf {
        alpha > 0f
    }
    val properties = PopupProperties(
        focusable = true,
        dismissOnClickOutside = true,
        dismissOnBackPress = true,
        usePlatformInsets = false,
    )
    val focusRequester = remember { FocusRequester() }
    val safeAreaInsets = WindowInsets.safeContent.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current

    val safeAreaStart = safeAreaInsets.calculateStartPadding(layoutDirection)
    val safeAreaEnd = safeAreaInsets.calculateEndPadding(layoutDirection)

    LaunchedEffect(showing, alpha, showingForAnimation, focusRequester.focusRequesterNodes.size) {
        if (showing && alpha == 1f && focusRequester.focusRequesterNodes.isNotEmpty()) {
            focusRequester.requestFocus()
        }

        if (!showing && showingForAnimation) {
            focusRequester.freeFocus()
        }
    }

    if (showing || showingForAnimation) {
        Popup(
            alignment = Alignment.Center,
            properties = properties,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f * alpha))
                    .fillMaxSize()
                    .onClick { onDismissRequest() }
                    .padding(
                        start = safeAreaStart,
                        top = safeAreaInsets.calculateTopPadding().takeIf { it > 0.dp } ?: (16.dp + LocalMenuBarHeight.current),
                        end = safeAreaEnd,
                        bottom = safeAreaInsets.calculateBottomPadding().takeIf { it > 0.dp } ?: 16.dp,
                    )
                    .alpha(alpha)
                    .onPreviewKeyEvent {
                        if (it.key == Key.Escape) {
                            onDismissRequest()
                            true
                        } else {
                            false
                        }
                    }
                    .focusable(true)
                    .focusRequester(focusRequester),
                contentAlignment = Alignment.Center,
            ) {
                with(LocalDensity.current) {
                    AlertDialogContents(
                        buttons,
                        modifier.then(
                            Modifier.widthIn(
                                max = minOf(
                                    constraints.maxWidth.toDp() - (32.dp.takeIf { safeAreaStart <= 0.dp && safeAreaEnd <= 0.dp } ?: 0.dp),
                                    maxWidth,
                                )
                            ).onClick {
                                // To prevent the Box's onClick consuming clicks on the dialog itself.
                            }.animateContentSize(),
                        ),
                        title,
                        text,
                        shape,
                        backgroundColor,
                        contentColor,
                    )
                }
            }
        }
    }
}
