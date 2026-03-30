import React, { useState, useEffect } from 'react';
import Layout from '../components/Layout.jsx';
import { getAllPatients } from '../services/api.js';

export default function AdminPatients() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => { loadPatients(); }, [page]);

  const loadPatients = async () => {
    setLoading(true);
    try {
      const res = await getAllPatients({ page, size: 15, search: search || undefined });
      const data = res.data.data;
      setPatients(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    loadPatients();
  };

  return (
    <Layout>
      <div className="page">
        <div className="page-header">
          <h1>Manage Patients 👥</h1>
          <p>View all registered patients</p>
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '12px', marginBottom: '24px', maxWidth: '500px' }}>
          <div style={{ flex: 1 }}>
            <input
              placeholder="🔍 Search by name or email..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              style={{
                width: '100%', background: 'var(--bg-secondary)',
                border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)',
                padding: '10px 14px', color: 'var(--text-primary)',
                fontFamily: 'var(--font-display)', fontSize: '14px', outline: 'none',
              }}
            />
          </div>
          <button type="submit" className="btn btn-primary">Search</button>
        </form>

        {/* Patients Table */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontWeight: 700, fontSize: '15px' }}>All Patients</h3>
            <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>{patients.length} patients found</span>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: 60 }}><div className="spinner" style={{ width: 40, height: 40 }} /></div>
          ) : patients.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '60px', color: 'var(--text-muted)' }}>
              <div style={{ fontSize: '40px', marginBottom: '12px' }}>👥</div>
              No patients found
            </div>
          ) : (
            <>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Patient</th>
                      <th>Email</th>
                      <th>Phone</th>
                      <th>Age</th>
                      <th>Blood</th>
                      <th>Joined</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {patients.map((p, i) => (
                      <tr key={p.id}>
                        <td style={{ color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', fontSize: '12px' }}>
                          #{p.id}
                        </td>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <div style={{
                              width: 32, height: 32, borderRadius: '50%',
                              background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))',
                              display: 'flex', alignItems: 'center', justifyContent: 'center',
                              fontSize: '12px', fontWeight: 700, color: '#000', flexShrink: 0,
                            }}>
                              {p.firstName?.[0]}{p.lastName?.[0]}
                            </div>
                            <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                              {p.firstName} {p.lastName}
                            </span>
                          </div>
                        </td>
                        <td>{p.email}</td>
                        <td style={{ fontFamily: 'var(--font-mono)' }}>{p.phone || '-'}</td>
                        <td>{p.age || '-'}</td>
                        <td>
                          {p.bloodGroup ? (
                            <span style={{
                              padding: '2px 8px', borderRadius: '20px',
                              background: 'rgba(239,68,68,0.1)',
                              border: '1px solid rgba(239,68,68,0.2)',
                              color: 'var(--accent-red)', fontSize: '12px', fontWeight: 700,
                            }}>
                              {p.bloodGroup}
                            </span>
                          ) : '-'}
                        </td>
                        <td style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
                          {p.createdAt?.split('T')[0]}
                        </td>
                        <td>
                          <span className={`badge ${p.enabled ? 'badge-confirmed' : 'badge-cancelled'}`}>
                            {p.enabled ? 'Active' : 'Disabled'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '20px' }}>
                  <button className="btn btn-secondary" onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={page === 0} style={{ fontSize: '13px', padding: '6px 14px' }}>
                    ← Prev
                  </button>
                  <span style={{
                    padding: '6px 14px', background: 'var(--bg-secondary)',
                    border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)',
                    fontSize: '13px', color: 'var(--text-secondary)',
                  }}>
                    Page {page + 1} of {totalPages}
                  </span>
                  <button className="btn btn-secondary" onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1} style={{ fontSize: '13px', padding: '6px 14px' }}>
                    Next →
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </Layout>
  );
}
