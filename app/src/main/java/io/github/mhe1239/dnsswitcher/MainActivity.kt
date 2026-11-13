package io.github.mhe1239.dnsswitcher

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private data class AppEntry(val pkg: String, val label: String)

// Position of the search box in the list: status, divider, mode pickers, divider, search.
private const val SEARCH_ITEM_INDEX = 4

// Same wording as Samsung's Private DNS screen.
private val MODE_LABELS = listOf(
    MODE_OFF to "사용 안함",
    MODE_AUTO to "자동(권장)",
    MODE_HOSTNAME to "프라이빗 DNS 공급자 호스트 이름",
)

class MainActivity : ComponentActivity() {
    private var resumeCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Screen(resumeCount) } }
    }

    override fun onResume() {
        super.onResume()
        resumeCount++ // re-read permission/setting state after returning from system settings
    }

    @Composable
    private fun Screen(resumeCount: Int) {
        // shortcut: labels load on the main thread; move to a coroutine if startup feels slow.
        val apps = remember { launchableApps() }
        var targets by remember { mutableStateOf(Prefs.targets(this)) }
        var query by remember { mutableStateOf("") }
        val shown = apps.filter { query.isBlank() || it.label.contains(query.trim(), true) || it.pkg.contains(query.trim(), true) }
        val listState = rememberLazyListState()
        // Bring the search box to the top so its results are not hidden under the keyboard.
        LaunchedEffect(query) { if (query.isNotEmpty()) listState.scrollToItem(SEARCH_ITEM_INDEX) }

        Scaffold { inner ->
            // imePadding: keep search results scrollable above the keyboard
            LazyColumn(Modifier.fillMaxSize().padding(inner).imePadding(), state = listState) {
                item(key = "status:$resumeCount") { Status() }
                item { HorizontalDivider() }
                item {
                    var defaultMode by remember { mutableStateOf(Prefs.defaultMode(this@MainActivity)) }
                    var targetMode by remember { mutableStateOf(Prefs.targetMode(this@MainActivity)) }
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ModePicker("기본", defaultMode) {
                            defaultMode = it
                            Prefs.setDefaultMode(this@MainActivity, it)
                        }
                        ModePicker("체크한 앱이 화면에 있을 때", targetMode) {
                            targetMode = it
                            Prefs.setTargetMode(this@MainActivity, it)
                        }
                        Text("바뀐 모드는 다음 앱 전환부터 적용됩니다.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                item { HorizontalDivider() }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        label = { Text("앱 검색 (이름 또는 패키지)") },
                        singleLine = true,
                    )
                }
                items(shown, key = { it.pkg }) { app ->
                    val checked = app.pkg in targets
                    val toggle = {
                        targets = if (checked) targets - app.pkg else targets + app.pkg
                        Prefs.setTargets(this@MainActivity, targets)
                    }
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable(role = Role.Checkbox, onClick = toggle)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(app.label)
                            Text(app.pkg, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ModePicker(title: String, selected: String, onSelect: (String) -> Unit) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Column(Modifier.selectableGroup()) {
            for ((mode, label) in MODE_LABELS) {
                Row(
                    Modifier.fillMaxWidth()
                        .selectable(selected = mode == selected, role = Role.RadioButton) { onSelect(mode) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = mode == selected, onClick = null)
                    Text(label, Modifier.padding(start = 8.dp))
                }
            }
        }
    }

    @Composable
    private fun Status() {
        val canWrite = checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        val serviceOn = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains(ComponentName(this, SwitchService::class.java).flattenToString()) == true
        val mode = Settings.Global.getString(contentResolver, KEY_MODE) ?: "-"
        val host = Settings.Global.getString(contentResolver, KEY_SPECIFIER)

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("현재 프라이빗 DNS: $mode${host?.let { " ($it)" } ?: ""}")
            if (host.isNullOrBlank()) {
                Text("먼저 시스템 설정에서 '프라이빗 DNS 공급자 호스트 이름'을 지정하세요.", color = MaterialTheme.colorScheme.error)
            }
            Text("설정 변경 권한: ${if (canWrite) "허용됨" else "없음"}")
            if (!canWrite) {
                Text("PC에서 한 번 실행하세요:", style = MaterialTheme.typography.bodySmall)
                SelectionContainer {
                    Text(
                        "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text("접근성 서비스: ${if (serviceOn) "켜짐" else "꺼짐"}")
            if (!serviceOn) {
                Button(onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                    Text("접근성 설정 열기")
                }
            }
        }
    }

    private fun launchableApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .map { AppEntry(it.activityInfo.packageName, it.loadLabel(packageManager).toString()) }
            .filter { it.pkg != packageName }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }
}
