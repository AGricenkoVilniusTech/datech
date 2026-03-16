const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  listClients: () => request('/clients'),
  createClient: (payload) => request('/clients', { method: 'POST', body: JSON.stringify(payload) }),

  listProjects: () => request('/projects'),
  createProject: (payload) => request('/projects', { method: 'POST', body: JSON.stringify(payload) }),
  getProfitability: (projectId) => request(`/projects/${projectId}/profitability`),

  listTimeEntries: () => request('/time-entries'),
  createTimeEntry: (payload) => request('/time-entries', { method: 'POST', body: JSON.stringify(payload) }),

  listInvoices: () => request('/invoices'),
  createInvoice: (payload) => request('/invoices', { method: 'POST', body: JSON.stringify(payload) }),
  listOverdueInvoices: () => request('/invoices/overdue'),

  listInvoiceReminders: () => request('/invoice-reminders'),

  getAlerts: () => request('/dashboard/alerts')
};