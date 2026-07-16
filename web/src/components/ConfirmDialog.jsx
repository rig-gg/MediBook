const ConfirmDialog = ({ title, message, onConfirm, onCancel, loading }) => (
  <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
    <div className="bg-white rounded-xl p-6 w-full max-w-sm mx-4 shadow-xl animate-fade-in-up">
      <h2 className="text-lg font-semibold text-[var(--color-ink)] mb-2">{title}</h2>
      <p className="text-sm text-[var(--color-ink-soft)] mb-6">{message}</p>
      <div className="flex justify-end gap-3">
        <button onClick={onCancel} disabled={loading} className="btn-ghost">Cancel</button>
        <button
          onClick={onConfirm}
          disabled={loading}
          className="bg-[var(--color-vital)] hover:bg-[#ff5643] disabled:opacity-40 text-white text-sm font-semibold px-4 py-2 rounded-lg transition"
        >
          {loading ? 'Deleting...' : 'Delete'}
        </button>
      </div>
    </div>
  </div>
);

export default ConfirmDialog;
