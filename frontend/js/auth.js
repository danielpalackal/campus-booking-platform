async function checkAuth() {
  try {
    const me = await API.me();
    document.querySelectorAll('[data-auth]').forEach(el => el.textContent = me.name + ' (' + me.role + ')');
    const role = me.role;
    document.querySelectorAll('[data-role]').forEach(el => {
      const need = el.getAttribute('data-role');
      if (need === 'TEACHER' && !(role === 'TEACHER' || role === 'SUPERADMIN')) el.classList.add('hidden');
      if (need === 'SUPERADMIN' && role !== 'SUPERADMIN') el.classList.add('hidden');
    });
    return me;
  } catch {
    if (!location.pathname.endsWith('index.html') && location.pathname !== '/') location.href = '/index.html';
    return null;
  }
}
async function doDevLogin(email) {
  await API.devLogin(email);
  location.href = '/pages/browse.html';
}
async function doLogout() {
  await API.logout();
  location.href = '/index.html';
}
