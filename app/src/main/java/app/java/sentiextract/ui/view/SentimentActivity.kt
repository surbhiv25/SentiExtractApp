package app.java.sentiextract.ui.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import app.java.sentiextract.BuildConfig
import app.java.sentiextract.R
import app.java.sentiextract.data.api.ApiHelper
import app.java.sentiextract.data.api.RetrofitBuilder
import app.java.sentiextract.data.model.ApiRequest
import app.java.sentiextract.ui.base.ViewModelFactory
import app.java.sentiextract.ui.viewmodel.MainViewModel
import app.java.sentiextract.utils.Status
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_sentiment.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SentimentActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var mGraphicOverlay: GraphicOverlay? = null
    private lateinit var capturedText: String
    private var isImageCaptured: Boolean = false

    // Max width (portrait mode)
    private var mImageMaxWidth: Int? = null

    // Max height (portrait mode)
    private var mImageMaxHeight: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sentiment)

        setupViewModel()

        mGraphicOverlay = findViewById(R.id.graphic_overlay);

        btn_text.setOnClickListener {
            isImageCaptured = false
            rl_first.visibility = View.GONE
            rl_byText.visibility = View.VISIBLE
            edittext.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            graphic_overlay.visibility = View.GONE
            txtView.visibility = View.GONE
            txt_showSentiment.visibility = View.GONE
            button_capture.visibility = View.GONE
            txt_extracted.visibility = View.GONE
        }

        btn_image.setOnClickListener {
            isImageCaptured = true
            rl_first.visibility = View.GONE
            rl_byText.visibility = View.VISIBLE
            edittext.visibility = View.GONE
            button_capture.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            txt_extracted.visibility = View.VISIBLE
            graphic_overlay.visibility = View.GONE
            txtView.visibility = View.GONE
            txt_showSentiment.visibility = View.GONE
        }

        btn_sentiment.setOnClickListener {
            setupObservers()
        }

        button_capture.setOnClickListener {
            openCameraIntent()
        }
    }

    private val REQUEST_CAPTURE_IMAGE = 100
    private fun openCameraIntent() {
         val pictureIntent = Intent(
             MediaStore.ACTION_IMAGE_CAPTURE
         )
         if(pictureIntent.resolveActivity(getPackageManager()) != null){
            //Create a file to store the image
            var photoFile : File? = null
            try {
                photoFile = createImageFileNew()
            } catch (ex: IOException) {
            }
            if (photoFile != null) {
                val photoURI : Uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider", photoFile
                )
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(
                    pictureIntent,
                    REQUEST_CAPTURE_IMAGE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE_IMAGE) {
            Glide.with(this).load(imageFilePathNew).into(imageView)
            /*val drawable = imageView.drawable as BitmapDrawable
            val bitmap = drawable.bitmap*/
            try {
                val f: File = File(imageFilePathNew)
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                var bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
                /*if (bitmap != null) {
                    // Get the dimensions of the View
                    val (targetWidth, maxHeight) = getTargetedWidthHeight()

                    // Determine how much to scale down the image
                    val scaleFactor = Math.max(
                        bitmap.getWidth() as Float / targetWidth!!.toFloat(),
                        bitmap.getHeight() as Float / maxHeight!!.toFloat()
                    )
                    val resizedBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.getWidth() / scaleFactor) as Int,
                        (bitmap.getHeight() / scaleFactor) as Int,
                        true
                    )
                    imageView.setImageBitmap(resizedBitmap)
                    bitmap = resizedBitmap
                }*/
                runTextRecognition(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }


           /* val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
            val firebaseVisionTextRecognizer= FirebaseVision.getInstance().onDeviceTextRecognizer
            firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener() {
                processTextRecognition(it)
            }.addOnFailureListener {
                //show error
                Log.d("TAG", "error occured: "+it)
            }*/
        }
    }

    // Gets the targeted width / height.
    private fun getTargetedWidthHeight(): Pair<Int?, Int?> {
        val targetWidth: Int?
        val targetHeight: Int?
        val maxWidthForPortraitMode: Int? = getImageMaxWidth()
        val maxHeightForPortraitMode: Int? = getImageMaxHeight()
        targetWidth = maxWidthForPortraitMode
        targetHeight = maxHeightForPortraitMode
        return Pair(targetWidth, targetHeight)
    }

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxWidth(): Int? {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = imageView.getWidth()
        }
        return mImageMaxWidth
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxHeight(): Int? {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight = imageView.getHeight()
        }
        return mImageMaxHeight
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(MainViewModel::class.java)
    }

    private fun setupObservers() {
        if(!isImageCaptured)
            capturedText = edittext.text.toString();

        val fetchedText = ApiRequest(capturedText)
        viewModel.getUsers(fetchedText).observe(this, {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        resource.data?.let { users ->
                            txtView.visibility = View.VISIBLE
                            txt_showSentiment.visibility = View.VISIBLE
                            if(users.equals("negative", true)){
                                txtView.text = "Oops!!\n\nYour calculated sentiment is"
                                txt_showSentiment.text = users
                                txt_showSentiment.setTextColor(getColor(android.R.color.holo_red_dark))
                            }else if(users.equals("positive", true)){
                                txtView.text = "Hurray!!\n\nYour calculated sentiment is"
                                txt_showSentiment.text = users
                                txt_showSentiment.setTextColor(getColor(android.R.color.holo_green_dark))
                            }else{
                                txtView.text = "Your calculated sentiment is"
                                txt_showSentiment.text = users
                                txt_showSentiment.setTextColor(getColor(android.R.color.holo_blue_dark))
                            }
                        }
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    var imageFilePathNew: String? = null
    @Throws(IOException::class)
    private fun createImageFileNew(): File? {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        imageFilePathNew = image.absolutePath
        return image
    }

    private fun runTextRecognition(mSelectedImage: Bitmap?) {
        val image = mSelectedImage?.let { InputImage.fromBitmap(it, 0) }
        val recognizer: TextRecognizer = TextRecognition.getClient()
        if (image != null) {
            recognizer.process(image)
                .addOnSuccessListener { image ->
                    Log.d("VALUE", image.text)
                    capturedText = image.text
                    txt_extracted.text = capturedText
                    //processTextRecognitionResult(image as Text)
                }
                /*.addOnSuccessListener(
                    OnSuccessListener<Any?> { texts ->
                        texts.
                        processTextRecognitionResult(texts as Text)
                    })*/
                .addOnFailureListener { e -> // Task failed with an exception
                    e.printStackTrace()
                }
        }
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks: List<Text.TextBlock> = texts.getTextBlocks()
        if (blocks.size == 0) {
            Toast.makeText(applicationContext, "No text found", Toast.LENGTH_LONG).show();
            return
        }
        mGraphicOverlay?.clear()
        for (i in blocks.indices) {
            val lines: List<Text.Line> = blocks[i].getLines()
            for (j in lines.indices) {
                val elements: List<Text.Element> = lines[j].getElements()
                for (k in elements.indices) {
                    Log.d("VALUE", "value new: "+elements[k])
                    val textGraphic: GraphicOverlay.Graphic = TextGraphic(
                        mGraphicOverlay,
                        elements[k]
                    )
                    mGraphicOverlay?.add(textGraphic)
                }
            }
        }
    }

    /*private fun processTextRecognition(text: FirebaseVisionText) {
        val textBlock: List<FirebaseVisionText.TextBlock> = text.textBlocks
        if (textBlock.isEmpty()) {
            textView.text = "No Text found"
            return
        }
        for (index in 0 until textBlock.size) {
            val lines: List<FirebaseVisionText.Line> = textBlock[index].lines
            for (j in 0 until lines.size) {
                val element: List<FirebaseVisionText.Element> = lines[j].elements
                for (k in 0 until element.size) {
                    textView.append(" " + element[k].text)
                }
            }
        }
    }*/
}