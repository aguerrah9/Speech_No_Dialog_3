package com.example.detectaine

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.camerax_mlkit.FaceDrawable
import com.example.camerax_mlkit.TextDrawable
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

        // Real-time contour detection
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        detectorRostro = FaceDetection.getClient(realTimeOpts)

        // Haz lo que necesites con el Bitmap
        if (bitmap != null) {
            rostroView.setImageBitmap(bitmap)
            reconoce()
        } else {
            datosTexto.setText("No se encontro la imagen")
        }

    }

    fun reconoce() {
        datosTexto.setText("Cargando...")
        botonReconocer.isEnabled = false
        rostroView.overlay.clear()

        val imagen = InputImage.fromBitmap(bitmap, 0)
        detectorRostro.process(imagen)
            .addOnSuccessListener { faces ->
                Log.v("faces", "faces: $faces")

                var faceBitmap: Bitmap? = null
                if (faces.size > 0) {
                    val rostro = faces[0]

                    faceBitmap = Bitmap.createBitmap(
                        bitmap,
                        rostro.boundingBox.left,
                        rostro.boundingBox.top,
                        rostro.boundingBox.width(),
                        rostro.boundingBox.height()
                    )
                }
                for (face in faces) {
                    val faceDrawable = FaceDrawable(face)

                    //previewView.overlay.clear()
                    rostroView.overlay.add(faceDrawable)
                }
            }
            .addOnCompleteListener {
                botonReconocer.isEnabled = true
            }

        detectorTexto.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { labels ->
                Log.v("Reconocedor textos", "labels: " + labels.text)

                var textosBox = ""
                for ( textBlock in labels?.textBlocks!! ) {
                    val textDrawable = TextDrawable(textBlock)
                    rostroView.overlay.add(textDrawable)
                    textosBox += textBlock.boundingBox?.left.toString() +" "+
                            textBlock.boundingBox?.top.toString()  +" "+ textBlock.text +"\n"
                }
                datosTexto.setText(textosBox)
            }
            .addOnCompleteListener {
                botonReconocer.isEnabled = true
            }
    }

    private fun cargarBitmapDesdeArchivo(rutaArchivo: String): Bitmap? {
        return BitmapFactory.decodeFile(rutaArchivo)
    }
}