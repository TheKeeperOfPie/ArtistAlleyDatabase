if ("serviceWorker" in navigator) {
    console.log("Registering worker.js")
    navigator.serviceWorker.register("worker.js")
        .then(reg => console.log("worker.js success", reg))
        .catch(err => console.error("worker.js failed", err))
}
