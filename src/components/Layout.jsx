import React from 'react';
import Sidebar from './Sidebar';

export default function Layout({ children }) {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{
        flex: 1,
        marginLeft: '240px',
        padding: '32px',
        transition: 'margin-left 0.3s ease',
        maxWidth: 'calc(100vw - 240px)',
      }}>
        {children}
      </main>
    </div>
  );
}
