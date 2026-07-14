import axiosInstance from '../api/axiosInstance';

export const login = async (username, password) => {
  const response = await axiosInstance.post('/auth/login', { username, password });
  return response.data;
};

export const logout = async (refreshToken) => {
  try {
    await axiosInstance.post('/auth/logout', { refreshToken });
  } catch {
    // Best-effort — server still blacklists the access token from the header
  }
};

export const registerAccount = async (formData) => {
  const response = await axiosInstance.post('/admin/register', formData);
  return response.data;
};
