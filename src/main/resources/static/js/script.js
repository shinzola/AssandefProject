document.addEventListener("DOMContentLoaded", function () {
  const navbar = document.getElementById("navbar");
  if (navbar) {
    function handleScroll() {
      if (window.scrollY > 50) {
        navbar.classList.add("scrolled");
      } else {
        navbar.classList.remove("scrolled");
      }
    }
    window.addEventListener("scroll", handleScroll);
  }
});

document.addEventListener("DOMContentLoaded", function () {
  const navbar = document.getElementById("navbar");
  if (navbar) {
    window.addEventListener("scroll", () => {
      if (window.scrollY > 50) navbar.classList.add("scrolled");
      else navbar.classList.remove("scrolled");
    });
  }
});


