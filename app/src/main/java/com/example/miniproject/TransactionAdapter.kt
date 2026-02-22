package com.example.miniproject

import android.graphics.Color
import android.view.LayoutInflater
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

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = transactionList[position]
        holder.upiId.text = currentItem.upiID
        holder.account.text = "A/C: ${currentItem.accountNumber}"
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        holder.date.text = currentItem.date.format(formatter)
        if (currentItem.type == 0) {
            holder.amount.text = "- ₹${currentItem.amount}"
            holder.amount.setTextColor(Color.parseColor("#DA3633")) // Red
        } else {
            holder.amount.text = "+ ₹${currentItem.amount}"
            holder.amount.setTextColor(Color.parseColor("#00E676")) // Green
        }

        if (position == openedPosition) {
            holder.foregroundCard.translationX = -300f // Keep it open
        } else {
            holder.foregroundCard.translationX = 0f    // Keep it closed
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

    override fun getItemCount(): Int {
        return transactionList.size
    }

    fun updateData(newList: List<Transaction>) {
        transactionList = newList
        notifyDataSetChanged()
    }

    private var openedPosition = -1

    fun getOpenedPosition(): Int = openedPosition

    fun setOpenedPosition(position: Int) {
        val previousOpen = openedPosition
        openedPosition = position

        // If another card was open, close it!
        if (previousOpen != -1) notifyItemChanged(previousOpen)
        // Force the new card to draw in the open state
        notifyItemChanged(openedPosition)
    }

    fun closeMenu() {
        if (openedPosition != -1) {
            val oldPosition = openedPosition
            openedPosition = -1
            notifyItemChanged(oldPosition)
        }
    }
}