package com.example.detectaine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
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

    private var textosyPosiciones = mutableListOf<TextoPosicion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_credencial)

        rostroView = findViewById(R.id.imageViewRostro)
        datosTexto = findViewById(R.id.textViewDatos)
        //datosTexto.movementMethod = ScrollingMovementMethod()
        datosTexto.setMovementMethod(ScrollingMovementMethod())
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

                    if (rostro.boundingBox.left >= 0 && rostro.boundingBox.top >= 0 &&
                                rostro.boundingBox.width() <= bitmap.width &&
                                rostro.boundingBox.height() <= bitmap.height
                    ) {
                        faceBitmap = Bitmap.createBitmap(
                            bitmap,
                            rostro.boundingBox.left,
                            rostro.boundingBox.top,
                            rostro.boundingBox.width(),
                            rostro.boundingBox.height()
                        )
                    } else {
                        Log.v("Recortar Rostro: ","no fue posible obtener la imagen completa del rostro" )
                    }
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
                    for (linea in textBlock.lines) {
                        val textDrawable = TextDrawable(linea)
                        rostroView.overlay.add(textDrawable)
                        textosBox += (linea.boundingBox!!.left.toFloat() * 100 / bitmap.width).toString() + " " +
                                (linea.boundingBox!!.top.toFloat() * 100 / bitmap.height).toString() + " " + linea.text + "\n"
                        textosyPosiciones.add(
                            TextoPosicion(
                                linea.boundingBox!!.left.toDouble() / bitmap.width,
                                linea.boundingBox!!.top.toDouble() / bitmap.height,
                                linea.text
                            )
                        )
                    }
                }
                datosTexto.setText(textosBox)
            }
            .addOnCompleteListener {
                botonReconocer.isEnabled = true
                calcularOrigen()
            }
    }

    private fun calcularOrigen() {

        val labelMexico = textosyPosiciones.filter { it.texto == "MÉXICO" }
        val labelINE = textosyPosiciones.filter { it.texto == "INSTITUTO FEDERAL ELECTORAL" || it.texto == "INSTITUTO NACIONAL ELECTORAL" }
        val labelCredencial = textosyPosiciones.filter { it.texto == "CREDENCIAL PARA VOTAR" }
        val labelNombre = textosyPosiciones.filter { it.texto == "NOMBRE" }

        if (labelMexico.size > 0 && labelNombre.size > 0) {
            val difLeft = labelMexico[0].left - labelNombre[0].left
            val difTop = labelMexico[0].top - labelNombre[0].top

            // INE posicion ideal w 35 h 8
            // Mexico posicion ideal w 20 h 10

            //val origenX =

            datosTexto.setText(
                "labelMexico: "+ labelMexico[0].toString()+ "\n\n" +
                "labelINE: " + labelINE[0].toString()+ "\n\n"+
                        "labelCredencial: " + labelCredencial[0].toString() + "\n\n"+
                        "labelNombre: " + labelNombre[0].toString()+ "\n\n" +
                        "Diferencia: "+ difLeft +" "+ difTop
            )
        } else {
            datosTexto.setText("No se pudo calcular el origen")
        }
    }

    private fun cargarBitmapDesdeArchivo(rutaArchivo: String): Bitmap? {
        return BitmapFactory.decodeFile(rutaArchivo)
    }
}

data class TextoPosicion(val left: Double, val top: Double, val texto: String)