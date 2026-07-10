import { useState, useEffect, useRef } from 'react';
import { getStaff, updateStaff } from './staffService';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const labelClasses =
  'block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5';

const ManageStaffPage = () => {
  const [staffList, setStaffList] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingStaff, setEditingStaff] = useState(null);
  const [form, setForm] = useState({ fullName: '', contactNumber: '', position: '' });
  const [saving, setSaving] = useState(false);
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  const fetchStaff = async (term = '') => {
    setLoading(true);
    setError('');
    try {
      const data = await getStaff(term);
      if (!mountedRef.current) return;
      setStaffList(data);
    } catch (err) {
      if (!mountedRef.current) return;
      setError(err.response?.data?.message || 'Failed to load staff.');
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  };

  useEffect(() => { fetchStaff(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchStaff(search.trim());
  };

  const openEdit = (staff) => {
    setEditingStaff(staff);
    setForm({
      fullName: staff.fullName || '',
      contactNumber: staff.contactNumber || '',
      position: staff.position || '',
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateStaff(editingStaff.staffId, form);
      setEditingStaff(null);
      await fetchStaff(search.trim());
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update staff.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-4xl">
      <h1 className="text-xl font-semibold text-[var(--color-ink)] mb-4">Manage Staff</h1>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email, position, or contact number"
          className={inputClasses}
        />
        <button type="submit" className="bg-[var(--color-vital)] hover:bg-[#ff5643] text-white text-sm font-semibold px-4 py-2.5 rounded-lg transition shadow-sm shadow-[var(--color-vital)]/20">
          Search
        </button>
        {search && (
          <button type="button" onClick={() => { setSearch(''); fetchStaff(''); }}
            className="text-sm text-[var(--color-ink-soft)] px-3 py-2.5 hover:text-[var(--color-ink)] transition">
            Clear
          </button>
        )}
      </form>

      {loading && <p className="text-sm text-[var(--color-ink-soft)]">Loading staff...</p>}

      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">{error}</p>
      )}

      {!loading && !error && staffList.length === 0 && (
        <p className="text-sm text-[var(--color-ink-soft)]">No staff found.</p>
      )}

      {!loading && !error && staffList.length > 0 && (
        <div className="bg-white border border-[var(--color-border)] rounded-lg divide-y divide-[var(--color-border)]">
          {staffList.map((s) => (
            <div key={s.staffId} className="px-5 py-4 flex justify-between items-center">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{s.fullName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">{s.position || '\u2014'}</p>
              </div>
              <div className="flex items-center gap-4 text-sm text-[var(--color-ink-soft)]">
                <div className="text-right">
                  <p>{s.contactNumber}</p>
                  <p>{s.email}</p>
                </div>
                <button
                  onClick={() => openEdit(s)}
                  className="text-[var(--color-panel-accent)] hover:text-[var(--color-panel)] font-medium transition"
                >
                  Edit
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {editingStaff && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md mx-4 shadow-xl">
            <h2 className="text-lg font-semibold text-[var(--color-ink)] mb-4">Edit Staff</h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className={labelClasses}>Full Name</label>
                <input
                  type="text" required
                  value={form.fullName}
                  onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                  className={inputClasses}
                />
              </div>
              <div>
                <label className={labelClasses}>Position</label>
                <input
                  type="text"
                  value={form.position}
                  onChange={(e) => setForm({ ...form, position: e.target.value })}
                  className={inputClasses}
                />
              </div>
              <div>
                <label className={labelClasses}>Contact Number</label>
                <input
                  type="text"
                  value={form.contactNumber}
                  onChange={(e) => setForm({ ...form, contactNumber: e.target.value })}
                  className={inputClasses}
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setEditingStaff(null)}
                  className="text-sm text-[var(--color-ink-soft)] px-4 py-2 rounded-lg hover:bg-[var(--color-border)] transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="bg-[var(--color-panel-accent)] hover:bg-[var(--color-panel)] disabled:opacity-40 text-white text-sm font-semibold px-4 py-2 rounded-lg transition"
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

export default ManageStaffPage;
