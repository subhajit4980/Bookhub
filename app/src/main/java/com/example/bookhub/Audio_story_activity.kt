package com.example.bookhub

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.Adapter.AudioAdapter
import com.example.bookhub.Adapter.MyAdapter
import com.example.bookhub.R
import com.example.bookhub.data.AudioData
import com.example.bookhub.data.books
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_audio_story.*
import kotlinx.android.synthetic.main.audio_record.*
import kotlinx.android.synthetic.main.pop_upload.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.Nullable


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val PICK_FILE = 99
private var uri: Uri?=null
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
    private lateinit var audioRecyclerView: RecyclerView
    private lateinit var audioList: ArrayList<AudioData>
    lateinit var adapter: AudioAdapter
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }
    @SuppressLint("SimpleDateFormat", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_story)
//        setContentView(R.layout.audio_record)
        chronometer=findViewById(R.id.chronometer)
        chronometer!!.base=SystemClock.elapsedRealtime()

        audioRecyclerView = findViewById(R.id.recyclerView)
        audioRecyclerView.layoutManager = LinearLayoutManager(this)
        audioRecyclerView.setHasFixedSize(true)
        audioList = arrayListOf<AudioData>()
        try {
            getaudio()
        }
        catch (e:Exception)
        {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        //        record audio
        // Record to the external cache directory for visibility
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)


        addpost.setOnClickListener {
            send.visibility=View.GONE
            home_view.visibility = View.GONE
            audio_post.visibility = View.VISIBLE
            mic.visibility = View.VISIBLE
            recordComp.visibility = View.GONE
        }
        backhome.setOnClickListener {
            home_view.visibility = View.VISIBLE
            audio_post.visibility = View.GONE
            animation_view.visibility = View.GONE
            send.visibility=View.GONE
            chronometer!!.base=SystemClock.elapsedRealtime()
            pauseoffset=0
            if(running)
            {
                chronometer!!.stop()
                pauseoffset=SystemClock.elapsedRealtime()- chronometer!!.base
                running=false
            }
            textView5.visibility=View.VISIBLE
        }


        mic.setOnClickListener {
            recordComp.visibility = View.VISIBLE
            mic.visibility = View.GONE
            vibration()
            textView5.visibility = View.GONE
            animation_view.visibility = View.VISIBLE
            animation_view.playAnimation()
            send.visibility = View.VISIBLE

            val sdf:SimpleDateFormat= SimpleDateFormat("dd.MM.yy_HH.mm.SS")
            fname=sdf.format(Date()).toString()
//            start choronometer
            if (!running)
//     create directory for audio file
            File(Environment.getExternalStorageDirectory().absolutePath+"/BookHUB/").mkdir()
//            startRecording()
            recordStart()
        }


        stop.setOnClickListener(View.OnClickListener { v->
            recordComp.visibility = View.GONE
            animation_view.visibility = View.GONE
            mic.visibility = View.VISIBLE
            textView5.visibility = View.VISIBLE
            send.visibility = View.GONE
            vibration()
            chronometer!!.base=SystemClock.elapsedRealtime()
            pauseoffset=0
            if(running)
            {
                chronometer!!.stop()
                pauseoffset=SystemClock.elapsedRealtime()- chronometer!!.base
                running=false
            }
            stopRecording()
            val namefile = EditText(v.context)
            val passwordResetDialog = androidx.appcompat.app.AlertDialog.Builder(v.context)
            passwordResetDialog.setTitle("Save Record ?")
            passwordResetDialog.setMessage("save and rename the file")
            namefile.setText("${fname.toString()}")
            passwordResetDialog.setView(namefile)
            passwordResetDialog.setPositiveButton(
                "OK"
            ) { dialog, which -> // extract the email and send reset link
                File(Environment.getExternalStorageDirectory().absolutePath+"/BookHUB/${fname.toString()}.mp3").
                renameTo(File(Environment.getExternalStorageDirectory().absolutePath+"/BookHUB/${namefile.text.toString()}.mp3"))
            }

            passwordResetDialog.setNegativeButton(
                "Delete"
            ) { dialog, which ->
                // close the dialog
                File(Environment.getExternalStorageDirectory().absolutePath+"/BookHUB/${fname.toString()}.mp3").delete()
            }
            passwordResetDialog.create().show()
        })


        play.setOnClickListener {
            vibration()
            animation_view.visibility = View.VISIBLE
            animation_view.playAnimation()
            //            start choronometer
            if (!running)
            {
                chronometer!!.base=SystemClock.elapsedRealtime()- pauseoffset
                chronometer!!.start()
                running=true
            }
            resumeRecording()
        }


        pause.setOnClickListener {
            animation_view.visibility = View.VISIBLE
            vibration()
            animation_view.pauseAnimation()
//            pause chronometer
            if(running)
            {
                chronometer!!.stop()
                pauseoffset=SystemClock.elapsedRealtime()- chronometer!!.base
                running=false
            }
            pauseRecording()

        }

        uploadDialog = Dialog(this)
        progressDialog= ProgressDialog(this)
        send.setOnClickListener {
            if(running)
            {
                chronometer!!.stop()
                pauseoffset=SystemClock.elapsedRealtime()- chronometer!!.base
                running=false
            }
            stopRecording()
            animation_view.pauseAnimation()
            uploadDialog!!.setContentView(R.layout.pop_upload)
            uploadDialog!!.closep.setOnClickListener {
                uploadDialog!!.dismiss()
            }
            val storyName:TextInputEditText=uploadDialog!!.findViewById(R.id.storyName)
            val disc:TextInputEditText=uploadDialog!!.findViewById(R.id.disc)
                uploadDialog!!.uploadButton.setOnClickListener {
                    if (storyName.toString() != "" && disc.toString() != "") {
                        progressDialog!!.setMessage("Uploading Started")
                        progressDialog!!.show()
//                val storyName:EditText=uploadDialog.findViewById(R.id.)
                        val Userid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
                        var name: String?=null
                        var refuser: DatabaseReference =
                            FirebaseDatabase.getInstance().getReference("bookhub/users")
                                .child(Userid)
                        refuser.get().addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val dataSnapshot = task.result
                                name = dataSnapshot.child("name").value.toString()
                            } else {
                                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        val ref: DatabaseReference =
                            FirebaseDatabase.getInstance().getReference("bookhub")
                        val audiop = AudioData(
                            storyName.text.toString(),
                            name,disc.text.toString(),
                            "0",
                            "0",
                            "bookhub/AudioStroy/${Userid.toString() + storyName.text.toString()}.mp3"
                        )
                        ref.child("AudioStory").child(storyName.text.toString()).setValue(audiop)
                            .addOnSuccessListener {
                                storyName.text!!.clear()
                                disc.text!!.clear()
                            }
                        val storageref: StorageReference =
                            FirebaseStorage.getInstance().reference.child("bookhub/AudioStroy/${Userid.toString() + storyName.text.toString()}.mp3")
                        val uri: Uri = Uri.fromFile(File(fileName))
                        storageref.putFile(uri).addOnSuccessListener {
                            progressDialog!!.dismiss()
                            Toast.makeText(this, "Uploading Finished", Toast.LENGTH_SHORT).show()
                            Log.v(LOG_TAG, "upload")
//                    OnSuccessListener<UploadTask.TaskSnapshot>
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
        backmain.setOnClickListener {
            finish()
        }
        add.setOnClickListener {
           val intent:Intent= Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("audio/*")
            startActivityForResult(Intent.createChooser(intent,"select Audio"),PICK_FILE)
        }
    }

    @Override
    internal fun onActivityResult(requestCode: Int, resultCode:Int, data:Intent) {
    super.onActivityResult(requestCode,resultCode,data)
    if (requestCode== PICK_FILE && resultCode== RESULT_OK){
        if (data!=null)
        {
           uri =data.getData()
            progressDialog!!.setMessage("Uploading Started")
            progressDialog!!.show()
            val Userid= FirebaseAuth.getInstance().currentUser!!.uid.toString()
            val storageref: StorageReference = FirebaseStorage.getInstance().reference.child("bookhub/AudioStroy/$Userid/${Userid.toString()}.mp3")
            storageref.putFile(uri!!).addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(this, "Uploading Finished", Toast.LENGTH_SHORT).show()
                Log.v(LOG_TAG,"upload")
            }
        }
    }
}

    private fun recordStart(){
            fileName = Environment.getExternalStorageDirectory().absolutePath +
                    "/BookHUB/${fname.toString()}.mp3"
            MediaRecorderReady()
            state=true
            try {
                recorder!!.prepare()
                recorder!!.start()
            } catch (e: IllegalStateException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            Toast.makeText(
                this, "Recording started",
                Toast.LENGTH_LONG
            ).show()
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
            } catch (e: java.lang.IllegalStateException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
            recorder = null
        }
    }
    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }
    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if(state) {
            if(!recordingStopped){
                Toast.makeText(this,"Paused!", Toast.LENGTH_SHORT).show()
                try{
                    recorder?.pause()
                }catch (e: java.lang.IllegalStateException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
                recordingStopped = true
            }else{
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this, "Resume!", Toast.LENGTH_SHORT).show()
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
        val dbref = FirebaseDatabase.getInstance().getReference("bookhub/AudioStory")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                audioList.clear()
                try {
                    if (snapshot.exists()) {
                        for (bookSnapshot in snapshot.children) {
                            val book = bookSnapshot.getValue(AudioData::class.java)!!
                            audioList.add(book)
                        }
                        adapter = AudioAdapter(audioList, this@Audio_story_activity, this@Audio_story_activity)
                        audioRecyclerView.adapter = adapter
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@Audio_story_activity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onItemClick(item: AudioData, position: Int) {
//        TODO("Not yet implemented")
        Toast.makeText(this, "selected", Toast.LENGTH_SHORT).show()
    }
}