import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (stored) setUser(JSON.parse(stored));
    setLoading(false);
  }, []);

  const loginUser = (userData, token) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    window.location.href = '/login';
  };

  const isAdmin = () => user?.role === 'ROLE_ADMIN';
  const isDoctor = () => user?.role === 'ROLE_DOCTOR';
  const isPatient = () => user?.role === 'ROLE_PATIENT';

  return (
    <AuthContext.Provider value={{ user, loading, loginUser, logout, isAdmin, isDoctor, isPatient }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
