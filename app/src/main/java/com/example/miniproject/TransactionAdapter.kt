package com.example.miniproject

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class TransactionAdapter(
    private var transactionList: List<Transaction>,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var openedPosition = -1

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val upiId: TextView = itemView.findViewById(R.id.item_upi_id)
        val amount: TextView = itemView.findViewById(R.id.item_amount)
        val account: TextView = itemView.findViewById(R.id.item_account)
        val date: TextView = itemView.findViewById(R.id.item_date)
        val btnEdit: View = itemView.findViewById(R.id.btn_edit)
        val btnDelete: View = itemView.findViewById(R.id.btn_delete)
        val foregroundCard: View = itemView.findViewById(R.id.foreground_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = transactionList[position]
        holder.upiId.text = currentItem.upiID
        holder.account.text = "A/C: ${currentItem.accountNumber}"
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        holder.date.text = currentItem.date.format(formatter)

        if (currentItem.type == 0) {
            holder.amount.text = "- ₹${currentItem.amount}"
            holder.amount.setTextColor(Color.parseColor("#DA3633"))
        } else {
            holder.amount.text = "+ ₹${currentItem.amount}"
            holder.amount.setTextColor(Color.parseColor("#00E676"))
        }

        // --- SWIPE LOGIC FOR FRAGMENT ---
        holder.foregroundCard.translationX = if (position == openedPosition) -300f else 0f

        var startX = 0f
        holder.foregroundCard.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    if (openedPosition != -1 && openedPosition != holder.adapterPosition) {
                        closeMenu()
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - startX
                    if (deltaX < 0 && deltaX > -400f) {
                        v.translationX = deltaX
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (v.translationX < -150f) {
                        v.animate().translationX(-300f).setDuration(200).start()
                        setOpenedPosition(holder.adapterPosition)
                    } else {
                        v.animate().translationX(0f).setDuration(200).start()
                        if (openedPosition == holder.adapterPosition) {
                            openedPosition = -1
                        }
                    }
                    v.performClick()
                    true
                }
                else -> false
            }
        }

        holder.btnEdit.setOnClickListener {
            closeMenu()
            onEditClick(currentItem)
        }

        holder.btnDelete.setOnClickListener {
            closeMenu()
            onDeleteClick(currentItem)
        }
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateData(newList: List<Transaction>) {
        transactionList = newList
        notifyDataSetChanged()
    }

    // --- LOGACTIVITY FIXES START ---

    // 1. Add this getter to fix the Unresolved Reference error
    fun getOpenedPosition(): Int = openedPosition

    // 2. Modified to ensure visual consistency
    fun setOpenedPosition(position: Int) {
        val previousOpen = openedPosition
        openedPosition = position

        if (previousOpen != -1 && previousOpen != position) {
            notifyItemChanged(previousOpen)
        }
        notifyItemChanged(position)
    }

    fun closeMenu() {
        if (openedPosition != -1) {
            val oldPosition = openedPosition
            openedPosition = -1
            notifyItemChanged(oldPosition)
        }
    }
    // --- LOGACTIVITY FIXES END ---
}