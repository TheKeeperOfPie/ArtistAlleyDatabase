if ("serviceWorker" in navigator) {
    console.log("Registering worker.js")
    navigator.serviceWorker.register("worker.js")
        .then(registration => {
            console.log("worker.js success", registration);
            registration.update();
        })
        .catch(error => console.error("worker.js failed", error))
}
