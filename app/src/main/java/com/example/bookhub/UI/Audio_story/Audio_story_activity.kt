package com.example.bookhub.UI.Audio_story

import android.Manifest
import android.Manifest.permission_group.STORAGE
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.widget.Chronometer
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookhub.Adapter.AudioAdapter
import com.example.bookhub.Models.AudioData
import com.example.bookhub.R
import com.example.bookhub.Utils.common.cropImage
import com.example.bookhub.Utils.common.getMP3Duration
import com.example.bookhub.Utils.common.launchGallery
import com.example.bookhub.Utils.common.search
import com.example.bookhub.Utils.constant.CAMERA_PERMISSION_CODE
import com.example.bookhub.Utils.constant.PICK_FILE
import com.example.bookhub.Utils.constant.REQUEST_RECORD_AUDIO_PERMISSION
import com.example.bookhub.Utils.constant.STORAGE_PERMISSION_CODE
import com.example.bookhub.Utils.constant.directoryPath
import com.example.bookhub.Utils.toast
import com.example.bookhub.ViewModel.ViewModel
import com.example.bookhub.databinding.ActivityAudioStoryBinding
import com.example.bookhub.databinding.AudioRecordBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@RequiresApi(Build.VERSION_CODES.S)
@AndroidEntryPoint
class Audio_story_activity : AppCompatActivity(),AudioAdapter.OnItemClickListner {
    var mydialog: Dialog? = null
    var uploadDialog: Dialog? = null
    private var recorder: MediaRecorder? = null
    private var fileName: String = ""
    private var fname: String = ""
    private val LOG_TAG = "AudioRecordTest"
    private var recordingStopped: Boolean = false
    private var state: Boolean = false
    private var chronometer:Chronometer?=null
    private var running: Boolean = false
    private  var pauseoffset:Long=0
    private var progressDialog: ProgressDialog? = null
    private var filePath: Uri? = null
    private var bytedata: ByteArray? = null
    private lateinit var image: ImageView
    var photoUri: Uri? = null
    private lateinit var audioRecyclerView: RecyclerView
    private lateinit var audioList: ArrayList<AudioData>
    lateinit var adapter: AudioAdapter
    private  var _binding1:ActivityAudioStoryBinding?=null
    private val binding1 get() = _binding1!!
    private var _binding2:AudioRecordBinding?=null
    private val binding2 get() = _binding2!!
    private var db = Firebase.firestore
    private lateinit var uri:Uri
    private val viewmodel: ViewModel by viewModels()
    val directory = File(directoryPath)
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SimpleDateFormat", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding1= ActivityAudioStoryBinding.inflate(layoutInflater)
        _binding2= AudioRecordBinding.bind(binding1.includelayout.root)
        setContentView(binding1.root)
        chronometer=findViewById(R.id.Chronometer)
        chronometer!!.base=SystemClock.elapsedRealtime()
        audioRecyclerView = binding1.recyclerView
        audioRecyclerView.layoutManager = LinearLayoutManager(this)
        audioRecyclerView.setHasFixedSize(true)
        audioList = arrayListOf<AudioData>()
        getaudio()
        //        record audio
        // Record to the external cache directory for visibility
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        binding1.addpost.setOnClickListener {
            binding2.send.visibility=View.GONE
            binding1.homeView.visibility = View.GONE
            binding1.audioPost.visibility = View.VISIBLE
            binding2.mic.visibility = View.VISIBLE
            binding2.recordComp.visibility = View.GONE
        }
        binding2.backhome.setOnClickListener {
            binding1.homeView.visibility = View.VISIBLE
            binding1.audioPost.visibility = View.GONE
            binding2.animationView.visibility = View.GONE
            binding2.send.visibility=View.GONE
            chronometer!!.base=SystemClock.elapsedRealtime()
            pauseoffset=0
            if(running)
            {
                chronometer!!.stop()
                pauseoffset=SystemClock.elapsedRealtime()- chronometer!!.base
                running=false
            }
            binding2.textView5.visibility=View.VISIBLE
        }


        binding2.mic.setOnClickListener {
            binding2.apply {
                recordComp.visibility = View.VISIBLE
                mic.visibility = View.GONE
                vibration()
                textView5.visibility = View.GONE
                animationView.visibility = View.VISIBLE
                animationView.playAnimation()
                send.visibility = View.VISIBLE
            }
            val sdf:SimpleDateFormat= SimpleDateFormat("dd.MM.yy_HH.mm.SS")
            fname=sdf.format(Date()).toString()
//            if chronometer is running
            chronometer!!.base = SystemClock.elapsedRealtime()
            pauseoffset = 0
            if (running) {
                chronometer!!.stop()
                pauseoffset = SystemClock.elapsedRealtime() - chronometer!!.base
                running = false
            }
//            start choronometer
            if (!running)
            {
//     create directory for audio file
                if (!directory.exists()) {
                    if (directory.mkdirs()) {
                        // Directory created successfully
                        recordStart()
                    } else {
                        // Failed to create directory
                        Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show()
                    }
                }else recordStart()
            }
        }


        binding2.apply {
            stop.setOnClickListener(View.OnClickListener { v ->
                recordComp.visibility = View.GONE
                animationView.visibility = View.GONE
                mic.visibility = View.VISIBLE
                textView5.visibility = View.VISIBLE
                send.visibility = View.GONE
                vibration()
                chronometer!!.base = SystemClock.elapsedRealtime()
                pauseoffset = 0
                if (running) {
                    chronometer!!.stop()
                    pauseoffset = SystemClock.elapsedRealtime() - chronometer!!.base
                    running = false
                }
                stopRecording()
                val namefile = EditText(v.context)
                val Dialog = androidx.appcompat.app.AlertDialog.Builder(v.context)
                Dialog.setTitle("Save Record ?")
                Dialog.setMessage("save and rename the file")
                namefile.setText("${fname.toString()}")
                Dialog.setView(namefile)
                Dialog.setPositiveButton(
                    "Save"
                ) { dialog, which ->
                    File(directoryPath, "${fname.toString()}.mp3").renameTo(
                        File(directoryPath , "${namefile.text.toString()}.mp3")
                    )
                }

                Dialog.setNegativeButton(
                    "Delete"
                ) { dialog, which ->
                    // close the dialog
                    File(directoryPath, "${fname.toString()}.mp3").delete()
                }
                Dialog.create().show()
            })
            play.setOnClickListener {
                vibration()
                animationView.visibility = View.VISIBLE
                animationView.playAnimation()
                //            start choronometer
                if (!running) {
                    chronometer!!.base = SystemClock.elapsedRealtime() - pauseoffset
                    chronometer!!.start()
                    running = true
                }
                resumeRecording()
            }


            pause.setOnClickListener {
                animationView.visibility = View.VISIBLE
                vibration()
                animationView.pauseAnimation()
//            pause chronometer
                if (running) {
                    chronometer!!.stop()
                    pauseoffset = SystemClock.elapsedRealtime() - chronometer!!.base
                    running = false
                }
                pauseRecording()

            }
        }
        uploadDialog = Dialog(this)
        progressDialog= ProgressDialog(this)
        binding2.send.setOnClickListener {
            if(running)
            {
                chronometer!!.stop()
                pauseoffset=SystemClock.elapsedRealtime()- chronometer!!.base
                running=false
            }
            stopRecording()
            binding2.animationView.pauseAnimation()
            uri=Uri.fromFile(File(fileName))
            upload_audio()
        }
        binding1.backmain.setOnClickListener {
            finish()
        }
        binding2.add.setOnClickListener {
            val intent:Intent= Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("audio/*")
            startActivityForResult(Intent.createChooser(intent,"select Audio"), PICK_FILE)
        }
    }

    private fun upload_audio() {
        uploadDialog!!.setContentView(R.layout.pop_upload)
        val closep:ImageView=uploadDialog!!.findViewById(R.id.closep)
        val uploadButton:AppCompatButton=uploadDialog!!.findViewById(R.id.uploadButton)
        closep.setOnClickListener {
            uploadDialog!!.dismiss()
        }
        val storyName:TextInputEditText=uploadDialog!!.findViewById(R.id.storyName)
        val disc:TextInputEditText=uploadDialog!!.findViewById(R.id.disc)
        val imagePick:ImageView= uploadDialog!!.findViewById(R.id.pickImage)
        image=uploadDialog!!.findViewById(R.id.thumbnail)
        imagePick.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, STORAGE_PERMISSION_CODE)
        }
        uploadButton.setOnClickListener {
            if (storyName.toString() != "" && disc.toString() != "") {
                progressDialog!!.setMessage("Uploading Started")
                progressDialog!!.show()
                val Userid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
                val ref = db.collection("AudioStory").document()
                val id = ref.id
                val audiop = AudioData(
                    storyName.text.toString(),
                    Userid,disc.text.toString(),
                    getMP3Duration(fileName),
                    "0",
                    id.toString(),
                )
                ref.set(audiop)
                    .addOnSuccessListener {
                        storyName.text!!.clear()
                        disc.text!!.clear()
                    }
                val storageref: StorageReference =
                    FirebaseStorage.getInstance().reference.child("bookhub/AudioStroy/${id.toString()}.mp3")
                storageref.putFile(uri).addOnSuccessListener {
                    progressDialog!!.dismiss()
                    if(bytedata!=null)
                    {
                        val ref=FirebaseStorage.getInstance().reference.child("bookhub/AudioStroy/image/${id.toString()}.jpeg")
                        ref.putBytes(bytedata!!).addOnSuccessListener {
                            toast("Uploading Finished")
                        }
                    }else{
                        toast("Uploading Finished")
                    }
                }
                uploadDialog!!.dismiss()

            } else {
                Toast.makeText(
                    this,
                    "story name and discription is required !",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        uploadDialog!!.show()
        uploadDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    private fun recordStart(){
        fileName =directoryPath+
                "${fname.toString()}.mp3"
        MediaRecorderReady()
        chronometer!!.start()
        running=true
        state=true
        try {
            recorder!!.prepare()
            recorder!!.start()
        } catch (_: Exception) {

        }

    }
    fun MediaRecorderReady() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
    }

    private fun stopRecording() {
        if (null != recorder) {
            state=false
            try {
//                recorder!!.prepare()
                recorder!!.stop()
                recorder!!.reset()
                recorder!!.release()
            } catch (_: Exception) {

            }
            recorder = null
        }
    }
    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }
    private fun pauseRecording() {
        if(state) {
            if(!recordingStopped){
                try{
                    recorder?.pause()
                }catch (_: Exception) {
                }
                recordingStopped = true
            }else{
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    private fun resumeRecording() {
        try {
            recorder?.resume()
            recordingStopped = false
        }catch (e: java.lang.IllegalStateException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun vibration() {
        var vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vib: VibrationEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.cancel()
            vibrator.vibrate(vib)
        }
    }
    private fun getaudio() {
        Firebase.firestore.collection("AudioStory").addSnapshotListener { Datasnapshot, error ->
            audioList.clear()
            if(error!=null)return@addSnapshotListener
            if (Datasnapshot!=null)
            {
                for (doc in Datasnapshot)
                {
                    val story=doc.toObject(AudioData::class.java)
                    audioList.add(story)
                }
                adapter = AudioAdapter(audioList,this@Audio_story_activity,this@Audio_story_activity,viewmodel)
                audioRecyclerView.adapter = adapter
            }
        }
//        searching
        val searchv=binding1.searchAudio
        search(searchv,audioList,this){
            adapter = AudioAdapter(it, this@Audio_story_activity, this,viewmodel)
            audioRecyclerView.adapter = adapter
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding1=null
        _binding2=null
    }
    override fun onItemClick(item: AudioData, position: Int) {
//        TODO("Not yet implemented")
        val intent=Intent(this,Play_Audio::class.java)
        intent.putExtra("id",item.postid)
        intent.putExtra("title",item.title)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if ( resultCode== RESULT_OK){
            if (requestCode== PICK_FILE && data!=null ) {
                uri = data.getData()!!
//                Toast.makeText(this, "Uploading", Toast.LENGTH_SHORT).show()
//                upload_audio(uri!!)
//                progressDialog!!.setMessage("Uploading Started")
//                progressDialog!!.show()
//                val Userid= FirebaseAuth.getInstance().currentUser!!.uid.toString()
//                val storageref: StorageReference = FirebaseStorage.getInstance().reference.child("bookhub/AudioStroy/$Userid/${Userid.toString()}.mp3")
//                storageref.putFile(uri!!).addOnSuccessListener {
//                    progressDialog!!.dismiss()
//                    Toast.makeText(this, "Uploading Finished", Toast.LENGTH_SHORT).show()
//                    Log.v(LOG_TAG,"upload")
//                }
            }
            if (requestCode == STORAGE_PERMISSION_CODE) {
                val imageUri = data!!.data
                // Crop the image using the CropImage library
                if (imageUri != null) {
                    cropImage(imageUri,this)
                }
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                val result = CropImage.getActivityResult(data)
                filePath = result.uri
                var bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath)
                val boas: ByteArrayOutputStream = ByteArrayOutputStream()
                bmp!!.compress(Bitmap.CompressFormat.JPEG, 25, boas)
                bytedata = boas.toByteArray()
                Glide.with(this).load(filePath).into(image!!)
            }
        }
    }
}