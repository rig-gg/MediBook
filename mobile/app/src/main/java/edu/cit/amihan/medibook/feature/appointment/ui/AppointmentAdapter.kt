package edu.cit.amihan.medibook.feature.appointment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.cit.amihan.medibook.R
import edu.cit.amihan.medibook.databinding.ItemAppointmentBinding
import edu.cit.amihan.medibook.feature.appointment.model.AppointmentResponse

class AppointmentAdapter(
    private var appointments: List<AppointmentResponse>
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        val ctx = holder.itemView.context

        holder.binding.tvDoctorName.text = appointment.doctorName ?: ""
        holder.binding.tvSlotTime.text = "${appointment.startTime ?: ""}  →  ${appointment.endTime ?: ""}"
        holder.binding.tvStatus.text = appointment.status ?: ""

        when (appointment.status) {
            "PENDING" -> {
                holder.binding.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.status_badge_pending)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_pending_text))
            }
            "CONFIRMED" -> {
                holder.binding.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.status_badge_confirmed)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_confirmed_text))
            }
            "COMPLETED" -> {
                holder.binding.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.status_badge_completed)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_completed_text))
            }
            "CANCELLED" -> {
                holder.binding.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.status_badge_cancelled)
                holder.binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_cancelled_text))
            }
        }
    }

    override fun getItemCount(): Int = appointments.size

    fun updateData(newAppointments: List<AppointmentResponse>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
}
