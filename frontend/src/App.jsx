import { useEffect, useMemo, useState } from 'react';
import { api } from './api';

function Panel({ title, children }) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      {children}
    </section>
  );
}

export default function App() {
  const [clients, setClients] = useState([]);
  const [projects, setProjects] = useState([]);
  const [timeEntries, setTimeEntries] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [alerts, setAlerts] = useState({ overBudgetProjects: [], overdueInvoices: [] });
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [profitability, setProfitability] = useState(null);
  const [error, setError] = useState('');

  const [clientForm, setClientForm] = useState({ name: '', email: '', company: '' });
  const [projectForm, setProjectForm] = useState({ name: '', clientId: '', budget: '', hourlyRate: '', currency: 'EUR', status: 'ACTIVE' });
  const [timeForm, setTimeForm] = useState({ projectId: '', date: '', hours: '', description: '' });
  const [invoiceForm, setInvoiceForm] = useState({ projectId: '', issueDate: '', dueDate: '', amount: '', remind3DaysBefore: false, remind1DayBefore: false, remindOnDueDate: false });
  async function loadAll() {
    try {
      setError('');
      const [c, p, t, i, a] = await Promise.all([
        api.listClients(),
        api.listProjects(),
        api.listTimeEntries(),
        api.listInvoices(),
        api.getAlerts()
      ]);
      setClients(c);
      setProjects(p);
      setTimeEntries(t);
      setInvoices(i);
      setAlerts(a);
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
    await api.createClient(clientForm);
    setClientForm({ name: '', email: '', company: '' });
    loadAll();
  }

  async function addProject(e) {
    e.preventDefault();
    await api.createProject({
      ...projectForm,
      clientId: Number(projectForm.clientId),
      budget: Number(projectForm.budget),
      hourlyRate: Number(projectForm.hourlyRate)
    });
    setProjectForm({ name: '', clientId: '', budget: '', hourlyRate: '', currency: 'EUR', status: 'ACTIVE' });
    loadAll();
  }

  async function addTimeEntry(e) {
    e.preventDefault();
    await api.createTimeEntry({
      ...timeForm,
      projectId: Number(timeForm.projectId),
      hours: Number(timeForm.hours),
      billable: true
    });
    setTimeForm({ projectId: '', date: '', hours: '', description: '' });
    loadAll();
  }

  async function addInvoice(e) {
    e.preventDefault();
    await api.createInvoice({
      ...invoiceForm,
      projectId: Number(invoiceForm.projectId),
      amount: Number(invoiceForm.amount),
      status: 'UNPAID'
    });

    setInvoiceForm({
      projectId: '',
      issueDate: '',
      dueDate: '',
      amount: '',
      remind3DaysBefore: false,
      remind1DayBefore: false,
      remindOnDueDate: false
    });

    loadAll();
  }

  async function checkProfitability() {
    if (!selectedProjectId) return;
    const result = await api.getProfitability(selectedProjectId);
    setProfitability(result);
  }

  return (
    <main className="container">
      <header>
        <h1>Datech Freelancer MVP</h1>
        <p>Clients, projects, time tracking, profitability and invoices in one place.</p>
      </header>

      {error && <p className="error">Error: {error}</p>}

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
          <input placeholder="Name" value={clientForm.name} onChange={(e) => setClientForm({ ...clientForm, name: e.target.value })} required />
          <input placeholder="Email" type="email" value={clientForm.email} onChange={(e) => setClientForm({ ...clientForm, email: e.target.value })} />
          <input placeholder="Company" value={clientForm.company} onChange={(e) => setClientForm({ ...clientForm, company: e.target.value })} />
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
          <select value={timeForm.projectId} onChange={(e) => setTimeForm({ ...timeForm, projectId: e.target.value })} required>
            <option value="">Select project</option>
            {projects
              .filter((p) => p.status !== 'ARCHIVED')
              .map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
            ))}
          </select>
          <input type="date" value={timeForm.date} onChange={(e) => setTimeForm({ ...timeForm, date: e.target.value })} required />
          <input placeholder="Hours" type="number" step="0.25" value={timeForm.hours} onChange={(e) => setTimeForm({ ...timeForm, hours: e.target.value })} required />
          <input placeholder="Description" value={timeForm.description} onChange={(e) => setTimeForm({ ...timeForm, description: e.target.value })} />
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

          <label>
            <input
              type="checkbox"
              checked={invoiceForm.remind3DaysBefore}
              onChange={(e) =>
                setInvoiceForm({ ...invoiceForm, remind3DaysBefore: e.target.checked })
              }
            />
            3 days before
          </label>

          <label>
            <input
              type="checkbox"
              checked={invoiceForm.remind1DayBefore}
              onChange={(e) =>
                setInvoiceForm({ ...invoiceForm, remind1DayBefore: e.target.checked })
              }
            />
            1 day before
          </label>

          <label>
            <input
              type="checkbox"
              checked={invoiceForm.remindOnDueDate}
              onChange={(e) =>
                setInvoiceForm({ ...invoiceForm, remindOnDueDate: e.target.checked })
              }
            />
            On due date
          </label>

          <button type="submit">Save</button>
        </form>
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

      <Panel title="Latest Invoices">
        <ul>
          {invoices.slice(0, 10).map((inv) => (
            <li key={inv.id}>
              #{inv.id} project {inv.projectId} | amount {inv.amount} | due {inv.dueDate} | {inv.status}
            </li>
          ))}
        </ul>
      </Panel>
    </main>
  );
}
