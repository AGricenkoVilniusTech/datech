import CategoryItem from './CategoryItem';

export default function CategoryList({ categories, onUpdate, onDelete }) {
  const income = categories.filter((c) => c.type === 'income');
  const expense = categories.filter((c) => c.type === 'expense');

  return (
    <div className="grid two">
      <div>
        <h3>Income</h3>
        <ul>
          {income.map((category) => (
            <CategoryItem
              key={category.id}
              category={category}
              onUpdate={onUpdate}
              onDelete={onDelete}
            />
          ))}
        </ul>
      </div>
      <div>
        <h3>Expense</h3>
        <ul>
          {expense.map((category) => (
            <CategoryItem
              key={category.id}
              category={category}
              onUpdate={onUpdate}
              onDelete={onDelete}
            />
          ))}
        </ul>
      </div>
    </div>
  );
}
