import axiosInstance from '../../api/axiosInstance';

export const createRecord = async ({ appointmentId, diagnosis, consultationNotes }) => {
  const response = await axiosInstance.post('/records/create', {
    appointmentId,
    diagnosis,
    consultationNotes,
  });
  return response.data;
};

export const getRecordsByPatient = async (patientId) => {
  const response = await axiosInstance.get(`/records/patient/${patientId}`);
  return response.data;
};