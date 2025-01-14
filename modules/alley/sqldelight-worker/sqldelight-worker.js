import sqlite3InitModule from "@sqlite.org/sqlite-wasm";

const mutableDatabasePath = "alleyUser.sqlite"
const readOnlyInputPath = "composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/database.sqlite"
const readOnlyDatabasePath = "alleyArtist.sqlite"

let db = null;
async function createDatabase() {
    const sqlite3 = await sqlite3InitModule({ print: console.log, printErr: console.error });
    console.log("Running SQLite3 version", sqlite3.version.libVersion);
    const response = await fetch(readOnlyInputPath);
    const fileBuffer = await response.arrayBuffer();
    if (sqlite3.oo1.OpfsDb) {
        await sqlite3.oo1.OpfsDb.importDb(readOnlyDatabasePath, fileBuffer)
        db = new sqlite3.oo1.OpfsDb(mutableDatabasePath, "c");
        db.exec("ATTACH DATABASE 'file:" + readOnlyDatabasePath + "?vfs=opfs&immutable=1' AS readOnly;");
    } else {
        console.log("OPFS not initialized");
        db = new sqlite3.oo1.Db(new Uint8Array(fileBuffer));
    }
}

function handleMessage() {
    const data = this.data;

    switch (data && data.action) {
        case "exec":
            if (!data["sql"]) {
                throw new Error("exec: Missing query string");
            }

            return postMessage({
                id: data.id,
                results: { values: db.exec({ sql: data.sql, bind: data.params, returnValue: "resultRows" }) },
            })
        case "begin_transaction":
            return postMessage({
                id: data.id,
                results: db.exec("BEGIN TRANSACTION;"),
            })
        case "end_transaction":
            return postMessage({
                id: data.id,
                results: db.exec("END TRANSACTION;"),
            })
        case "rollback_transaction":
            return postMessage({
                id: data.id,
                results: db.exec("ROLLBACK TRANSACTION;"),
            })
        default:
            console.log(`Unsupported action: ${data && data.action}`)
            throw new Error(`Unsupported action: ${data && data.action}`);
    }
}

function handleError(err) {
    return postMessage({
        id: this.data.id,
        error: err,
    });
}

if (typeof importScripts === "function") {
    db = null;
    const sqlModuleReady = createDatabase();
    self.onmessage = (event) => {
        return sqlModuleReady
            .then(handleMessage.bind(event))
            .catch(handleError.bind(event));
    }
}
