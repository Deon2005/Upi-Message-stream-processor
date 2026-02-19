package com.example.miniproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
}