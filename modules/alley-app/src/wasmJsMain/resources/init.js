class SkipWaitingBridge {
    #composeCallback = null;
    #newWorker = null;

    onComposeReady(callback) {
        this.#composeCallback = callback;
        this.#onChange();
        return;
    }

    onNewWorkerReady(worker) {
        this.#newWorker = worker;
        this.#onChange();
        return;
    }

    skipWaiting() {
        if (this.#newWorker !== null) {
            this.#newWorker.postMessage("SKIP_WAITING");
        }
        return;
    }

    #onChange() {
        if (this.#composeCallback !== null && this.#newWorker !== null) {
            this.#composeCallback();
        }
        return;
    }
}

const globalSkipWaitingBridge = new SkipWaitingBridge();

window.addEventListener("beforeinstallprompt", event => {
  window.deferredInstallPrompt = event;
});

if ("serviceWorker" in navigator) {
    console.log("Registering serviceWorker.js")
    // TODO: Automatically call update()?
    navigator.serviceWorker.register("serviceWorker.js")
        .then(registration => {
            console.log("serviceWorker.js success", registration);
            const installingWorker = registration.installing;
            const waitingWorker = registration.waiting;
            if (installingWorker !== null) {
                console.log("installingWorker ready to take over");
                installingWorker.onstatechange = function() {
                    if (installingWorker.state === "activated") {
                        console.log("installingWorker activated, reloading");
                        installingWorker.onstatechange = null;
                        window.location.reload();
                    }
                }
                globalSkipWaitingBridge.onNewWorkerReady(installingWorker);
            } else if (waitingWorker !== null) {
                console.log("waitingWorker ready to take over");
                waitingWorker.onstatechange = function() {
                    if (waitingWorker.state === "activated") {
                        console.log("waitingWorker activated, reloading");
                        waitingWorker.onstatechange = null;
                        window.location.reload();
                    }
                }
                globalSkipWaitingBridge.onNewWorkerReady(waitingWorker);
            } else {
                registration.addEventListener("updatefound", () => {
                    const newWorker = registration.installing;
                    const oldWorker = registration.active;
                    console.log("new serviceWorker.js install", newWorker);
                    newWorker.onstatechange = function() {
                        console.log("new serviceWorker.js state", newWorker.state);
                        if (newWorker.state === "installed" || newWorker.state === "waiting") {
                            console.log("new serviceWorker.js ready to take over");
                            globalSkipWaitingBridge.onNewWorkerReady(newWorker);
                        } else if (newWorker.state === "activated") {
                            console.log("new serviceWorker.js activated, reloading");
                            newWorker.onstatechange = null;
                            window.location.reload();
                        }
                    }
                });
            }
        })
        .catch(error => console.error("serviceWorker.js failed", error))
}
