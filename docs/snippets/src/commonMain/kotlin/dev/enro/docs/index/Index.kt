// Snippets for docs/ghpages/index.md
package dev.enro.docs.index

import kotlinx.datetime.LocalDate

// The docs show these as bare signatures; an interface lets them compile.
private interface Signatures {
    // region docs:index/signatures
    fun showProfile(userId: String): Unit
    fun selectDate(minDate: LocalDate? = null, maxDate: LocalDate? = null): LocalDate
    // endregion
}
