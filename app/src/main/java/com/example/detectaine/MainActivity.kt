package com.example.detectaine

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture.*
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.video.*
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camerax_mlkit.FaceDrawable
import com.example.camerax_mlkit.TextDrawable
import com.example.detectaine.databinding.ActivityMainBinding
import com.example.detectaine.drawables.ObjectDetectedDrawable
import com.example.detectaine.drawables.RecuadroDrawable
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var botonFoto: Button
    private lateinit var botonFlash: Button

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var objectDetector: ObjectDetector
    // When using Latin script library
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var faceDetector : FaceDetector

    lateinit var objectoDetectado: DetectedObject
    //lateinit var rostro: Face
    lateinit var textos: Text
    //lateinit var imagenRecortada: Bitmap

    var cambiandoActividad =  false
    var tomarFoto = false
    //var hayTextos = false
    //var hayRostro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.viewFinder.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        viewBinding.viewFinder.scaleType = PreviewView.ScaleType.FIT_CENTER

        botonFoto = findViewById(R.id.buttonTomarFoto)
        botonFoto.setOnClickListener { tomarFoto() }

        botonFlash = findViewById(R.id.buttonFlash)

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
        faceDetector = FaceDetection.getClient(realTimeOpts)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun tomarFoto() {
        tomarFoto = true
        botonFoto.isEnabled = false
    }
    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {

        var cameraController = LifecycleCameraController(baseContext)
        val previewView: PreviewView = viewBinding.viewFinder

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(objectDetector),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->

                botonFlash.setOnClickListener {
                    val currentFlashMode = cameraController.cameraInfo?.torchState?.value
                    if (currentFlashMode == null || currentFlashMode == 0) {
                        cameraController.enableTorch(true)
                        this.botonFlash.setText("Flash Off")
                    } else {
                        cameraController.enableTorch(false)
                        this.botonFlash.setText("Flash On")
                    }
                }


                val objectResults = result?.getValue(objectDetector)

                var hayRostro = false
                var hayTextos = false

                if ((objectResults == null) ||
                    (objectResults.size == 0) ||
                    (objectResults.first() == null)
                ) {
                    Log.v(TAG, "No hay objectos")
                    previewView.overlay.clear()

                    val recuadro = RecuadroDrawable(previewView)
                    previewView.overlay.add(recuadro)

                    previewView.setOnTouchListener { _, _ -> false } //no-op
                    return@MlKitAnalyzer
                } else {
                    previewView.overlay.clear()

                    val recuadro = RecuadroDrawable(previewView)
                    previewView.overlay.add(recuadro)

                    for (obj in objectResults) {
                        if ( obj.labels.size > 0 && obj.labels[0].text.contains("license")) {
                            Log.v("objeto", obj.toString())
                            val objectDrawable = ObjectDetectedDrawable(obj)
                            previewView.overlay.add(objectDrawable)

                            val box = obj.boundingBox
                            val prevBti = previewView.bitmap
                            if (prevBti != null &&
                                (box.width() + box.left) < prevBti.width &&
                                (box.height() + box.top) < prevBti.height &&
                                box.left >= 0 && box.top >= 0
                            ) {
                                //Log.v(TAG, "preview: "+ prevBti.width +" "+ prevBti.height + " box : "+ box.flattenToString() )
                                //val visionImage = InputImage.fromByteBuffer( previewView.bitmap )
                                val crooppedBitmap = Bitmap.createBitmap(
                                    prevBti,
                                    box.left,
                                    box.top,
                                    box.width(),
                                    box.height()
                                )
                                val inputImage = InputImage.fromBitmap(crooppedBitmap, 0)

                                recognizer.process(InputImage.fromBitmap(crooppedBitmap, 0))
                                    .addOnSuccessListener { labels ->
                                        Log.v(TAG, "labels: " + labels.text)

                                        if (labels.text.contains("INSTITUTO NACIONAL ELECTORAL") ||
                                                labels.text.contains("INSTITUTO FEDERAL ELECTORAL")){
                                            hayTextos = true
                                            textos = labels
                                        }
                                        for ( textBlock in labels?.textBlocks!! ) {
                                            for (linea in textBlock.lines) {
                                                val textDrawable =
                                                    TextDrawable(linea, box.left, box.top)

                                                //previewView.overlay.clear()
                                                previewView.overlay.add(textDrawable)
                                            }
                                        }
                                        if (hayTextos) {

                                            faceDetector.process(InputImage.fromBitmap(crooppedBitmap, 0))
                                                .addOnSuccessListener { faces ->
                                                    Log.v(TAG, "faces: $faces")

                                                    ///var faceBitmap: Bitmap? = null
                                                    if (faces.size > 0) {
                                                        hayRostro = true
                                                        //rostro = faces[0]

                                                        /*faceBitmap = Bitmap.createBitmap(
                                                            crooppedBitmap,
                                                            rostro.boundingBox.left,
                                                            rostro.boundingBox.top,
                                                            rostro.boundingBox.width(),
                                                            rostro.boundingBox.height()
                                                        )*/
                                                    }
                                                    for (face in faces) {
                                                        val faceDrawable = FaceDrawable(face, box.left, box.top)

                                                        //previewView.overlay.clear()
                                                        previewView.overlay.add(faceDrawable)
                                                    }
                                                    if (hayTextos && hayRostro) {
                                                        Log.v("credencial","si hay credencial")

                                                        // Guardar el Bitmap en la memoria interna
                                                        val rutaDeArchivo = guardarBitmapEnArchivoInterno(crooppedBitmap, "nombre_archivo.png")

                                                        // Crear un Intent para cambiar a ActivityB
                                                        val intent = Intent(this, DatosCredencialActivity::class.java)

                                                        // Pasar la ruta del archivo como extra
                                                        intent.putExtra("rutaArchivo", rutaDeArchivo)

                                                        if (!cambiandoActividad && tomarFoto) {
                                                            cambiandoActividad = true
                                                            startActivity(intent)
                                                        }
                                                    }
                                                }
                                        }
                                    }
                            }

                        }
                    }
                }
            }
        )

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController

    }

    private fun guardarBitmapEnArchivoInterno(bitmap: Bitmap, nombreArchivo: String): String {
        val directorioInterno = filesDir
        val archivo = File(directorioInterno, nombreArchivo)

        try {
            val stream = FileOutputStream(archivo)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return archivo.absolutePath
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cambiandoActividad = false
    }

    companion object {
        private const val TAG = "Detecta INE App"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cambiandoActividad = false
        tomarFoto = false
        botonFoto.isEnabled = true
    }
}
