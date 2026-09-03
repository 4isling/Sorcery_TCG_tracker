package com.hayse.sorcery.feature.social.domain.usecase

import com.hayse.sorcery.feature.social.domain.model.TradeableItem
import com.hayse.sorcery.feature.social.domain.model.WantedItem
import com.hayse.sorcery.feature.social.domain.repository.TradeListRepository
import kotlinx.coroutines.flow.Flow

class ObserveTradeableUseCase(private val repository: TradeListRepository) {
    operator fun invoke(): Flow<List<TradeableItem>> = repository.observeTradeable()
}

class ObserveWantedUseCase(private val repository: TradeListRepository) {
    operator fun invoke(): Flow<List<WantedItem>> = repository.observeWanted()
}

class SetTradeableUseCase(private val repository: TradeListRepository) {
    suspend operator fun invoke(slug: String, finish: String, quantity: Int) =
        repository.setTradeable(slug, finish, quantity)
}

class SetWantedUseCase(private val repository: TradeListRepository) {
    suspend operator fun invoke(slug: String, finish: String, quantity: Int) =
        repository.setWanted(slug, finish, quantity)
}
