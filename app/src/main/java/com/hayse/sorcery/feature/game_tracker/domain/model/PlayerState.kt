package com.hayse.sorcery.feature.game_tracker.domain.model

import com.hayse.sorcery.core.shared.model.Element

data class PlayerState(
    val life: Int,
    val avatarStatus: AvatarStatus = AvatarStatus.Active,
    val sitesControlled: Int = 0,
    val manaAvailable: Int = 0,
    val affinity: Map<Element, Int> = Element.entries.associateWith { 0 },
    val avatarName: String? = null,
    val avatarImageUri: String? = null,
    val pseudo: String? = null,
)
