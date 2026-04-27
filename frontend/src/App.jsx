import { useEffect, useMemo, useState } from 'react';
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
  const [clients, setClients] = useState([]);
  const [projects, setProjects] = useState([]);
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
  const [clientForm, setClientForm] = useState({name: '', email: '', company: '', additionalInfo: '' });
  const [projectForm, setProjectForm] = useState({ name: '', clientId: '', budget: '', hourlyRate: '', currency: 'EUR', status: 'ACTIVE' });
  const [timeForm, setTimeForm] = useState({ projectId: '', date: '', hours: '', description: '' });
  const [invoiceForm, setInvoiceForm] = useState({ projectId: '', issueDate: '', dueDate: '', amount: '', taxRate: '', remind3DaysBefore: false, remind1DayBefore: false, remindOnDueDate: false });
  const [expenseForm, setExpenseForm] = useState({ projectId: '', amount: '', category: '', description: '', date: '' });
  const [successMsg, setSuccessMsg] = useState('');

  const [selectedClient, setSelectedClient] = useState(null);
  const [clientSearch, setClientSearch] = useState('');
  const [showClientSearch, setShowClientSearch] = useState(false);

  const [editingClient, setEditingClient] = useState(null);
  const [editClientForm, setEditClientForm] = useState({
    name: '',
    email: '',
    company: '',
    additionalInfo: ''
  });
  function startEditClient() {
    if (!selectedClient) return;
  
    setEditingClient(selectedClient);
    setEditClientForm({
      name: selectedClient.name || '',
      email: selectedClient.email || '',
      company: selectedClient.company || '',
      additionalInfo: selectedClient.additionalInfo || ''
    });
  }
  
  async function saveClientEdit(e) {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
  
    const trimmedName = editClientForm.name.trim();
  
    if (!trimmedName) {
      setError('Name is required.');
      return;
    }
  
    if (trimmedName.length < 2 || trimmedName.length > 100) {
      setError('Name must be between 2 and 100 characters.');
      return;
    }
  
    if (editClientForm.email && !editClientForm.email.includes('@')) {
      setError('Invalid email format.');
      return;
    }
  
    try {
      const updatedClient = await api.updateClient(editingClient.id, {
        ...editClientForm,
        name: trimmedName
      });
  
      setSelectedClient(updatedClient);
      setEditingClient(null);
      setEditClientForm({ name: '', email: '', company: '', additionalInfo: '' });
      setSuccessMsg('Client updated successfully!');
      setTimeout(() => setSuccessMsg(''), 3000);
  
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }
  
  async function deleteSelectedClient() {
    if (!selectedClient) return;
  
    const confirmed = window.confirm(`Are you sure you want to delete client "${selectedClient.name}"?`);
    if (!confirmed) return;
  
    try {
      setError('');
      setSuccessMsg('');
  
      await api.deleteClient(selectedClient.id);
  
      setSelectedClient(null);
      setEditingClient(null);
      setSuccessMsg('Client deleted successfully!');
      setTimeout(() => setSuccessMsg(''), 3000);
  
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }

  async function loadAll() {
    try {
      setError('');
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
    
      setClientForm({ name: '', email: '', company: '', additionalInfo: '' });
      setSuccessMsg('Client created successfully!');
    
      setTimeout(() => setSuccessMsg(''), 3000);
    
      loadAll();
    } catch (e) {
      setError(e.message);
    }
  }

  async function addProject(e) {
    e.preventDefault();
    try {
      setError('');
      await api.createProject({
        ...projectForm,
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

  const latestClients = [...clients].slice(-5).reverse();

  const filteredClients = clients.filter((client) => {
    const query = clientSearch.toLowerCase().trim();
  
    if (!query) return true;
  
    return (
      client.name?.toLowerCase().includes(query) ||
      client.email?.toLowerCase().includes(query) ||
      client.company?.toLowerCase().includes(query) ||
      client.additionalInfo?.toLowerCase().includes(query)
    );
  });
  
  function matchesAdditionalInfo(client) {
    const query = clientSearch.toLowerCase().trim();
  
    if (!query) return false;
  
    return (
      client.additionalInfo?.toLowerCase().includes(query) &&
      !client.name?.toLowerCase().includes(query) &&
      !client.email?.toLowerCase().includes(query) &&
      !client.company?.toLowerCase().includes(query)
    );
  }

  function highlightMatch(text, query) {
    if (!text || !query.trim()) {
      return text || 'not provided';
    }
  
    const lowerText = text.toLowerCase();
    const lowerQuery = query.toLowerCase().trim();
    const index = lowerText.indexOf(lowerQuery);
  
    if (index === -1) {
      return text;
    }
  
    return (
      <>
        {text.slice(0, index)}
        <mark>{text.slice(index, index + lowerQuery.length)}</mark>
        {text.slice(index + lowerQuery.length)}
      </>
    );
  }


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
          <textarea
            placeholder="Additional info (phone, address, notes)"
            value={clientForm.additionalInfo}
            onChange={(e) => setClientForm({ ...clientForm, additionalInfo: e.target.value })}
          />
          <button type="submit">Save</button>
        </form>
      </Panel>

      <Panel title="Clients">
      <div className="client-actions">
  <button type="button" onClick={() => setShowClientSearch(true)}>
    Search clients
  </button>

  <button
    type="button"
    disabled={!selectedClient}
    onClick={startEditClient}
  >
    Edit selected
  </button>

  <button
    type="button"
    disabled={!selectedClient}
    onClick={deleteSelectedClient}
  >
    Delete selected
  </button>
</div>

  <h3>Latest 5 clients</h3>
  <p className="hint">Click a client row to select it. After selecting, Edit and Delete become available.</p>

  {latestClients.length === 0 ? (
    <p>No clients found</p>
  ) : (
    <div className="client-table">
  <div className="client-row client-header">
    <span>Name</span>
    <span>Email</span>
    <span>Company</span>
    <span>Status</span>
  </div>

  {latestClients.map((client) => (
    <div
      key={client.id}
      className={`client-row ${selectedClient?.id === client.id ? 'selected' : ''}`}
      onClick={() => setSelectedClient(client)}
      title="Click to select this client"
    >
      <span>{client.name}</span>
      <span>{client.email || 'No email'}</span>
      <span>{client.company || 'No company'}</span>
      <span>{selectedClient?.id === client.id ? 'Selected' : 'Click to select'}</span>
    </div>
  ))}
</div>
  )}

  {selectedClient && (
    <div className="result">
      <p><strong>Selected client:</strong> {selectedClient.name}</p>
      <p>Email: {selectedClient.email || 'not provided'}</p>
      <p>Company: {selectedClient.company || 'not provided'}</p>
      <p>
  Additional info: {highlightMatch(selectedClient.additionalInfo, clientSearch)}
</p>
    </div>
  )}

{editingClient && (
  <div className="result">
    <h3>Edit client</h3>

    <form onSubmit={saveClientEdit} className="form-inline">
      <input
        placeholder="Name"
        value={editClientForm.name}
        onChange={(e) => setEditClientForm({ ...editClientForm, name: e.target.value })}
        required
      />

      <input
        placeholder="Email"
        type="email"
        value={editClientForm.email}
        onChange={(e) => setEditClientForm({ ...editClientForm, email: e.target.value })}
      />

      <input
        placeholder="Company"
        value={editClientForm.company}
        onChange={(e) => setEditClientForm({ ...editClientForm, company: e.target.value })}
      />

      <textarea
        placeholder="Additional info (phone, address, notes)"
        value={editClientForm.additionalInfo}
        onChange={(e) => setEditClientForm({ ...editClientForm, additionalInfo: e.target.value })}
      />

      <button type="submit">Save changes</button>

      <button
        type="button"
        onClick={() => {
          setEditingClient(null);
          setEditClientForm({ name: '', email: '', company: '' });
        }}
      >
        Cancel
      </button>
    </form>
  </div>
)}

  {showClientSearch && (
    <div className="result">
      <h3>Search clients</h3>

      <div className="search-row">
  <input
    className="search-input"
    placeholder="Search by name, email or company"
    value={clientSearch}
    onChange={(e) => setClientSearch(e.target.value)}
  />

<button
  type="button"
  onClick={() => {
    setShowClientSearch(false);
    setClientSearch('');
  }}
>
  Close
</button>
</div>

      <div className="client-table">
  <div className="client-row client-header">
    <span>Name</span>
    <span>Email</span>
    <span>Company</span>
    <span>Action</span>
  </div>

  {filteredClients.map((client) => (
    <div
      key={client.id}
      className="client-row"
      onClick={() => {
        setSelectedClient(client);
        setShowClientSearch(false);
      }}
      title="Click to select this client"
    >
      <span>{client.name}</span>
      <span>{client.email || 'No email'}</span>
      <span>{client.company || 'No company'}</span>
      <span>
        {matchesAdditionalInfo(client) ? (
          <strong className="additional-match">Additional info match — select to view</strong>
        ) : (
          'Select'
        )}
      </span>
    </div>
  ))}
</div>
    </div>
  )}
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