import axiosInstance from '../../api/axiosInstance';

export const getAllAppointments = async (status = null) => {
  const params = status ? { status } : {};
  const response = await axiosInstance.get('/appointments', { params });
  return response.data;
};

export const updateAppointmentStatus = async (appointmentId, status) => {
  const response = await axiosInstance.patch(
    `/appointments/${appointmentId}/status`,
    null,
    { params: { status } }
  );
  return response.data;
};

export const deleteAppointment = async (appointmentId) => {
  await axiosInstance.delete(`/appointments/${appointmentId}`);
};