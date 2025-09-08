document.querySelectorAll(".response-tabs .tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    // Remove active from all
    document
      .querySelectorAll(".response-tabs .tab")
      .forEach((t) => t.classList.remove("active"));
    document
      .querySelectorAll(".tab-pane")
      .forEach((p) => p.classList.remove("active"));

    // Activate selected
    tab.classList.add("active");
    document.getElementById(tab.dataset.target).classList.add("active");
  });
});

document.addEventListener("DOMContentLoaded", () => {
  const sendBtn = document.querySelector(".search-box button");

  sendBtn.addEventListener("click", async () => {
    const method = document.getElementById("http-method").value;
    const url = document.querySelector(".search-box input").value;
    const body = document.getElementById("request-body").value;

    // Collect headers
    const headerRows = document.querySelectorAll("#headers .header-row");
    const headers = {};
    headerRows.forEach(row => {
      const key = row.querySelector("input[placeholder='Header Name']").value;
      const value = row.querySelector("input[placeholder='Header Value']").value;
      if (key) headers[key] = value;
    });

    // Build request DTO
    const requestDto = {
      method,
      url,
      headers,
      body
    };

    try {
      const response = await fetch("/api/transactions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(requestDto)
      });

      if (!response.ok) {
        throw new Error("Server error: " + response.status);
      }

      const data = await response.json();

      // Update response panel
      document.querySelector("#response-results pre").textContent =
        JSON.stringify(data, null, 2);

      document.querySelector("#response-body pre").textContent =
        data.body || "(empty)";

      document.querySelector("#response-headers pre").textContent =
        JSON.stringify(data.headers, null, 2);

      // You could add cookies later
    } catch (err) {
      console.error(err);
      document.querySelector("#response-results pre").textContent =
        "Error: " + err.message;
    }
  });
});
