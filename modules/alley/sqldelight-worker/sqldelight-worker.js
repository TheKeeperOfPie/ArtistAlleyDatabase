import sqlite3InitModule from "@sqlite.org/sqlite-wasm";

const mutableDatabasePath = "alleyUser.sqlite"
const readOnlyInputPath = "composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/database.sqlite"
const readOnlyInputHashPath = "composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/databaseHash.txt"
const readOnlyDatabasePath = "alleyArtist.sqlite"
const readOnlyDatabaseHashPath = "alleyArtistHash.txt"

let db = null;
async function createDatabase() {
    const sqlite3 = await sqlite3InitModule({ print: console.log, printErr: console.error });

    let opfsRoot;
    let forceNoOpfs;
    try {
        forceNoOpfs = localStorage.getItem("forceNoOpfs");
    } catch (error) {
    }

    try {
        if (forceNoOpfs != "true" && sqlite3.oo1.OpfsDb) {
            opfsRoot = await navigator.storage.getDirectory();
        }
    } catch(error) {
        console.log("Failed to load OPFS");
    }

    if (opfsRoot) {
        const hashFile = await opfsRoot.getFileHandle(readOnlyDatabaseHashPath, { create: true });
        const hashHandle = await hashFile.createSyncAccessHandle();
        const oldSize = hashHandle.getSize();
        let oldHash = ""
        if (oldSize > 0) {
            const oldHashDataView = new DataView(new ArrayBuffer(hashHandle.getSize()));
            hashHandle.read(oldHashDataView);
            const textDecoder = new TextDecoder();
            oldHash = textDecoder.decode(oldHashDataView);
        }

        const newHashResponse = await fetch(readOnlyInputHashPath);
        const newHash = await newHashResponse.text();

        console.log("Old database hash", oldHash);
        console.log("New database hash", newHash);

        if (oldHash != newHash) {
            console.log("Importing new database")
            const response = await fetch(readOnlyInputPath);
            const fileBuffer = await response.arrayBuffer();
            await sqlite3.oo1.OpfsDb.importDb(readOnlyDatabasePath, fileBuffer);
            const textEncoder = new TextEncoder();
            const encoded = textEncoder.encode(newHash);
            hashHandle.truncate(0);
            hashHandle.write(encoded);
            hashHandle.flush();
        }
        hashHandle.close();

        db = new sqlite3.oo1.OpfsDb(mutableDatabasePath, "c");
        db.exec("ATTACH DATABASE 'file:" + readOnlyDatabasePath + "?vfs=opfs&immutable=1' AS readOnly;");
    } else {
        // TODO: Show warning that persistence is not supported for favorites (or just disable)
        console.log("OPFS not initialized");
        db = new sqlite3.oo1.DB();
        const response = await fetch(readOnlyInputPath);
        const fileBuffer = await response.arrayBuffer();
        const data = sqlite3.wasm.allocFromTypedArray(fileBuffer);
        const rc = sqlite3.capi.sqlite3_deserialize(
          db.pointer, 'main', data, fileBuffer.byteLength, fileBuffer.byteLength,
          sqlite3.capi.SQLITE_DESERIALIZE_FREEONCLOSE | sqlite3.capi.SQLITE_DESERIALIZE_RESIZEABLE
        );
        db.checkRc(rc);
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
