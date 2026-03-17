import { useState } from 'react';
import DeleteConfirmDialog from './DeleteConfirmDialog';

export default function CategoryItem({ category, onUpdate, onDelete }) {
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(category.name);
  const [color, setColor] = useState(category.color || '');
  const [confirmOpen, setConfirmOpen] = useState(false);

  async function save() {
    await onUpdate(category.id, { name: name.trim(), color: color || null });
    setEditing(false);
  }

  return (
    <li>
      {editing ? (
        <div className="form-inline">
          <input value={name} onChange={(e) => setName(e.target.value)} />
          <input value={color} placeholder="#RRGGBB" onChange={(e) => setColor(e.target.value)} />
          <button type="button" onClick={save}>Save</button>
          <button type="button" onClick={() => setEditing(false)}>Cancel</button>
        </div>
      ) : (
        <div className="form-inline">
          <span>{category.name} ({category.type}) {category.color || ''}</span>
          <button type="button" onClick={() => setEditing(true)}>Edit</button>
          <button type="button" onClick={() => setConfirmOpen(true)}>Delete</button>
        </div>
      )}

      <DeleteConfirmDialog
        open={confirmOpen}
        text="Are you sure you want to delete this category?"
        onCancel={() => setConfirmOpen(false)}
        onConfirm={async () => {
          await onDelete(category.id);
          setConfirmOpen(false);
        }}
      />
    </li>
  );
}
