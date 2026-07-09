import { useState, useEffect, useRef } from 'react';
import { getDoctors } from './doctorService';

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
    <div className="max-w-4xl mx-auto">
      <h1 className="text-xl font-semibold text-slate-800 mb-4">Doctors</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={specialization}
          onChange={(e) => setSpecialization(e.target.value)}
          placeholder="Filter by specialization (e.g. Cardiology)"
          className="flex-1 border border-slate-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400"
        />
        <button
          type="submit"
          className="bg-slate-800 text-white text-sm px-4 py-2 rounded-md hover:bg-slate-700"
        >
          Search
        </button>
        {specialization && (
          <button
            type="button"
            onClick={handleClear}
            className="text-sm text-slate-500 px-3 py-2 hover:text-slate-700"
          >
            Clear
          </button>
        )}
      </form>

      {loading && <p className="text-sm text-slate-500">Loading doctors...</p>}

      {!loading && error && (
        <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-md px-4 py-3">
          {error}
        </p>
      )}

      {!loading && !error && doctors.length === 0 && (
        <p className="text-sm text-slate-500">No doctors found.</p>
      )}

      {!loading && !error && doctors.length > 0 && (
        <div className="bg-white border border-slate-200 rounded-md divide-y divide-slate-100">
          {doctors.map((doc) => (
            <div key={doc.doctorId} className="px-4 py-3 flex justify-between items-center">
              <div>
                <p className="font-medium text-slate-800">{doc.fullName}</p>
                <p className="text-sm text-slate-500">{doc.specialization || 'General'}</p>
              </div>
              <div className="text-right text-sm text-slate-500">
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