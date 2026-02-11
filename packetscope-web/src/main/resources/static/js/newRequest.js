// Dropdown request method
const wrapper = document.getElementById("method-select");
const trigger = document.getElementById("method-trigger");
const value = document.getElementById("method-value");
const notificationBar = document.getElementById("notification");
let notificationTimer;

// show notification
function showNotification(msg, type = 'info') {
  clearTimeout(notificationTimer);

  notificationBar.innerHTML = `<p>${msg}</p>`;

  notificationBar.classList.remove('error', 'success', 'info', 'hidden');
  notificationBar.classList.add(type);

  notificationTimer = setTimeout(() => {
    notificationBar.classList.add("hidden");
  }, 3000);
}

trigger.addEventListener("click", () => {
    wrapper.classList.toggle("open");
});

wrapper.querySelectorAll(".option").forEach(opt => {
    opt.addEventListener("click", () => {
        value.textContent = opt.textContent;
        wrapper.classList.remove("open");
    });
});

/* close when clicking outside */
document.addEventListener("click", e => {
    if (!wrapper.contains(e.target)) {
        wrapper.classList.remove("open");
    }
});

// Tab switching
document.querySelectorAll(".response-tabs .tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".response-tabs .tab")
      .forEach((t) => t.classList.remove("active"));
    document.querySelectorAll(".tab-pane")
      .forEach((p) => p.classList.remove("active"));

    tab.classList.add("active");
    document.getElementById(tab.dataset.target).classList.add("active");
  });
});

/* ====================== DOM Content Loader ====================== */
document.addEventListener("DOMContentLoaded", () => {
  const sendBtn = document.getElementById("send-request");
  const addParamBtn = document.getElementById("add-param");
  const addHeaderBtn = document.getElementById("add-header");
  const saveBtn = document.getElementById("save-request");

  // Helper: build Request DTO
  function buildRequestDto() {
    const method = document.getElementById("http-method").textContent;
    let url = document.getElementById("request-url").value.trim();
    const body = document.getElementById("request-body").value;

    // Collect query params
    const paramRows = document.querySelectorAll("#query-params .param-row");
    const params = new URLSearchParams();
    paramRows.forEach((row) => {
      const key = row.querySelector("input[placeholder='Key']").value;
      const value = row.querySelector("input[placeholder='Value']").value;
      if (key) params.append(key, value);
    });
    if ([...params].length > 0) {
      url += (url.includes("?") ? "&" : "?") + params.toString();
    }

    // Collect headers
    const headerRows = document.querySelectorAll("#headers .header-row");
    const headers = {};
    headerRows.forEach((row) => {
      const key = row.querySelector("input[placeholder='Header Name']").value;
      const value = row.querySelector("input[placeholder='Header Value']").value;
      if (key) headers[key] = value;
    });

    return { method, url, headers, body };
  }


  /* ======================   Dynamic Parameters Add/Remove ====================== */
  // Dynamic add parameters rows
  addParamBtn.addEventListener("click", () => {
    const div = document.createElement("div");
    div.className = "param-row";
    div.innerHTML = `
      <input class="parameter" type="text" placeholder="Key" />
      <input  class="parameter" type="text" placeholder="Value" />
      <button class="remove-param">X</button>
    `;
    document.getElementById("query-params").insertBefore(div, addParamBtn);
    div.querySelector(".remove-param").addEventListener("click", () =>
      div.remove()
    );
  });

  // Dynamic add header rows
  addHeaderBtn.addEventListener("click", () => {
    const div = document.createElement("div");
    div.className = "header-row";
    div.innerHTML = `
      <input class="parameter" type="text" placeholder="Header Name" />
      <input class="parameter" type="text" placeholder="Header Value" />
      <button class="remove-header">X</button>
    `;
    document.getElementById("headers").insertBefore(div, addHeaderBtn);
    div.querySelector(".remove-header").addEventListener("click", () =>
      div.remove()
    );
  });

  // Initial remove buttons
  document.querySelectorAll(".remove-param").forEach((btn) => {
    btn.addEventListener("click", (e) => e.target.parentElement.remove());
  });
  document.querySelectorAll(".remove-header").forEach((btn) => {
    btn.addEventListener("click", (e) => e.target.parentElement.remove());
  });

  /* ======================   Send Request ====================== */

  sendBtn.addEventListener("click", async () => {
    try{
    const requestDto = buildRequestDto();
    }catch(e){
        showNotification("error: "+e.message, 'error');
    }

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

      if (!response.ok) throw new Error("Server error: " + response.status);

      const data = await response.json();

      // Save preview result globally so we can "Save" later
      window.lastTransactionRequest = requestDto;
      window.lastTransactionResponse = data;

      // Update UI
      document.getElementById("result-status").textContent =
        "Status: " + data.status;
      document.getElementById("result-body").textContent =
        data.body || "(empty)";
      document.getElementById("result-headers").textContent =
        JSON.stringify(data.headers || {}, null, 2);
      document.getElementById("result-cookies").textContent =
        JSON.stringify(data.cookies || {}, null, 2);
    } catch (err) {
      console.error(err);
      document.getElementById("result-status").textContent =
        "Error: " + err.message;
    }
  });

  /* ====================== Save Button (explicit persistence) ====================== */

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

      const data = await response.text();
      alert(data); // "Transaction saved successfully!"
    } catch (err) {
      alert("Error saving transaction: " + err.message);
    }
  });
});
