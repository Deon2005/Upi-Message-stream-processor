package com.example.miniproject

import android.app.Application
import android.widget.ImageButton
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
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

    val expensetoday: StateFlow<Double> = dao.getDailySum(today, 0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expensemonth: StateFlow<Double> = dao.getMonthlySum(startOfMonth, endOfMonth, 0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val todayDebit: StateFlow<Double> = dao.getSMSDailySum(today,0,0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),0.0)
    val monthDebit: StateFlow<Double> = dao.getSMSMonthlySum(startOfMonth, endOfMonth, 0,0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val todayCredit: StateFlow<Double> = dao.getSMSDailySum(today, 1,0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val monthCredit: StateFlow<Double> = dao.getSMSMonthlySum(startOfMonth, endOfMonth, 1,0).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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


                val xValue = if (index == 0 || index == 2) {
                    currentDate.dayOfWeek.value.toFloat()
                } else {
                    currentDate.dayOfMonth.toFloat()
                }

                val yValue = totalForDay.toFloat()
                newChartData.add(Pair(xValue, yValue))

                currentDate = currentDate.plusDays(1)
            }

            chart_Data.value=newChartData
        }

        }
    }
    fun getHistory(
        typeString: String,
        modeString: String,
        dateMode: String,
        startMs: Long,
        endMs: Long
    ): Flow<List<Transaction>> {

        val typeInt = when (typeString) {
            "Debit" -> 0
            "Credit" -> 1
            else -> -1
        }

        val modeInt = when (modeString) {
            "Online" -> 0
            "Cash" -> 1
            else -> -1
        }

        val startDate = if (dateMode == "All") LocalDate.of(2000, 1, 1) else convertToLocalDate(startMs)
        val endDate = if (dateMode == "All") LocalDate.of(2100, 12, 31) else convertToLocalDate(endMs)

        // BRANCHING LOGIC: Pick the cleanest query
        return when {
            typeInt == -1 && modeInt == -1 -> dao.getAllHistory(startDate, endDate)
            typeInt != -1 && modeInt == -1 -> dao.getHistoryByType(typeInt, startDate, endDate)
            typeInt == -1 && modeInt != -1 -> dao.getHistoryByMode(modeInt, startDate, endDate)
            else -> dao.getHistoryFullFilter(typeInt, modeInt, startDate, endDate)
        }
    }
    private fun convertToLocalDate(timestamp: Long): LocalDate {
        return java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction.id) // Ensure your DAO has a @Delete function
        }
    }
}