import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../auth/AuthContext';
import { getDoctorByUserId } from '../doctors/doctorService';
import { getDoctorAppointments } from '../appointments/doctorAppointmentService';
import { createRecord } from './recordService';
import { inputClasses, labelClasses } from '../../styles/formClasses';

const DoctorRecordsPage = () => {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [doctorId, setDoctorId] = useState(null);

  const [recordFor, setRecordFor] = useState(null);
  const [diagnosis, setDiagnosis] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [recordError, setRecordError] = useState('');
  const [message, setMessage] = useState('');
  const [fdaSuggestions, setFdaSuggestions] = useState([]);
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  useEffect(() => {
    const loadDoctor = async () => {
      try {
        const doc = await getDoctorByUserId(user.userId);
        if (mountedRef.current) setDoctorId(doc.doctorId);
      } catch (err) {
        if (mountedRef.current) {
          setError('Failed to load doctor profile.');
          setLoading(false);
        }
      }
    };
    loadDoctor();
  }, [user.userId]);

  const fetchConfirmed = async () => {
    if (!doctorId) return;
    setLoading(true);
    setError('');
    try {
      const data = await getDoctorAppointments(doctorId, 'CONFIRMED');
      if (mountedRef.current) setAppointments(data);
    } catch (err) {
      if (mountedRef.current) setError(err.response?.data?.message || 'Failed to load appointments.');
    } finally {
      if (mountedRef.current) setLoading(false);
    }
  };

  useEffect(() => {
    if (doctorId) fetchConfirmed();
  }, [doctorId]);

  const openRecord = (appt) => {
    setRecordFor(appt);
    setDiagnosis('');
    setNotes('');
    setRecordError('');
    setFdaSuggestions([]);
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
      const result = await createRecord({
        appointmentId: recordFor.appointmentId,
        diagnosis,
        consultationNotes: notes,
      });
      setMessage(`Record saved for ${recordFor.patientName}. Appointment marked completed.`);
      setFdaSuggestions(result.fdaSuggestions || []);
      setRecordFor(null);
      if (mountedRef.current) fetchConfirmed();
    } catch (err) {
      setRecordError(err.response?.data?.message || 'Failed to save record.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="animate-fade-in-up">
      <div className="dashboard-header">
        <p className="dashboard-header-eyebrow">Doctor Workspace</p>
        <h1 className="dashboard-header-title">My Consultations</h1>
        <p className="dashboard-header-subtitle">Write consultation records for confirmed appointments.</p>
      </div>

      {message && <p className="text-sm text-emerald-600 font-medium mb-3 bg-emerald-50 border border-emerald-200 rounded-lg px-4 py-3">{message}</p>}

      {fdaSuggestions.length > 0 && (
        <div className="dashboard-card p-5 mb-4 animate-fade-in-up">
          <div className="flex items-center gap-2 mb-3">
            <span className="font-mono text-[11px] uppercase tracking-wide text-[var(--color-panel-accent)]">
              FR-010 &middot; OpenFDA Drug Suggestions
            </span>
          </div>
          <p className="text-sm text-[var(--color-ink-soft)] mb-3">
            Based on the diagnosis, the following drug classifications were retrieved from the OpenFDA database:
          </p>
          <div className="space-y-2">
            {fdaSuggestions.map((drug, idx) => (
              <div key={idx} className="flex items-start gap-3 p-3 rounded-lg bg-[var(--color-bg)] border border-[var(--color-border)]">
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-sm text-[var(--color-ink)]">
                    {drug.brandName || drug.genericName || 'Unknown Drug'}
                  </p>
                  {drug.brandName && drug.genericName && (
                    <p className="text-xs text-[var(--color-ink-soft)] mt-0.5">
                      Generic: {drug.genericName}
                    </p>
                  )}
                  {drug.route && (
                    <p className="text-xs text-[var(--color-ink-soft)]">
                      Route: {drug.route}
                    </p>
                  )}
                  {drug.indication && (
                    <p className="text-xs text-[var(--color-ink-soft)] mt-1 line-clamp-2">
                      {drug.indication}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
          <button
            onClick={() => setFdaSuggestions([])}
            className="mt-3 text-xs text-[var(--color-ink-soft)] hover:text-[var(--color-ink)] transition"
          >
            Dismiss
          </button>
        </div>
      )}

      {loading && (
        <div className="flex items-center gap-2 text-sm text-[var(--color-ink-soft)]">
          <span className="spinner" /> Loading...
        </div>
      )}
      {!loading && error && (
        <p className="text-sm text-[var(--color-vital)] bg-red-50 border border-red-200 rounded-lg px-4 py-3">{error}</p>
      )}
      {!loading && !error && appointments.length === 0 && (
        <div className="dashboard-card p-8 text-center">
          <p className="text-sm text-[var(--color-ink-soft)]">No confirmed appointments awaiting a record.</p>
        </div>
      )}

      {!loading && !error && appointments.length > 0 && (
        <div className="dashboard-card divide-y divide-[var(--color-border)]">
          {appointments.map((appt) => (
            <div key={appt.appointmentId} className="px-5 py-4 flex items-center justify-between hover:bg-[var(--color-bg)] transition">
              <div>
                <p className="font-medium text-[var(--color-ink)]">{appt.patientName}</p>
                <p className="text-sm text-[var(--color-ink-soft)]">
                  {new Date(appt.startTime).toLocaleString()}
                </p>
              </div>
              <button
                onClick={() => openRecord(appt)}
                className="text-sm bg-[var(--color-panel-accent)] hover:bg-[var(--color-panel)] text-white font-semibold px-4 py-2 rounded-lg transition"
              >
                Write Record &amp; Complete
              </button>
            </div>
          ))}
        </div>
      )}

      {recordFor && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl w-full max-w-md p-5 shadow-xl animate-fade-in-up">
            <h2 className="text-lg font-semibold text-[var(--color-ink)] mb-1">Consultation Record</h2>
            <p className="text-sm text-[var(--color-ink-soft)] mb-4">
              {recordFor.patientName} &mdash; {new Date(recordFor.startTime).toLocaleString()}
            </p>
            <form onSubmit={submitRecord} className="space-y-3">
              <div>
                <label className={labelClasses}>Diagnosis</label>
                <input value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)} className={inputClasses} placeholder="e.g. Hypertension, Stage 1" />
              </div>
              <div>
                <label className={labelClasses}>Consultation Notes</label>
                <textarea value={notes} onChange={(e) => setNotes(e.target.value)} rows={4} className={inputClasses} placeholder="Observations, advice, follow-up..." />
              </div>
              {recordError && <p className="text-sm text-[var(--color-vital)] font-medium">{recordError}</p>}
              <div className="flex gap-2 pt-1">
                <button type="submit" disabled={saving} className="btn-primary flex-1">{saving ? 'Saving...' : 'Save & Complete'}</button>
                <button type="button" onClick={() => setRecordFor(null)} className="btn-ghost">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorRecordsPage;
