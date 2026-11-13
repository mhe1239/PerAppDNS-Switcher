package io.github.mhe1239.dnsswitcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DnsRuleTest {
    private val targets = setOf("com.example.target")

    @Test fun targetAppGetsAuto() =
        assertEquals(MODE_AUTO, desiredMode("com.example.target", targets, "dns.example"))

    @Test fun otherAppGetsHostname() =
        assertEquals(MODE_HOSTNAME, desiredMode("com.example.other", targets, "dns.example"))

    @Test fun noHostnameLeavesSettingAlone() {
        assertNull(desiredMode("com.example.other", targets, null))
        assertNull(desiredMode("com.example.other", targets, " "))
    }

    @Test fun targetWinsEvenWithoutHostname() =
        assertEquals(MODE_AUTO, desiredMode("com.example.target", targets, null))

    @Test fun chosenModesAreUsed() {
        assertEquals(MODE_OFF, desiredMode("com.example.target", targets, "dns.example", MODE_AUTO, MODE_OFF))
        assertEquals(MODE_AUTO, desiredMode("com.example.other", targets, "dns.example", MODE_AUTO, MODE_OFF))
    }

    @Test fun hostnameAsTargetModeNeedsHostname() {
        assertEquals(MODE_HOSTNAME, desiredMode("com.example.target", targets, "dns.example", MODE_OFF, MODE_HOSTNAME))
        assertNull(desiredMode("com.example.target", targets, null, MODE_OFF, MODE_HOSTNAME))
    }
}
