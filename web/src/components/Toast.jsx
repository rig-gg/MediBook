import { useState, useEffect } from 'react';

const Toast = ({ message, type = 'error', onDismiss, duration = 4000 }) => {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setVisible(false);
      setTimeout(() => onDismiss?.(), 300);
    }, duration);
    return () => clearTimeout(timer);
  }, [duration, onDismiss]);

  const colors = {
    error: 'bg-red-50 border-red-200 text-red-700',
    success: 'bg-emerald-50 border-emerald-200 text-emerald-700',
  };

  return (
    <div
      className={`fixed top-5 right-5 z-50 max-w-sm border rounded-lg px-4 py-3 shadow-lg transition-all duration-300 ${
        visible ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-2'
      } ${colors[type] || colors.error}`}
    >
      <div className="flex items-start justify-between gap-3">
        <p className="text-sm font-medium">{message}</p>
        <button
          onClick={() => { setVisible(false); setTimeout(() => onDismiss?.(), 300); }}
          className="text-current opacity-50 hover:opacity-100 text-lg leading-none"
        >
          &times;
        </button>
      </div>
    </div>
  );
};

export default Toast;
