import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../auth/AuthContext';
import { getDoctors, updateDoctor, deleteDoctor } from './doctorService';
import { inputClasses, labelClasses } from '../../styles/formClasses';
import ConfirmDialog from '../../components/ConfirmDialog';
import Toast from '../../components/Toast';

const DoctorListPage = () => {
  const { user } = useAuth();
  const canManage = user?.role === 'ADMIN' || user?.role === 'STAFF';
  const isAdmin = user?.role === 'ADMIN';

  const [doctors, setDoctors] = useState([]);
  const [specialization, setSpecialization] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const mountedRef = useRef(true);

  const [editingDoctor, setEditingDoctor] = useState(null);
  const [form, setForm] = useState({ fullName: '', specialization: '', contactNumber: '' });
  const [saving, setSaving] = useState(false);
  const [deletingDoctor, setDeletingDoctor] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [toast, setToast] = useState(null);

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
      setError(err.response?.data?.message || 'Failed to load doctors.');
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  };

  useEffect(() => { fetchDoctors(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchDoctors(specialization.trim());
  };

  const handleClear = () => {
    setSpecialization('');
    fetchDoctors('');
  };

  const openEdit = (doctor) => {
    setEditingDoctor(doctor);
    setForm({
      fullName: doctor.fullName || '',
      specialization: doctor.specialization || '',
      contactNumber: doctor.contactNumber || '',
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateDoctor(editingDoctor.doctorId, form);
      setEditingDoctor(null);
      await fetchDoctors(specialization.trim());
      setToast({ message: `${form.fullName} updated.`, type: 'success' });
    } catch (err) {
      setToast({ message: err.response?.data?.message || 'Failed to update doctor.', type: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      const name = deletingDoctor.fullName;
      await deleteDoctor(deletingDoctor.doctorId);
      setDoctors((prev) => prev.filter((d) => d.doctorId !== deletingDoctor.doctorId));
      setDeletingDoctor(null);
      setToast({ message: `${name} has been removed.`, type: 'success' });
    } catch (err) {
      setDeletingDoctor(null);
      setToast({ message: err.response?.data?.message || 'Failed to delete doctor.', type: 'error' });
    } finally {
      setDeleting(false);
    }
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
              <div className="flex items-center gap-4">
                <div className="text-right text-sm text-[var(--color-ink-soft)]">
                  <p>{doc.contactNumber}</p>
                  <p>{doc.email}</p>
                </div>
                {canManage && (
                  <button
                    onClick={() => openEdit(doc)}
                    className="text-sm text-[var(--color-panel-accent)] hover:text-[var(--color-panel)] font-medium transition"
                  >
                    Edit
                  </button>
                )}
                {isAdmin && (
                  <button
                    onClick={() => setDeletingDoctor(doc)}
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

      {editingDoctor && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md mx-4 shadow-xl animate-fade-in-up">
            <h2 className="text-lg font-semibold text-[var(--color-ink)] mb-4">Edit Doctor</h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className={labelClasses}>Full Name</label>
                <input type="text" required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} className={inputClasses} />
              </div>
              <div>
                <label className={labelClasses}>Specialization</label>
                <input type="text" value={form.specialization} onChange={(e) => setForm({ ...form, specialization: e.target.value })} className={inputClasses} />
              </div>
              <div>
                <label className={labelClasses}>Contact Number</label>
                <input type="text" value={form.contactNumber} onChange={(e) => setForm({ ...form, contactNumber: e.target.value })} className={inputClasses} />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setEditingDoctor(null)} className="btn-ghost">Cancel</button>
                <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving...' : 'Save'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
      {deletingDoctor && (
        <ConfirmDialog
          title="Delete Doctor"
          message={`Delete Dr. ${deletingDoctor.fullName}? This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setDeletingDoctor(null)}
          loading={deleting}
        />
      )}
      {toast && <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />}
    </div>
  );
};

export default DoctorListPage;
