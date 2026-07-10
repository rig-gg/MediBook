import { useState, useEffect, useRef } from 'react';
import { getDoctors, updateDoctor } from './doctorService';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const labelClasses =
  'block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5';

const ManageDoctorsPage = () => {
  const [doctors, setDoctors] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingDoctor, setEditingDoctor] = useState(null);
  const [form, setForm] = useState({ fullName: '', specialization: '', contactNumber: '' });
  const [saving, setSaving] = useState(false);
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  const fetchDoctors = async (term = '') => {
    setLoading(true);
    setError('');
    try {
      const data = await getDoctors(term);
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
    fetchDoctors(search.trim());
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
      await fetchDoctors(search.trim());
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update doctor.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="animate-fade-in-up">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Administration</p>
        <h1 className="dashboard-header-title">Manage Doctors</h1>
        <p className="dashboard-header-subtitle">View and update doctor profiles.</p>
      </div>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Filter by specialization"
          className={inputClasses + ' max-w-xs'}
        />
        <button type="submit" className="btn-accent">Search</button>
        {search && (
          <button type="button" onClick={() => { setSearch(''); fetchDoctors(''); }} className="btn-ghost">Clear</button>
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
              <div className="flex items-center gap-4 text-sm text-[var(--color-ink-soft)]">
                <div className="text-right">
                  <p>{doc.contactNumber}</p>
                  <p>{doc.email}</p>
                </div>
                <button
                  onClick={() => openEdit(doc)}
                  className="text-[var(--color-panel-accent)] hover:text-[var(--color-panel)] font-medium transition"
                >
                  Edit
                </button>
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
    </div>
  );
};

export default ManageDoctorsPage;
