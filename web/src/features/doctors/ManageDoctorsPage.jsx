import { useState, useEffect, useRef } from 'react';
import { getDoctors, updateDoctor } from './doctorService';

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
    <div className="max-w-4xl mx-auto">
      <h1 className="text-xl font-semibold text-slate-800 mb-4">Manage Doctors</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Filter by specialization"
          className="flex-1 border border-slate-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400"
        />
        <button type="submit" className="bg-slate-800 text-white text-sm px-4 py-2 rounded-md hover:bg-slate-700">
          Search
        </button>
        {search && (
          <button type="button" onClick={() => { setSearch(''); fetchDoctors(''); }}
            className="text-sm text-slate-500 px-3 py-2 hover:text-slate-700">
            Clear
          </button>
        )}
      </form>

      {loading && <p className="text-sm text-slate-500">Loading doctors...</p>}

      {!loading && error && (
        <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-md px-4 py-3">{error}</p>
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
              <div className="flex items-center gap-4 text-sm text-slate-500">
                <div className="text-right">
                  <p>{doc.contactNumber}</p>
                  <p>{doc.email}</p>
                </div>
                <button
                  onClick={() => openEdit(doc)}
                  className="text-blue-600 hover:text-blue-700 font-medium"
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
          <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
            <h2 className="text-lg font-semibold text-slate-800 mb-4">Edit Doctor</h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Full Name</label>
                <input
                  type="text" required
                  value={form.fullName}
                  onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                  className="w-full border border-slate-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Specialization</label>
                <input
                  type="text"
                  value={form.specialization}
                  onChange={(e) => setForm({ ...form, specialization: e.target.value })}
                  className="w-full border border-slate-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Contact Number</label>
                <input
                  type="text"
                  value={form.contactNumber}
                  onChange={(e) => setForm({ ...form, contactNumber: e.target.value })}
                  className="w-full border border-slate-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-slate-400"
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setEditingDoctor(null)}
                  className="text-sm text-slate-600 px-4 py-2 rounded-md hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="bg-slate-800 text-white text-sm px-4 py-2 rounded-md hover:bg-slate-700 disabled:opacity-50"
                >
                  {saving ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManageDoctorsPage;
