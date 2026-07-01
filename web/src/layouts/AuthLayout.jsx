const AuthLayout = ({ eyebrow, title, subtitle, children }) => {
  return (
    <div className="min-h-screen flex flex-col lg:flex-row">
      {/* Left: brand panel */}
      <div className="relative lg:w-[42%] bg-[var(--color-panel)] text-white flex flex-col justify-between px-8 py-10 lg:px-14 lg:py-14 overflow-hidden">
        <div className="flex items-center gap-3">
          <span className="w-2.5 h-2.5 rounded-full bg-[var(--color-vital)] pulse-dot" />
          <span className="font-display text-2xl tracking-tight">MediBook</span>
        </div>

        <div className="mt-16 lg:mt-0">
          <p className="font-mono text-xs tracking-[0.2em] uppercase text-[var(--color-vital)] mb-4">
            Clinic Operations Portal
          </p>
          <h1 className="font-display text-4xl lg:text-[2.75rem] leading-[1.1] font-semibold">
            Every patient's <em className="italic text-[var(--color-vital)]">vitals</em>, every clinic's rhythm, in one place.
          </h1>
          <p className="mt-5 text-[15px] text-white/70 max-w-sm leading-relaxed">
            Staff, doctors, and administrators keep appointments, records, and rosters in sync — from a single, secure dashboard.
          </p>
        </div>

        <p className="hidden lg:block text-xs text-white/40 font-mono">
          MediBook © 2026 · Systems Integration &amp; Architecture
        </p>

        <svg
          className="absolute bottom-0 left-0 w-full h-16 lg:h-20"
          viewBox="0 0 600 80"
          preserveAspectRatio="none"
          fill="none"
        >
          <path
            className="pulse-line-path"
            d="M0 40 L140 40 L162 40 L174 8 L190 72 L206 40 L230 40 L600 40"
            stroke="rgba(255,255,255,0.25)"
            strokeWidth="2"
          />
        </svg>
      </div>

      {/* Right: form panel */}
      <div className="flex-1 flex items-center justify-center px-6 py-12 lg:py-10 bg-[var(--color-bg)]">
        <div className="w-full max-w-sm">
          {eyebrow && (
            <p className="font-mono text-xs tracking-[0.2em] uppercase text-[var(--color-panel-accent)] mb-3">
              {eyebrow}
            </p>
          )}
          <h2 className="font-display text-3xl font-semibold text-[var(--color-ink)] mb-1">
            {title}
          </h2>
          {subtitle && (
            <p className="text-sm text-[var(--color-ink-soft)] mb-8">{subtitle}</p>
          )}
          {children}
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;