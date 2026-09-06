package com.hayse.sorcery.feature.collection

import com.hayse.sorcery.feature.collection.data.csv.CuriosaCsvParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CuriosaCsvParserTest {

    @Test
    fun `parse ligne simple`() {
        val csv = """
            card name,set,finish,product,quantity,notes
            Sea Serpent,Alpha,Standard,Booster,3,
        """.trimIndent()
        val result = CuriosaCsvParser.parse(csv)
        assertEquals(1, result.rows.size)
        val row = result.rows.first()
        assertEquals("Sea Serpent", row.cardName)
        assertEquals("Alpha", row.set)
        assertEquals("Standard", row.finish)
        assertEquals("Booster", row.product)
        assertEquals(3, row.quantity)
        assertTrue(result.invalid.isEmpty())
    }

    @Test
    fun `product normalise les espaces en underscores`() {
        val csv = """
            card name,set,finish,product,quantity
            Foot Soldier,Beta,Foil,Box Topper,1
        """.trimIndent()
        val row = CuriosaCsvParser.parse(csv).rows.single()
        assertEquals("Box_Topper", row.product)
    }

    @Test
    fun `product camelCase normalise en souligne`() {
        val csv = """
            card name,set,finish,product,quantity
            Bruin,Arthurian Legends,Standard,BoxTopper,1
        """.trimIndent()
        val row = CuriosaCsvParser.parse(csv).rows.single()
        assertEquals("Box_Topper", row.product)
    }

    @Test
    fun `nouveau format Curiosa avec BOM et Set Name`() {
        val csv = "﻿Card Name,Card Slug,Quantity,Set Name,Printed At,Product,Finish,Notes\n" +
            "\"Aaj-kegon Ghost Crabs\",\"aaj_kegon_ghost_crabs\",\"1\",\"Gothic\",\"2025-12-05T07:00:00.000Z\",\"Booster\",\"Standard\",\"\""
        val result = CuriosaCsvParser.parse(csv)
        assertTrue(result.invalid.isEmpty())
        val row = result.rows.single()
        assertEquals("Aaj-kegon Ghost Crabs", row.cardName)
        assertEquals("Gothic", row.set)
        assertEquals("Standard", row.finish)
        assertEquals("Booster", row.product)
        assertEquals(1, row.quantity)
    }

    @Test
    fun `alias Set Name reconnu`() {
        val csv = """
            card name,set name,finish,product,quantity
            Sea Serpent,Alpha,Standard,Booster,3
        """.trimIndent()
        val row = CuriosaCsvParser.parse(csv).rows.single()
        assertEquals("Alpha", row.set)
    }

    @Test
    fun `ordre des colonnes indifferent`() {
        val csv = """
            quantity,product,finish,set,card name
            2,Booster,Standard,Alpha,Sea Serpent
        """.trimIndent()
        val row = CuriosaCsvParser.parse(csv).rows.single()
        assertEquals("Sea Serpent", row.cardName)
        assertEquals(2, row.quantity)
    }

    @Test
    fun `champs entre guillemets avec virgule`() {
        val csv = "card name,set,finish,product,quantity\n\"Serpent, Sea\",Alpha,Standard,Booster,1"
        val row = CuriosaCsvParser.parse(csv).rows.single()
        assertEquals("Serpent, Sea", row.cardName)
    }

    @Test
    fun `guillemets echappes`() {
        val csv = "card name,set,finish,product,quantity\n\"The \"\"Great\"\" One\",Alpha,Standard,Booster,1"
        val row = CuriosaCsvParser.parse(csv).rows.single()
        assertEquals("The \"Great\" One", row.cardName)
    }

    @Test
    fun `quantite invalide collectee dans invalid`() {
        val csv = """
            card name,set,finish,product,quantity
            Sea Serpent,Alpha,Standard,Booster,abc
        """.trimIndent()
        val result = CuriosaCsvParser.parse(csv)
        assertTrue(result.rows.isEmpty())
        assertEquals(1, result.invalid.size)
    }

    @Test
    fun `en-tete manquant rejette le corps`() {
        val csv = """
            name,set,finish,product,quantity
            Sea Serpent,Alpha,Standard,Booster,1
        """.trimIndent()
        val result = CuriosaCsvParser.parse(csv)
        assertTrue(result.rows.isEmpty())
        assertEquals(1, result.invalid.size)
    }

    @Test
    fun `lignes vides ignorees`() {
        val csv = "card name,set,finish,product,quantity\n\nSea Serpent,Alpha,Standard,Booster,1\n\n"
        val result = CuriosaCsvParser.parse(csv)
        assertEquals(1, result.rows.size)
        assertTrue(result.invalid.isEmpty())
    }
}
