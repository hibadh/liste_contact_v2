package com.example.liste_de_contacts_v2

import java.io.Serializable

/**
 * Modèle de données représentant un Contact.
 * Implémente Serializable pour permettre le transfert via Intent entre activités.
 */
data class Contact(
    val id: Int,
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var imageUri: String? = null // Stocke l'URI sous forme de String (ex: content://... ou android.resource://...)
) : Serializable
