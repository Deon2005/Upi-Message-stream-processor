package com.example.miniproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showactivitymain()
        requestPermission()
        checkifnoregex()

    }

    fun showactivitymain() {
        setContentView(R.layout.activity_main)
        if (supportFragmentManager.findFragmentById(R.id.bottom_nav_container) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.bottom_nav_container, NavBarFragment())
                .commit()
        }
        val expensetoday = findViewById<TextView>(R.id.todayexpense)
        val expensemonth = findViewById<TextView>(R.id.monthexpense)
        val debittoday = findViewById<TextView>(R.id.todaydebit)
        val debitmonth = findViewById<TextView>(R.id.monthdebit)
        val credittoday = findViewById<TextView>(R.id.todaycredit)
        val creditmonth = findViewById<TextView>(R.id.monthcredit)
        lifecycleScope.launch {
            viewModel.todayDebit.collect { amount ->
                debittoday.text = amount.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.monthDebit.collect { amount ->
                debitmonth.text = amount.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.todayCredit.collect { amount ->
                credittoday.text = amount.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.monthCredit.collect { amount ->
                creditmonth.text = amount.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.todayDebit.collect { amount ->
                expensetoday.text = amount.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.monthDebit.collect { amount ->
                expensemonth.text = amount.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.chartdata.collect { dataPoints ->
                    updateChart(dataPoints)
            }
        }
        var chartleft=findViewById<ImageButton>(R.id.chartleft)
        var chartright=findViewById<ImageButton>(R.id.chartright)
        val chartOption=findViewById<TextView>(R.id.chartoption)

        chartleft.setOnClickListener()
        {
            chartOption.setText(viewModel.previousChart())
        }
        chartright.setOnClickListener()
        {
            chartOption.setText(viewModel.nextChart())
        }
        lifecycleScope.launch {
            viewModel.currentOptionIndex.collect { option ->
                chartOption.text = viewModel.options[option]
            }
        }
    }

    fun updateChart(dataPoints: List<Pair<Float, Float>>) {
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        if (dataPoints.isEmpty()) {
            lineChart.clear()
            return
        }
        val entries=ArrayList<Entry>()
        for(point in dataPoints)
        {
            entries.add(Entry(point.first, point.second))
        }

        val dataSet = LineDataSet(entries, "Expense")
        dataSet.color = ContextCompat.getColor(this, R.color.black)
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.black)
        dataSet.setDrawFilled(true)
        dataSet.lineWidth=3f
        dataSet.mode=LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(dataSet)
        lineChart.data=lineData
        lineChart.invalidate()
        lineChart.animateX(1000)
        lineChart.xAxis.setDrawGridLines(false)

        val yAxis = lineChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.setDrawGridLines(false)

    }

    fun requestPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS),1)
        }
    }

    private fun checkifnoregex()
    {
        lifecycleScope.launch {
            val database=AppDatabase.getDatabase(this@MainActivity)
            val count=database.regexDao().getCount()
            if(count==0)
            {
                callSetupDialog()
                receiveRegex()
            }
        }
    }

    fun receiveRegex() {
        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            // We only need the sample SMS to generate the pattern
            val sampleSMS = bundle.getString("bundle_sample")
            val activedialog=supportFragmentManager.findFragmentByTag("setupDialog") as? SetupDialogFragment

            if (sampleSMS != null) {
                lifecycleScope.launch {
                    activedialog?.onLoading()
                    Toast.makeText(this@MainActivity, "Asking Gemini for Regex...", Toast.LENGTH_SHORT).show()

                    val helper = GeminiHelper(this@MainActivity)
                    val result=helper.generateRegexFromSms(sampleSMS)


                    if (result != null) {
                        // 2. Just Log and Toast the raw pattern
                        Toast.makeText(this@MainActivity, "Regex Fetched!", Toast.LENGTH_LONG).show()
                            if (activedialog!=null&&activedialog.isVisible)
                            {
                                activedialog.onRegexGenerated(
                                    regex =  result.pattern,
                                    type = result.extractedWord
                                )
                            }
                    }

                    else {
                        Log.e("GEMINI_TEST", "❌ Failed to generate Regex")
                        Toast.makeText(this@MainActivity, "Failed to get Regex", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun callSetupDialog() {
        val dialog = SetupDialogFragment()
        dialog.show(supportFragmentManager, "setupDialog")
    }
}