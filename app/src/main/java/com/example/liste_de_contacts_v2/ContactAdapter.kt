package com.example.liste_de_contacts_v2

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

/**
 * Adaptateur personnalisé pour afficher la liste de contacts dans la ListView.
 */
class ContactAdapter(
    context: Context,
    private val contacts: List<Contact>
) : ArrayAdapter<Contact>(context, 0, contacts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_contact,
            parent,
            false
        )

        val contact = getItem(position) ?: return view

        val ivPhoto = view.findViewById<ImageView>(R.id.ivContactPhoto)
        val tvName = view.findViewById<TextView>(R.id.tvContactName)
        val tvPhone = view.findViewById<TextView>(R.id.tvContactPhone)

        tvName.text = "${contact.prenom} ${contact.nom}"
        tvPhone.text = contact.telephone

        if (!contact.imageUri.isNullOrEmpty()) {
            try {
                ivPhoto.setImageURI(Uri.parse(contact.imageUri))
            } catch (e: Exception) {
                ivPhoto.setImageResource(R.drawable.ic_avatar)
            }
        } else {
            ivPhoto.setImageResource(R.drawable.ic_avatar)
        }

        return view
    }
}
