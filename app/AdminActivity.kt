import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class AdminActivity : AppCompatActivity() {
    private lateinit var queue: MutableList<String>
    private lateinit var queueListView: ListView
    private lateinit var addMemberEditText: EditText
    private lateinit var addMemberButton: Button
    private lateinit var waitTimeEditText: EditText
    private lateinit var setWaitTimeButton: Button

    private lateinit var timer: CountDownTimer
    private var timeRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        queue = mutableListOf()
        queueListView = findViewById(R.id.queueListView)
        addMemberEditText = findViewById(R.id.addMemberEditText)
        addMemberButton = findViewById(R.id.addMemberButton)
        waitTimeEditText = findViewById(R.id.waitTimeEditText)
        setWaitTimeButton = findViewById(R.id.setWaitTimeButton)

        queueListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, queue)

        addMemberButton.setOnClickListener {
            val memberName = addMemberEditText.text.toString()

            if (memberName.isNotEmpty()) {
                queue.add(memberName)
                addMemberEditText.text.clear()
                (queueListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }
        }

        setWaitTimeButton.setOnClickListener {
            val waitTimeString = waitTimeEditText.text.toString()

            if (waitTimeString.isNotEmpty()) {
                try {
                    val waitTime = waitTimeString.toLong()

                    if (waitTime > 0) {
                        timeRemaining = waitTime * 1000
                        startTimer()
                    } else {
                        Toast.makeText(this, "Wait time must be positive", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid wait time", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val minutes = (timeRemaining / 1000) / 60
                val seconds = (timeRemaining / 1000) % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                setTitle("Virtual Queue - $timeString remaining")
            }

            override fun onFinish() {
                setTitle("Virtual Queue")
                Toast.makeText(this@AdminActivity, "Queue is now empty", Toast.LENGTH_SHORT).show()
                queue.clear()
                (queueListView.adapter as ArrayAdapter<
