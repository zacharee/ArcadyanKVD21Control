package dev.zwander.common.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.common.GradleConfig
import dev.zwander.common.model.SettingsModel
import dev.zwander.common.util.UrlHandler
import dev.zwander.resources.common.MR

private data class SocialIconData(
    val img: ImageResource,
    val link: String,
    val desc: StringResource,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.About() {
    val socialIcons = remember {
        listOf(
            SocialIconData(
                img = MR.images.github,
                link = "https://github.com/zacharee/ArcadyanKVD21Control",
                desc = MR.strings.github,
            ),
            SocialIconData(
                img = MR.images.mastodon,
                link = "https://androiddev.social/@wander1236",
                desc = MR.strings.mastodon,
            ),
            SocialIconData(
                img = MR.images.patreon,
                link = "https://www.patreon.com/zacharywander",
                desc = MR.strings.patreon,
            ),
            SocialIconData(
                img = MR.images.web,
                link = "https://zwander.dev",
                desc = MR.strings.website,
            ),
        )
    }

    FlowRow(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(),
    ) {
        socialIcons.forEach { (img, link, desc) ->
            IconButton(
                onClick = {
                    UrlHandler.launchUrl(link)
                },
            ) {
                Icon(
                    painter = painterResource(img),
                    contentDescription = stringResource(desc),
                )
            }
        }
    }
}

@Composable
fun AboutTitleAddon() {
    var clickCount by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(clickCount) {
        if (clickCount >= 10) {
            clickCount = 0
            SettingsModel.fuzzerEnabled.value = !SettingsModel.fuzzerEnabled.value
        }
    }

    Text(
        text = GradleConfig.versionName,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.clickable(
            enabled = true,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {
                clickCount++
            },
        )
    )
}
