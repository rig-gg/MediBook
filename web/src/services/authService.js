import axiosInstance from '../api/axiosInstance';

export const login = async (username, password) => {
  const response = await axiosInstance.post('/auth/login', { username, password });
  return response.data; // { token, userId, username, fullName, role }
};

export const registerAccount = async (formData) => {
  // formData: { username, password, email, fullName, role, specialization?, contactNumber? }
  const response = await axiosInstance.post('/admin/register', formData);
  return response.data; // success message string
};