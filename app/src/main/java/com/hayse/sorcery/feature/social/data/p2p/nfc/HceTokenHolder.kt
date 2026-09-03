package com.hayse.sorcery.feature.social.data.p2p.nfc

/**
 * Pont entre l'écran d'appairage (qui connaît le token de l'hôte) et [TokenHostApduService]
 * (instancié par le système, sans accès au ViewModel).
 *
 * `null` = aucun token à servir (l'hôte ne diffuse pas). Écrit uniquement pendant l'appairage.
 */
object HceTokenHolder {
    @Volatile
    var token: String? = null
}
