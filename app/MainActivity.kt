import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import java.util.*


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: QueueAdapter

    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var timePicker: TimePicker
    private lateinit var durationDialog: AlertDialog

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var adminLocation: Location

    private lateinit var loginButton: Button

    private var userLocation: Location? = null

    private var queue = LinkedList<Member>()

    private var timerRunning = false
    private var totalTimeInMillis: Long = 0
    private var timeLeftInMillis: Long = 0
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.queueRecyclerView)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        adapter = QueueAdapter(queue)
        recyclerView.adapter = adapter

        timerText = findViewById(R.id.timerText)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        timePicker = LayoutInflater.from(this).inflate(R.layout.dialog_duration, null) as TimePicker
        durationDialog = AlertDialog.Builder(this)
            .setTitle("Set Duration")
            .setMessage("Set the expected time of the queue")
            .setView(timePicker)
            .setPositiveButton("Set", DialogInterface.OnClickListener { _, _ ->
                val minutes = timePicker.minute
                totalTimeInMillis = minutes * 60 * 1000
                timeLeftInMillis = totalTimeInMillis
                updateTimer()
            })
            .setNegativeButton("Cancel", null)
            .create()

        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, 1)
        }

        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onStart() {
        super.onStart()
        if (!checkLocationPermission()) {
            requestLocationPermission()
        } else {
            if (!isLocationEnabled()) {
                showLocationSettingsDialog()
            } else {
                googleApiClient.connect()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    override fun onConnected(p0: Bundle?) {
        if (checkLocationPermission())
