package com.example.liste_de_contacts_v2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // Contacts initiaux correspondant exactement à la maquette du PDF
    private val contactList = mutableListOf(
        Contact(1, "Dupont", "Claire", "06 12 34 56 78", "claire.dupont@example.com"),
        Contact(2, "Martin", "Thomas", "07 23 45 67 89", "thomas.martin@example.com"),
        Contact(3, "Bernard", "Sophie", "06 98 76 54 32", "sophie.bernard@example.com"),
        Contact(4, "Petit", "Lucas", "07 11 22 33 44", "lucas.petit@example.com"),
        Contact(5, "Moreau", "Julien", "06 55 44 33 22", "julien.moreau@example.com")
    )

    private lateinit var adapter: ContactAdapter
    private var selectedPosition: Int = -1

    private val detailActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val isDelete = result.data?.getBooleanExtra("is_delete", false) ?: false
            if (isDelete && selectedPosition in contactList.indices) {
                contactList.removeAt(selectedPosition)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Contact supprimé !", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val updatedContact = result.data?.getSerializableExtra("updated_contact", Contact::class.java)
            val isNew = result.data?.getBooleanExtra("is_new", false) ?: false

            if (updatedContact != null) {
                if (isNew || selectedPosition !in contactList.indices) {
                    contactList.add(updatedContact)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Nouveau contact ajouté !", Toast.LENGTH_SHORT).show()
                } else {
                    contactList[selectedPosition] = updatedContact
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Contact mis à jour avec succès !", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listView = findViewById<ListView>(R.id.listViewContacts)
        val btnAddHeader = findViewById<ImageButton>(R.id.btnAddContactHeader)

        adapter = ContactAdapter(this, contactList)
        listView.adapter = adapter

        // Clic sur un contact de la liste -> Modification
        listView.setOnItemClickListener { _, _, position, _ ->
            selectedPosition = position
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("contact", contactList[position])
                putExtra("is_new", false)
            }
            detailActivityLauncher.launch(intent)
        }

        // Action de création d'un nouveau contact (bouton + dans l'en-tête)
        btnAddHeader.setOnClickListener {
            selectedPosition = -1
            val newId = (contactList.maxOfOrNull { it.id } ?: 0) + 1
            val newContact = Contact(newId, "", "", "", "")
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("contact", newContact)
                putExtra("is_new", true)
            }
            detailActivityLauncher.launch(intent)
        }
    }
}
