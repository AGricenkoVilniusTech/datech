import React from 'react';

export default function SharedInvoiceView({ token }) {
  const [data, setData] = React.useState(null);
  const [error, setError] = React.useState('');

  React.useEffect(() => {
    fetch(`http://localhost:8080/api/invoices/shared/${token}`)
      .then(r => r.json())
      .then(setData)
      .catch(() => setError('Sąskaita nerasta arba nuoroda nebegalioja.'));
  }, [token]);

  if (error) return (
    <div style={{ display:'flex', justifyContent:'center', alignItems:'center', minHeight:'100vh', fontFamily:'sans-serif' }}>
      <div style={{ textAlign:'center', color:'#ef4444' }}>{error}</div>
    </div>
  );

  if (!data) return (
    <div style={{ display:'flex', justifyContent:'center', alignItems:'center', minHeight:'100vh', fontFamily:'sans-serif' }}>
      <div style={{ color:'#64748b' }}>Kraunama...</div>
    </div>
  );

  return (
    <div style={{ minHeight:'100vh', background:'#f8fafc', display:'flex', justifyContent:'center', alignItems:'center', fontFamily:'sans-serif', padding:'24px' }}>
      <div style={{ background:'#fff', borderRadius:'12px', border:'1px solid #e2e8f0', padding:'40px', maxWidth:'480px', width:'100%' }}>
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'32px' }}>
          <div>
            <h1 style={{ fontSize:'22px', fontWeight:'700', margin:0 }}>Sąskaita faktūra</h1>
            <p style={{ fontSize:'13px', color:'#64748b', margin:'4px 0 0' }}>#{data.id}</p>
          </div>
          <span style={{
            padding:'6px 14px', borderRadius:'20px', fontSize:'12px', fontWeight:'600',
            background: data.status==='PAID' ? '#dcfce7' : data.status==='OVERDUE' ? '#fee2e2' : '#fef9c3',
            color: data.status==='PAID' ? '#16a34a' : data.status==='OVERDUE' ? '#dc2626' : '#ca8a04'
          }}>
            {data.status}
          </span>
        </div>
        <div style={{ borderTop:'1px solid #f1f5f9', borderBottom:'1px solid #f1f5f9', padding:'20px 0', marginBottom:'24px' }}>
          <div style={{ display:'flex', justifyContent:'space-between', marginBottom:'12px' }}>
            <span style={{ color:'#64748b', fontSize:'14px' }}>Mokėjimo terminas</span>
            <span style={{ fontWeight:'600', fontSize:'14px' }}>{data.dueDate}</span>
          </div>
          <div style={{ display:'flex', justifyContent:'space-between' }}>
            <span style={{ color:'#64748b', fontSize:'14px' }}>Prieiga</span>
            <span style={{ fontSize:'14px', color:'#3b82f6' }}>Tik skaityti</span>
          </div>
        </div>
        <div style={{ background:'#f8fafc', borderRadius:'8px', padding:'16px', textAlign:'center' }}>
          <p style={{ fontSize:'12px', color:'#94a3b8', margin:'0 0 4px' }}>Ši nuoroda yra laikina ir skirta tik peržiūrai</p>
        </div>
      </div>
    </div>
  );
}
