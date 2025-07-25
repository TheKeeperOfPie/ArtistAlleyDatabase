type AppsScriptEvent = GoogleAppsScript.Events.AppsScriptEvent
type DriveFile = GoogleAppsScript.Drive.File
type Folder = GoogleAppsScript.Drive.Folder
type SheetsOnEdit = GoogleAppsScript.Events.SheetsOnEdit
type SheetsOnOpen = GoogleAppsScript.Events.SheetsOnOpen
type Spreadsheet = GoogleAppsScript.Spreadsheet.Spreadsheet
type Sheet = GoogleAppsScript.Spreadsheet.Sheet
type SheetRange = GoogleAppsScript.Spreadsheet.Range

const SEPARATOR = ", "
const scriptProperties = PropertiesService.getScriptProperties()
const ROOT_FOLDER_ID = scriptProperties.getProperty("rootFolderId")!!
const STAMP_RALLY_ROOT_FOLDER_ID = scriptProperties.getProperty("stampRallyFolderId")!!

const DOMAINS = [".com", ".ee", ".app", ".ca", ".site", ".gg", ".social"]
const SHOP_DOMAINS = ["storenvy.com", "bigcartel.com", "etsy.com", "inprnt.com",
    "myshopify.com", "threadless.com", "itch.io", "faire.com", "gumroad.com",
    "gallerynucleus.com", "redbubble.com"]
const COMMISSION_DOMAINS = ["vgen.co"]

function onOpen(event: SheetsOnOpen) {
    SpreadsheetApp.getUi()
        .createMenu("Actions")
        .addItem("Fix artist links", "fixArtistLinks")
        .addItem("Fix catalog images", "fixCatalogImageRows")
        .addItem("Fix stamp rally images", "fixStampRallyImageRows")
        .addToUi()
}

function onEdit(event: SheetsOnEdit) {
    const range = event.range
    const sheet = range.getSheet()
    const row = range.getRow()
    const validationColumn = findColumnForHeader(sheet, "Validation")
    const unlockAll = sheet.getRange(2, validationColumn).getValue() == "UNLOCK_ALL"
    Logger.log(`unlockAll = ${unlockAll}`)
    Logger.log(`getNumRows = ${range.getNumRows()}`)
    Logger.log(`getNumColumns = ${range.getNumColumns()}`)
    if (sheet.getName() == "Artists" && !unlockAll) {
        if (range.getNumRows() != 1 || range.getNumColumns() != 1) {
            SpreadsheetApp.getUi().alert(`CAUTION: Edited multiple rows without UNLOCK_ALL, cannot rollback`)
            return
        } else if (validationColumn != range.getColumn() &&
            findValueForHeader(sheet, row, "Validation") != "UNLOCKED"
        ) {
            range.setValue(event.oldValue)
            SpreadsheetApp.getUi().alert(`Row ${row} was not UNLOCKED`)
            return
        }
    }

    onArbitraryLinkInput(event)
    multiSelect(event)
}

function onArbitraryLinkInput(event: SheetsOnEdit) {
    const range = event.range
    if (range.getNumRows() != 1 || range.getNumColumns() != 1) return

    const sheet = range.getSheet()
    const inputColumn = findColumnForHeader(sheet, "Input")
    const column = range.getColumn()
    if (column != inputColumn) return

    const input = event.value.replace("http://", "https://")
    let targetColumn = findColumnForHeader(sheet, "Links")!!
    if (SHOP_DOMAINS.some(domain => input.indexOf(domain) > 0)) {
        targetColumn = findColumnForHeader(sheet, "Store")!!
    }
    if (COMMISSION_DOMAINS.some(domain => input.indexOf(domain) > 0)) {
        targetColumn = findColumnForHeader(sheet, "Commissions")!!
    }

    const row = range.getRow()
    const linksRange = sheet.getRange(row, targetColumn, 1, 1)
    const linksText = linksRange.getValue()
    Logger.log(`input = ${input}`)

    const domainEndIndex = findDomainEnd(input)
    if (domainEndIndex < 0) {        
        SpreadsheetApp.getUi().alert(`TLD unsupported`)
        return
    }

    const domainPart = input.substring(0, domainEndIndex)
        .replace("twitter.com", "x.com")

    let pathEndIndex = input.indexOf("?")
    if (pathEndIndex <= 0) {
        pathEndIndex = input.length
    }

    let pathPart = input.substring(domainEndIndex, pathEndIndex)
    if (pathPart.lastIndexOf("/") == pathPart.length - 1) {
        pathPart = pathPart.substring(0, pathPart.length - 1)
    }

    const canonicalLink = domainPart + pathPart

    if (linksText == undefined || linksText.length == 0) {
        linksRange.setValue(canonicalLink)
        range.setValue("")
        return
    }

    const links = linksText.split("\n")
    if (links.includes(canonicalLink)) {
        SpreadsheetApp.getUi().alert(`Row ${row} already has link ${canonicalLink}`)
        range.setValue("")
        return
    }

    links.push(canonicalLink)
    links.sort()
    Logger.log(`Setting new links = ${links}`)

    fixLinks(linksRange, links.join("\n"))
    range.setValue("")
    sheet.autoResizeRows(row, 1)
}

function findDomainEnd(input: string): number {
    let index = -1
    DOMAINS.forEach(domain => {
        if (index < 6) {
            index = input.indexOf(domain) + domain.length
        }
    })
    return index
}

function multiSelect(event: SheetsOnEdit) {
    const range = event.range

    const validationRule = range.getDataValidation()
    if (!validationRule) return

    const criteriaType = validationRule.getCriteriaType()
    if (criteriaType !== SpreadsheetApp.DataValidationCriteria.VALUE_IN_RANGE) return

    const oldValue = event.oldValue
    const newValue = event.value
    Logger.log(`newValue = ${newValue}`)
    Logger.log(`oldValue = ${oldValue}`)
    if (event.oldValue == newValue) {
        range.setValue("")
        return
    }

    if (newValue == undefined || newValue.length == 0) return
    if (oldValue == undefined || newValue.indexOf(",") != -1) {
        range.setValue(newValue)
        return
    }

    const currentValues = (event.oldValue || "").split(SEPARATOR).map(value => value.trim())
    const index = currentValues.indexOf(newValue)
    let notify = false
    if (index > -1) {
        currentValues.splice(index, 1)
        notify = true
    } else {
        currentValues.push(newValue)
    }

    range.setValue(currentValues.sort().join(", "))
    if (notify) {
        SpreadsheetApp.getUi().alert(`${newValue} REMOVED from tags`)
    }
}

function fixLinks(range: SheetRange, text: string = range.getValue() as string) {
    let lastIndex = 0
    let nextIndex = text.indexOf("\n")
    const newTextBuilder = SpreadsheetApp.newRichTextValue().setText(text)
    while (nextIndex != -1 && nextIndex != lastIndex) {
        newTextBuilder.setLinkUrl(lastIndex, nextIndex, text.substring(lastIndex, nextIndex))
        lastIndex = nextIndex + 1
        nextIndex = text.indexOf("\n", lastIndex)
    }
    newTextBuilder.setLinkUrl(lastIndex, text.length, text.substring(lastIndex, text.length))
    range.setRichTextValue(newTextBuilder.build())
}

function runRegexpOverString(regExp: RegExp, text: string, block: (match: RegExpExecArray) => void) {
    let lastIndex = 0
    let match: RegExpExecArray | null
    while ((match = regExp.exec(text)) !== null) {
        if (regExp.lastIndex == lastIndex) {
            lastIndex = regExp.lastIndex++
            continue
        } else {
            lastIndex = regExp.lastIndex
        }
        block(match)
    }
}

function findValueForHeader(sheet: Sheet, row: number, header: string): string | undefined {
    const tableColumn = findColumnForHeader(sheet, header)
    if (tableColumn == undefined) return undefined
    return sheet.getRange(row, tableColumn).getDisplayValue()
}

function findColumnForHeader(sheet: Sheet, header: string): number | undefined {
    const topRow = sheet.getRange(1, 1, 1, sheet.getLastColumn())
    const tableColumn = topRow.getValues()[0]
        .findIndex(value => value == header) + 1
    if (tableColumn == 0) return undefined
    return tableColumn
}

function findArtistRow(
    table: string,
    spreadsheet: Spreadsheet = SpreadsheetApp.getActiveSpreadsheet(),
    sheet: Sheet = spreadsheet.getSheetByName("Artists")!!,
): number | undefined {
    // Assumes first column (1) is table name
    const range = sheet.getRange(1, 1, sheet.getLastRow(), 1)
    const index = range.getValues().findIndex(value => table == value[0])
    if (index == -1) return undefined
    return index + 1
}

function findArtistName(table: string): string | undefined {
    const spreadsheet = SpreadsheetApp.getActiveSpreadsheet()
    const sheet = spreadsheet.getSheetByName("Artists")!!
    const row = findArtistRow(table, spreadsheet, sheet)
    if (row == undefined) return undefined
    const name = findValueForHeader(sheet, row, "Artist")
    if (name == undefined) return undefined
    return name
}

function fixCatalogImageRows() {
    const sheet = validateActiveSheet("Artists")
    const range = sheet.getActiveRange()
    if (range == null)
        return
    for (let row = range.getRow(); row <= range.getLastRow(); row++) {
        fixCatalogImageRow(sheet, row)
    }
}

function fixCatalogImageRow(sheet: Sheet, row: number) {
    const booth = findValueForHeader(sheet, row, "Booth")
    if (booth == undefined) return
    const folder = findFolder(ROOT_FOLDER_ID, booth)
    if (folder == undefined) return

    if (!folder.getName().includes("-")) {
        const artistName = findValueForHeader(sheet, row, "Artist")
        folder.setName(booth + " - " + artistName)
    }

    const driveColumn = findColumnForHeader(sheet, "Drive")!!
    sheet.getRange(row, driveColumn).setValue(folder.getUrl())
    const sortedFiles = getSortedFiles(folder)
    insertImages(sheet, row, "Catalog images", sortedFiles)
}

function fixStampRallyImageRows() {
    const sheet = validateActiveSheet("Stamp Rallies")
    const range = sheet.getActiveRange()
    if (range == undefined) return
    for (let row = range.getRow(); row <= range.getLastRow(); row++) {
        fixStampRallyImageRow(sheet, row)
    }
}

function fixStampRallyImageRow(sheet: Sheet, row: number) {
    const tables = findValueForHeader(sheet, row, "Tables")
    if (tables == undefined) throw Error("Tables not found")
    const fandom = findValueForHeader(sheet, row, "Theme")
    if (fandom == undefined) throw Error("Theme not found")
    let foundTable: String | undefined = undefined
    runRegexpOverString(/^([A-L]\d{2}){0,1}(.*$)/gm, tables, match => {
        if (foundTable != undefined) return
        const table = match[1]
        if (table != undefined) {
            foundTable = table
            return
        }
    })
    if (foundTable == undefined) throw Error("Table not found")
    const foldersByTable = findFolders(STAMP_RALLY_ROOT_FOLDER_ID, foundTable)
    let folder: Folder | undefined = undefined
    if (foldersByTable.length > 1) {
        const foldersByFandom = findFolders(STAMP_RALLY_ROOT_FOLDER_ID, foundTable + " - " + fandom)
        if (foldersByFandom.length == 1) {
            folder = foldersByFandom[0]
        } else if (foldersByFandom.length > 1) {
            throw Error("Multiple folders found, please specify artist")
        }
    } else {
        folder = foldersByTable[0]
    }
    if (folder == undefined) throw Error("Folder not found for table " + foundTable)

    if (!folder.getName().includes("-")) {
        const artistName = findArtistName(foundTable)
        if (artistName == undefined) throw Error("Could not find artist by table")
        folder.setName(foundTable + " - " + fandom + " - " + artistName)
    }

    const sortedFiles = getSortedFiles(folder)
    if (sortedFiles.length == 0) throw Error("No images found in " + folder.getName())
    insertImages(sheet, row, "Images", sortedFiles)
}

function getSortedFiles(folder: Folder): DriveFile[] {
    const files: DriveFile[] = []
    const iterator = folder.getFiles()
    while (iterator.hasNext()) {
        files.push(iterator.next())
    }

    if (files.length == 0) return files
    return files.sort((first, second) => (parseFileIndex(first) > parseFileIndex(second) ? 1 : -1))
}

function insertImages(sheet: Sheet, row: number, targetColumnName: string, images: DriveFile[]) {
    const firstColumn = findColumnForHeader(sheet, targetColumnName)
    if (firstColumn == undefined) throw Error(targetColumnName + " column missing")

    let maxHeight = 0

    images.forEach((image, index) => {
        const range = sheet.getRange(row, firstColumn + index)
        if (!range.isBlank()) return
        let blob = image.getBlob()
        if (blob == undefined) return
        const contentType = blob.getContentType()
        if (contentType == undefined || !contentType.startsWith("image")) return
        const size = ImgApp.getSize(blob)
        let width = size.width
        let height = size.height
        if (width > 1024) {
            const resizedImage = ImgApp.doResize(image.getId(), Math.min(width, 1024))
            width = resizedImage.resizedwidth
            height = resizedImage.resizedheight
            blob = resizedImage.blob
        }
        const heightToWidth = height / width
        const scaledHeight = heightToWidth * 600
        if (scaledHeight > maxHeight) {
            maxHeight = scaledHeight
        }
        range.setValue(
            SpreadsheetApp.newCellImage()
                .setSourceUrl(blobToDataUrl(blob))
                .build()
        )
    })

    if (maxHeight > 0) {
        sheet.setRowHeight(row, maxHeight)
    }
}

function parseFileIndex(file: DriveFile): number {
    const name = file.getName()
    let separatorIndex = name.indexOf("-")
    if (separatorIndex < 0) {
        separatorIndex = name.indexOf(".")
    }
    return Number(name.substring(0, separatorIndex).trim())
}

function blobToDataUrl(blob: GoogleAppsScript.Base.Blob): string {
    return "data:".concat(blob.getContentType()!!, ";base64,").concat(Utilities.base64Encode(blob.getBytes()))
}

function validateActiveSheet(sheetName: string): Sheet {
    const sheet = SpreadsheetApp.getActiveSheet()
    if (!sheet.getName().includes("maintainer") && sheet.getName() != sheetName) {
        throw Error("Not on " + sheetName + " sheet")
    }
    return sheet
}

function findFolder(rootFolderId: string, booth: string): Folder | undefined {
    const rootFolder = DriveApp.getFolderById(rootFolderId)
    if (rootFolder == undefined) throw Error("Cannot find Drive folder")
    const folders = rootFolder.getFolders()
    while (folders.hasNext()) {
        const folder = folders.next()
        if (folder.getName().startsWith(booth)) {
            return folder
        }
    }
    return undefined
}

function findFolders(rootFolderId: string, booth: string): Folder[] {
    const rootFolder = DriveApp.getFolderById(rootFolderId)
    if (rootFolder == undefined) throw Error("Cannot find Drive folder")
    const folders = rootFolder.getFolders()
    const found: Folder[] = []
    while (folders.hasNext()) {
        const folder = folders.next()
        if (folder.getName().startsWith(booth)) {
            found.push(folder)
        }
    }
    return found
}
