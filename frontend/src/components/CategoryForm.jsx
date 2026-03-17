import { useState } from 'react';

export default function CategoryForm({ onCreate }) {
  const [form, setForm] = useState({ name: '', type: 'expense', color: '' });
  const [error, setError] = useState('');

  async function submit(e) {
    e.preventDefault();
    if (!form.name.trim()) {
      setError('Name is required');
      return;
    }
    if (form.color && !/^#[0-9A-Fa-f]{6}$/.test(form.color)) {
      setError('Color must match #RRGGBB');
      return;
    }
    setError('');
    await onCreate({
      name: form.name.trim(),
      type: form.type,
      color: form.color || null
    });
    setForm({ name: '', type: 'expense', color: '' });
  }

  return (
    <form onSubmit={submit} className="form-inline">
      <input
        placeholder="Category name"
        value={form.name}
        onChange={(e) => setForm({ ...form, name: e.target.value })}
        required
      />
      <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
        <option value="expense">Expense</option>
        <option value="income">Income</option>
      </select>
      <input
        placeholder="#RRGGBB"
        value={form.color}
        onChange={(e) => setForm({ ...form, color: e.target.value })}
      />
      <button type="submit">Add Category</button>
      {error && <span className="error">{error}</span>}
    </form>
  );
}
