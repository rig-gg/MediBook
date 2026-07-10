package edu.cit.amihan.medibook.feature.doctor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cit.amihan.medibook.databinding.ItemDoctorBinding
import edu.cit.amihan.medibook.feature.doctor.model.Doctor

class DoctorAdapter(
    private var doctors: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(val binding: ItemDoctorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.binding.tvDoctorName.text = doctor.fullName ?: "Unknown"
        holder.binding.tvSpecialization.text = doctor.specialization ?: "General"
        holder.binding.tvContact.text = "${doctor.contactNumber ?: ""}  •  ${doctor.email ?: ""}"
        holder.binding.root.setOnClickListener { onDoctorClick(doctor) }
    }

    override fun getItemCount(): Int = doctors.size

    fun updateData(newDoctors: List<Doctor>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }
}