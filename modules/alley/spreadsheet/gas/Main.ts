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

function onOpen(event: SheetsOnOpen) {
    SpreadsheetApp.getUi()
        .createMenu("Actions")
        .addItem("Fix artist links", "fixArtistLinks")
        .addItem("Fix catalog images", "fixCatalogImageRows")
        .addItem("Fix map cells", "fixMapCells")
        .addItem("Fix stamp rally images", "fixStampRallyImageRows")
        .addItem("Fix table links", "fixTableLinks")
        .addToUi()
}

function onEdit(event: SheetsOnEdit) {
    multiSelect(event)
}

function multiSelect(event: SheetsOnEdit) {
    const sheet = event.source
    const range = event.range

    const validationRule = range.getDataValidation()
    if (!validationRule) return

    const criteriaType = validationRule.getCriteriaType()
    if (criteriaType !== SpreadsheetApp.DataValidationCriteria.VALUE_IN_RANGE) return

    const validationRange = validationRule.getCriteriaValues()[0]

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

function fixArtistLinks() {
    runOverActiveCells(cell => {
        const text = cell.getValue() as string
        let lastIndex = 0
        let nextIndex = text.indexOf("\n")
        const newTextBuilder = SpreadsheetApp.newRichTextValue().setText(text)
        while (nextIndex != -1 && nextIndex != lastIndex) {
            newTextBuilder.setLinkUrl(lastIndex, nextIndex, text.substring(lastIndex, nextIndex))
            lastIndex = nextIndex + 1
            nextIndex = text.indexOf("\n", lastIndex)
        }
        newTextBuilder.setLinkUrl(lastIndex, text.length, text.substring(lastIndex, text.length))
        cell.setRichTextValue(newTextBuilder.build())
    })
}

function fixTableLinks() {
    runOverActiveCells(cell => {
        const text = cell.getValue()
        if (text.length == 0) return
        // First add any new text
        let newText = ""
        runRegexpOverString(/^([A-L]\d{2}){0,1}(.*$)/gm, text, match => {
            const table = match[1]
            const remainder = match[2]
            if (table == undefined || (remainder != undefined && remainder.length > 0)) {
                newText += `${match[0]}\n`
                return
            }
            const artist = findArtistName(table)
            if (artist != undefined) {
                newText += `${table} - ${artist}\n`
            } else {
                newText += `${table}\n`
            }
        })
        newText = newText.trim()

        // Then re-run on the new text so the indexes work out
        const builder = SpreadsheetApp.newRichTextValue()
            .setText(newText)
        runRegexpOverString(/^([A-L]\d{2}){0,1}(.*$)/gm, newText, match => {
            const index = match["index"]
            const line = match[0]
            const table = match[1]
            const link = findArtistLink(table)
            if (link == undefined) return
            builder.setLinkUrl(index, index + line.length, link)
        })
        cell.setRichTextValue(builder.build())
    })
}

function runOverActiveCells(block: (cell: SheetRange) => void) {
    runOverAllCells(SpreadsheetApp.getActiveRange(), block)
}

function runOverAllCells(range: SheetRange, block: (cell: SheetRange) => void) {
    const numRows = range.getNumRows()
    const numColumns = range.getNumColumns()
    for (let relativeRow = 1; relativeRow <= numRows; relativeRow++) {
        for (let relativeColumn = 1; relativeColumn <= numColumns; relativeColumn++) {
            const cell = range.getCell(relativeRow, relativeColumn)
            const text = cell.getValue()
            if (text.length != 0) {
                block(cell)
            }
        }
    }
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

function findArtistLink(table: string): string | undefined {
    const spreadsheet = SpreadsheetApp.getActiveSpreadsheet()
    const sheet = spreadsheet.getSheetByName("Artists")!!
    const row = findArtistRow(table, spreadsheet, sheet)
    if (row == undefined) return undefined
    const spreadsheetId = spreadsheet.getId()
    // Use column B so that link previews show artist name
    return `https://docs.google.com/spreadsheets/d/${spreadsheetId}/edit#gid=${sheet.getSheetId()}&fvid=0&range=B${row}`
}

function fixMapCells() {
    const range = SpreadsheetApp.getActiveRange()
    runOverAllCells(range, cell => {
        const text = cell.getValue()
        if (text.length == 0) {
            cell.clearFormat()
            return
        }
        const newTextBuilder = SpreadsheetApp.newRichTextValue()
            .setText(text)
        const link = findArtistLink(text)
        if (link != undefined) {
            newTextBuilder.setLinkUrl(link)
        }

        // Set style after to override link styling
        newTextBuilder.setTextStyle(
            SpreadsheetApp.newTextStyle()
                .setForegroundColor("#000000")
                .setFontSize(16)
                .setBold(true)
                .setUnderline(false)
                .build()
        )

        const row = cell.getRow()
        let background: string
        if (row <= 22) {
            background = "#d5c1dd"
        } else if (row <= 40) {
            background = "#c7dbe6"
        } else {
            background = "#fdd6d9"
        }

        cell.setBackground(background)
            .setRichTextValue(newTextBuilder.build())
            .setHorizontalAlignment("center")
            .setBorder(true, true, true, true, false, false, "#888888", SpreadsheetApp.BorderStyle.SOLID_MEDIUM)
    })

    const sheet = SpreadsheetApp.getActiveSheet()
    sheet.setColumnWidths(2, sheet.getLastColumn() - 1, 75)
        .autoResizeColumns(1, range.getNumColumns()) // Ignore buffer on the left side
        .autoResizeRows(range.getRow(), range.getNumRows())
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
    if (files[0].getName().indexOf("-") < 0) {
        const sortedByLastUpdated = files.sort((first, second) => (first.getLastUpdated() > second.getLastUpdated() ? 1 : -1))
        sortedByLastUpdated.forEach((file, index) => {
            file.setName(index + " - " + file.getName())
        })
        return sortedByLastUpdated
    } else {
        return files.sort((first, second) => (parseFileIndex(first) > parseFileIndex(second) ? 1 : -1))
    }
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
    const separatorIndex = name.indexOf("-")
    return Number(name.substring(0, separatorIndex).trim())
}

function blobToDataUrl(blob: GoogleAppsScript.Base.Blob): string {
    return "data:".concat(blob.getContentType()!!, ";base64,").concat(Utilities.base64Encode(blob.getBytes()))
}

function validateActiveSheet(sheetName: string): Sheet {
    const sheet = SpreadsheetApp.getActiveSheet()
    if (sheet.getName() != sheetName) {
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
