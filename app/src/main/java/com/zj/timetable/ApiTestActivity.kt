package com.zj.timetable

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zj.timetable.ui.theme.ImportTimetableTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)

/**
 * API测试界面
 * 提供可视化的API测试工具，用户可以点击按钮测试各种API功能
 */
class ApiTestActivity : ComponentActivity() {
    
    private lateinit var apiHelper: ThirdPartyApiHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiHelper = ThirdPartyApiHelper(this)
        
        setContent {
            ImportTimetableTheme {
                ApiTestScreen()
            }
        }
    }
    
    @Composable
    fun ApiTestScreen() {
        val testResult = remember { mutableStateOf("等待测试...") }
        val isLoading = remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("日历API测试工具", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 提醒测试区域
                TestSection(
                    title = "📝 提醒测试",
                    color = Color(0xFFE3F2FD)
                ) {
                    TestButton("添加不重复提醒", isLoading) { 
                        testAddSimpleReminder(testResult, isLoading)
                    }
                    TestButton("添加每天重复提醒", isLoading) { 
                        testAddDailyReminder(testResult, isLoading)
                    }
                    TestButton("添加每周重复提醒", isLoading) { 
                        testAddWeeklyReminder(testResult, isLoading)
                    }
                    TestButton("查询所有提醒", isLoading) { 
                        testQueryAllReminders(testResult, isLoading)
                    }
                    TestButton("按标题搜索提醒", isLoading) { 
                        testQueryReminderByTitle(testResult, isLoading)
                    }
                    TestButton("按时间范围查询提醒", isLoading) { 
                        testQueryReminderByTime(testResult, isLoading)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 课程测试区域
                TestSection(
                    title = "📚 课程测试",
                    color = Color(0xFFFFF3E0)
                ) {
                    TestButton("添加课程-周一第1-2节", isLoading) { 
                        testAddCurriculum1(testResult, isLoading)
                    }
                    TestButton("添加课程-周三第4节", isLoading) { 
                        testAddCurriculum2(testResult, isLoading)
                    }
                    TestButton("添加课程-单周上课", isLoading) { 
                        testAddCurriculumOddWeeks(testResult, isLoading)
                    }
                    TestButton("查询所有课程", isLoading) { 
                        testQueryAllCurriculums(testResult, isLoading)
                    }
                    TestButton("按名称搜索课程", isLoading) { 
                        testQueryCurriculumByName(testResult, isLoading)
                    }
                    TestButton("按时间范围查询课程", isLoading) { 
                        testQueryCurriculumByTime(testResult, isLoading)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 综合测试区域
                TestSection(
                    title = "🔧 综合测试",
                    color = Color(0xFFE8F5E9)
                ) {
                    TestButton("完整CRUD测试-提醒", isLoading) { 
                        testReminderCRUD(testResult, isLoading)
                    }
                    TestButton("完整CRUD测试-课程", isLoading) { 
                        testCurriculumCRUD(testResult, isLoading)
                    }
                    TestButton("测试时间展开功能", isLoading) { 
                        testTimeExpansion(testResult, isLoading)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 测试结果显示区域
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "测试结果",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isLoading.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        val resultScrollState = rememberScrollState()
                        Text(
                            text = testResult.value,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(resultScrollState),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    fun TestSection(
        title: String,
        color: Color,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = color),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                content()
            }
        }
    }
    
    @Composable
    fun TestButton(
        text: String,
        isLoading: MutableState<Boolean>,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            enabled = !isLoading.value
        ) {
            Text(text)
        }
    }
    
    // ==================== 提醒测试方法 ====================
    
    private fun testAddSimpleReminder(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在添加不重复提醒...\n"
        
        Thread {
            try {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
                
                val id = apiHelper.addReminder(
                    title = "明天开会",
                    startTime = calendar.timeInMillis,
                    description = "会议室A",
                    alarmTime = 2, // 提前15分钟
                    repeatType = "none"
                )
                
                runOnUiThread {
                    if (id != null) {
                        result.value += "✅ 添加成功！\n"
                        result.value += "提醒ID: $id\n"
                        result.value += "标题: 明天开会\n"
                        result.value += "时间: ${dateFormat.format(Date(calendar.timeInMillis))}\n"
                        result.value += "描述: 会议室A\n"
                        result.value += "提前提醒: 15分钟\n"
                        result.value += "重复: 不重复\n"
                        showToast("✅ 添加成功，ID=$id")
                    } else {
                        result.value += "❌ 添加失败！请检查日志\n"
                        showToast("❌ 添加失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testAddDailyReminder(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在添加每天重复提醒...\n"
        
        Thread {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 8)
                calendar.set(Calendar.MINUTE, 0)
                
                val id = apiHelper.addReminder(
                    title = "每日打卡",
                    startTime = calendar.timeInMillis,
                    description = "记得打卡",
                    alarmTime = 0, // 准时提醒
                    repeatType = "daily",
                    repeatCount = 30 // 重复30次
                )
                
                runOnUiThread {
                    if (id != null) {
                        result.value += "✅ 添加成功！\n"
                        result.value += "提醒ID: $id\n"
                        result.value += "标题: 每日打卡\n"
                        result.value += "时间: 每天 ${dateFormat.format(Date(calendar.timeInMillis))}\n"
                        result.value += "重复: 每天，共30次\n"
                        showToast("✅ 添加成功，ID=$id")
                    } else {
                        result.value += "❌ 添加失败！\n"
                        showToast("❌ 添加失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testAddWeeklyReminder(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在添加每周重复提醒...\n"
        
        Thread {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 19)
                calendar.set(Calendar.MINUTE, 0)
                
                val id = apiHelper.addReminder(
                    title = "健身提醒",
                    startTime = calendar.timeInMillis,
                    description = "去健身房",
                    alarmTime = 3, // 提前30分钟
                    repeatType = "weekly",
                    repeatDays = "1,3,5" // 周一、三、五
                )
                
                runOnUiThread {
                    if (id != null) {
                        result.value += "✅ 添加成功！\n"
                        result.value += "提醒ID: $id\n"
                        result.value += "标题: 健身提醒\n"
                        result.value += "时间: 19:00\n"
                        result.value += "重复: 每周一、三、五\n"
                        result.value += "提前提醒: 30分钟\n"
                        showToast("✅ 添加成功，ID=$id")
                    } else {
                        result.value += "❌ 添加失败！\n"
                        showToast("❌ 添加失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testQueryAllReminders(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在查询所有提醒...\n"
        
        Thread {
            try {
                val queryResult = apiHelper.queryAllReminders()
                
                runOnUiThread {
                    if (queryResult != null) {
                        result.value += "✅ 查询成功！\n"
                        result.value += "找到 ${queryResult.count} 条提醒\n\n"
                        
                        queryResult.items.take(10).forEachIndexed { index, reminder ->
                            result.value += "【提醒 ${index + 1}】\n"
                            result.value += "ID: ${reminder["id"]}\n"
                            result.value += "标题: ${reminder["title"]}\n"
                            result.value += "时间: ${reminder["start_time"]?.let { 
                                dateFormat.format(Date((it as Double).toLong()))
                            }}\n"
                            result.value += "重复: ${reminder["repeat_type"]}\n"
                            result.value += "\n"
                        }
                        
                        if (queryResult.count > 10) {
                            result.value += "...还有 ${queryResult.count - 10} 条\n"
                        }
                        showToast("✅ 找到 ${queryResult.count} 条提醒")
                    } else {
                        result.value += "❌ 查询失败！\n"
                        showToast("❌ 查询失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testQueryReminderByTitle(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在按标题搜索提醒（关键词：'提醒'）...\n"
        
        Thread {
            try {
                val queryResult = apiHelper.queryReminderByTitle("提醒")
                
                runOnUiThread {
                    if (queryResult != null) {
                        result.value += "✅ 搜索成功！\n"
                        result.value += "找到 ${queryResult.count} 条包含'提醒'的记录\n\n"
                        
                        queryResult.items.forEach { reminder ->
                            result.value += "• ${reminder["title"]}\n"
                        }
                        showToast("✅ 找到 ${queryResult.count} 条")
                    } else {
                        result.value += "❌ 搜索失败！\n"
                        showToast("❌ 搜索失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testQueryReminderByTime(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在按时间范围查询提醒（未来7天）...\n"
        
        Thread {
            try {
                val calendar = Calendar.getInstance()
                val startTime = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 7)
                val endTime = calendar.timeInMillis
                
                val queryResult = apiHelper.queryReminderByTimeRange(startTime, endTime)
                
                runOnUiThread {
                    if (queryResult != null) {
                        result.value += "✅ 查询成功！\n"
                        result.value += "未来7天有 ${queryResult.count} 条提醒\n\n"
                        
                        queryResult.items.forEach { reminder ->
                            result.value += "• ${reminder["title"]}\n"
                            val time = reminder["occurrence_time"] ?: reminder["start_time"]
                            time?.let {
                                result.value += "  时间: ${dateFormat.format(Date((it as Double).toLong()))}\n"
                            }
                        }
                        showToast("✅ 找到 ${queryResult.count} 条")
                    } else {
                        result.value += "❌ 查询失败！\n"
                        showToast("❌ 查询失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    // ==================== 课程测试方法 ====================
    
    private fun testAddCurriculum1(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在添加课程（周一第1-2节）...\n"
        
        Thread {
            try {
                val id = apiHelper.addCurriculum(
                    name = "高等数学",
                    timeRule = "1-1-2", // 周一第1-2节
                    weeks = "1,2,3,4,5,6,7,8,9,10", // 第1-10周
                    location = "教学楼A101",
                    teacher = "张老师",
                    color = "#FF5733"
                )
                
                runOnUiThread {
                    if (id != null) {
                        result.value += "✅ 添加成功！\n"
                        result.value += "课程ID: $id\n"
                        result.value += "课程名: 高等数学\n"
                        result.value += "时间: 周一第1-2节\n"
                        result.value += "周数: 第1-10周\n"
                        result.value += "地点: 教学楼A101\n"
                        result.value += "教师: 张老师\n"
                        showToast("✅ 添加成功，ID=$id")
                    } else {
                        result.value += "❌ 添加失败！可能与已有课程时间冲突\n"
                        showToast("❌ 添加失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testAddCurriculum2(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在添加课程（周三第4节）...\n"
        
        Thread {
            try {
                val id = apiHelper.addCurriculum(
                    name = "大学英语",
                    timeRule = "3-4", // 周三第4节
                    weeks = "1,2,3,4,5,6,7,8",
                    location = "教学楼B203",
                    teacher = "李老师"
                )
                
                runOnUiThread {
                    if (id != null) {
                        result.value += "✅ 添加成功！\n"
                        result.value += "课程ID: $id\n"
                        result.value += "课程名: 大学英语\n"
                        result.value += "时间: 周三第4节\n"
                        result.value += "周数: 第1-8周\n"
                        result.value += "地点: 教学楼B203\n"
                        showToast("✅ 添加成功，ID=$id")
                    } else {
                        result.value += "❌ 添加失败！\n"
                        showToast("❌ 添加失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testAddCurriculumOddWeeks(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在添加课程（单周上课）...\n"
        
        Thread {
            try {
                val id = apiHelper.addCurriculum(
                    name = "体育",
                    timeRule = "5-6-7", // 周五第6-7节
                    weeks = "1,3,5,7,9,11,13,15", // 单周
                    location = "体育馆",
                    teacher = "王老师"
                )
                
                runOnUiThread {
                    if (id != null) {
                        result.value += "✅ 添加成功！\n"
                        result.value += "课程ID: $id\n"
                        result.value += "课程名: 体育\n"
                        result.value += "时间: 周五第6-7节\n"
                        result.value += "周数: 单周（1,3,5,7,9,11,13,15）\n"
                        result.value += "地点: 体育馆\n"
                        showToast("✅ 添加成功，ID=$id")
                    } else {
                        result.value += "❌ 添加失败！\n"
                        showToast("❌ 添加失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testQueryAllCurriculums(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在查询所有课程...\n"
        
        Thread {
            try {
                val queryResult = apiHelper.queryAllCurriculums()
                
                runOnUiThread {
                    if (queryResult != null) {
                        result.value += "✅ 查询成功！\n"
                        result.value += "找到 ${queryResult.count} 门课程\n\n"
                        
                        queryResult.items.take(10).forEachIndexed { index, curriculum ->
                            result.value += "【课程 ${index + 1}】\n"
                            result.value += "ID: ${curriculum["id"]}\n"
                            result.value += "课程: ${curriculum["name"]}\n"
                            result.value += "时间规则: ${curriculum["time_rule"]}\n"
                            result.value += "周数: ${curriculum["weeks"]}\n"
                            result.value += "地点: ${curriculum["location"]}\n"
                            result.value += "教师: ${curriculum["teacher"]}\n"
                            result.value += "\n"
                        }
                        
                        if (queryResult.count > 10) {
                            result.value += "...还有 ${queryResult.count - 10} 门\n"
                        }
                        showToast("✅ 找到 ${queryResult.count} 门课程")
                    } else {
                        result.value += "❌ 查询失败！\n"
                        showToast("❌ 查询失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testQueryCurriculumByName(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在按名称搜索课程（关键词：'数学'）...\n"
        
        Thread {
            try {
                val queryResult = apiHelper.queryCurriculumByName("数学")
                
                runOnUiThread {
                    if (queryResult != null) {
                        result.value += "✅ 搜索成功！\n"
                        result.value += "找到 ${queryResult.count} 门包含'数学'的课程\n\n"
                        
                        queryResult.items.forEach { curriculum ->
                            result.value += "• ${curriculum["name"]}\n"
                            result.value += "  ${curriculum["time_rule"]}, ${curriculum["location"]}\n"
                        }
                        showToast("✅ 找到 ${queryResult.count} 门")
                    } else {
                        result.value += "❌ 搜索失败！\n"
                        showToast("❌ 搜索失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testQueryCurriculumByTime(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "正在按时间范围查询课程（本周）...\n"
        
        Thread {
            try {
                val calendar = Calendar.getInstance()
                // 设置到本周一开始
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val startTime = calendar.timeInMillis
                
                // 设置到本周日结束
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                val endTime = calendar.timeInMillis
                
                val queryResult = apiHelper.queryCurriculumByTimeRange(startTime, endTime)
                
                runOnUiThread {
                    if (queryResult != null) {
                        result.value += "✅ 查询成功！\n"
                        result.value += "本周有 ${queryResult.count} 门课程\n\n"
                        
                        queryResult.items.forEach { curriculum ->
                            result.value += "• ${curriculum["name"]}\n"
                            val time = curriculum["occurrence_time"]
                            time?.let {
                                result.value += "  ${dateFormat.format(Date((it as Double).toLong()))}\n"
                            }
                        }
                        showToast("✅ 找到 ${queryResult.count} 门")
                    } else {
                        result.value += "❌ 查询失败！\n"
                        showToast("❌ 查询失败")
                    }
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "❌ 异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    // ==================== 综合测试 ====================
    
    private fun testReminderCRUD(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "开始完整CRUD测试（提醒）...\n\n"
        
        Thread {
            try {
                // 1. 添加
                result.value += "【步骤1】添加提醒\n"
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR, 2)
                
                val id = apiHelper.addReminder(
                    title = "CRUD测试提醒",
                    startTime = calendar.timeInMillis,
                    description = "测试用"
                )
                
                if (id == null) {
                    runOnUiThread {
                        result.value += "❌ 添加失败，测试中止\n"
                        loading.value = false
                    }
                    return@Thread
                }
                
                result.value += "✅ 添加成功，ID=$id\n\n"
                Thread.sleep(500)
                
                // 2. 查询
                result.value += "【步骤2】查询提醒\n"
                val queryResult = apiHelper.queryReminderById(id)
                if (queryResult != null && queryResult.count > 0) {
                    result.value += "✅ 查询成功，找到记录\n\n"
                } else {
                    result.value += "❌ 查询失败\n\n"
                }
                Thread.sleep(500)
                
                // 3. 更新
                result.value += "【步骤3】更新提醒\n"
                val updated = apiHelper.updateReminder(
                    id = id,
                    title = "CRUD测试提醒（已更新）",
                    startTime = calendar.timeInMillis,
                    description = "已修改"
                )
                result.value += if (updated) "✅ 更新成功\n\n" else "❌ 更新失败\n\n"
                Thread.sleep(500)
                
                // 4. 删除
                result.value += "【步骤4】删除提醒\n"
                val deleted = apiHelper.deleteReminder(id)
                result.value += if (deleted) "✅ 删除成功\n\n" else "❌ 删除失败\n\n"
                Thread.sleep(500)
                
                // 5. 验证删除
                result.value += "【步骤5】验证删除\n"
                val verifyResult = apiHelper.queryReminderById(id)
                if (verifyResult == null || verifyResult.count == 0) {
                    result.value += "✅ 验证成功，记录已删除\n\n"
                } else {
                    result.value += "⚠️ 记录仍然存在\n\n"
                }
                
                runOnUiThread {
                    result.value += "========== 测试完成 ==========\n"
                    showToast("✅ CRUD测试完成")
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "\n❌ 测试异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testCurriculumCRUD(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "开始完整CRUD测试（课程）...\n\n"
        
        Thread {
            try {
                // 1. 添加
                result.value += "【步骤1】添加课程\n"
                val id = apiHelper.addCurriculum(
                    name = "CRUD测试课程",
                    timeRule = "2-3-4", // 周二第3-4节
                    weeks = "1,2,3,4,5",
                    location = "测试教室"
                )
                
                if (id == null) {
                    runOnUiThread {
                        result.value += "❌ 添加失败，测试中止\n"
                        loading.value = false
                    }
                    return@Thread
                }
                
                result.value += "✅ 添加成功，ID=$id\n\n"
                Thread.sleep(500)
                
                // 2. 查询
                result.value += "【步骤2】查询课程\n"
                val queryResult = apiHelper.queryCurriculumById(id)
                if (queryResult != null && queryResult.count > 0) {
                    result.value += "✅ 查询成功\n\n"
                } else {
                    result.value += "❌ 查询失败\n\n"
                }
                Thread.sleep(500)
                
                // 3. 更新
                result.value += "【步骤3】更新课程\n"
                val updated = apiHelper.updateCurriculum(
                    id = id,
                    name = "CRUD测试课程（已更新）",
                    timeRule = "2-3-4",
                    weeks = "1,2,3,4,5",
                    location = "测试教室（新）"
                )
                result.value += if (updated) "✅ 更新成功\n\n" else "❌ 更新失败\n\n"
                Thread.sleep(500)
                
                // 4. 删除
                result.value += "【步骤4】删除课程\n"
                val deleted = apiHelper.deleteCurriculum(id)
                result.value += if (deleted) "✅ 删除成功\n\n" else "❌ 删除失败\n\n"
                Thread.sleep(500)
                
                // 5. 验证
                result.value += "【步骤5】验证删除\n"
                val verifyResult = apiHelper.queryCurriculumById(id)
                if (verifyResult == null || verifyResult.count == 0) {
                    result.value += "✅ 验证成功，记录已删除\n\n"
                } else {
                    result.value += "⚠️ 记录仍然存在\n\n"
                }
                
                runOnUiThread {
                    result.value += "========== 测试完成 ==========\n"
                    showToast("✅ CRUD测试完成")
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "\n❌ 测试异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun testTimeExpansion(result: MutableState<String>, loading: MutableState<Boolean>) {
        loading.value = true
        result.value = "测试重复规则的时间展开功能...\n\n"
        
        Thread {
            try {
                // 1. 添加一个每周重复的提醒
                result.value += "【步骤1】添加每周重复提醒\n"
                val calendar = Calendar.getInstance()
                
                val id = apiHelper.addReminder(
                    title = "时间展开测试",
                    startTime = calendar.timeInMillis,
                    repeatType = "weekly"
                )
                
                if (id == null) {
                    runOnUiThread {
                        result.value += "❌ 添加失败\n"
                        loading.value = false
                    }
                    return@Thread
                }
                
                result.value += "✅ 添加成功，ID=$id\n\n"
                Thread.sleep(500)
                
                // 2. 查询未来4周的展开实例
                result.value += "【步骤2】查询未来4周的展开实例\n"
                val startTime = calendar.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 4)
                val endTime = calendar.timeInMillis
                
                val queryResult = apiHelper.queryReminderByTimeRange(startTime, endTime)
                
                if (queryResult != null) {
                    result.value += "✅ 查询成功\n"
                    result.value += "找到 ${queryResult.count} 个实例\n\n"
                    
                    queryResult.items.filter { 
                        it["id"].toString() == id.toString() 
                    }.forEach { instance ->
                        val occTime = instance["occurrence_time"]
                        occTime?.let {
                            result.value += "• ${dateFormat.format(Date((it as Double).toLong()))}\n"
                        }
                    }
                    result.value += "\n"
                } else {
                    result.value += "❌ 查询失败\n\n"
                }
                
                // 3. 清理
                result.value += "【步骤3】清理测试数据\n"
                apiHelper.deleteReminder(id)
                result.value += "✅ 清理完成\n"
                
                runOnUiThread {
                    result.value += "\n========== 测试完成 ==========\n"
                    showToast("✅ 时间展开测试完成")
                    loading.value = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    result.value += "\n❌ 测试异常: ${e.message}\n"
                    loading.value = false
                }
            }
        }.start()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
