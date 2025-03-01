if ("serviceWorker" in navigator) {
    console.log("Registering serviceWorker.js")
    // TODO: Automatically call update()?
    navigator.serviceWorker.register("serviceWorker.js")
        .then(registration => console.log("serviceWorker.js success", registration))
        .catch(error => console.error("serviceWorker.js failed", error))
}
