package com.hayse.sorcery.feature.social.domain.model

/** Une correspondance d'échange sur une impression précise. */
data class MatchLine(
    val slug: String,
    val finish: String,
    val quantity: Int,
)
