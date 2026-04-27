export default function DeleteConfirmDialog({ open, text, onConfirm, onCancel }) {
  if (!open) return null;

  return (
    <div className="dialog-backdrop">
      <div className="dialog">
        <p>{text}</p>
        <div className="form-inline">
          <button type="button" onClick={onConfirm}>Delete</button>
          <button type="button" onClick={onCancel}>Cancel</button>
        </div>
      </div>
    </div>
  );
}
