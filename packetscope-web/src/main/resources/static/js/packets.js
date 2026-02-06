let lastCapturedAt = null;
let lastPacketId = null;

async function refresh() {

const from = new Date(Date.now() - 60000).toISOString();

let url = `/packets?from=${from}&limit=200`;

if (lastCapturedAt && lastPacketId) {
    url += `&lastCapturedAt=${lastCapturedAt}&lastPacketId=${lastPacketId}`;
}

const res = await fetch(url);
const data = await res.json();

if (!data.length) return;

const tbody = document.querySelector('#packets tbody');

data.forEach(p => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>${p.capturedAt}</td>
        <td>${p.sourceIp}</td>
        <td>${p.destinationIp}</td>
        <td>${p.protocol}</td>
        <td>${p.sourcePort ?? ""}</td>
        <td>${p.destinationPort ?? ""}</td>
        <td>${p.packetSize}</td>
        <td>${p.interfaceName}</td>
        <td>${p.direction}</td>
    `;
    tbody.prepend(tr);

    lastCapturedAt = p.capturedAt;
    lastPacketId = p.packetId;
});

// keep table bounded
while (tbody.children.length > 300) {
    tbody.removeChild(tbody.lastChild);
}
}

refresh();
setInterval(refresh, 2000);