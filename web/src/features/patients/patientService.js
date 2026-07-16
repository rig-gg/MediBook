import axiosInstance from '../../api/axiosInstance';

export const getPatients = async (search = '') => {
  const params = search ? { search } : {};
  const response = await axiosInstance.get('/patients', { params });
  return response.data;
};

export const getPatientById = async (patientId) => {
  const response = await axiosInstance.get(`/patients/${patientId}`);
  return response.data;
};

export const deletePatient = async (patientId) => {
  await axiosInstance.delete(`/patients/${patientId}`);
};
