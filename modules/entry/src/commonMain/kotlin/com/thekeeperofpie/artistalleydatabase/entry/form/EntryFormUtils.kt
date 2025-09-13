package com.thekeeperofpie.artistalleydatabase.entry.form

fun EntryFormSection.LockState.rotateLockState(wasEverDifferent: Boolean) = when (this) {
    EntryFormSection.LockState.LOCKED -> EntryFormSection.LockState.UNLOCKED
    EntryFormSection.LockState.UNLOCKED -> if (wasEverDifferent) EntryFormSection.LockState.DIFFERENT else EntryFormSection.LockState.LOCKED
    EntryFormSection.LockState.DIFFERENT -> EntryFormSection.LockState.LOCKED
}
