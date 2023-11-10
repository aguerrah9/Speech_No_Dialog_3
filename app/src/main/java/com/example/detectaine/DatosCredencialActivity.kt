package com.example.detectaine

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.camerax_mlkit.FaceDrawable
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class DatosCredencialActivity : AppCompatActivity() {

    private lateinit var rostroView: ImageView
    private lateinit var datosTexto: TextView
    private lateinit var botonReconocer: Button

    private val detectorTexto = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var detectorRostro : FaceDetector

    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_credencial)

        rostroView = findViewById(R.id.imageViewRostro)
        datosTexto = findViewById(R.id.textViewDatos)
        botonReconocer = findViewById(R.id.buttonReconoce)
        botonReconocer.setOnClickListener { reconoce() }

        // Recibir la ruta del archivo del Intent
        val rutaArchivo = intent.getStringExtra("rutaArchivo")

        // Cargar el Bitmap desde la memoria interna
        bitmap = rutaArchivo?.let { cargarBitmapDesdeArchivo(it) }!!

        // Haz lo que necesites con el Bitmap
        if (bitmap != null) {
            rostroView.setImageBitmap(bitmap)
            //reconoce()
        } else {
            datosTexto.setText("No se encontro la imagen")
        }

        // Real-time contour detection
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        detectorRostro = FaceDetection.getClient(realTimeOpts)


    }

    fun reconoce() {
        datosTexto.setText("Cargando...")
        botonReconocer.isEnabled = false

        val imagen = InputImage.fromBitmap(bitmap, 0)
        detectorRostro.process(imagen)
            .addOnSuccessListener { faces ->
                Log.v("faces", "faces: $faces")

                var faceBitmap: Bitmap? = null
                if (faces.size > 0) {
                    rostro = faces[0]

                    faceBitmap = Bitmap.createBitmap(
                        crooppedBitmap,
                        rostro.boundingBox.left,
                        rostro.boundingBox.top,
                        rostro.boundingBox.width(),
                        rostro.boundingBox.height()
                    )
                }
                for (face in faces) {
                    val faceDrawable = FaceDrawable(face, box.left, box.top)

                    //previewView.overlay.clear()
                    previewView.overlay.add(faceDrawable)
                }
            }
    }

    private fun cargarBitmapDesdeArchivo(rutaArchivo: String): Bitmap? {
        return BitmapFactory.decodeFile(rutaArchivo)
    }
}