async function refresh() {
    const res = await fetch('/flows/active');
    const data = await res.json();

    const tbody = document.querySelector('#flows tbody');
    tbody.innerHTML = '';

    data.forEach(r => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${r.protocol}</td>
            <td>${r.ep1}</td>
            <td>${r.ep2}</td>
            <td>${r.packet_count}</td>
            <td>${r.total_bytes}</td>
            <td>${r.first_seen}</td>
            <td>${r.last_seen}</td>
        `;
        tbody.appendChild(tr);
    });
}

refresh();
setInterval(refresh, 2000);