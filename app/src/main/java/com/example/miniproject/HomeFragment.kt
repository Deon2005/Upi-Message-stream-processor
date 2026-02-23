package com.example.miniproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val expensetoday = view.findViewById<TextView>(R.id.todayexpense)
        val expensemonth = view.findViewById<TextView>(R.id.monthexpense)
        val debittoday = view.findViewById<TextView>(R.id.todaydebit)
        val debitmonth = view.findViewById<TextView>(R.id.monthdebit)
        val credittoday = view.findViewById<TextView>(R.id.todaycredit)
        val creditmonth = view.findViewById<TextView>(R.id.monthcredit)
        val chartleft = view.findViewById<ImageButton>(R.id.chartleft)
        val chartright = view.findViewById<ImageButton>(R.id.chartright)
        val chartOption = view.findViewById<TextView>(R.id.chartoption)

        // Observe the ViewModel data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayDebit.collect { debittoday.text = it.toString()}
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.expensetoday.collect { expensetoday.text = it.toString() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthDebit.collect { debitmonth.text = it.toString()  }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.expensemonth.collect { expensemonth.text = it.toString() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayCredit.collect { credittoday.text = it.toString() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthCredit.collect { creditmonth.text = it.toString() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chartdata.collect { updateChart(it, view) }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentOptionIndex.collect { chartOption.text = viewModel.options[it] }
        }

        // Setup Clicks
        chartleft.setOnClickListener { chartOption.text = viewModel.previousChart() }
        chartright.setOnClickListener { chartOption.text = viewModel.nextChart() }

        expensetoday.setOnClickListener { showlog("expensetoday") }
        expensemonth.setOnClickListener { showlog("expensemonth") }
        debittoday.setOnClickListener { showlog("debittoday") }
        debitmonth.setOnClickListener { showlog("debitmonth") }
        credittoday.setOnClickListener { showlog("credittoday") }
        creditmonth.setOnClickListener { showlog("creditmonth") }
    }

    private fun showlog(type: String) {
        val intent = Intent(requireContext(), LogActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    private fun updateChart(dataPoints: List<Pair<Float, Float>>, view: View) {
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)
        if (dataPoints.isEmpty()) {
            lineChart.clear()
            return
        }
        val entries = ArrayList<Entry>()
        for (point in dataPoints) {
            entries.add(Entry(point.first, point.second))
        }

        val dataSet = LineDataSet(entries, "Expense")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.white)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        dataSet.setDrawFilled(true)
        dataSet.lineWidth = 3f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()
        lineChart.animateX(1000)
        lineChart.xAxis.setDrawGridLines(false)

        val yAxis = lineChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.setDrawGridLines(false)
    }
}