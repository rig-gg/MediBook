import { useState, useEffect, useRef } from 'react';
import { getPatients } from './patientService';

const ManagePatientsPage = () => {
  const [patients, setPatients] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  const fetchPatients = async (searchTerm = '') => {
    setLoading(true);
    setError('');
    try {
      const data = await getPatients(searchTerm);
      if (!mountedRef.current) return;
      setPatients(data);
    } catch (err) {
      if (!mountedRef.current) return;
      setError(
        err.response?.data?.message || 'Failed to load patients. Please try again.'
      );
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  };

  useEffect(() => {
    fetchPatients();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchPatients(search.trim());
  };

  const handleClear = () => {
    setSearch('');
    fetchPatients('');
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-xl font-semibold text-slate-800 mb-4">Patients</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email, or contact number"
          className="flex-1 border border-slate-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400"
        />
        <button
          type="submit"
          className="bg-slate-800 text-white text-sm px-4 py-2 rounded-md hover:bg-slate-700"
        >
          Search
        </button>
        {search && (
          <button
            type="button"
            onClick={handleClear}
            className="text-sm text-slate-500 px-3 py-2 hover:text-slate-700"
          >
            Clear
          </button>
        )}
      </form>

      {loading && <p className="text-sm text-slate-500">Loading patients...</p>}

      {!loading && error && (
        <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-md px-4 py-3">
          {error}
        </p>
      )}

      {!loading && !error && patients.length === 0 && (
        <p className="text-sm text-slate-500">No patients found.</p>
      )}

      {!loading && !error && patients.length > 0 && (
        <div className="bg-white border border-slate-200 rounded-md divide-y divide-slate-100">
          {patients.map((p) => (
            <div key={p.patientId} className="px-4 py-3 flex justify-between items-center">
              <div>
                <p className="font-medium text-slate-800">{p.fullName}</p>
                <p className="text-sm text-slate-500">{p.email}</p>
              </div>
              <div className="text-right text-sm text-slate-500">
                <p>{p.contactNumber || '—'}</p>
                <p>{p.dateOfBirth || '—'}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ManagePatientsPage;
