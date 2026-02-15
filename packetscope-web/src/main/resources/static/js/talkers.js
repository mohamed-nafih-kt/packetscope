async function refresh() {
    try{
        const res = await fetch('/talkers');
        if (!res.ok) return;

        const data = await res.json();

        const tbody = document.querySelector('#talkers tbody');
        tbody.innerHTML = '';

        data.forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${r.ip}</td>
                <td>${r.bytes_sent}</td>
                <td>${r.packets}</td>
            `;
            tbody.appendChild(tr);
        });
        } catch(e) {
               console.error(e);
        }
}

refresh();
setInterval(refresh, 2000);