package com.hayse.sorcery.feature.game_tracker.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.skin.SetSkin
import com.hayse.sorcery.core.ui.theme.skin.SkinnedBackground
import com.hayse.sorcery.core.ui.theme.skin.Skins

private class SkinPreviewProvider : PreviewParameterProvider<SetSkin> {
    override val values: Sequence<SetSkin> = Skins.all.asSequence()
}

@Preview(name = "GameMenu — skins", showBackground = true)
@Composable
private fun GameMenuScreenSkinPreview(
    @PreviewParameter(SkinPreviewProvider::class) skin: SetSkin,
) {
    SorceryTheme(skin = skin) {
        SkinnedBackground {
            GameMenuScreen(onNewGame = {}, onHistory = {})
        }
    }
}
