package com.hayse.sorcery.feature.cards.data.repository

import android.content.Context

/**
 * Choisit l'image représentative d'une carte parmi ses impressions.
 *
 * Toutes les impressions n'ont pas d'image embarquée dans `assets/cards/images` : certains avatars
 * (ex. Gothic) ont pour première impression une promo sans visuel. On préfère donc la première
 * impression dont le `.webp` existe réellement, avec repli sur la première impression.
 */
class CardImageResolver(context: Context) {

    private val availableSlugs: Set<String> by lazy {
        runCatching {
            context.assets.list(IMAGE_DIR)
                ?.filter { it.endsWith(WEBP_SUFFIX) }
                ?.map { it.removeSuffix(WEBP_SUFFIX) }
                ?.toSet()
        }.getOrNull().orEmpty()
    }

    fun imageUriForSlugs(slugs: List<String>): String? {
        val slug = slugs.firstOrNull { it in availableSlugs } ?: slugs.firstOrNull() ?: return null
        return "$IMAGE_BASE/$slug$WEBP_SUFFIX"
    }

    companion object {
        private const val IMAGE_DIR = "cards/images"
        private const val IMAGE_BASE = "file:///android_asset/cards/images"
        private const val WEBP_SUFFIX = ".webp"
    }
}
