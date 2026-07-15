import axiosInstance from '../../api/axiosInstance';

export const createSchedule = async ({ doctorId, startTime, endTime }) => {
  const response = await axiosInstance.post('/schedules', { doctorId, startTime, endTime });
  return response.data;
};

export const getSchedules = async (doctorId = null) => {
  const params = doctorId ? { doctorId } : {};
  const response = await axiosInstance.get('/schedules', { params });
  return response.data;
};

export const updateSchedule = async (scheduleId, { doctorId, startTime, endTime }) => {
  const response = await axiosInstance.put(`/schedules/${scheduleId}`, { doctorId, startTime, endTime });
  return response.data;
};