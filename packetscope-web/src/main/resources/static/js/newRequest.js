/* ============================================================
   1. GLOBAL SELECTORS & STATE
   ============================================================ */
const wrapper = document.getElementById("method-select");
const trigger = document.getElementById("method-trigger");
const value = document.getElementById("method-value");
const notificationBar = document.getElementById("notification");
let notificationTimer;

// Global state to store the most recent successful request/response
window.lastTransactionRequest = null;
window.lastTransactionResponse = null;


/* ============================================================
   2. UI HELPER FUNCTIONS
   ============================================================ */

/**
 * Displays a temporary notification bar at the top of the screen
 */
function showNotification(msg, type = 'info') {
  clearTimeout(notificationTimer);

  notificationBar.innerHTML = `<p>${msg}</p>`;
  notificationBar.classList.remove('error', 'success', 'info', 'hidden');
  notificationBar.classList.add(type);

  notificationTimer = setTimeout(() => {
    notificationBar.classList.add("hidden");
  }, 3000);
}

/**
 * Extracts data from the UI to create a Request DTO
 * @returns {Object} { method, url, headers, body }
 */
function buildRequestDto() {
  const method = document.getElementById("method-value").textContent;
  let url = document.getElementById("request-url").value.trim();
  const body = document.getElementById("request-body").value;

  // 1. Collect query params from dynamic rows
  const paramRows = document.querySelectorAll("#query-params .param-row");
  const params = new URLSearchParams();
  paramRows.forEach((row) => {
    const key = row.querySelector("input[placeholder='Key']").value;
    const val = row.querySelector("input[placeholder='Value']").value;
    if (key) params.append(key, val);
  });

  // Append params to URL if they exist
  if ([...params].length > 0) {
    url += (url.includes("?") ? "&" : "?") + params.toString();
  }

  // 2. Collect headers from dynamic rows
  const headerRows = document.querySelectorAll("#headers .header-row");
  const headers = {};
  headerRows.forEach((row) => {
    const key = row.querySelector("input[placeholder='Header Name']").value;
    const val = row.querySelector("input[placeholder='Header Value']").value;
    if (key) headers[key] = val;
  });

  return { method, url, headers, body };
}


/* ============================================================
   3. EVENT LISTENERS & INITIALIZATION
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {

  // Element References
  const sendBtn = document.getElementById("send-request");
  const addParamBtn = document.getElementById("add-param");
  const addHeaderBtn = document.getElementById("add-header");
  const saveBtn = document.getElementById("save-request");

  /* --- Dropdown Logic --- */
  trigger.addEventListener("click", () => {
    wrapper.classList.toggle("open");
  });

  wrapper.querySelectorAll(".option").forEach(opt => {
    opt.addEventListener("click", () => {
      value.textContent = opt.textContent;
      wrapper.classList.remove("open");
    });
  });

  // Close dropdown when clicking outside
  document.addEventListener("click", (e) => {
    if (!wrapper.contains(e.target)) {
      wrapper.classList.remove("open");
    }
  });

  /* --- Tab Switching Logic --- */
  document.querySelectorAll(".response-tabs .tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      // Clear active states
      document.querySelectorAll(".response-tabs .tab").forEach(t => t.classList.remove("active"));
      document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));

      // Set current active
      tab.classList.add("active");
      const targetPane = document.getElementById(tab.dataset.target);
      if (targetPane) targetPane.classList.add("active");
    });
  });

  /* --- Dynamic Row Management (Params & Headers) --- */

  // Add Query Param Row
  addParamBtn.addEventListener("click", () => {
    const div = document.createElement("div");
    div.className = "param-row";
    div.innerHTML = `
      <input class="parameter" type="text" placeholder="Key" />
      <input class="parameter" type="text" placeholder="Value" />
      <button class="remove-param">X</button>
    `;
    document.getElementById("query-params").insertBefore(div, addParamBtn);
    div.querySelector(".remove-param").addEventListener("click", () => div.remove());
  });

  // Add Header Row
  addHeaderBtn.addEventListener("click", () => {
    const div = document.createElement("div");
    div.className = "header-row";
    div.innerHTML = `
      <input class="parameter" type="text" placeholder="Header Name" />
      <input class="parameter" type="text" placeholder="Header Value" />
      <button class="remove-header">X</button>
    `;
    document.getElementById("headers").insertBefore(div, addHeaderBtn);
    div.querySelector(".remove-header").addEventListener("click", () => div.remove());
  });

  // Delegate initial removal buttons
  document.querySelectorAll(".remove-param, .remove-header").forEach((btn) => {
    btn.addEventListener("click", (e) => e.target.parentElement.remove());
  });


  /* ============================================================
     4. API EXECUTION & PERSISTENCE
     ============================================================ */

  /**
   * SEND REQUEST: Executes the API call via the proxy/transaction endpoint
   */
  sendBtn.addEventListener("click", async () => {
    const requestDto = buildRequestDto();

    if (!requestDto.url) {
      showNotification("Please enter a URL", "error");
      return;
    }

    try {
      const response = await fetch("/api/transactions/execute", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestDto),
      });

      showNotification("Request sent", "info");

      if (!response.ok){
            const errorMsg = `Server returned ${response.status}: ${response.statusText}`;
            showNotification(errorMsg, "error");
            return;
       }

      const data = await response.json();
      showNotification("Request sent", "success");

      // Cache for the Save button logic
      window.lastTransactionRequest = requestDto;
      window.lastTransactionResponse = data;

      // Update Result UI
      document.getElementById("result-status").textContent = `Status: ${data.status}`;
      document.getElementById("result-body").textContent = data.body || "(empty)";
      document.getElementById("result-headers").textContent = JSON.stringify(data.headers || {}, null, 2);
      document.getElementById("result-cookies").textContent = JSON.stringify(data.cookies || {}, null, 2);

    } catch (error) {
      console.error("Request failed:", error);
      showNotification(error.message, "error");
      document.getElementById("result-status").textContent = `Error: ${error.message}`;
    }
  });

  /**
   * SAVE REQUEST: Persists the last executed request to the database
   */
  saveBtn.addEventListener("click", async () => {
    if (!window.lastTransactionRequest) {
      alert("Please run the request first before saving!");
      return;
    }

    try {
      const response = await fetch("/api/transactions/save", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(window.lastTransactionRequest),
      });

      if (!response.ok) throw new Error("Save failed: " + response.status);

      const message = await response.text();
      showNotification("Transaction saved to database", "success");
    } catch (err) {
      showNotification(err.message, "error");
    }
  });

});