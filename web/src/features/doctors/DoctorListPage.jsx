import { useState, useEffect, useRef } from 'react';
import { getDoctors } from './doctorService';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

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
    <div className="max-w-4xl">
      <h1 className="text-xl font-semibold text-[var(--color-ink)] mb-4">Doctors</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={specialization}
          onChange={(e) => setSpecialization(e.target.value)}
          placeholder="Filter by specialization (e.g. Cardiology)"
          className={inputClasses}
        />
        <button
          type="submit"
          className="bg-[var(--color-vital)] hover:bg-[#ff5643] text-white text-sm font-semibold px-4 py-2.5 rounded-lg transition shadow-sm shadow-[var(--color-vital)]/20"
        >
          Search
        </button>
        {specialization && (
          <button
            type="button"
            onClick={handleClear}
            className="text-sm text-[var(--color-ink-soft)] px-3 py-2.5 hover:text-[var(--color-ink)] transition"
          >
            Clear
          </button>
        )}
      </form>

      {loading && <p className="text-sm text-[var(--color-ink-soft)]">Loading doctors...</p>}

      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">
          {error}
        </p>
      )}

      {!loading && !error && doctors.length === 0 && (
        <p className="text-sm text-[var(--color-ink-soft)]">No doctors found.</p>
      )}

      {!loading && !error && doctors.length > 0 && (
        <div className="bg-white border border-[var(--color-border)] rounded-lg divide-y divide-[var(--color-border)]">
          {doctors.map((doc) => (
            <div key={doc.doctorId} className="px-5 py-4 flex justify-between items-center">
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
