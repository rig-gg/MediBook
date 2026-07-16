import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getSchedules, updateSchedule } from './scheduleService';
import { getDoctors } from '../doctors/doctorService';
import { inputClasses, labelClasses } from '../../styles/formClasses';

const ManageSchedulesPage = () => {
  const [schedules, setSchedules] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [editing, setEditing] = useState(null);
  const [formData, setFormData] = useState({ doctorId: '', startTime: '', endTime: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const loadSchedules = () => {
    getSchedules()
      .then(setSchedules)
      .catch(() => setError('Failed to load schedules.'));
  };

  useEffect(() => {
    loadSchedules();
    getDoctors()
      .then(setDoctors)
      .catch(() => {});
  }, []);

  const startEdit = (schedule) => {
    setEditing(schedule);
    setFormData({
      doctorId: schedule.doctorId || '',
      startTime: schedule.startTime ? schedule.startTime.slice(0, 16) : '',
      endTime: schedule.endTime ? schedule.endTime.slice(0, 16) : '',
    });
    setError('');
    setMessage('');
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.startTime || !formData.endTime) {
      setError('Start time and end time are required.');
      return;
    }
    if (new Date(formData.endTime) <= new Date(formData.startTime)) {
      setError('End time must be after start time.');
      return;
    }

    setLoading(true);
    try {
      await updateSchedule(editing.scheduleId, {
        doctorId: Number(formData.doctorId),
        startTime: formData.startTime,
        endTime: formData.endTime,
      });
      setMessage('Schedule updated successfully.');
      setEditing(null);
      loadSchedules();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update schedule.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="animate-fade-in-up">
      <div className="dashboard-header">
        <div className="flex items-start justify-between">
          <div>
            <p className="dashboard-header-eyebrow">Scheduling</p>
            <h1 className="dashboard-header-title">Manage Schedules</h1>
            <p className="dashboard-header-subtitle">View and edit doctor availability slots.</p>
          </div>
          <Link to="/schedules/new" className="btn-accent whitespace-nowrap">
            + Add Schedule
          </Link>
        </div>
      </div>

      {message && (
        <p className="text-sm text-[var(--color-panel-accent)] font-medium bg-teal-50 border border-teal-200 rounded-lg px-4 py-3 mb-4">{message}</p>
      )}
      {error && !editing && (
        <p className="text-sm text-[var(--color-vital)] font-medium mb-4">{error}</p>
      )}

      {editing && (
        <div className="dashboard-card p-6 mb-6">
          <h2 className="text-lg font-semibold mb-4">Edit Schedule Slot</h2>
          <form onSubmit={handleSave} className="space-y-4">
            <div>
              <label className={labelClasses}>Start Time</label>
              <input
                type="datetime-local"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                className={inputClasses}
              />
            </div>
            <div>
              <label className={labelClasses}>End Time</label>
              <input
                type="datetime-local"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                className={inputClasses}
              />
            </div>
            {error && (
              <p className="text-sm text-[var(--color-vital)] font-medium">{error}</p>
            )}
            <div className="flex gap-3">
              <button type="submit" disabled={loading} className="btn-accent">
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
              <button type="button" onClick={() => setEditing(null)} className="btn-outline">
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="dashboard-card overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-[var(--color-border)]">
              <th className="text-left px-4 py-3 font-semibold text-[var(--color-label)]">Doctor</th>
              <th className="text-left px-4 py-3 font-semibold text-[var(--color-label)]">Start Time</th>
              <th className="text-left px-4 py-3 font-semibold text-[var(--color-label)]">End Time</th>
              <th className="text-left px-4 py-3 font-semibold text-[var(--color-label)]">Status</th>
              <th className="text-right px-4 py-3 font-semibold text-[var(--color-label)]">Action</th>
            </tr>
          </thead>
          <tbody>
            {schedules.map((s) => (
              <tr key={s.scheduleId} className="border-b border-[var(--color-border)] last:border-0">
                <td className="px-4 py-3">{s.doctorName || `Doctor #${s.doctorId}`}</td>
                <td className="px-4 py-3">{s.startTime ? new Date(s.startTime).toLocaleString() : '—'}</td>
                <td className="px-4 py-3">{s.endTime ? new Date(s.endTime).toLocaleString() : '—'}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs font-semibold px-2 py-1 rounded-full ${s.isAvailable ? 'bg-teal-100 text-teal-700' : 'bg-gray-100 text-gray-500'}`}>
                    {s.isAvailable ? 'Available' : 'Booked'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <button onClick={() => startEdit(s)} className="text-xs font-semibold text-[var(--color-panel-accent)] hover:underline">
                    Edit
                  </button>
                </td>
              </tr>
            ))}
            {schedules.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-[var(--color-label)]">No schedules found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ManageSchedulesPage;
