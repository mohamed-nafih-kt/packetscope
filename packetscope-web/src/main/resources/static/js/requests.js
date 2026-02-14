// ================== dynamic loading ==================
function load(page, btn) {
  document.getElementById("view").src = page;

  document
    .querySelectorAll(".tile")
    .forEach((b) => b.classList.remove("active"));

  if (btn) {
    btn.classList.add("active");
  }
}