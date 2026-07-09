import { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex items-center justify-center min-h-screen bg-slate-50">
          <div className="bg-white border border-slate-200 rounded-xl p-8 max-w-md mx-4 text-center">
            <p className="text-3xl mb-3">!</p>
            <h1 className="text-lg font-semibold text-slate-800 mb-2">Something went wrong</h1>
            <p className="text-sm text-slate-500 mb-4">{this.state.error?.message || 'An unexpected error occurred.'}</p>
            <button
              onClick={() => window.location.href = '/'}
              className="bg-slate-800 text-white text-sm px-4 py-2 rounded-md hover:bg-slate-700"
            >
              Back to Home
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
