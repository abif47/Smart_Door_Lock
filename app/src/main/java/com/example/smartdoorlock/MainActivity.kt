package com.example.smartdoorlock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationid= 101
    private lateinit var StatusSensor: TextView
    private lateinit var StatusPintu: TextView
    private lateinit var ButtonBuka: Button
    private lateinit var ButtonTutup: Button
    lateinit var ref: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ref = FirebaseDatabase.getInstance().getReference("")

        StatusSensor = findViewById(R.id.tv_sensor)
        StatusPintu = findViewById(R.id.tv_pintu)
        ButtonBuka = findViewById(R.id.btn_buka)
        ButtonTutup = findViewById(R.id.btn_tutup)

        createNotificationChannel()
        ButtonBuka.setOnClickListener {
            ref.child("Status_Pintu").setValue("Pintu Terbuka")
        }

        ButtonTutup.setOnClickListener {
            ref.child("Status_Pintu").setValue("Pintu Tertutup")
        }
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    StatusSensor.setText(snapshot.child("Status_Sensor").value.toString())
                    StatusPintu.setText(snapshot.child("Status_Pintu").value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // kosong
            }
        })
        val postReference = FirebaseDatabase.getInstance().getReference("Status_Sensor")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue()
                if (post != null) {
                    if (post.equals("Ada Orang di depan Pintu")){
                        sendNotification()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        postReference.addValueEventListener(postListener)
    }
    private fun  createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun sendNotification(){
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Kondisi Depan Pintu")
            .setContentText("Ada Orang di depan Pintu")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)){
            notify(notificationid, builder.build())
        }
    }
}