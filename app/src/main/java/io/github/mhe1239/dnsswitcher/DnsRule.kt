package io.github.mhe1239.dnsswitcher

// Values of Settings.Global "private_dns_mode" (the constants are hidden API).
const val MODE_OFF = "off"
const val MODE_AUTO = "opportunistic"
const val MODE_HOSTNAME = "hostname"

/** Private DNS mode wanted while [foregroundPackage] is on screen, or null to leave the setting alone. */
fun desiredMode(
    foregroundPackage: String,
    targets: Set<String>,
    specifier: String?,
    defaultMode: String = MODE_HOSTNAME,
    targetMode: String = MODE_AUTO,
): String? {
    val mode = if (foregroundPackage in targets) targetMode else defaultMode
    // Hostname mode without a hostname would break DNS, so keep the current setting instead.
    return if (mode == MODE_HOSTNAME && specifier.isNullOrBlank()) null else mode
}
