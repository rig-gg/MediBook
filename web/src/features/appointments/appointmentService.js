import axiosInstance from '../../api/axiosInstance';

export const getAllAppointments = async (status = null) => {
  const params = status ? { status } : {};
  const response = await axiosInstance.get('/appointments', { params });
  return response.data; // [{ appointmentId, patientId, patientName, doctorId, doctorName, startTime, endTime, status, createdAt }]
};

export const updateAppointmentStatus = async (appointmentId, status) => {
  const response = await axiosInstance.patch(
    `/appointments/${appointmentId}/status`,
    null,
    { params: { status } }
  );
  return response.data;
};