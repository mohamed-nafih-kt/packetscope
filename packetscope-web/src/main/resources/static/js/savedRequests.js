/* ============================================================
   Saved Requests Inspector
   ============================================================ */

let savedRequests = [];
let selected = null;

const list = document.getElementById("requests");
const search = document.getElementById("search");

const detailGeneral = document.getElementById("detail-general");
const detailHeaders = document.getElementById("detail-headers");
const detailBody = document.getElementById("detail-body");
const detailResponse = document.getElementById("detail-response");

const replayBtn = document.getElementById("replay");
const deleteBtn = document.getElementById("delete");

/* ============================================================
   INITIAL LOAD
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {
    loadSaved();
});

/* ============================================================
   FETCH SAVED REQUESTS
   ============================================================ */

async function loadSaved() {
    try {
        const res = await fetch("/api/transactions/history");
        savedRequests = await res.json();
        renderList(savedRequests);
    } catch (e) {
        console.error("Failed loading saved requests", e);
    }
}

/* ============================================================
   RENDER LIST
   ============================================================ */

function renderList(data) {
    list.innerHTML = "";

    data.forEach(req => {

        const li = document.createElement("li");
        li.className = "request-item";

        li.innerHTML = `
            <span class="method ${req.method}">${req.method}</span>
            <span class="url">${req.url}</span>
            <span class="time">${formatTime(req.created_at)}</span>
        `;

        li.addEventListener("click", () => select(req, li));

        list.appendChild(li);
    });
}

/* ============================================================
   SELECT REQUEST
   ============================================================ */

function select(req, element) {

    document.querySelectorAll(".request-item")
        .forEach(i => i.classList.remove("active"));

    element.classList.add("active");

    selected = req;

    detailGeneral.textContent = JSON.stringify({
        id: req.id,
        method: req.method,
        url: req.url,
        created_at: req.created_at
    }, null, 2);

    detailHeaders.textContent = JSON.stringify(req.request_headers || {}, null, 2);
    detailBody.textContent = req.request_body || "(empty)";
    detailResponse.textContent = req.response_body || "(empty)";
}

/* ============================================================
   SEARCH
   ============================================================ */

search.addEventListener("input", () => {

    const q = search.value.toLowerCase();

    const filtered = savedRequests.filter(r =>
        r.url.toLowerCase().includes(q) ||
        r.method.toLowerCase().includes(q)
    );

    renderList(filtered);
});

/* ============================================================
   REPLAY
   ============================================================ */

replayBtn.addEventListener("click", async () => {

    if (!selected) return alert("Select a request first");

    // Store transaction temporarily
    sessionStorage.setItem("replayRequest", JSON.stringify(selected));

    // Navigate to New Request tab/page
    window.parent.load("new-request.html");

});

/* ============================================================
   DELETE
   ============================================================ */

deleteBtn.addEventListener("click", async () => {

    if (!selected) return alert("Select a request first");

    if (!confirm("Delete this transaction?")) return;

    await fetch(`/api/transactions/${selected.id}`, {
        method: "DELETE"
    });

    selected = null;
    loadSaved();
});

/* ============================================================
   UTIL
   ============================================================ */

function formatTime(ts) {
    if (!ts) return "";
    return new Date(ts).toLocaleTimeString();
}
