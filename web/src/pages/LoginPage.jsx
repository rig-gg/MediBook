import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import AuthLayout from '../layouts/AuthLayout';

const inputClasses =
  'w-full rounded-lg border border-[var(--color-border)] bg-white px-3.5 py-2.5 text-sm text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-panel-accent)]/40 focus:border-[var(--color-panel-accent)] transition';

const labelClasses =
  'block text-xs font-medium font-mono uppercase tracking-wide text-[var(--color-ink-soft)] mb-1.5';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const loggedInUser = await login(username, password);

      if (loggedInUser.role === 'PATIENT') {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        setError('PATIENT accounts can only access the mobile app. Please use the Android application.');
        return;
      }

      if (loggedInUser.role === 'ADMIN') {
        navigate('/admin/register');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError('Invalid username or password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      eyebrow="Sign In"
      title="Welcome back"
      subtitle="Enter your credentials to access the clinic dashboard."
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className={labelClasses}>Username</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className={inputClasses}
            placeholder="e.g. drsantos"
          />
        </div>

        <div>
          <label className={labelClasses}>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className={inputClasses}
            placeholder="••••••••"
          />
        </div>

        {error && (
          <p className="text-sm text-[var(--color-vital)] font-medium">{error}</p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-[var(--color-vital)] hover:bg-[#ff5643] disabled:opacity-40 text-white text-sm font-semibold py-2.5 rounded-lg transition shadow-sm shadow-[var(--color-vital)]/20"
        >
          {loading ? 'Signing in…' : 'Sign In'}
        </button>

        <p className="text-xs text-[var(--color-ink-soft)] text-center pt-2">
          Staff and doctor accounts are provisioned by your system administrator.
        </p>
      </form>
    </AuthLayout>
  );
};

export default LoginPage;