import { useState, useEffect } from 'react';
import { getAllAppointments } from '../appointments/appointmentService';
import { createRecord } from './recordService';

const DoctorRecordsPage = () => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [recordFor, setRecordFor] = useState(null);
  const [diagnosis, setDiagnosis] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [recordError, setRecordError] = useState('');
  const [message, setMessage] = useState('');

  const fetchConfirmed = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getAllAppointments('CONFIRMED');
      setAppointments(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load appointments.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchConfirmed();
  }, []);

  const openRecord = (appt) => {
    setRecordFor(appt);
    setDiagnosis('');
    setNotes('');
    setRecordError('');
  };

  const submitRecord = async (e) => {
    e.preventDefault();
    setRecordError('');
    if (!diagnosis.trim()) {
      setRecordError('Diagnosis is required.');
      return;
    }
    setSaving(true);
    try {
      await createRecord({
        appointmentId: recordFor.appointmentId,
        diagnosis,
        consultationNotes: notes,
      });
      setMessage(`Record saved for ${recordFor.patientName}. Appointment marked completed.`);
      setRecordFor(null);
      fetchConfirmed();
    } catch (err) {
      setRecordError(err.response?.data?.message || 'Failed to save record.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-4xl">
      <h1 className="text-xl font-semibold text-[var(--color-ink)] mb-4">My Consultations</h1>

      {message && <p className="text-sm text-emerald-600 font-medium mb-3">{message}</p>}
      {loading && <p className="text-sm text-[var(--color-ink-soft)]">Loading...</p>}
      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">{error}</p>
      )}
      {!loading && !error && appointments.length === 0 && (
        <p className="text-sm text-[var(--color-ink-soft)]">No confirmed appointments awaiting a record.</p>
      )}

      {!loading && !error && appointments.length > 0 && (
        <div className="bg-white border border-[var(--color-border)] rounded-lg divide-y divide-[var(--color-border)]">
          {appointments.map((appt) => (
            <div key={appt.appointmentId} className="px-5 py-4 flex items-center justify-between">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{appt.patientName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">
                  {new Date(appt.startTime).toLocaleString()}
                </p>
              </div>
              <button
                onClick={() => openRecord(appt)}
                className="text-sm text-[var(--color-panel-accent)] hover:text-[var(--color-panel)] font-medium transition"
              >
                Write Record & Complete
              </button>
            </div>
          ))}
        </div>
      )}

      {recordFor && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl w-full max-w-md p-5 shadow-xl">
            <h2 className="text-lg font-semibold text-[var(--color-ink)] mb-1">Consultation Record</h2>
            <p className="text-sm text-[var(--color-ink-soft)] mb-4">
              {recordFor.patientName} — {new Date(recordFor.startTime).toLocaleString()}
            </p>
            <form onSubmit={submitRecord} className="space-y-3">
              <div>
                <label className="block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5">
                  Diagnosis
                </label>
                <input
                  value={diagnosis}
                  onChange={(e) => setDiagnosis(e.target.value)}
                  className="w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition"
                  placeholder="e.g. Hypertension, Stage 1"
                />
              </div>
              <div>
                <label className="block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5">
                  Consultation Notes
                </label>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  rows={4}
                  className="w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition"
                  placeholder="Observations, advice, follow-up..."
                />
              </div>
              {recordError && <p className="text-sm text-[var(--color-vital)] font-medium">{recordError}</p>}
              <div className="flex gap-2 pt-1">
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 bg-[var(--color-panel-accent)] hover:bg-[var(--color-panel)] disabled:opacity-40 text-white text-sm font-semibold py-2.5 rounded-lg transition"
                >
                  {saving ? 'Saving\u2026' : 'Save & Complete'}
                </button>
                <button
                  type="button"
                  onClick={() => setRecordFor(null)}
                  className="px-4 text-sm text-[var(--color-ink-soft)] hover:text-[var(--color-ink)] font-medium transition"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorRecordsPage;
