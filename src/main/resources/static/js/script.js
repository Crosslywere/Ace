const toggler = document.querySelector(".toggler-btn");
if (toggler != null) {
    toggler.addEventListener("click", function(){
        document.querySelector("#sidebar").classList.toggle("collapsed")
    });
}
