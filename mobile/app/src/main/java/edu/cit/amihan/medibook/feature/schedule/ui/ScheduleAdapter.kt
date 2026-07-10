package edu.cit.amihan.medibook.feature.schedule.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cit.amihan.medibook.databinding.ItemScheduleBinding
import edu.cit.amihan.medibook.feature.schedule.model.DoctorSchedule

class ScheduleAdapter(
    private var schedules: List<DoctorSchedule>,
    private val onSlotClick: (DoctorSchedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.binding.tvSlotTime.text = "${schedule.startTime ?: ""}  →  ${schedule.endTime ?: ""}"
        holder.binding.root.setOnClickListener { onSlotClick(schedule) }
    }

    override fun getItemCount(): Int = schedules.size

    fun updateData(newSchedules: List<DoctorSchedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }
}