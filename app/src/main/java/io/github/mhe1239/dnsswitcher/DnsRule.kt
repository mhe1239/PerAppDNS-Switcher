package io.github.mhe1239.dnsswitcher

// Values of Settings.Global "private_dns_mode" (the constants are hidden API).
const val MODE_AUTO = "opportunistic"
const val MODE_HOSTNAME = "hostname"

/** Private DNS mode wanted while [foregroundPackage] is on screen, or null to leave the setting alone. */
fun desiredMode(foregroundPackage: String, targets: Set<String>, specifier: String?): String? = when {
    foregroundPackage in targets -> MODE_AUTO
    specifier.isNullOrBlank() -> null // no hostname to go back to
    else -> MODE_HOSTNAME
}
