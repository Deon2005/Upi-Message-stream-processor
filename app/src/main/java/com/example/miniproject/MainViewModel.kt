package com.example.miniproject

import android.app.Application
import android.widget.ImageButton
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel (application: Application): AndroidViewModel(application)
{
    private val dao= AppDatabase.getDatabase(application).transactionDao()

    private val today= LocalDate.now()
    private val startOfMonth = today.withDayOfMonth(1)
    private val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

    val todayDebit: StateFlow<Double> = dao.getDailySum(today,0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),0.0)
    val monthDebit: StateFlow<Double> = dao.getMonthlySum(startOfMonth, endOfMonth, 0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val todayCredit: StateFlow<Double> = dao.getDailySum(today, 1).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val monthCredit: StateFlow<Double> = dao.getMonthlySum(startOfMonth, endOfMonth, 1).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    //Code for the line graph
    private val chart_Data= MutableStateFlow<List<Pair<Float,Float>>>(emptyList())
    val chartdata: StateFlow<List<Pair<Float,Float>>> = chart_Data.asStateFlow()
    val options =arrayOf("This week","This month","Last week","Last month")
    private val _currentOptionIndex = MutableStateFlow(0)
    val currentOptionIndex: StateFlow<Int> = _currentOptionIndex.asStateFlow()
    private var chartJob: Job? = null
    init{
        loadData()
    }

    fun nextChart(): String {
        _currentOptionIndex.value=(_currentOptionIndex.value+1)%options.size
        loadData()
        return options[_currentOptionIndex.value]
    }
    fun previousChart(): String {
        _currentOptionIndex.value = (_currentOptionIndex.value - 1 + options.size) % options.size
        loadData()
        return options[_currentOptionIndex.value]
    }

    private fun loadData() {
        chartJob?.cancel()
        chartJob=viewModelScope.launch {
            val index = _currentOptionIndex.value
            val startDate: LocalDate
            val endDate: LocalDate

            when (index) {
                0 -> {
                    startDate = today.with(java.time.DayOfWeek.MONDAY)
                    endDate = today//.with(java.time.DayOfWeek.SUNDAY)
                }

                1 -> {
                    startDate = startOfMonth
                    endDate = today
                }

                2 -> {
                    val lastweek = today.minusWeeks(1)
                    startDate = lastweek.with(java.time.DayOfWeek.MONDAY)
                    endDate = lastweek.with(java.time.DayOfWeek.SUNDAY)
                }

                3 -> {
                    val lastmonth = today.minusMonths(1)
                    startDate = lastmonth.withDayOfMonth(1)
                    endDate = lastmonth.withDayOfMonth(lastmonth.lengthOfMonth())
                }

                else -> return@launch
            }
            dao.getDailyTotals(startDate,endDate).collect{
                rawData->
                val dataMap = rawData.associate { it.date to it.total }
                val newChartData = mutableListOf<Pair<Float,Float>>()
                var currentDate = startDate

            while (!currentDate.isAfter(endDate)) {
                val totalForDay = dataMap[currentDate] ?: 0.0

                // If it's a Week view, X axis is the Day of Week (1 to 7)
                // If it's a Month view, X axis is the Day of Month (1 to 31)
                val xValue = if (index == 0 || index == 2) {
                    currentDate.dayOfWeek.value.toFloat()
                } else {
                    currentDate.dayOfMonth.toFloat()
                }

                val yValue = totalForDay.toFloat()
                newChartData.add(Pair(xValue, yValue))

                // Move to the very next day for the next loop iteration
                currentDate = currentDate.plusDays(1)
            }
        /*for(item in rawData)
        {
            val xValue = if (_currentOptionIndex.value == 0 || _currentOptionIndex.value == 2) {
                item.date.dayOfWeek.value.toFloat()
            } else {
                item.date.dayOfMonth.toFloat()
            }

            val yValue = item.total.toFloat()
            newChartData.add(Pair(xValue, yValue))
        }*/
            chart_Data.value=newChartData
        }}
    }
}