import { useState } from 'react';
import { registerAccount } from '../auth/authService';
import AuthLayout from '../layouts/AuthLayout';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const labelClasses =
  'block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5';

const AdminRegisterPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    fullName: '',
    role: 'STAFF',
    specialization: '',
    contactNumber: '',
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const result = await registerAccount(formData);
      setMessage(result);
      setFormData({
        username: '',
        password: '',
        email: '',
        fullName: '',
        role: 'STAFF',
        specialization: '',
        contactNumber: '',
      });
    } catch (err) {
      setError(err.response?.data || 'Failed to create account.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      eyebrow="Admin Panel"
      title="Provision an account"
      subtitle="Create a Staff or Doctor login. Credentials are issued directly — share them with the new team member."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className={labelClasses}>Full Name</label>
          <input
            type="text"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            required
            className={inputClasses}
            placeholder="Dr. Maria Santos"
          />
        </div>

        <div>
          <label className={labelClasses}>Username</label>
          <input
            type="text"
            name="username"
            value={formData.username}
            onChange={handleChange}
            required
            className={inputClasses}
            placeholder="drsantos"
          />
        </div>

        <div>
          <label className={labelClasses}>Email</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            className={inputClasses}
            placeholder="drsantos@medibook.com"
          />
        </div>

        <div>
          <label className={labelClasses}>Password</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            className={inputClasses}
            placeholder="••••••••"
          />
        </div>

        <div>
          <label className={labelClasses}>Role</label>
          <select
            name="role"
            value={formData.role}
            onChange={handleChange}
            className={inputClasses}
          >
            <option value="STAFF">Staff</option>
            <option value="DOCTOR">Doctor</option>
          </select>
        </div>

        {formData.role === 'DOCTOR' && (
          <div className="rounded-lg border border-[var(--color-border)] bg-white/60 p-4 space-y-4">
            <p className="font-mono text-[11px] uppercase tracking-wide text-[var(--color-panel-accent)]">
              Doctor Details
            </p>
            <div>
              <label className={labelClasses}>Specialization</label>
              <input
                type="text"
                name="specialization"
                value={formData.specialization}
                onChange={handleChange}
                className={inputClasses}
                placeholder="Pediatrics"
              />
            </div>

            <div>
              <label className={labelClasses}>Contact Number</label>
              <input
                type="text"
                name="contactNumber"
                value={formData.contactNumber}
                onChange={handleChange}
                className={inputClasses}
                placeholder="09171234567"
              />
            </div>
          </div>
        )}

        {message && (
          <p className="text-sm text-[var(--color-panel-accent)] font-medium">{message}</p>
        )}
        {error && (
          <p className="text-sm text-[var(--color-vital)] font-medium">{String(error)}</p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-[var(--color-vital)] hover:bg-[#ff5643] disabled:opacity-40 text-white text-sm font-semibold py-2.5 rounded-lg transition shadow-sm shadow-[var(--color-vital)]/20"
        >
          {loading ? 'Creating…' : 'Create Account'}
        </button>
      </form>
    </AuthLayout>
  );
};

export default AdminRegisterPage;