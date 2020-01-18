package com.example.greedygame

import android.content.ContentValues
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_operations.*
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


class OperationsActivity : AppCompatActivity() {
    lateinit var currentShownBitmap: Bitmap
    lateinit var originalBitmap:Bitmap
    private val operationsPerformedList = ArrayList<Pair<OperationType,Bitmap>>()
    val text = "Greedy Game"
    enum class OperationType() {
        VERTICAL_ROTATION,
        HORIZONTAL_ROTATION,
        OPACITY,
        ADD_TEXT,
        ADD_TEXT_AND_LOGO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operations)
        val imageUri = Uri.parse(intent.getStringExtra("filePath"))

        originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        imageView.setImageBitmap(originalBitmap)
        currentShownBitmap = originalBitmap

        setListeners()

    }

    private fun setListeners() {

        fliphorizontal.setOnClickListener {
            flipHorizontal(currentShownBitmap)
        }
        flipvertical.setOnClickListener {
            flipVertical(currentShownBitmap)
        }

        opacity.setOnClickListener {
            setOpacity(currentShownBitmap);
        }
        addtext.setOnClickListener {
            val textBitmap= textOnBitmap(currentShownBitmap)
            if(operationsPerformedList.size==3)
                operationsPerformedList.removeAt(0)
            operationsPerformedList.add(Pair(OperationType.ADD_TEXT,textBitmap!!))
            currentShownBitmap = textBitmap
            imageView.setImageBitmap(textBitmap)

        }
        addtextandlogo.setOnClickListener {
            val textBitmap= textAndLogoOnBitmap(currentShownBitmap)
            if(operationsPerformedList.size==3)
                operationsPerformedList.removeAt(0)
            operationsPerformedList.add(Pair(OperationType.ADD_TEXT_AND_LOGO,textBitmap!!))
            currentShownBitmap = textBitmap
            imageView.setImageBitmap(textBitmap)
        }
        save.setOnClickListener {
            saveImage(currentShownBitmap)
        }
    }
    private fun undoOperations() {
        if (operationsPerformedList.isEmpty()){
            Toast.makeText(this,"No opertaions Performed",Toast.LENGTH_SHORT).show()
        }
        else if (operationsPerformedList.size==1)
        {
            currentShownBitmap=originalBitmap
            operationsPerformedList.removeAt(operationsPerformedList.size-1)
            imageView.setImageBitmap(currentShownBitmap)
        }
        else{
            currentShownBitmap=operationsPerformedList.get(operationsPerformedList.size-2).second
            operationsPerformedList.removeAt(operationsPerformedList.size-1)
            imageView.setImageBitmap(currentShownBitmap)
        }
    }

    private fun setOpacity(bitmap: Bitmap) {
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val transBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(transBitmap)
        canvas.drawARGB(0, 0, 0, 0)
        val paint = Paint()
        paint.setAlpha(50)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        if(operationsPerformedList.size==3)
            operationsPerformedList.removeAt(0)
        operationsPerformedList.add(Pair(OperationType.OPACITY,transBitmap))
        currentShownBitmap = transBitmap
        imageView.setImageBitmap(transBitmap)

    }

    private fun flipHorizontal(bitmap: Bitmap){
        val cx = bitmap.width / 2f
        val cy = bitmap.height / 2f
        val flippedBitmap = bitmap.flip(-1f, 1f, cx, cy)
        if(operationsPerformedList.size==3)
            operationsPerformedList.removeAt(0)
        operationsPerformedList.add(Pair(OperationType.HORIZONTAL_ROTATION,flippedBitmap))

        currentShownBitmap = flippedBitmap

        imageView.setImageBitmap(flippedBitmap)
    }
    private fun flipVertical(bitmap: Bitmap){
        val cx = bitmap.width / 2f
        val cy = bitmap.height / 2f
        val flippedBitmap = bitmap.flip(1f, -1f, cx, cy)
        if(operationsPerformedList.size==3)
            operationsPerformedList.removeAt(0)
        operationsPerformedList.add(Pair(OperationType.VERTICAL_ROTATION,flippedBitmap))

        currentShownBitmap = flippedBitmap

        imageView.setImageBitmap(flippedBitmap)
    }

    private fun textOnBitmap(bitmap: Bitmap): Bitmap? {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(result)
        val bounds = Rect()
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        //text and rectangle
        val fm = Paint.FontMetrics()
        val paint = Paint()
        paint.textSize = 28f
        paint.getFontMetrics(fm)
        paint.getTextBounds(text,0,text.length,bounds)

        val xPos = canvas.width  / 2 - bounds.width()/2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2 )
        paint.color = Color.BLACK
        val margin = 10f
        canvas.drawRect(xPos - margin, yPos + fm.top - margin,
            xPos + paint.measureText(text) + margin, yPos + fm.bottom
                    + margin, paint)
        paint.color = Color.GREEN
        canvas.drawText(text, xPos.toFloat(), yPos, paint)

        return result

    }
    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
    private fun textAndLogoOnBitmap(bitmap: Bitmap): Bitmap? {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val logo = getResizedBitmap(BitmapFactory.decodeResource(resources, R.drawable.gglogo),150)
        val bounds = Rect()

        val margin = 10f

        //LOGO
        val canvas = Canvas(result)
        val logopaint = Paint()
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawBitmap(logo,margin,margin, logopaint )

        //TEXT
        val textPaint = Paint()
        textPaint.textSize = 34f
        textPaint.isUnderlineText = false;
        textPaint.getTextBounds(text,0,text.length,bounds)
        val textWidth = bounds.width()
        val textHeight = bounds.height()
        val xPos = bitmap.width - textWidth
        val yPos = textHeight
        val backgroundPaint = Paint()
        backgroundPaint.color = Color.BLACK
        textPaint.color = Color.GREEN
        canvas.drawText(text, xPos.toFloat()-10, logo.height+margin, textPaint)
        return result

    }

    private fun Bitmap.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap {
        val matrix = Matrix().apply { postScale(x, y, cx, cy) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun saveImage(bitmap: Bitmap) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + "GG")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + "GG")

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + "GG.png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            // .DATA is deprecated in API 29
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menuoperations, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.undo -> {
                undoOperations()
            }
        }
        return super.onOptionsItemSelected(item);

    }

}

