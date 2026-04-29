package com.zj.timetable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.zj.timetable.ui.theme.ImportTimetableTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var receiver: TimetableImportReceiver
    private val calendarPermission = "com.zui.calendar.permission.ACCESS_CALENDAR"
    private val importResult = mutableStateOf<ImportResult?>(null)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 注册广播接收器
        receiver = TimetableImportReceiver { result ->
            importResult.value = result
        }
        val filter = IntentFilter("com.zui.calendar.ACTION_TIMETABLE_IMPORT_RESULT")
        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        
        setContent {
            ImportTimetableTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimetableImportScreen(
                        modifier = Modifier.padding(innerPadding),
                        hasPermission = checkCalendarPermission(),
                        importResult = importResult.value,
                        onRequestPermission = { launcher ->
                            launcher.launch(calendarPermission)
                        },
                        onImportTimetable = { uri ->
                            if (checkCalendarPermission()) {
                                importResult.value = null // 清空之前的结果
                                importTimetable(uri)
                            } else {
                                Toast.makeText(
                                    this,
                                    "请先授予日历访问权限",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onClearResult = {
                            importResult.value = null
                        }
                    )
                }
            }
        }
    }

    private fun checkCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            calendarPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun importTimetable(imageUri: Uri) {
        val calendarPackage = "com.zui.calendar"
        val calendarAuthority = "com.zui.app.calendar"
        
        // 检查日历应用的 ContentProvider 是否存在
        val providerInfo = packageManager.resolveContentProvider(calendarAuthority, 0)
        if (providerInfo == null) {
            Toast.makeText(
                this,
                "未找到日历应用，请确保已安装支持该功能的日历应用",
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, "日历应用的 ContentProvider 不存在: $calendarAuthority")
            return
        }
        
        // 授予权限
        grantUriPermission(
            calendarPackage,
            imageUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        // 调用 ContentProvider
        val extras = Bundle().apply {
            putString("image_uri", imageUri.toString())
        }

        try {
            val result = contentResolver.call(
                "content://$calendarAuthority".toUri(),
                "importTimetableFromUri",
                null,
                extras
            )

            val accepted = result?.getBoolean("accepted", false) ?: false
            if (accepted) {
                Toast.makeText(this, "正在识别课程表...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "请求被拒绝", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "调用失败: ${e.message}")
            Toast.makeText(this, "调用失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private class TimetableImportReceiver(
        private val onResult: (ImportResult) -> Unit
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra("success", false)
            val scheduleId = intent.getLongExtra("schedule_id", -1)
            val errorCode = intent.getIntExtra("error_code", 0)
            val errorMessage = intent.getStringExtra("error_message")

            Log.d(
                TAG,
                "onReceive: success:$success scheduleId:$scheduleId errorCode:$errorCode errorMessage:$errorMessage"
            )
            
            val result = if (success) {
                ImportResult.Success(scheduleId)
            } else {
                val message = when (errorCode) {
                    1001 -> "图片Uri无效"
                    1002 -> "网络不可用"
                    1003 -> "请先登录账号"
                    1004 -> "图片加载失败"
                    1006 -> "图片中未识别到课程表"
                    1009 -> "识别超时，请重试"
                    2001 -> "正在处理其他导入请求"
                    else -> errorMessage ?: "导入失败"
                }
                ImportResult.Error(errorCode, message)
            }
            
            onResult(result)
            Toast.makeText(
                context,
                if (success) "导入成功，课表ID: $scheduleId" else result.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    sealed class ImportResult(val message: String) {
        data class Success(val scheduleId: Long) : ImportResult("导入成功，课表ID: $scheduleId")
        data class Error(val errorCode: Int, val errorMessage: String) : ImportResult(errorMessage)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun TimetableImportScreen(
    modifier: Modifier = Modifier,
    hasPermission: Boolean,
    importResult: MainActivity.ImportResult?,
    onRequestPermission: ((androidx.activity.result.ActivityResultLauncher<String>) -> Unit),
    onImportTimetable: (Uri) -> Unit,
    onClearResult: () -> Unit
) {
    val permissionGranted = remember { mutableStateOf(hasPermission) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted.value = isGranted
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onImportTimetable(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "导入课程表",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        if (!permissionGranted.value) {
            Button(
                onClick = {
                    onRequestPermission(permissionLauncher)
                },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("授予日历访问权限")
            }
        }
        
        Button(
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            enabled = permissionGranted.value
        ) {
            Text("选择课程表图片")
        }
        
        // 显示导入结果
        importResult?.let { result ->
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (result) {
                        is MainActivity.ImportResult.Success -> Color(0xFFE8F5E9)
                        is MainActivity.ImportResult.Error -> Color(0xFFFFEBEE)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = when (result) {
                            is MainActivity.ImportResult.Success -> "✓ 导入成功"
                            is MainActivity.ImportResult.Error -> "✗ 导入失败"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (result) {
                            is MainActivity.ImportResult.Success -> Color(0xFF2E7D32)
                            is MainActivity.ImportResult.Error -> Color(0xFFC62828)
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    when (result) {
                        is MainActivity.ImportResult.Success -> {
                            Text(
                                text = "课表ID: ${result.scheduleId}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        is MainActivity.ImportResult.Error -> {
                            Text(
                                text = "错误码: ${result.errorCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "错误信息: ${result.errorMessage}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onClearResult,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("清除结果")
                    }
                }
            }
        }
    }
}