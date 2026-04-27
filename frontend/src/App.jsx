import { useEffect, useMemo, useState } from 'react';
import SharedInvoiceView from './SharedInvoiceView';
import { api } from './api';
import CategoryForm from './components/CategoryForm';
import CategoryList from './components/CategoryList';
import TransactionFilter from './components/TransactionFilter';

function Panel({ title, children }) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      {children}
    </section>
  );
}

export default function App() {
  const token = window.location.pathname.startsWith('/shared/')
    ? window.location.pathname.replace('/shared/', '')
    : null;

  if (token) return <SharedInvoiceView token={token} />;

  const [clients, setClients] = useState([]);
  const [projects, setProjects] = useState([]);
  const [projectErrors, setProjectErrors] = useState({});
  const [timeEntries, setTimeEntries] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [categories, setCategories] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [reminders, setReminders] = useState([]);
  const [alerts, setAlerts] = useState({ overBudgetProjects: [], overdueInvoices: [] });
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [profitability, setProfitability] = useState(null);
  const [error, setError] = useState('');

  const [clientForm, setClientForm] = useState({ name: '', email: '', company: '' });
  const [projectForm, setProjectForm] = useState({ name: '', clientId: '', budget: '', hourlyRate: '', currency: 'EUR', status: 'ACTIVE' });
  const [timeForm, setTimeForm] = useState({ projectId: '', date: '', hours: '', description: '' });
  const [invoiceForm, setInvoiceForm] = useState({ projectId: '', issueDate: '', dueDate: '', amount: '', taxRate: '', remind3DaysBefore: false, remind1DayBefore: false, remindOnDueDate: false });
  const [expenseForm, setExpenseForm] = useState({ projectId: '', amount: '', category: '', description: '', date: '' });
  const [successMsg, setSuccessMsg] = useState('');


  async function loadAll() {
    try {
      //setError('');
      const [c, p, t, i, a, r, cat, tx] = await Promise.all([
        api.listClients(),
        api.listProjects(),
        api.listTimeEntries(),
        api.listInvoices(),
        api.getAlerts(),
        api.listInvoiceReminders(),
        api.listCategories(),
        api.listTransactions()
      ]);
      setClients(c);
      setProjects(p);
      setTimeEntries(t);
      setInvoices(i);
      setAlerts(a);
      setReminders(r);
      setCategories(cat);
      setTransactions(tx);
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  const totalTrackedHours = useMemo(
    () => timeEntries.reduce((sum, entry) => sum + Number(entry.hours || 0), 0),
    [timeEntries]
  );

  async function addClient(e) {
    e.preventDefault();
    setSuccessMsg('');
    setError('');

    const trimmedName = clientForm.name.trim();

    if (!trimmedName) {
      setError('Name is required.');
      return;
    }

    if (trimmedName.length < 2 || trimmedName.length > 100) {
      setError('Name must be between 2 and 100 characters.');
      return;
    }
    if (clientForm.email && !clientForm.email.includes('@')) {
      setError('Invalid email format.');
      return;
    }

    try {
      await api.createClient({
        ...clientForm,
        name: trimmedName
      });
    
      setClientForm({ name: '', email: '', company: '' });
      setSuccessMsg('Client created successfully!');
    
      setTimeout(() => setSuccessMsg(''), 3000);
    
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }

  async function addProject(e) {
    e.preventDefault();
    setError('');

    const errors = {};
    const trimmedName = projectForm.name.trim();

    if (!trimmedName || trimmedName.length < 2)
      errors.name = 'Project name must be at least 2 characters.';
    if (trimmedName.length > 120)
      errors.name = 'Project name must be under 120 characters.';
    if (!projectForm.clientId)
      errors.clientId = 'Please select a client.';
    if (projectForm.hourlyRate < 0)
      errors.hourlyRate = 'Hourly rate must be 0 or greater.';
    if (projectForm.budget < 0)
      errors.budget = 'Budget must be 0 or greater.';

    if (Object.keys(errors).length > 0) {
    setProjectErrors(errors);
    return;
  }
    setProjectErrors({});
    
    try {
      await api.createProject({
        ...projectForm,
        name: trimmedName,
        clientId: Number(projectForm.clientId),
        budget: Number(projectForm.budget),
        hourlyRate: Number(projectForm.hourlyRate)
      });
      setProjectForm({ name: '', clientId: '', budget: '', hourlyRate: '', currency: 'EUR', status: 'ACTIVE' });
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }

  async function addTimeEntry(e) {
    e.preventDefault();
    try {
      setError('');
      await api.createTimeEntry({
        ...timeForm,
        projectId: Number(timeForm.projectId),
        hours: Number(timeForm.hours),
        billable: true
      });
      setTimeForm({ projectId: '', date: '', hours: '', description: '' });
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }

  async function addInvoice(e) {
    e.preventDefault();

    const today = new Date().toISOString().split('T')[0];

    const anyReminderSelected =
      invoiceForm.remind3DaysBefore ||
      invoiceForm.remind1DayBefore ||
      invoiceForm.remindOnDueDate;

    if (!invoiceForm.dueDate) {
      setError('Due date is required.');
      return;
    }

    if (invoiceForm.dueDate < today) {
      setError('Due date cannot be in the past.');
      return;
    }

    if (!anyReminderSelected) {
      setError('Select at least one reminder option.');
      return;
    }

    try {
      setError('');

      await api.createInvoice({
        ...invoiceForm,
        projectId: Number(invoiceForm.projectId),
        amount: Number(invoiceForm.amount),
        taxRate: Number(invoiceForm.taxRate),
        status: 'UNPAID'
      });

      setInvoiceForm({
        projectId: '',
        issueDate: '',
        dueDate: '',
        amount: '',
        taxRate: '',
        remind3DaysBefore: false,
        remind1DayBefore: false,
        remindOnDueDate: false
      });

      loadAll();
    } catch (e) {
      setError(e.message);
    }

  }

  async function addExpense(e) {
    e.preventDefault();
    await api.createExpense({
      ...expenseForm,
      projectId: Number(expenseForm.projectId),
      amount: Number(expenseForm.amount)
    });
    setExpenseForm({ projectId: '', amount: '', category: '', description: '', date: '' });
    loadAll();
  }

  async function checkProfitability() {
    if (!selectedProjectId) return;
    try {
      setError('');
      const result = await api.getProfitability(selectedProjectId);
      setProfitability(result);
    } catch (e) {
      setError(e.message);
    }
  }

  const subtotal = Number(invoiceForm.amount || 0);
  const taxRate = Number(invoiceForm.taxRate || 0);

  const taxAmount = (subtotal * (taxRate / 100)).toFixed(2);
  const total = (subtotal + Number(taxAmount)).toFixed(2);


  async function createCategory(payload) {
    await api.createCategory(payload);
    setCategories(await api.listCategories());
  }

  async function updateCategory(id, payload) {
    await api.updateCategory(id, payload);
    setCategories(await api.listCategories());
  }

  async function deleteCategory(id) {
    await api.deleteCategory(id);
    setCategories(await api.listCategories());
  }

  async function applyTransactionFilters(filters) {
    const result = await api.listTransactions({
      from: filters.from,
      to: filters.to,
      categoryId: filters.categoryId ? Number(filters.categoryId) : null
    });
    setTransactions(result);
  }

  async function clearTransactionFilters() {
    setTransactions(await api.listTransactions());
  }

  return (
    <main className="container">
      <header>
        <h1>Datech Freelancer MVP</h1>
        <p>Clients, projects, time tracking, profitability and invoices in one place.</p>
      </header>

      {error && <p className="error" style={{color: 'red'}}>Error: {error}</p>}
      {successMsg && <p className="success" style={{color: 'green'}}>{successMsg}</p>}

      <Panel title="Dashboard">
        <div className="grid two">
          <div className="kpi">
            <strong>{clients.length}</strong>
            <span>Clients</span>
          </div>
          <div className="kpi">
            <strong>{projects.length}</strong>
            <span>Projects</span>
          </div>
          <div className="kpi">
            <strong>{totalTrackedHours.toFixed(2)}</strong>
            <span>Tracked hours</span>
          </div>
          <div className="kpi warning">
            <strong>{alerts.overdueInvoices.length}</strong>
            <span>Overdue invoices</span>
          </div>
        </div>
        <p>Over budget projects: {alerts.overBudgetProjects.length}</p>
      </Panel>

      <Panel title="Add Client">
        <form onSubmit={addClient} className="form-inline">
          <input
            placeholder="Name"
            value={clientForm.name}
            onChange={(e) => setClientForm({ ...clientForm, name: e.target.value })}
            required
          />
          <input
            placeholder="Email"
            type="email"
            value={clientForm.email}
            onChange={(e) => setClientForm({ ...clientForm, email: e.target.value })}
          />
          <input
            placeholder="Company"
            value={clientForm.company}
            onChange={(e) => setClientForm({ ...clientForm, company: e.target.value })}
          />
          <button type="submit">Save</button>
        </form>
      </Panel>

      <Panel title="Add Project">
        <form onSubmit={addProject} className="form-inline">
          <input
            placeholder="Project name"
            value={projectForm.name}
            onChange={(e) => setProjectForm({ ...projectForm, name: e.target.value })}
            required
          />
          <select
            value={projectForm.clientId}
            onChange={(e) => setProjectForm({ ...projectForm, clientId: e.target.value })}
            required
          >
            <option value="">Select client</option>
            {clients.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          <input
            placeholder="Budget"
            type="number"
            step="0.01"
            min="0"
            value={projectForm.budget}
            onChange={(e) => setProjectForm({ ...projectForm, budget: e.target.value })}
            required
          />
          <input
            placeholder="Hourly rate"
            type="number"
            step="0.01"
            min="0"
            value={projectForm.hourlyRate}
            onChange={(e) => setProjectForm({ ...projectForm, hourlyRate: e.target.value })}
            required
          />
          <select
            value={projectForm.currency}
            onChange={(e) => setProjectForm({ ...projectForm, currency: e.target.value })}
            required
          >
            <option value="EUR">EUR</option>
            <option value="USD">USD</option>
            <option value="GBP">GBP</option>
          </select>
          <select
            value={projectForm.status}
            onChange={(e) => setProjectForm({ ...projectForm, status: e.target.value })}
            required
          >
            <option value="ACTIVE">ACTIVE</option>
            <option value="ARCHIVED">ARCHIVED</option>
          </select>
          <button type="submit">Save</button>
        </form>
      </Panel>

      <Panel title="Track Time">
        <form onSubmit={addTimeEntry} className="form-inline">
          <select
            value={timeForm.projectId}
            onChange={(e) => setTimeForm({ ...timeForm, projectId: e.target.value })}
            required
          >
            <option value="">Select project</option>
            {projects
              .filter((p) => p.status !== 'ARCHIVED')
              .map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
            ))}
          </select>
          <input
            type="date"
            value={timeForm.date}
            onChange={(e) => setTimeForm({ ...timeForm, date: e.target.value })}
            required
          />
          <input
            placeholder="Hours"
            type="number"
            step="0.25"
            value={timeForm.hours}
            onChange={(e) => setTimeForm({ ...timeForm, hours: e.target.value })}
            required
          />
          <input
            placeholder="Description"
            value={timeForm.description}
            onChange={(e) => setTimeForm({ ...timeForm, description: e.target.value })}
          />
          <button type="submit">Save</button>
        </form>
      </Panel>

      <Panel title="Add Expense">
        <form onSubmit={addExpense} className="form-inline">
          <select value={expenseForm.projectId} onChange={(e) => setExpenseForm({ ...expenseForm, projectId: e.target.value })} required>
            <option value="">Select project</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <input type="date" value={expenseForm.date} onChange={(e) => setExpenseForm({ ...expenseForm, date: e.target.value })} required />
          <input placeholder="Amount" type="number" step="0.01" value={expenseForm.amount} onChange={(e) => setExpenseForm({ ...expenseForm, amount: e.target.value })} required />
          <input placeholder="Category" value={expenseForm.category} onChange={(e) => setExpenseForm({ ...expenseForm, category: e.target.value })} />
          <input placeholder="Description" value={expenseForm.description} onChange={(e) => setExpenseForm({ ...expenseForm, description: e.target.value })} />
          <button type="submit">Save</button>
        </form>
      </Panel>

      <Panel title="Create Invoice">
        <form onSubmit={addInvoice} className="form-inline">
          <select
            value={invoiceForm.projectId}
            onChange={(e) => setInvoiceForm({ ...invoiceForm, projectId: e.target.value })}
            required
          >
            <option value="">Select project</option>
            {projects
              .filter((p) => p.status !== 'ARCHIVED')
              .map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
          </select>
          <input
            type="date"
            value={invoiceForm.issueDate}
            onChange={(e) => setInvoiceForm({ ...invoiceForm, issueDate: e.target.value })}
            required
          />
          <input
            type="date"
            value={invoiceForm.dueDate}
            onChange={(e) => setInvoiceForm({ ...invoiceForm, dueDate: e.target.value })}
            required
          />
          <input
            placeholder="Amount"
            type="number"
            step="0.01"
            value={invoiceForm.amount}
            onChange={(e) => setInvoiceForm({ ...invoiceForm, amount: e.target.value })}
            required
          />
          <input
            placeholder="VAT %"
            type="number"
            step="0.01"
            value={invoiceForm.taxRate}
            onChange={(e) => setInvoiceForm({ ...invoiceForm, taxRate: e.target.value })}
          />
          <label>
            <input
              type="checkbox"
              checked={invoiceForm.remind3DaysBefore}
              onChange={(e) => setInvoiceForm({ ...invoiceForm, remind3DaysBefore: e.target.checked })}
            />
            3 days before
          </label>
          <label>
            <input
              type="checkbox"
              checked={invoiceForm.remind1DayBefore}
              onChange={(e) => setInvoiceForm({ ...invoiceForm, remind1DayBefore: e.target.checked })}
            />
            1 day before
          </label>
          <label>
            <input
              type="checkbox"
              checked={invoiceForm.remindOnDueDate}
              onChange={(e) => setInvoiceForm({ ...invoiceForm, remindOnDueDate: e.target.checked })}
            />
            On due date
          </label>
          <button type="submit">Save</button>
        </form>
        <div className="result">
          <p>Subtotal: {subtotal.toFixed(2)}</p>
          <p>Tax: {taxAmount}</p>
          <p>Total: {total}</p>
        </div>
      </Panel>

      <Panel title="Scheduled Reminders">
        <ul>
          {reminders.length === 0 ? (
            <li>No reminders found</li>
          ) : (
            reminders.map((r) => (
              <li key={r.id}>
                Invoice #{r.invoiceId} | {r.type} | {r.remindAt} | {r.status}
              </li>
            ))
          )}
        </ul>
      </Panel>

      <Panel title="Profitability Check">
        <div className="form-inline">
          <select value={selectedProjectId} onChange={(e) => setSelectedProjectId(e.target.value)}>
            <option value="">Select project</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <button type="button" onClick={checkProfitability}>Calculate</button>
        </div>
        {profitability && (
          <div className="result">
            <p>Revenue: {Number(profitability.revenue).toFixed(2)}</p>
            <p>Budget: {Number(profitability.budget).toFixed(2)}</p>
            <p>Difference: {Number(profitability.profitability).toFixed(2)}</p>
            <p>Status: {profitability.overBudget ? 'Over budget' : 'Within budget'}</p>
          </div>
        )}
      </Panel>

      <Panel title="Client Portal — Share Invoice">
        <ShareInvoicePanel invoices={invoices} />
      </Panel>

      <Panel title="Latest Invoices">
        <ul>
          {invoices.slice(0, 10).map((inv) => (
            <li key={inv.id}>
              #{inv.id} project {inv.projectId} | amount {inv.amount} | due {inv.dueDate} | {inv.status}
            </li>
          ))}
        </ul>
      </Panel>

      <Panel title="Categories">
        <CategoryForm onCreate={createCategory} />
        <CategoryList categories={categories} onUpdate={updateCategory} onDelete={deleteCategory} />
      </Panel>

      <Panel title="Transactions">
        <TransactionFilter
          categories={categories}
          onApply={applyTransactionFilters}
          onClear={clearTransactionFilters}
        />
        {transactions.length === 0 ? (
          <p>No transactions match the selected filters</p>
        ) : (
          <ul>
            {transactions.map((tx) => (
              <li key={tx.id}>
                {tx.date} | {tx.type || 'n/a'} | {tx.amount} | category {tx.categoryId || 'none'}
              </li>
            ))}
          </ul>
        )}
      </Panel>
    </main>
  );
}
function ShareInvoicePanel({ invoices }) {
  const [invoiceId, setInvoiceId] = useState('');
  const [shareData, setShareData] = useState(null);
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  async function generate() {
    if (!invoiceId) return;
    setLoading(true);
    setErr('');
    setShareData(null);
    try {
      const result = await api.shareInvoice(invoiceId);
      const fullUrl = `${window.location.origin}/shared/${result.token}`;
      setShareData({ url: fullUrl, expires: result.expiresAt });
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  }

  function copy() {
    navigator.clipboard.writeText(shareData.url);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <div>
      <div className="form-inline" style={{ marginBottom: '12px' }}>
        <select value={invoiceId} onChange={(e) => setInvoiceId(e.target.value)}>
          <option value="">Select invoice</option>
          {invoices.map((inv) => (
            <option key={inv.id} value={inv.id}>
              #{inv.id} — {inv.amount} ({inv.status})
            </option>
          ))}
        </select>
        <button type="button" onClick={generate} disabled={!invoiceId || loading}>
          {loading ? 'Generating...' : 'Generate Share Link'}
        </button>
      </div>
      {err && <p style={{ color: 'red' }}>{err}</p>}
      {shareData && (
        <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '16px', background: '#f8fafc', marginTop: '12px' }}>
          <p style={{ fontSize: '12px', color: '#64748b', marginBottom: '6px' }}>
            Share URL (read-only) · Expires: {shareData.expires.slice(0, 10)}
          </p>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <input
              readOnly
              value={shareData.url}
              style={{ flex: 1, padding: '8px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px', background: '#fff' }}
            />
            <button
              type="button"
              onClick={copy}
              style={{ padding: '8px 16px', borderRadius: '6px', background: copied ? '#22c55e' : '#3b82f6', color: '#fff', border: 'none', cursor: 'pointer', fontSize: '13px' }}
            >
              {copied ? '✓ Copied!' : 'Copy'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
