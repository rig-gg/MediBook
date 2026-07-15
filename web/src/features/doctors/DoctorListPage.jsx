import { useState, useEffect, useRef } from 'react';
import { getDoctors } from './doctorService';
import { inputClasses } from '../../styles/formClasses';

const DoctorListPage = () => {
  const [doctors, setDoctors] = useState([]);
  const [specialization, setSpecialization] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  const fetchDoctors = async (searchTerm = '') => {
    setLoading(true);
    setError('');
    try {
      const data = await getDoctors(searchTerm);
      if (!mountedRef.current) return;
      setDoctors(data);
    } catch (err) {
      if (!mountedRef.current) return;
      setError(
        err.response?.data?.message || 'Failed to load doctors. Please try again.'
      );
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  };

  useEffect(() => {
    fetchDoctors();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchDoctors(specialization.trim());
  };

  const handleClear = () => {
    setSpecialization('');
    fetchDoctors('');
  };

  return (
    <div className="animate-fade-in-up">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Directory</p>
        <h1 className="dashboard-header-title">Doctors</h1>
        <p className="dashboard-header-subtitle">Browse all registered healthcare professionals.</p>
      </div>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={specialization}
          onChange={(e) => setSpecialization(e.target.value)}
          placeholder="Filter by specialization (e.g. Cardiology)"
          className={inputClasses + ' max-w-xs'}
        />
        <button type="submit" className="btn-accent">Search</button>
        {specialization && (
          <button type="button" onClick={handleClear} className="btn-ghost">Clear</button>
        )}
      </form>

      {loading && (
        <div className="flex items-center gap-2 text-sm text-[var(--color-ink-soft)]">
          <span className="spinner" /> Loading doctors...
        </div>
      )}

      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">{error}</p>
      )}

      {!loading && !error && doctors.length === 0 && (
        <div className="dashboard-card p-8 text-center">
          <p className="text-sm text-[var(--color-ink-soft)]">No doctors found.</p>
        </div>
      )}

      {!loading && !error && doctors.length > 0 && (
        <div className="dashboard-card divide-y divide-[var(--color-border)]">
          {doctors.map((doc) => (
            <div key={doc.doctorId} className="px-5 py-4 flex justify-between items-center hover:bg-[var(--color-bg)] transition">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{doc.fullName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">{doc.specialization || 'General'}</p>
              </div>
              <div className="text-right text-sm text-[var(--color-ink-soft)]">
                <p>{doc.contactNumber}</p>
                <p>{doc.email}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default DoctorListPage;
