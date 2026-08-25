package com.hayse.sorcery.feature.collection.data.csv

import com.hayse.sorcery.feature.collection.domain.model.CuriosaRow

/** Résultat du parsing : lignes valides + lignes rejetées (brutes, pour le rapport). */
data class CuriosaParseResult(
    val rows: List<CuriosaRow>,
    val invalid: List<String>,
)

/**
 * Parseur du CSV d'export Curiosa (colonnes : card name, set, finish, product, quantity, notes).
 *
 * - Lecture **par en-tête** (tolérant à l'ordre des colonnes).
 * - Champs entre guillemets gérés (virgules, retours à la ligne et guillemets échappés `""`).
 * - `product` normalisé (`Box Topper` -> `Box_Topper`).
 * - Ligne sans quantité entière valide ou colonnes requises manquantes -> `invalid`.
 */
object CuriosaCsvParser {

    private val REQUIRED = listOf("card name", "set", "finish", "product", "quantity")

    fun parse(text: String): CuriosaParseResult {
        val records = tokenize(text)
        if (records.isEmpty()) return CuriosaParseResult(emptyList(), emptyList())

        val header = records.first().map { it.trim().lowercase() }
        val index = REQUIRED.associateWith { header.indexOf(it) }
        if (index.values.any { it < 0 }) {
            // En-tête inexploitable : tout le corps est rejeté.
            return CuriosaParseResult(emptyList(), records.drop(1).map { it.joinToString(",") })
        }

        val rows = mutableListOf<CuriosaRow>()
        val invalid = mutableListOf<String>()
        for (record in records.drop(1)) {
            if (record.all { it.isBlank() }) continue
            fun field(name: String): String = record.getOrNull(index.getValue(name))?.trim().orEmpty()
            val qty = field("quantity").toIntOrNull()
            val name = field("card name")
            if (qty == null || name.isEmpty()) {
                invalid += record.joinToString(",")
                continue
            }
            rows += CuriosaRow(
                cardName = name,
                set = field("set"),
                finish = field("finish"),
                product = normalizeProduct(field("product")),
                quantity = qty,
            )
        }
        return CuriosaParseResult(rows, invalid)
    }

    fun normalizeProduct(raw: String): String = raw.trim().replace(" ", "_")

    /** Découpe le texte CSV en enregistrements de champs (RFC4180 minimal). */
    private fun tokenize(text: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        var sawAny = false

        fun endField() {
            fields.add(field.toString())
            field.setLength(0)
        }
        fun endRecord() {
            endField()
            records.add(fields)
            fields = mutableListOf()
            sawAny = false
        }

        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> when {
                    c == '"' && i + 1 < text.length && text[i + 1] == '"' -> { field.append('"'); i++ }
                    c == '"' -> inQuotes = false
                    else -> field.append(c)
                }
                c == '"' -> { inQuotes = true; sawAny = true }
                c == ',' -> { endField(); sawAny = true }
                c == '\r' -> { /* ignore, géré par \n */ }
                c == '\n' -> if (sawAny || field.isNotEmpty() || fields.isNotEmpty()) endRecord()
                else -> { field.append(c); sawAny = true }
            }
            i++
        }
        if (field.isNotEmpty() || fields.isNotEmpty() || sawAny) endRecord()
        return records
    }
}
