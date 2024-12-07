const toggler = document.querySelector(".toggler-btn");
if (toggler != null) {
    toggler.addEventListener("click", function(){
        document.querySelector("#sidebar").classList.toggle("collapsed")
    });
}

function timeWriter(t) {
    var h = Math.floor(t % (60 * 60 * 24) / (60 * 60));
    var m = Math.floor(t % (60 * 60) / 60);
    var s = Math.floor(t % 60);
    return (h > 0 ? h + ':' : '') + (m >= 10 ? m : '0' + m) + ':' + (s >= 10 ? s : '0' + s);
}

window.addEventListener("pageshow", event => {
    var historyTraversal = event.persisted || (typeof window.performance != "undefined" && window.performance.navigation.type === 2);
    if (historyTraversal) {
        window.location.reload();
    }
});