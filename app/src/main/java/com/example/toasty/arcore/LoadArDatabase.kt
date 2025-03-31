package com.example.toasty.arcore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.toasty.R
import com.example.toasty.fragments.CustomArFragment
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.pow
import kotlin.math.sqrt

class LoadArDatabase : AppCompatActivity(), Scene.OnUpdateListener {

    private lateinit var arFragment: CustomArFragment
    private lateinit var textView: TextView
    private lateinit var btnSerialize: Button
    private var aid: AugmentedImageDatabase? = null
    private lateinit var file: File
    private val ASSET_ID = "female_head.glb"
    enum class Shape {
        CUBE,
        SPHERE,
        CYLINDER
    }
    private val anchorNodes = mutableListOf<AnchorNode>()
    var shapeType: Shape = Shape.CUBE
    private val manageStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkManageExternalStoragePermission()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_database)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as CustomArFragment
        textView = findViewById(R.id.txtImageFound)
        btnSerialize = findViewById(R.id.btnSerialize)
        btnSerialize.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("startARSphere WRITE_EXTERNAL_STORAGE permission not granted! ${Build.VERSION.SDK_INT}")
                // Check and request MANAGE_EXTERNAL_STORAGE permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    checkAndRequestManageExternalStoragePermission()
                }
                return@setOnClickListener
            }
            println("startARSphere WRITE_EXTERNAL_STORAGE permission granted!")
            serialize()

        }
        startARSphere()

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkAndRequestManageExternalStoragePermission() {
        println("startARSphere checkAndRequestManageExternalStoragePermission")
        if (!isManageExternalStoragePermissionGranted()) {
            // Launch the settings screen for MANAGE_EXTERNAL_STORAGE
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            manageStoragePermissionLauncher.launch(intent)
        } else {
            // FirebaseData.onInit(this@MainActivity)
            serialize()
            Log.d("startARSphere", "MANAGE_EXTERNAL_STORAGE permission already granted")
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun isManageExternalStoragePermissionGranted(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isManageExternalStoragePermissionGranted()) {
                Log.d("startARSphere", "MANAGE_EXTERNAL_STORAGE permission granted")
            } else {
                Log.d("startARSphere", "MANAGE_EXTERNAL_STORAGE permission denied")
            }
        }
    }


    private fun serialize() {
        try {
            println("startARSphere serialize call...${Environment.getExternalStorageDirectory()}")
            file = File(Environment.getExternalStorageDirectory().toString() + "/db.imgdb")
            val outputStream = FileOutputStream(file)
            val bitmapEarth: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.earth)
            val bitmapFace: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.face)
            aid?.let {
                it.addImage("earth", bitmapEarth)
                it.addImage("face", bitmapFace)
                it.serialize(outputStream)
            }
            outputStream.close()
            Toast.makeText(this@LoadArDatabase, "Database serialized", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startARSphere() {
        // Start ARCore or load ArFragment
        println("startARSphere call..")
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            println("startARSphere setOnTapArPlaneListener...")
            val anchor = hitResult.createAnchor()
            distanceBetweenArNodes(anchor)
            //renderableLayoutView(anchor)
            //loadGLB(anchor)
            //renderableGLB(anchor)
            //arFragment.arSceneView.scene.addOnUpdateListener(this)
            /*MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.BLUE))
                .thenAccept { material ->
                    println("startARSphere thenAccept...$material")
                    arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdate)
                }*/
        }
    }

    private fun distanceBetweenArNodes(anchor: Anchor){
        if (anchorNodes.size < 2) {
            //val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNodes.add(anchorNode)
            arFragment.arSceneView.scene.addChild(anchorNode)
        }

        if (anchorNodes.size == 2) {
            val distance = measureDistance(anchorNodes[0], anchorNodes[1])
            textView.text = "Distance: $distance meters"
            println("Distance: $distance meters")
        }
    }
    private fun loadGLB(anchor: Anchor){
        ModelRenderable.builder()
            .setSource(this, Uri.parse("fox_face.sfb"))
            .build()
            .thenAccept { modelRenderable ->
                println("startARSphere loadGLBFailed..loadRenderable.")
                loadRenderable(anchor, modelRenderable)
            }
            .exceptionally { throwable ->
                println("startARSphere loadGLBFailed..${throwable.message}.")
                return@exceptionally null
            }
    }

    override fun onUpdate(frameTime: FrameTime) {
        try {
            //println("startARSphere addOnUpdateListener...")
            val frame: Frame? = arFragment.arSceneView.arFrame
            val images: Collection<AugmentedImage>? =
                frame?.getUpdatedTrackables(AugmentedImage::class.java)
            println("startARSphere ...imagessize=${images?.size}...")
            if (images != null) {
                for (image in images) {
                    /*                    if (image.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                                            println("startARSphere ...name=${image.name}...")
                                            if (image.name.equals("000.jpg")) {
                                                textView.text = "000 image is found"
                                            } else if (image.name.equals("earth")) {
                                                textView.text = "earth image is found"
                                            } else {
                                                textView.text = "searching image"
                                            }
                                        }*/
                    if (image.trackingState == TrackingState.TRACKING) {
                        if (image.name.equals("face")) {
                            textView.text = "Face image is found"
                            val anchor: Anchor = image.createAnchor(image.centerPose)
                            MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
                                .thenAccept { material ->
                                    shapeType = Shape.SPHERE
                                    renderableShape(anchor, material)
                                }
                        } else if (image.name.equals("earth")) {
                            textView.text = "earth image is found"
                            val anchor: Anchor = image.createAnchor(image.centerPose)
                            //renderableGLB(anchor)
                            MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.BLUE))
                                .thenAccept { material ->
                                    shapeType = Shape.CYLINDER
                                    renderableShape(anchor, material)
                                }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renderableShape(anchor: Anchor, material: Material){
        try{
            val renderableShape: ModelRenderable = when (shapeType) {
                Shape.CUBE -> {
                    ShapeFactory.makeCube(
                        Vector3(0.1f, 0.1f, 0.1f),
                        Vector3(0f, 0.1f, 0f),
                        material
                    )
                }

                Shape.SPHERE -> {
                    ShapeFactory.makeSphere(0.1f, Vector3(0f, 0.1f, 0f), material)
                }

                Shape.CYLINDER -> {
                    ShapeFactory.makeCylinder(0.1f, 0.2f, Vector3(0f, 0.2f, 0f), material)
                }
            }
            loadRenderable(anchor, renderableShape)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun renderableGLB(anchor: Anchor) {
        try {
            val renderableSource: RenderableSource = RenderableSource.builder()
                .setSource(this, Uri.parse(ASSET_ID), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()
            ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(ASSET_ID)
                .build()
                .thenAccept { modelRenderable ->
                    loadRenderable(anchor, modelRenderable)
                }
                .exceptionally { throwable ->
                    AlertDialog.Builder(this@LoadArDatabase).setMessage(throwable.message).show()
                    return@exceptionally null
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadRenderable(anchor: Anchor, renderableShape: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        val transformbleNode = TransformableNode(arFragment.transformationSystem)
        transformbleNode.setParent(anchorNode)
        transformbleNode.renderable = renderableShape
        //anchorNode.renderable = renderableShape
        arFragment.arSceneView.scene.addChild(anchorNode)
        transformbleNode.select()
        println("startARSphere anchorNode...$anchorNode")
    }

    private fun renderableLayoutView(anchor: Anchor){
        ViewRenderable.builder()
            .setView(this@LoadArDatabase, R.layout.activity_main)
            .build()
            .thenAccept { viewRenderable->
                val anchorNode = AnchorNode(anchor)
                val transformbleNode = TransformableNode(arFragment.transformationSystem)
                transformbleNode.setParent(anchorNode)
                transformbleNode.renderable = viewRenderable
                //anchorNode.renderable = renderableShape
                arFragment.arSceneView.scene.addChild(anchorNode)
                transformbleNode.select()
                println("startARSphere renderableLayoutView...$anchorNode")
                val view = viewRenderable.view
                // handle component views view.findViewById
            }
    }

    public fun loadDB(session: Session, config: Config) {
        try {
            file = File(Environment.getExternalStorageDirectory().toString() + "/db.imgdb")
            //val dbStream: InputStream = resources.openRawResource(R.raw.sample_database)
            val dbStream = FileInputStream(file)
            aid = AugmentedImageDatabase.deserialize(session, dbStream)
            config.setAugmentedImageDatabase(aid)
            println("startARSphere loadDB ...$aid")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public fun loadDatabase(session: Session, config: Config) {
        try {
            val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.earth)
            aid = AugmentedImageDatabase(session)
            aid?.addImage("earth", bitmap)
            config.setAugmentedImageDatabase(aid)
            println("startARSphere loadDB ...$aid")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun measureDistance(node1: AnchorNode, node2: AnchorNode): Float {
        val point1 = node1.worldPosition
        val point2 = node2.worldPosition
        return sqrt(
            (point1.x - point2.x).pow(2) +
                    (point1.y - point2.y).pow(2) +
                    (point1.z - point2.z).pow(2)
        )
    }
}