package com.example.detectaine

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.camerax_mlkit.FaceDrawable
import com.example.camerax_mlkit.TextDrawable
import com.example.detectaine.drawables.CirculoDrawable
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.atan


class DatosCredencialActivity : AppCompatActivity() {

    private lateinit var rostroView: ImageView
    private lateinit var datosTexto: TextView
    private lateinit var botonReconocer: Button

    private lateinit var objectDetector: ObjectDetector
    private val detectorTexto = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var detectorRostro : FaceDetector

    private lateinit var bitmap: Bitmap

    private val anguloMiIdealRad = atan( (-2).toFloat() /15 )
    private val anguloMiIdeal = (anguloMiIdealRad * 180) / Math.PI

    private var textosyPosiciones = mutableListOf<TextoPosicion>()
    private var anguloGeneralRotacion = 0.0

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

        val localModel =
            LocalModel.Builder().setAssetFilePath("custom_models/object_labeler.tflite").build()
        // Live detection and tracking
        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                //.setClassificationConfidenceThreshold(0.5f)
                //.setMaxPerObjectLabelCount(3)
                .build()
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

        // Real-time contour detection
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        detectorRostro = FaceDetection.getClient(realTimeOpts)

        // Haz lo que necesites con el Bitmap
        if (bitmap != null) {
            //rostroView.setImageBitmap(bitmap)
            reconoce()
        } else {
            datosTexto.setText("No se encontro la imagen")
        }

    }

    fun reconoce() {
        datosTexto.setText("Cargando...")
        botonReconocer.isEnabled = false
        rostroView.overlay.clear()

        //rostroView.setImageBitmap(null)
        rostroView.setImageBitmap(bitmap)

        // Obtener el Drawable desde el ImageView
        val drawable: Drawable? = rostroView.drawable

        // Verificar si el drawable es una instancia de BitmapDrawable
        if (drawable is BitmapDrawable) {
            // Obtener el Bitmap del BitmapDrawable
            bitmap = drawable.bitmap

            // Ahora tienes el Bitmap que representa el contenido del ImageView
            // Haz lo que necesites con el bitmap
        }

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

        textosyPosiciones.clear()
        detectorTexto.process(imagen)
            .addOnSuccessListener { labels ->
                Log.v("Reconocedor textos", "labels: " + labels.text)

                var textosBox = ""
                for ( textBlock in labels?.textBlocks!! ) {
                    for (linea in textBlock.lines) {
                        val textDrawable = TextDrawable(linea)
                        rostroView.overlay.add(textDrawable)
                        textosBox += (linea.boundingBox!!.left.toFloat()).toString() + " " +
                                (linea.boundingBox!!.top.toFloat()).toString() + " " + linea.text + "\n"
                        textosyPosiciones.add(
                            TextoPosicion(
                                linea.boundingBox!!.left.toDouble(),
                                linea.boundingBox!!.top.toDouble(),
                                linea.text
                            )
                        )
                    }
                }
                datosTexto.setText( "tamaño imagen: "+ bitmap.width +" "+bitmap.height+ "\n\n"
                        + textosBox)
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
        val labelDomicilio = textosyPosiciones.filter { it.texto == "FECHA DE NACIMIENTO" }

        if (labelNombre.isNotEmpty() && labelDomicilio.isNotEmpty()
            //&& labelINE.size > 0 && labelMexico.size > 0
        ) {

            /*val diffmix = labelINE[0].left - labelMexico[0].left
            val diffmiy = labelINE[0].top - labelMexico[0].top
            val mi = atan( (diffmiy/diffmix) )
            val angmi = mi * 180 / Math.PI
            val anguloRotacion = angmi - anguloMiIdeal
            var rotacion = ""

            if (anguloRotacion < 0) rotacion = "izquierda"
            else if (anguloRotacion > 0) rotacion = "derecha"
            else rotacion = "sin rotacion"*/

            val difLeft = labelDomicilio[0].left - labelNombre[0].left
            val difTop = labelDomicilio[0].top - labelNombre[0].top

            val rads = atan(difLeft/difTop)
            val theta = (rads * 180) / Math.PI
            anguloGeneralRotacion = theta

            //if (theta < 0) theta = 180 - theta
            //val rotacion2 = 90 - theta

            // INE posicion ideal w 35 h 8
            // Mexico posicion ideal w 20 h 10

            // (10.08547 - 6.324786) = 3.760684
            // (37.446808 - 23.19149) = 14.255318
            // 3.760684 / 14.255318 = 0.2638
            // atan( 0.2638 ) =

            val textoAnterior = datosTexto.text.toString()
            datosTexto.setText( textoAnterior + "\n\n" +
                    "angulo: "+ theta + "\n\n"
            )

            if (anguloGeneralRotacion >= 0.5 || anguloGeneralRotacion <= -0.5) {
                // Rotar el Bitmap
                val matrix = Matrix()
                matrix.preRotate(anguloGeneralRotacion.toFloat()) // Rotar 90 grados en sentido horario

                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                //val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap,bitmap.width,bitmap.height, true)
                val scaledBitmap = Bitmap.createBitmap(
                    rotatedBitmap,
                    rotatedBitmap.width/2 - bitmap.width/2,
                    rotatedBitmap.height/2 - bitmap.height/2,
                    bitmap.width,
                    bitmap.height
                )

                //rostroView.setImageBitmap(rotatedBitmap)
                bitmap = scaledBitmap
                //reconoce()
            } else {
                muestraOrigen()
            }

        } else {
            val textoAnterior = datosTexto.text.toString()
            datosTexto.setText(textoAnterior + "\n\nNo se pudo calcular el angulo de rotacion")
        }
    }

    private fun muestraOrigen() {
        val labelMexico = textosyPosiciones.filter { it.texto == "MÉXICO" }
        val labelINE = textosyPosiciones.filter { it.texto == "INSTITUTO FEDERAL ELECTORAL" || it.texto == "INSTITUTO NACIONAL ELECTORAL" }
        val labelCredencial = textosyPosiciones.filter { it.texto == "CREDENCIAL PARA VOTAR" }

        if (labelMexico.isNotEmpty() && labelINE.isNotEmpty() ) {
            val difx = labelINE[0].left - labelMexico[0].left // este debe ser proporcion 15
            val dify = labelINE[0].top - labelMexico[0].top // este debe ser proporcion -2

            val posOy = labelINE[0].top - dify*(8.toDouble()/-3)
            val pos0x = labelINE[0].left - difx*(35.toDouble()/15)

            val posOy2 = labelMexico[0].top - dify*(10.toDouble()/-3)
            val pos0x2 = labelMexico[0].left - difx*(20.toDouble()/15)

            val circuloOrigen = CirculoDrawable(pos0x,posOy)
            rostroView.overlay.add(circuloOrigen)

            val circuloOrigen2 = CirculoDrawable(pos0x,posOy)
            rostroView.overlay.add(circuloOrigen2)

            val esquina1 = CirculoDrawable(0.0,0.0)
            rostroView.overlay.add(esquina1)
            val esquina2 = CirculoDrawable(0.0,bitmap.height.toDouble())
            rostroView.overlay.add(esquina2)
            val esquina3 = CirculoDrawable(bitmap.width.toDouble(),0.0)
            rostroView.overlay.add(esquina3)
            val esquina4 = CirculoDrawable(bitmap.width.toDouble(), bitmap.height.toDouble())
            rostroView.overlay.add(esquina4)

            val textoAnterior = datosTexto.text.toString()
            datosTexto.setText( textoAnterior + ""+ "\n\n"+
                "INE: "+ labelINE[0].left.toString() + " " + labelINE[0].top.toString() + "\n\n"+
                "Mexico: "+ labelMexico[0].left.toString() + " " + labelMexico[0].top.toString() + "\n\n"+
                "difere: "+ difx.toString() + " " + dify.toString() + "\n\n"+
                "origen: "+ pos0x.toString() + " " + posOy.toString() + "\n\n"+
                "orige2: "+ pos0x2.toString() + " " + posOy2.toString() + "\n\n"
            )
        }
    }
    private fun identificaTextos() {
        for (texto in textosyPosiciones) {
            if (texto.texto == "DOMICILIO") {
                var distancias = mutableListOf<TextoDistancia>()
                for (otrotexto in textosyPosiciones) {
                    val distancia = distancia(texto,otrotexto)
                    distancias.add(TextoDistancia(distancia,otrotexto.texto))
                }
                distancias.sortBy { it.distancia }
                datosTexto.setText(distancias.toString())
            }
        }
    }

    private fun distancia( texto1:TextoPosicion, texto2:TextoPosicion ) : Double {
        val difx = texto1.left - texto2.left
        val dify = texto1.top - texto2.top
        val cuadrados = difx*difx + dify*dify
        val dist = Math.sqrt(cuadrados)
        return dist
    }

    private fun cargarBitmapDesdeArchivo(rutaArchivo: String): Bitmap? {
        return BitmapFactory.decodeFile(rutaArchivo)
    }
}

data class TextoPosicion(val left: Double, val top: Double, val texto: String)
data class TextoDistancia(val distancia: Double, val texto: String)