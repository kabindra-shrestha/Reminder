package com.kabindra.reminder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kabindra.reminder.R
import com.kabindra.reminder.entity.Reminder
import kotlinx.android.synthetic.main.adapter_reminder_recycler_view_items.view.*

class ReminderRecyclerViewAdapter(val context: Context?, var reminders: List<Reminder>?) :
    RecyclerView.Adapter<ReminderRecyclerViewAdapter.ViewHolder>() {

    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.adapter_reminder_recycler_view_items, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item: Reminder = reminders!!.get(position)

        viewHolder.reminder_title?.text = item.reminderTitle
        var letter = "A"
        if (item.reminderTitle != null && item.reminderTitle!!.isNotEmpty()) {
            letter = item.reminderTitle!!.substring(0, 1)
        }

        viewHolder.reminder_date_time?.text = item.reminderDate + " " + item.reminderTime

        if (item.reminderRepeat!!) {
            viewHolder.recycle_repeat_info?.text =
                "Every ${item.reminderRepeatTime} ${item.reminderRepeatType}(s)"
        } else {
            viewHolder.recycle_repeat_info?.text = "Repeat Off"
        }

        if (item.reminderEnable!!) {
            viewHolder.reminder_active.setImageResource(R.drawable.reminder_active_on)
        } else {
            viewHolder.reminder_active.setImageResource(R.drawable.reminder_active_off)
        }

        viewHolder.reminder_repeat_switch.isChecked = item.reminderEnable!!

        viewHolder.itemView.setOnClickListener { listener.onClick(it, position) }

        viewHolder.reminder_repeat_switch.setOnClickListener {
            val isSwitch = viewHolder.reminder_repeat_switch.isChecked

            if (isSwitch) {
                viewHolder.reminder_active.setImageResource(R.drawable.reminder_active_on)
            } else {
                viewHolder.reminder_active.setImageResource(R.drawable.reminder_active_off)
            }

            listener.onSwitchClick(
                it, Reminder(
                    item.reminderId,
                    item.reminderTitle,
                    item.reminderDate,
                    item.reminderTime,
                    item.reminderRepeat,
                    item.reminderRepeatTime,
                    item.reminderRepeatType,
                    isSwitch
                ), position
            )
        }
    }

    override fun getItemCount(): Int {
        return reminders!!.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reminder_repeat_switch = view.reminder_repeat_switch
        val reminder_title = view.reminder_title
        val reminder_date_time = view.reminder_date_time
        val recycle_repeat_info = view.recycle_repeat_info
        val reminder_active = view.reminder_active
    }

    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
        fun onSwitchClick(
            view: View,
            reminder: Reminder,
            position: Int
        )
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun setNotifyData(reminders: List<Reminder>?) {
        this.reminders = reminders

        notifyDataSetChanged()
    }

}