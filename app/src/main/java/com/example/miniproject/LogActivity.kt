package com.example.miniproject

import android.R.attr.duration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.collections.emptyList
import android.view.Window
import com.google.android.material.transition.platform.MaterialContainerTransform

class LogActivity: AppCompatActivity() {
    private lateinit var adapter: TransactionAdapter
    private val today= LocalDate.now()
    private val startOfMonth = today.withDayOfMonth(1)
    private val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
    val dao=AppDatabase.getDatabase(this).transactionDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        window.sharedElementEnterTransition = MaterialContainerTransform().apply{
            addTarget(android.R.id.content)
            duration = 1000L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 600L
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logdata)

        val btn_back = findViewById<ImageButton>(R.id.btn_back)
        btn_back.setOnClickListener {
            supportFinishAfterTransition()
        }
        val incomingTransitionName=intent.getStringExtra("transitionName")
        findViewById<View>(android.R.id.content).transitionName = incomingTransitionName
        val logType =intent.getStringExtra("type")
        Log.d("LogActivity", "Log type: $logType")
        val detail_total_amount=findViewById<TextView>(R.id.detail_total_amount)
        val detail_title=findViewById<TextView>(R.id.detail_title)
        val list_header=findViewById<TextView>(R.id.list_header)



        val recyclerView =findViewById<RecyclerView>(R.id.recyclerView_transactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter=TransactionAdapter(
            transactionList = emptyList(),
            onEditClick = { transaction ->
                Log.d("LogActivity", "Edit transaction: $transaction.id")
                val dialog = TransactionDialogFragment.newInstance(transaction)
                dialog.show(supportFragmentManager, "TransactionDialog") },
            onDeleteClick = { transaction ->
                Log.d("LogActivity", "Delete transaction: $transaction.id")
                lifecycleScope.launch {
                    dao.deleteTransaction(transaction.id)
                }
            }
            )
        recyclerView.adapter = adapter
        lifecycleScope.launch {
            if(logType=="expensetoday")
             {

                 detail_title.setText("Expense")
                 list_header.setText("Today's Expense")
                 detail_total_amount.setTextColor(Color.parseColor("#DA3633"))
                 launch {
                     dao.getTransactionsByDateandtype(today, 0).collect { fullList ->
                         adapter.updateData(fullList)
                     }
                 }
                 launch {
                     dao.getDailySum(today, 0).collect { amount ->
                         val safeAmount = amount ?: 0.0
                         detail_total_amount.text = "₹"+safeAmount.toString()
                     }
                 }
            }
            else if(logType=="expensemonth")
            {
                detail_title.setText("Expense")
                list_header.setText("Current Months's Expense")
                detail_total_amount.setTextColor(Color.parseColor("#DA3633"))
                launch {
                    dao.getMonthlydata(startOfMonth, endOfMonth, 0).collect { fullList ->
                        adapter.updateData(fullList)
                    }
                }
                launch {
                    dao.getMonthlySum(startOfMonth, endOfMonth, 0).collect { amount ->
                        val safeAmount = amount ?: 0.0
                        detail_total_amount.text = "₹"+safeAmount.toString()
                    }
                }
            }
            else if(logType=="debittoday")
            {
                detail_title.setText("Debits")
                list_header.setText("Debits Today")
                detail_total_amount.setTextColor(Color.parseColor("#DA3633"))
                launch {
                    dao.getSMSTransactionsByDateandtype(today, 0,0).collect { fullList ->
                        adapter.updateData(fullList)
                    }
                }
                launch {
                    dao.getSMSDailySum(today, 0,0).collect { amount ->
                        val safeAmount = amount ?: 0.0
                        detail_total_amount.text = "₹"+safeAmount.toString()
                    }
                }
            }
            else if(logType=="debitmonth")
            {
                detail_title.setText("Debit")
                list_header.setText("This Months's Debits")
                detail_total_amount.setTextColor(Color.parseColor("#DA3633"))
                launch {
                    dao.getSMSMonthlydata(startOfMonth, endOfMonth, 0,0).collect { fullList ->
                        adapter.updateData(fullList)
                    }
                }
                launch {
                    dao.getSMSMonthlySum(startOfMonth, endOfMonth, 0,0).collect { amount ->
                        val safeAmount = amount ?: 0.0
                        detail_total_amount.text = "₹"+safeAmount.toString()
                    }
                }
            }
            else if(logType=="credittoday")
            {
                detail_title.setText("Credit")
                list_header.setText("Credits Today")
                detail_total_amount.setTextColor(Color.parseColor("#01E901"))
                launch {
                    dao.getSMSTransactionsByDateandtype(today, 1,0).collect { fullList ->
                        adapter.updateData(fullList)
                    }
                }
                launch {
                    dao.getSMSDailySum(today, 1,0).collect { amount ->
                        val safeAmount = amount ?: 0.0
                        detail_total_amount.text = "₹"+safeAmount.toString()
                    }
                }
            }
            else if(logType=="creditmonth")
            {
                detail_title.setText("Credit")
                list_header.setText("This Months's Credit")
                detail_total_amount.setTextColor(Color.parseColor("#01E901"))
                launch {
                    dao.getSMSMonthlydata(startOfMonth, endOfMonth, 1,0).collect { fullList ->
                        adapter.updateData(fullList)
                    }
                }
                launch {
                    dao.getSMSMonthlySum(startOfMonth, endOfMonth, 1,0).collect { amount ->
                        val safeAmount = amount ?: 0.0
                        detail_total_amount.text = "₹"+safeAmount.toString()
                    }
                }
            }
        }

        val swipeCallback=object :androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0,androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean{
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.setOpenedPosition(viewHolder.adapterPosition)
            }
            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            )
            {
                if(actionState ==androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE)
                {
                    val foregroundView = (viewHolder as TransactionAdapter.TransactionViewHolder).foregroundCard
                    val maxSwipeDistance = -300f
                    val newDx = if (dX < maxSwipeDistance) maxSwipeDistance else dX

                    androidx.recyclerview.widget.ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                        c, recyclerView, foregroundView, newDx, dY, actionState, isCurrentlyActive
                    )
                }
            }
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val foregroundView = (viewHolder as TransactionAdapter.TransactionViewHolder).foregroundCard
                val position = viewHolder.adapterPosition

                // If this is our opened row, firmly hold it at -300.
                // Otherwise, let Android reset it to 0 as normal.
                if (position == adapter.getOpenedPosition()) {
                    foregroundView.translationX = -300f
                } else {
                    androidx.recyclerview.widget.ItemTouchHelper.Callback.getDefaultUIUtil().clearView(foregroundView)
                }

                super.clearView(recyclerView, viewHolder)
            }
        }
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

}