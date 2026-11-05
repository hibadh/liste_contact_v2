package com.example.liste_de_contacts_v2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class DetailActivity : AppCompatActivity() {

    private lateinit var contact: Contact
    private var isNewContact: Boolean = false
    private var currentImageUri: String? = null

    private lateinit var ivPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText

    // Launcher pour la Galerie
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Ignore si la permission n'est pas persistante
            }
            currentImageUri = uri.toString()
            ivPhoto.setImageURI(uri)
        } else {
            Toast.makeText(this, "Sélection d'image annulée", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher pour la Caméra
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedUri = saveBitmapToCache(bitmap)
            if (savedUri != null) {
                currentImageUri = savedUri.toString()
                ivPhoto.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Prise de photo annulée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupération des vues
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvDetailTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val btnDelete = findViewById<ImageButton>(R.id.btnDeleteContact)
        val ivCameraBadge = findViewById<ImageView>(R.id.ivCameraBadge)

        ivPhoto = findViewById(R.id.ivDetailPhoto)
        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etTelephone = findViewById(R.id.etTelephone)
        etEmail = findViewById(R.id.etEmail)

        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnCall = findViewById<ImageButton>(R.id.btnCall)
        val btnSendEmail = findViewById<ImageButton>(R.id.btnSendEmail)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Récupération du contact transmis et du mode (Nouveau / Modification)
        isNewContact = intent.getBooleanExtra("is_new", false)
        val receivedContact = intent.getSerializableExtra("contact", Contact::class.java)

        if (receivedContact == null) {
            Toast.makeText(this, "Erreur : Contact non trouvé", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        contact = receivedContact
        currentImageUri = contact.imageUri

        // Titre
        tvDetailTitle.text = if (isNewContact) "Nouveau contact" else "Détail du contact"

        // Bouton retour
        btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Bouton supprimer (trash icon)
        btnDelete.setOnClickListener {
            if (!isNewContact) {
                val resultIntent = Intent().apply {
                    putExtra("is_delete", true)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        // Clic sur l'icône appareil photo du badge avatar
        ivCameraBadge.setOnClickListener {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                Toast.makeText(this, "Impossible d'ouvrir l'appareil photo", Toast.LENGTH_SHORT).show()
            }
        }

        // Remplissage des champs
        etNom.setText(contact.nom)
        etPrenom.setText(contact.prenom)
        etTelephone.setText(contact.telephone)
        etEmail.setText(contact.email)

        if (!contact.imageUri.isNullOrEmpty()) {
            try {
                ivPhoto.setImageURI(Uri.parse(contact.imageUri))
            } catch (_: Exception) {
                ivPhoto.setImageResource(R.drawable.ic_avatar)
            }
        } else {
            ivPhoto.setImageResource(R.drawable.ic_avatar)
        }

        // Action Galerie
        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Action Caméra
        btnCamera.setOnClickListener {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                Toast.makeText(this, "Impossible d'ouvrir l'appareil photo", Toast.LENGTH_SHORT).show()
            }
        }

        // Action Appeler (Intent implicite)
        btnCall.setOnClickListener {
            val phone = etTelephone.text?.toString()?.trim() ?: ""
            if (phone.isNotEmpty()) {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                try {
                    startActivity(callIntent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(this, "Aucune application de téléphone disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                etTelephone.error = "Veuillez entrer un numéro de téléphone"
            }
        }

        // Action Envoyer e-mail (Intent implicite)
        btnSendEmail.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            if (email.isNotEmpty()) {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                try {
                    startActivity(emailIntent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(this, "Aucune application de messagerie disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                etEmail.error = "Veuillez entrer une adresse e-mail"
            }
        }

        // Action Enregistrer
        btnSave.setOnClickListener {
            val nom = etNom.text?.toString()?.trim() ?: ""
            val prenom = etPrenom.text?.toString()?.trim() ?: ""
            val phone = etTelephone.text?.toString()?.trim() ?: ""
            val email = etEmail.text?.toString()?.trim() ?: ""

            if (nom.isEmpty()) {
                etNom.error = "Le nom est obligatoire"
                return@setOnClickListener
            }
            if (prenom.isEmpty()) {
                etPrenom.error = "Le prénom est obligatoire"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                etTelephone.error = "Le numéro de téléphone est obligatoire"
                return@setOnClickListener
            }

            // Mise à jour de l'objet contact
            contact.nom = nom
            contact.prenom = prenom
            contact.telephone = phone
            contact.email = email
            contact.imageUri = currentImageUri

            val resultIntent = Intent().apply {
                putExtra("updated_contact", contact)
                putExtra("is_new", isNewContact)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val file = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
