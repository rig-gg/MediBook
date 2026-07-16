import axiosInstance from '../../api/axiosInstance';

// Fetch all doctors, optionally filtered by specialization
export const getDoctors = async (specialization = '') => {
  const params = specialization ? { specialization } : {};
  const response = await axiosInstance.get('/doctors', { params });
  return response.data;
};

export const getDoctorById = async (doctorId) => {
  const response = await axiosInstance.get(`/doctors/${doctorId}`);
  return response.data;
};

export const updateDoctor = async (doctorId, data) => {
  const response = await axiosInstance.put(`/doctors/${doctorId}`, data);
  return response.data;
};

export const getDoctorByUserId = async (userId) => {
  const response = await axiosInstance.get(`/doctors/user/${userId}`);
  return response.data;
};

export const deleteDoctor = async (doctorId) => {
  await axiosInstance.delete(`/doctors/${doctorId}`);
};