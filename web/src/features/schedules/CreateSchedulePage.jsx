import { useState, useEffect } from 'react';
import { getDoctors } from '../doctors/doctorService';
import { createSchedule } from './scheduleService';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const labelClasses =
  'block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5';

const CreateSchedulePage = () => {
  const [doctors, setDoctors] = useState([]);
  const [formData, setFormData] = useState({
    doctorId: '',
    startTime: '',
    endTime: '',
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getDoctors()
      .then(setDoctors)
      .catch(() => setError('Failed to load doctors.'));
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!formData.doctorId || !formData.startTime || !formData.endTime) {
      setError('Please fill in doctor, start time, and end time.');
      return;
    }

    if (new Date(formData.endTime) <= new Date(formData.startTime)) {
      setError('End time must be after start time.');
      return;
    }

    setLoading(true);
    try {
      const created = await createSchedule({
        doctorId: Number(formData.doctorId),
        startTime: formData.startTime,
        endTime: formData.endTime,
      });
      setMessage(`Slot created for Dr. ${created.doctorName} at ${created.startTime}.`);
      setFormData({ doctorId: '', startTime: '', endTime: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create schedule.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="animate-fade-in-up max-w-lg">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Scheduling</p>
        <h1 className="dashboard-header-title">Add Doctor Schedule Slot</h1>
        <p className="dashboard-header-subtitle">Create a new available time slot for a doctor.</p>
      </div>

      <div className="dashboard-card p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className={labelClasses}>Doctor</label>
            <select name="doctorId" value={formData.doctorId} onChange={handleChange} className={inputClasses}>
              <option value="">Select a doctor</option>
              {doctors.map((doc) => (
                <option key={doc.doctorId} value={doc.doctorId}>
                  {doc.fullName} &mdash; {doc.specialization || 'General'}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className={labelClasses}>Start Time</label>
            <input type="datetime-local" name="startTime" value={formData.startTime} onChange={handleChange} className={inputClasses} />
          </div>

          <div>
            <label className={labelClasses}>End Time</label>
            <input type="datetime-local" name="endTime" value={formData.endTime} onChange={handleChange} className={inputClasses} />
          </div>

          {message && (
            <p className="text-sm text-[var(--color-panel-accent)] font-medium bg-teal-50 border border-teal-200 rounded-lg px-4 py-3">{message}</p>
          )}
          {error && (
            <p className="text-sm text-[var(--color-vital)] font-medium">{error}</p>
          )}

          <button type="submit" disabled={loading} className="btn-accent w-full">
            {loading ? 'Creating...' : 'Create Slot'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateSchedulePage;
