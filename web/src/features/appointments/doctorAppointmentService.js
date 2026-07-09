import axiosInstance from '../../api/axiosInstance';

export const getDoctorAppointments = async (doctorId, status = null) => {
  const params = status ? { status } : {};
  const response = await axiosInstance.get(`/appointments/doctor/${doctorId}`, { params });
  return response.data;
};
