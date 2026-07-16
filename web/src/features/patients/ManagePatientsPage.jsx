import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../auth/AuthContext';
import { getPatients, deletePatient } from './patientService';
import { inputClasses } from '../../styles/formClasses';
import ConfirmDialog from '../../components/ConfirmDialog';
import Toast from '../../components/Toast';

const ManagePatientsPage = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [patients, setPatients] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deletingPatient, setDeletingPatient] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [toast, setToast] = useState(null);
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

  const handleDelete = async () => {
    setDeleting(true);
    try {
      const name = deletingPatient.fullName;
      await deletePatient(deletingPatient.patientId);
      setPatients((prev) => prev.filter((p) => p.patientId !== deletingPatient.patientId));
      setDeletingPatient(null);
      setToast({ message: `${name} has been removed.`, type: 'success' });
    } catch (err) {
      setDeletingPatient(null);
      setToast({ message: err.response?.data?.message || 'Failed to delete patient.', type: 'error' });
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="animate-fade-in-up">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Directory</p>
        <h1 className="dashboard-header-title">Patients</h1>
        <p className="dashboard-header-subtitle">Registered patients and their contact information.</p>
      </div>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email, or contact number"
          className={inputClasses + ' max-w-xs'}
        />
        <button type="submit" className="btn-accent">Search</button>
        {search && (
          <button type="button" onClick={handleClear} className="btn-ghost">Clear</button>
        )}
      </form>

      {loading && (
        <div className="flex items-center gap-2 text-sm text-[var(--color-ink-soft)]">
          <span className="spinner" /> Loading patients...
        </div>
      )}

      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">{error}</p>
      )}

      {!loading && !error && patients.length === 0 && (
        <div className="dashboard-card p-8 text-center">
          <p className="text-sm text-[var(--color-ink-soft)]">No patients found.</p>
        </div>
      )}

      {!loading && !error && patients.length > 0 && (
        <div className="dashboard-card divide-y divide-[var(--color-border)]">
          {patients.map((p) => (
            <div key={p.patientId} className="px-5 py-4 flex justify-between items-center hover:bg-[var(--color-bg)] transition">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{p.fullName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">{p.email}</p>
              </div>
              <div className="flex items-center gap-4">
                <div className="text-right text-sm text-[var(--color-ink-soft)]">
                  <p>{p.contactNumber || '\u2014'}</p>
                  <p>{p.dateOfBirth || '\u2014'}</p>
                </div>
                {isAdmin && (
                  <button
                    onClick={() => setDeletingPatient(p)}
                    className="text-sm text-[var(--color-vital)] hover:text-[#ff5643] font-medium transition"
                  >
                    Delete
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
      {deletingPatient && (
        <ConfirmDialog
          title="Delete Patient"
          message={`Delete ${deletingPatient.fullName}? This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setDeletingPatient(null)}
          loading={deleting}
        />
      )}
      {toast && <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />}
    </div>
  );
};

export default ManagePatientsPage;
