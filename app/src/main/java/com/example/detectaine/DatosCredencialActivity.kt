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
import com.example.detectaine.drawables.LineaDrawable
import com.example.detectaine.drawables.RectDrawable
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
import java.lang.Math.abs
import kotlin.math.atan
import kotlin.math.roundToInt


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

    private var tipoCredencial = ""

    private var puntoOrigenX = -10.0
    private var puntoOrigenY = -10.0
    private var conversionX = 0.0
    private var conversionY = 0.0

    private var aPaternoX = 100.0
    private var aPaternoY = 100.0
    private var aMaternoX = 100.0
    private var aMaternoY = 100.0
    private var nombresX = 100.0
    private var nombresY = 100.0
    private var calleX = 100.0
    private var calleY = 100.0
    private var coloniaX = 100.0
    private var coloniaY = 100.0
    private var ciudadX = 100.0
    private var ciudadY = 100.0
    private var claveX = 100.0
    private var claveY = 100.0
    private var curpX = 100.0
    private var curpY = 100.0
    private var fNacimientoX = 100.0
    private var fNacimientoY = 100.0
    private var sexoX = 100.0
    private var sexoY = 100.0
    private var seccionX = 100.0
    private var seccionY = 100.0
    private var anioRegistroX = 100.0
    private var anioRegistroY = 100.0
    private var vigenciaX = 100.0
    private var vigenciaY = 100.0

    private var aPaterno = ""
    private var aMaterno = ""
    private var nombres = ""
    private var calle = ""
    private var colonia = ""
    private var ciudad = ""
    private var clave = ""
    private var curp = ""
    private var sexo = ""
    private var fechaNacimiento = ""
    private var seccion = ""
    private var anioRegistro = ""
    private var vigencia = ""

    private val toleranciaX = 1.5
    private val toleranciaY = 1.5

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
                                linea.text,
                                linea.boundingBox!!.bottom.toDouble()
                            )
                        )
                    }
                }
                datosTexto.setText( "tamaño imagen: "+ bitmap.width +" "+bitmap.height+ "\n\n"
                        + textosBox)
            }
            .addOnCompleteListener {
                botonReconocer.isEnabled = true
                tipoCredencial()
            }
    }

    private fun tipoCredencial() {

        val labelIFE = textosyPosiciones.filter { it.texto == "INSTITUTO FEDERAL ELECTORAL" }
        val labelINE = textosyPosiciones.filter { it.texto == "INSTITUTO NACIONAL ELECTORAL" }
        val labelCURP = textosyPosiciones.filter { it.texto.contains("CURP") }
        val labelMexico = textosyPosiciones.filter { it.texto == "MÉXICO" }
        val labelEstado = textosyPosiciones.filter { it.texto.contains("ESTADO") }
        val labelAddress = textosyPosiciones.filter { it.texto.contains("ADDRESS") }
        val labelVigencia = textosyPosiciones.filter { it.texto.contains("VIGENCIA") }

        if (labelIFE.size > 0) {
            // es de tipo A - D
            if (labelCURP.isEmpty() && labelVigencia.isEmpty()) {
                // es A o B
                tipoCredencial = "AB"
            } else {
                // es C o D
                if (labelMexico.isEmpty()) {
                    // es C
                    tipoCredencial = "C"
                } else {
                    // es D
                    tipoCredencial = "D"
                }
            }
        }
        if (labelINE.size > 0) {
            // es de tipo E - H
            if (labelEstado.isNotEmpty()) {
                // es E o F
                if (labelAddress.isEmpty()) {
                    // es E
                    tipoCredencial = "E"
                } else {
                    // es F
                    tipoCredencial = "F"
                }
            } else {
                // es G o H
                if (labelAddress.isEmpty()) {
                    // es G
                    tipoCredencial = "G"
                } else {
                    // es H
                    tipoCredencial = "H"
                }
            }
        }

        if (tipoCredencial != "" ) {
            datosTexto.setText("tipo Credencial: "+tipoCredencial)
            calcularOrigen()
        } else {
            datosTexto.setText("No se pudo detectar el tipo de Credencial")
        }
    }

    private fun calcularOrigen() {

        val labelMexico = textosyPosiciones.filter { it.texto == "MÉXICO" }
        val labelINE = textosyPosiciones.filter { it.texto == "INSTITUTO FEDERAL ELECTORAL" || it.texto == "INSTITUTO NACIONAL ELECTORAL" }
        val labelCredencial = textosyPosiciones.filter { it.texto == "CREDENCIAL PARA VOTAR" }
        val labelNombre = textosyPosiciones.filter { it.texto == "NOMBRE" }
        val labelDomicilio = textosyPosiciones.filter { it.texto.contains("CLAVE DE ELECTOR") }

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
        val labelNombre = textosyPosiciones.filter { it.texto == "NOMBRE" }

        if (labelMexico.isNotEmpty() && labelINE.isNotEmpty() ) {
            val difx = labelINE[0].left - labelMexico[0].left // este debe ser proporcion 15
            val dify = labelINE[0].top - labelMexico[0].top // este debe ser proporcion -2

            val difMNx = labelNombre[0].left - labelMexico[0].left // este debe ser proporcion 15
            val difMNy = labelNombre[0].top - labelMexico[0].top // este debe ser proporcion -2

            conversionX = difMNx/12.5 //difx/14.8
            conversionY = difMNy/22.0 //dify/-3.6

            val posOy = labelNombre[0].top - 32.0*conversionY
            val pos0x = labelNombre[0].left - 33.0*conversionX

            val posOy2 = labelMexico[0].top - 9.0*conversionY
            val pos0x2 = labelMexico[0].left - 20.0*conversionX

            val circuloOrigen = CirculoDrawable(pos0x,posOy)
            rostroView.overlay.add(circuloOrigen)

            val circuloOrigen2 = CirculoDrawable(pos0x2,posOy2)
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

            //dibujaLinea(pos0x,posOy,labelMexico[0].left,labelMexico[0].top)

            puntoOrigenX = (pos0x + pos0x2)/2
            puntoOrigenY = (posOy + posOy2)/2

            buscaDatos()

        } else {
            val textoAnterior = datosTexto.text.toString()
            datosTexto.setText(textoAnterior + "\n\nNo se pudo calcular el punto de origen")
        }
    }

    fun dibujaLinea( x1:Double, y1:Double, x2:Double, y2:Double ) {
        val lineaDrawable = LineaDrawable(x1, y1, x2, y2)
        rostroView.overlay.add(lineaDrawable)
    }

    private fun buscaDatos() {

        // para mexico
        val puntoMexicoX = puntoOrigenX + 20.0*conversionX
        val puntoMexicoY = puntoOrigenY + 9.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,puntoMexicoX,puntoMexicoY)

        // para INE
        val puntoINEx = puntoOrigenX + 35.0*conversionX
        val puntoINEy = puntoOrigenY + 7.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,puntoINEx,puntoINEy)

        // para apellido paterno
        aPaternoX = puntoOrigenX + 33.0*conversionX
        aPaternoY = puntoOrigenY + 36.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,aPaternoX,aPaternoY)

        aMaternoX = puntoOrigenX + 33.0*conversionX
        aMaternoY = puntoOrigenY + 40.0*conversionY

        nombresX = puntoOrigenX + 33.0*conversionX
        nombresY = puntoOrigenY + 44.0*conversionY

        calleX = puntoOrigenX + 33.0*conversionX
        calleY = puntoOrigenY + 59.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,calleX,calleY)

        coloniaX = puntoOrigenX + 33.0*conversionX
        coloniaY = puntoOrigenY + 63.0*conversionY

        ciudadX = puntoOrigenX + 33.0*conversionX
        ciudadY = puntoOrigenY + 67.0*conversionY

        claveX = puntoOrigenX + 33.0*conversionX
        claveY = puntoOrigenY + 74.0*conversionY

        curpX = puntoOrigenX + 33.0*conversionX
        curpY = puntoOrigenY + 83.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,curpX,curpY)

        sexoX = puntoOrigenX + 86.5*conversionX
        sexoY = puntoOrigenY + 32.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,sexoX,sexoY)

        fNacimientoX = puntoOrigenX + 33.0*conversionX
        fNacimientoY = puntoOrigenY + 92.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,fNacimientoX,fNacimientoY)

        seccionX = puntoOrigenX + 58.0*conversionX
        seccionY = puntoOrigenY + 92.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,seccionX,seccionY)

        anioRegistroX = puntoOrigenX + 69.0*conversionX
        anioRegistroY = puntoOrigenY + 83.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,anioRegistroX,anioRegistroY)

        vigenciaX = puntoOrigenX + 70.0*conversionX
        vigenciaY = puntoOrigenY + 92.0*conversionY
        dibujaLinea(puntoOrigenX,puntoOrigenY,vigenciaX,vigenciaY)

        val finalX = puntoOrigenX + 100.0*conversionX
        val finalY = puntoOrigenY + 100.0*conversionY
        val rectCredencial = RectDrawable(puntoOrigenX,puntoOrigenY,finalX,finalY)
        rostroView.overlay.add(rectCredencial)

        identificaTextos()

    }

    private fun identificaTextos() {

        datosTexto.scrollY = 0
        datosTexto.setText("Identificando...")

        for (texto in textosyPosiciones) {
            val aPatDistX = (texto.left - aPaternoX) / conversionX
            if (abs(aPatDistX) <= toleranciaX) {
                val aPatDistY = (texto.top - aPaternoY) / conversionY
                if (abs(aPatDistY) <= toleranciaY) {
                    aPaterno = texto.texto //+ " "+aPatDistX.dosDecimales()+" "+aPatDistY.dosDecimales()
                }
            }
            val aMatDistX = (texto.left - aMaternoX) / conversionX
            if (abs(aMatDistX) <= toleranciaX) {
                val aMatDistY = (texto.top - aMaternoY) / conversionY
                if (abs(aMatDistY) <= toleranciaY) {
                    aMaterno = texto.texto //+ " "+aMatDistX.dosDecimales()+" "+aMatDistY.dosDecimales()
                }
            }
            val nombresDistX = (texto.left - nombresX) / conversionX
            if (abs(nombresDistX) <= toleranciaX) {
                val nombresDistY = (texto.top - nombresY) / conversionY
                if (abs(nombresDistY) <= toleranciaY) {
                    nombres = texto.texto //+ " "+nombresDistX.dosDecimales()+" "+nombresDistY.dosDecimales()
                }
            }
            val calleDistX = (texto.left - calleX) / conversionX
            if (abs(calleDistX) <= toleranciaX) {
                val calleDistY = (texto.top - calleY) / conversionY
                if (abs(calleDistY) <= toleranciaY) {
                    calle = texto.texto //+ " "+calleDistX.dosDecimales()+" "+calleDistY.dosDecimales()
                }
            }
            val coloniaDistX = (texto.left - coloniaX) / conversionX
            if (abs(coloniaDistX) <= toleranciaX) {
                val coloniaDistY = (texto.top - coloniaY) / conversionY
                if (abs(coloniaDistY) <= toleranciaY) {
                    colonia = texto.texto //+ " "+coloniaDistX.dosDecimales()+" "+coloniaDistY.dosDecimales()
                }
            }
            val ciudadDistX = (texto.left - ciudadX) / conversionX
            if (abs(ciudadDistX) <= toleranciaX) {
                val ciudadDistY = (texto.top - ciudadY) / conversionY
                if (abs(ciudadDistY) <= toleranciaY) {
                    ciudad = texto.texto //+ " "+ciudadDistX.dosDecimales()+" "+ciudadDistY.dosDecimales()
                }
            }
            val claveDistX = (texto.left - claveX) / conversionX
            if (abs(claveDistX) <= toleranciaX) {
                val claveDistY = (texto.top - claveY) / conversionY
                if (abs(claveDistY) <= toleranciaY) {
                    clave = texto.texto //+ " "+claveDistX.dosDecimales()+" "+claveDistY.dosDecimales()
                }
            }
            val curpDistX = (texto.left - curpX) / conversionX
            if (abs(curpDistX) <= toleranciaX) {
                val curpDistY = (texto.top - curpY) / conversionY
                if (abs(curpDistY) <= toleranciaY) {
                    curp = texto.texto //+ " "+curpDistX.dosDecimales()+" "+curpDistY.dosDecimales()
                }
            }
            val sexoDistX = (texto.left - sexoX) / conversionX
            if (abs(sexoDistX) <= toleranciaX) {
                val sexoDistY = (texto.top - sexoY) / conversionY
                if (abs(sexoDistY) <= toleranciaY) {
                    sexo = texto.texto //+ " "+sexoDistX.dosDecimales()+" "+sexoDistY.dosDecimales()
                }
            }
            val fnacDistX = (texto.left - fNacimientoX) / conversionX
            if (abs(fnacDistX) <= toleranciaX) {
                val fnacDistY = (texto.top - fNacimientoY) / conversionY
                if (abs(fnacDistY) <= toleranciaY) {
                    fechaNacimiento = texto.texto //+ " "+fnacDistX.dosDecimales()+" "+fnacDistY.dosDecimales()
                }
            }
            val seccionDistX = (texto.left - seccionX) / conversionX
            if (abs(seccionDistX) <= toleranciaX) {
                val seccionDistY = (texto.top - seccionY) / conversionY
                if (abs(seccionDistY) <= toleranciaY) {
                    seccion = texto.texto //+ " "+seccionDistX.dosDecimales()+" "+seccionDistY.dosDecimales()
                }
            }
            val anioRegDistX = (texto.left - anioRegistroX) / conversionX
            if (abs(anioRegDistX) <= toleranciaX) {
                val anioRegDistY = (texto.top - anioRegistroY) / conversionY
                if (abs(anioRegDistY) <= toleranciaY) {
                    anioRegistro = texto.texto //+ " "+anioRegDistX.dosDecimales()+" "+anioRegDistY.dosDecimales()
                }
            }
            val vigenciaDistX = (texto.left - vigenciaX) / conversionX
            if (abs(vigenciaDistX) <= toleranciaX) {
                val vigenciaDistY = (texto.top - vigenciaY) / conversionY
                if (abs(vigenciaDistY) <= toleranciaY) {
                    vigencia = texto.texto //+ " "+vigenciaDistX.dosDecimales()+" "+vigenciaDistY.dosDecimales()
                }
            }
        }

        datosTexto.setText("Apellido Paterno: "+aPaterno+"\n"+
                "Apellido Materno: "+aMaterno+"\n"+
                "Nombre(s): "+nombres+"\n"+
                "Dirección: "+calle+"\n"+
                "Colonia: "+colonia+"\n"+
                "Ciudad: "+ciudad+"\n"+
                "Clave Elector: "+clave+"\n"+
                "CURP: "+curp+"\n"+
                "Sexo: "+sexo+"\n"+
                "Fecha de Nacimiento: "+fechaNacimiento+"\n"+
                "Sección: "+seccion+"\n"+
                "Año de Registro: "+anioRegistro+"\n"+
                "Vigencia: "+vigencia+"\n"
        )

        val matrizGrises = convertirAGrises(bitmap)
        val matrizFiltrada = filtroSobel(matrizGrises)
        mostrarMatrizGrisesEnImageView(matrizFiltrada, rostroView)

    }

    fun filtroSobel(matrizGrises: Array<IntArray>): Array<IntArray> {
        val width = matrizGrises[0].size
        val height = matrizGrises.size

        val matrizFiltrada = Array(height) { IntArray(width) }
        val filtro: Array<Array<Int>> = arrayOf(
            arrayOf(-1, 0, 1),
            arrayOf(-1, 0, 1),
            arrayOf(-1, 0, 1)
        )
        val filtro2: Array<Array<Int>> = arrayOf(
            arrayOf( 1, 0,-1),
            arrayOf( 1, 0,-1),
            arrayOf( 1, 0,-1)
        )
        val filtro3: Array<Array<Int>> = arrayOf(
            arrayOf(-1,-1,-1),
            arrayOf( 0, 0, 0),
            arrayOf( 1, 1, 1)
        )
        val filtro4: Array<Array<Int>> = arrayOf(
            arrayOf( 1, 2, 1),
            arrayOf( 0, 0, 0),
            arrayOf(-1,-2,-1)
        )
        val filtro5: Array<Array<Int>> = arrayOf(
            arrayOf(-1, 0,-1),
            arrayOf( 0, 4, 0),
            arrayOf(-1, 0,-1)
        )

        var maximo = 0
        var minimo = 0
        var maxMat = 0
        var minMat = 0
        for (y in 1 until height-1) {
            for (x in 1 until width-1) {

                if (matrizGrises[y][x] > maxMat) maxMat = matrizGrises[y][x]
                if (matrizGrises[y][x] < minMat) minMat = matrizGrises[y][x]

                val producto =
                    matrizGrises[y-1][x-1]*filtro[0][0] + matrizGrises[y-1][x]*filtro[0][1] + matrizGrises[y-1][x+1]*filtro[0][2]
                +   matrizGrises[y][x-1]*filtro[1][0] + matrizGrises[y][x]*filtro[1][1] + matrizGrises[y][x+1]*filtro[1][2]
                +   matrizGrises[y+1][x-1]*filtro[2][0] + matrizGrises[y+1][x]*filtro[2][1] + matrizGrises[y+1][x+1]*filtro[2][2]
                val producto2 =
                    matrizGrises[y-1][x-1]*filtro2[0][0] + matrizGrises[y-1][x]*filtro2[0][1] + matrizGrises[y-1][x+1]*filtro2[0][2]
                +   matrizGrises[y][x-1]*filtro2[1][0] + matrizGrises[y][x]*filtro2[1][1] + matrizGrises[y][x+1]*filtro2[1][2]
                +   matrizGrises[y+1][x-1]*filtro2[2][0] + matrizGrises[y+1][x]*filtro2[2][1] + matrizGrises[y+1][x+1]*filtro2[2][2]
                val producto3 =
                    matrizGrises[y-1][x-1]*filtro3[0][0] + matrizGrises[y-1][x]*filtro3[0][1] + matrizGrises[y-1][x+1]*filtro3[0][2]
                +   matrizGrises[y][x-1]*filtro3[1][0] + matrizGrises[y][x]*filtro3[1][1] + matrizGrises[y][x+1]*filtro3[1][2]
                +   matrizGrises[y+1][x-1]*filtro3[2][0] + matrizGrises[y+1][x]*filtro3[2][1] + matrizGrises[y+1][x+1]*filtro3[2][2]
                val producto4 =
                    matrizGrises[y-1][x-1]*filtro4[0][0] + matrizGrises[y-1][x]*filtro4[0][1] + matrizGrises[y-1][x+1]*filtro4[0][2]
                +   matrizGrises[y][x-1]*filtro4[1][0] + matrizGrises[y][x]*filtro4[1][1] + matrizGrises[y][x+1]*filtro4[1][2]
                +   matrizGrises[y+1][x-1]*filtro4[2][0] + matrizGrises[y+1][x]*filtro4[2][1] + matrizGrises[y+1][x+1]*filtro4[2][2]
                val producto5 =
                    matrizGrises[y-1][x-1]*filtro5[0][0] + matrizGrises[y-1][x]*filtro5[0][1] + matrizGrises[y-1][x+1]*filtro5[0][2]
                +   matrizGrises[y][x-1]*filtro5[1][0] + matrizGrises[y][x]*filtro5[1][1] + matrizGrises[y][x+1]*filtro5[1][2]
                +   matrizGrises[y+1][x-1]*filtro5[2][0] + matrizGrises[y+1][x]*filtro5[2][1] + matrizGrises[y+1][x+1]*filtro5[2][2]

                val promedio = producto/9
                val promedio2 = producto2/9
                val promedio3 = producto3/9
                val promedio4 = producto4/9
                val promedio5 = producto4/9

                val verticales = (promedio + promedio2)/2

                val final = abs(promedio4)

                if (final > maximo) maximo = final
                if (final < minimo) minimo = final
                val thresh = (minimo + maximo) / 2

                matrizFiltrada[y][x] = final/*if ( final > thresh ) {
                    255
                } else {
                    0
                }*/
            }
        }
        Log.v("maximo Matriz: ",maxMat.toString())
        Log.v("minimo Matriz: ",minMat.toString())
        Log.v("maximo: ",maximo.toString())
        Log.v("minimo: ",minimo.toString())
        return matrizFiltrada
    }

    fun mostrarMatrizGrisesEnImageView(matrizGrises: Array<IntArray>, imageView: ImageView) {
        val width = matrizGrises[0].size
        val height = matrizGrises.size

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = Color.rgb(matrizGrises[y][x], matrizGrises[y][x], matrizGrises[y][x])
                bitmap.setPixel(x, y, color)
            }
        }

        imageView.setImageBitmap(bitmap)
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

    fun convertirAGrises(bitmap: Bitmap): Array<IntArray> {
        val width = bitmap.width
        val height = bitmap.height
        val matrizGrises = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Conversión a escala de grises
                val gris = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                //val pixelGris = Color.rgb(gris, gris, gris)

                //bitmap.setPixel(x, y, pixelGris)
                matrizGrises[y][x] = gris
            }
        }
        return matrizGrises
    }

    private fun Double.dosDecimales(): Double {
        val res = (this * 100.0).roundToInt() / 100.0
        return res
    }
}

data class TextoPosicion(val left: Double, val top: Double, val texto: String, val bottom: Double)
data class TextoDistancia(val distancia: Double, val texto: String)