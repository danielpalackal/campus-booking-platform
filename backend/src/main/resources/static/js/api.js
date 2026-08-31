const API = {
  async req(path, opts = {}) {
    const r = await fetch(path, { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(opts.headers||{}) }, ...opts });
    const text = await r.text();
    let data; try { data = text ? JSON.parse(text) : {}; } catch { data = { raw: text }; }
    if (!r.ok) throw new Error(data.error || data.message || r.statusText);
    return data;
  },
  me() { return this.req('/api/auth/me'); },
  logout() { return this.req('/api/auth/logout', { method: 'POST' }); },
  devLogin(email, password) { return this.req('/api/auth/dev-login', { method: 'POST', body: JSON.stringify({ email, password }) }); },
  googleLogin(idToken) { return this.req('/api/auth/google', { method: 'POST', body: JSON.stringify({ idToken }) }); },
  rooms(params={}) {
    const q = new URLSearchParams(params).toString();
    return this.req('/api/rooms' + (q ? '?' + q : ''));
  },
  availability(roomId, date) { return this.req(`/api/rooms/${roomId}/availability?date=${date}`); },
  pendingCount(roomId, date, slot) { return this.req(`/api/rooms/${roomId}/pending-count?date=${date}&slot=${slot}`); },
  book(roomId, bookingDate, startTime, purpose) {
    return this.req('/api/bookings', { method: 'POST', body: JSON.stringify({ roomId, bookingDate, startTime, purpose }) });
  },
  myBookings() { return this.req('/api/bookings/me'); },
  cancel(id) { return this.req(`/api/bookings/${id}`, { method: 'DELETE' }); },
  pendingQueue() { return this.req('/api/bookings/pending'); },
  approve(id) { return this.req(`/api/bookings/${id}/approve`, { method: 'PUT' }); },
  reject(id, reason) { return this.req(`/api/bookings/${id}/reject`, { method: 'PUT', body: JSON.stringify({ reason }) }); },
  adminUsers() { return this.req('/api/admin/users'); },
  adminAdd(email, name, role, password) { return this.req('/api/admin/users', { method: 'POST', body: JSON.stringify({ email, name, role, password }) }); },
  adminRemove(id) { return this.req(`/api/admin/users/${id}`, { method: 'DELETE' }); }
};
