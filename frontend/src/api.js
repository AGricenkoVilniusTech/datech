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
    let message = `HTTP ${response.status}`;
    try {
      const data = await response.json();
      message = data.error || data.message || message;
    } catch {
      const text = await response.text();
      if (text) message = text;
    }
    throw new Error(message);
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
  listCategories: () => request('/categories'),
  createCategory: (payload) => request('/categories', { method: 'POST', body: JSON.stringify(payload) }),
  updateCategory: (id, payload) => request(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteCategory: (id) => request(`/categories/${id}`, { method: 'DELETE' }),
  listTransactions: (filters = {}) => {
    const query = new URLSearchParams();
    if (filters.from) query.set('from', filters.from);
    if (filters.to) query.set('to', filters.to);
    if (filters.categoryId) query.set('category_id', String(filters.categoryId));
    const suffix = query.toString() ? `?${query}` : '';
    return request(`/transactions${suffix}`);
  },
  getAlerts: () => request('/dashboard/alerts'),
  createExpense: (payload) => request('/expenses', { method: 'POST', body: JSON.stringify(payload) })
};