package edu.cit.amihan.medibook.ui.appointment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cit.amihan.medibook.databinding.ItemAppointmentBinding
import edu.cit.amihan.medibook.model.AppointmentResponse

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
        holder.binding.tvDoctorName.text = "Dr. ${appointment.doctorName}"
        holder.binding.tvSlotTime.text = "${appointment.startTime}  →  ${appointment.endTime}"
        holder.binding.tvStatus.text = appointment.status
    }

    override fun getItemCount(): Int = appointments.size

    fun updateData(newAppointments: List<AppointmentResponse>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }
}