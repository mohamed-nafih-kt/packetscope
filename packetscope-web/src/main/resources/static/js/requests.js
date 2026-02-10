// ================== dynamic loading ==================
function load(page, btn) {
  document.getElementById("view").src = page;

  document
    .querySelectorAll(".grid div")
    .forEach((b) => b.classList.remove("active"));

  btn.classList.add("active");
}