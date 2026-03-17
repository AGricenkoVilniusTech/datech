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
  const [expenses, setExpenses] = useState([]);
  const [alerts, setAlerts] = useState({ overBudgetProjects: [], overdueInvoices: [] });
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [profitability, setProfitability] = useState(null);
  const [error, setError] = useState('');

  const [clientForm, setClientForm] = useState({ name: '', email: '', company: '' });
  const [projectForm, setProjectForm] = useState({ name: '', clientId: '', budget: '', hourlyRate: '' });
  const [timeForm, setTimeForm] = useState({ projectId: '', date: '', hours: '', description: '' });
  const [invoiceForm, setInvoiceForm] = useState({ projectId: '', issueDate: '', dueDate: '', amount: '', taxRate: '' });
  const [expenseForm, setExpenseForm] = useState({ projectId: '', amount: '', category: '', description: '', date: '' });
  const [successMsg, setSuccessMsg] = useState('');

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
    setSuccessMsg('');
    setError('');

    if (!clientForm.name || clientForm.name.trim().length < 2) {
      setError('Name is required and must be at least 2 characters.');
      return;
    }
    if (clientForm.email && !clientForm.email.includes('@')) {
      setError('Invalid email format.');
      return;
    }

    try {
      await api.createClient(clientForm);
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
    await api.createProject({
      ...projectForm,
      clientId: Number(projectForm.clientId),
      budget: Number(projectForm.budget),
      hourlyRate: Number(projectForm.hourlyRate)
    });
    setProjectForm({ name: '', clientId: '', budget: '', hourlyRate: '' });
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
      taxRate: Number(invoiceForm.taxRate),
      status: 'UNPAID'
    });
    setInvoiceForm({ projectId: '', issueDate: '', dueDate: '', amount: '' });
    loadAll();
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
    const result = await api.getProfitability(selectedProjectId);
    setProfitability(result);
  }

const subtotal = Number(invoiceForm.amount || 0);
const taxRate = Number(invoiceForm.taxRate || 0);

const taxAmount = (subtotal * (taxRate / 100)).toFixed(2);
const total = (subtotal + Number(taxAmount)).toFixed(2);


  return (
    <main className="container">
      <header>
        <h1>Datech Freelancer MVP</h1>
        <p>Clients, projects, time tracking, profitability and invoices in one place.</p>
      </header>

      {/* {error && <p className="error">Error: {error}</p>} */}

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
          <input placeholder="Name" value={clientForm.name} onChange={(e) => setClientForm({ ...clientForm, name: e.target.value })} required />
          <input placeholder="Email" type="email" value={clientForm.email} onChange={(e) => setClientForm({ ...clientForm, email: e.target.value })} />
          <input placeholder="Company" value={clientForm.company} onChange={(e) => setClientForm({ ...clientForm, company: e.target.value })} />
          <button type="submit">Save</button>
        </form>
      </Panel>

      <Panel title="Add Project">
        <form onSubmit={addProject} className="form-inline">
          <input placeholder="Project name" value={projectForm.name} onChange={(e) => setProjectForm({ ...projectForm, name: e.target.value })} required />
          <select value={projectForm.clientId} onChange={(e) => setProjectForm({ ...projectForm, clientId: e.target.value })} required>
            <option value="">Select client</option>
            {clients.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          <input placeholder="Budget" type="number" step="0.01" value={projectForm.budget} onChange={(e) => setProjectForm({ ...projectForm, budget: e.target.value })} required />
          <input placeholder="Hourly rate" type="number" step="0.01" value={projectForm.hourlyRate} onChange={(e) => setProjectForm({ ...projectForm, hourlyRate: e.target.value })} required />
          <button type="submit">Save</button>
        </form>
      </Panel>

      <Panel title="Track Time">
        <form onSubmit={addTimeEntry} className="form-inline">
          <select value={timeForm.projectId} onChange={(e) => setTimeForm({ ...timeForm, projectId: e.target.value })} required>
            <option value="">Select project</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <input type="date" value={timeForm.date} onChange={(e) => setTimeForm({ ...timeForm, date: e.target.value })} required />
          <input placeholder="Hours" type="number" step="0.25" value={timeForm.hours} onChange={(e) => setTimeForm({ ...timeForm, hours: e.target.value })} required />
          <input placeholder="Description" value={timeForm.description} onChange={(e) => setTimeForm({ ...timeForm, description: e.target.value })} />
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
          <select value={invoiceForm.projectId} onChange={(e) => setInvoiceForm({ ...invoiceForm, projectId: e.target.value })} required>
            <option value="">Select project</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <input type="date" value={invoiceForm.issueDate} onChange={(e) => setInvoiceForm({ ...invoiceForm, issueDate: e.target.value })} required />
          <input type="date" value={invoiceForm.dueDate} onChange={(e) => setInvoiceForm({ ...invoiceForm, dueDate: e.target.value })} required />
          <input placeholder="Amount" type="number" step="0.01" value={invoiceForm.amount} onChange={(e) => setInvoiceForm({ ...invoiceForm, amount: e.target.value })} required />
          <input placeholder="VAT %" type="number" step="0.01" value={invoiceForm.taxRate} onChange={(e) => setInvoiceForm({ ...invoiceForm, taxRate: e.target.value })} />
          <button type="submit">Save</button>
        </form>
        <div className="result">
          <p>Subtotal: {subtotal.toFixed(2)}</p>
          <p>Tax: {taxAmount}</p>
          <p>Total: {total}</p>
        </div>
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