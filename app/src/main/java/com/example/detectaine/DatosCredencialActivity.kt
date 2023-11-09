package com.example.detectaine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.widget.ImageView
import android.widget.TextView
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.text.Text

class DatosCredencialActivity : AppCompatActivity() {

    private lateinit var rostroView: ImageView
    private lateinit var datosTexto: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_credencial)

        rostroView = findViewById(R.id.imageViewRostro)
        datosTexto = findViewById(R.id.textViewDatos)

        // Recibir datos de diferentes tipos
        val clave = intent.getStringExtra("clave")
        val rostro = intent.getSerializableExtra("rostro") as Face // El segundo parámetro es el valor predeterminado en caso de que no se encuentre el dato
        val textos = intent.getSerializableExtra("textos") as Text // El segundo parámetro es el valor predeterminado

        datosTexto.setText("aqui van los datos")
    }
}