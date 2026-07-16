import axiosInstance from '../../api/axiosInstance';

export const getStaff = async (search = '') => {
  const params = search ? { search } : {};
  const response = await axiosInstance.get('/staff', { params });
  return response.data;
};

export const getStaffById = async (staffId) => {
  const response = await axiosInstance.get(`/staff/${staffId}`);
  return response.data;
};

export const updateStaff = async (staffId, data) => {
  const response = await axiosInstance.put(`/staff/${staffId}`, data);
  return response.data;
};

export const deleteStaff = async (staffId) => {
  await axiosInstance.delete(`/staff/${staffId}`);
};
