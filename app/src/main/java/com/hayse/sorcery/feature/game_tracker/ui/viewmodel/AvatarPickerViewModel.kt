package com.hayse.sorcery.feature.game_tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.cards.domain.model.CardFilter
import com.hayse.sorcery.feature.cards.domain.repository.CardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AvatarPickerViewModel(
    cardRepository: CardRepository,
) : ViewModel() {

    val avatars: StateFlow<List<Card>> = cardRepository
        .observeCards(CardFilter(type = AVATAR_TYPE))
        .map { entries -> entries.map { it.card }.distinctBy { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        const val AVATAR_TYPE = "Avatar"
    }
}
