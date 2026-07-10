import { useState, useEffect, useRef } from 'react';
import { getPatients } from './patientService';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

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
    <div className="max-w-4xl">
      <h1 className="text-xl font-semibold text-[var(--color-ink)] mb-4">Patients</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email, or contact number"
          className={inputClasses}
        />
        <button
          type="submit"
          className="bg-[var(--color-vital)] hover:bg-[#ff5643] text-white text-sm font-semibold px-4 py-2.5 rounded-lg transition shadow-sm shadow-[var(--color-vital)]/20"
        >
          Search
        </button>
        {search && (
          <button
            type="button"
            onClick={handleClear}
            className="text-sm text-[var(--color-ink-soft)] px-3 py-2.5 hover:text-[var(--color-ink)] transition"
          >
            Clear
          </button>
        )}
      </form>

      {loading && <p className="text-sm text-[var(--color-ink-soft)]">Loading patients...</p>}

      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">
          {error}
        </p>
      )}

      {!loading && !error && patients.length === 0 && (
        <p className="text-sm text-[var(--color-ink-soft)]">No patients found.</p>
      )}

      {!loading && !error && patients.length > 0 && (
        <div className="bg-white border border-[var(--color-border)] rounded-lg divide-y divide-[var(--color-border)]">
          {patients.map((p) => (
            <div key={p.patientId} className="px-5 py-4 flex justify-between items-center">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{p.fullName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">{p.email}</p>
              </div>
              <div className="text-right text-sm text-[var(--color-ink-soft)]">
                <p>{p.contactNumber || '\u2014'}</p>
                <p>{p.dateOfBirth || '\u2014'}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ManagePatientsPage;
