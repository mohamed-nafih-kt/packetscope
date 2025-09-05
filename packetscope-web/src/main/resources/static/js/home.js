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
