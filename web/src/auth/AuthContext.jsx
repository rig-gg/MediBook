import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { login as loginRequest } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On app load, restore session from localStorage if present
  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (username, password) => {
    const data = await loginRequest(username, password);
    const loggedInUser = {
      userId: data.userId,
      username: data.username,
      fullName: data.fullName,
      role: data.role,
    };

    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(loggedInUser));
    setUser(loggedInUser);

    return loggedInUser;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
};