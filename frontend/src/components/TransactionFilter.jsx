import { useState } from 'react';

export default function TransactionFilter({ categories, onApply, onClear }) {
  const [filters, setFilters] = useState({ from: '', to: '', categoryId: '' });
  const [error, setError] = useState('');

  function handleApply() {
    if (filters.from && filters.to && filters.from > filters.to) {
      setError('Start date must be before end date');
      return;
    }
    setError('');
    onApply(filters);
  }

  function handleClear() {
    const empty = { from: '', to: '', categoryId: '' };
    setFilters(empty);
    setError('');
    onClear();
  }

  return (
    <div>
      <div className="form-inline">
        <input
          type="date"
          value={filters.from}
          onChange={(e) => setFilters({ ...filters, from: e.target.value })}
        />
        <input
          type="date"
          value={filters.to}
          onChange={(e) => setFilters({ ...filters, to: e.target.value })}
        />
        <select
          value={filters.categoryId}
          onChange={(e) => setFilters({ ...filters, categoryId: e.target.value })}
        >
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <button type="button" onClick={handleApply}>Apply Filter</button>
        <button type="button" onClick={handleClear}>Clear Filters</button>
      </div>
      {error && <p className="error">{error}</p>}
    </div>
  );
}