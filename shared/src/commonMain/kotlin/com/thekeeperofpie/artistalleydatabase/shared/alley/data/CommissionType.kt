package com.thekeeperofpie.artistalleydatabase.shared.alley.data

enum class CommissionType {
    ANY,
    ON_SITE,
    ONLINE,
    VGEN,
    OTHER,
    ;

    companion object {
        fun parseFlags(commissions: List<String>): Long {
            val commissionOnsite = commissions.contains("On-site")
            val commissionOnline = commissions.contains("Online") ||
                    commissions.any {
                        it.contains("http", ignoreCase = true) &&
                                !it.contains("vgen.co", ignoreCase = true)
                    }
            val commissionVGen = commissions.any {
                it.contains("vgen.co", ignoreCase = true)
            }
            val commissionOther = commissions.filterNot {
                it.contains("On-site", ignoreCase = true) ||
                        it.contains("Online", ignoreCase = true) ||
                        it.contains("http", ignoreCase = true) ||
                        it.contains("vgen.co", ignoreCase = true)
            }.isNotEmpty()

            val entries = CommissionType.entries
            val types = listOfNotNull(
                ON_SITE.takeIf { commissionOnsite },
                ONLINE.takeIf { commissionOnline },
                VGEN.takeIf { commissionVGen },
                OTHER.takeIf { commissionOther },
            ).toMutableList()
            if (types.isNotEmpty()) {
                types += ANY
            }
            return types.fold(0L) { flags, type ->
                val index = entries.indexOf(type)
                flags or (1L shl index)
            }
        }
    }
}
