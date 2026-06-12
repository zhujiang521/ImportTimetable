package com.zj.timetable

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson

/**
 * 第三方API助手类
 * 封装新版thirdPartyApi接口调用
 * 
 * 对应日历应用的新API：
 * - Authority: com.zui.app.calendar
 * - Method: thirdPartyApi
 * - 支持：提醒(reminder_*)和课程(curriculum_*)接口
 */
class ThirdPartyApiHelper(private val context: Context) {
    
    private val gson = Gson()
    private val calendarUri = "content://com.zui.app.calendar".toUri()
    
    companion object {
        private const val TAG = "ThirdPartyApiHelper"
    }
    
    // ==================== 通用API调用方法 ====================
    
    /**
     * 通用API调用方法
     * @param type 操作类型（如 reminder_add, curriculum_query 等）
     * @param data 数据对象（会被转换为JSON）
     * @return ApiResponse对象，如果调用失败返回null
     */
    private fun callApi(type: String, data: Any): ApiResponse? {
        return try {
            val jsonData = gson.toJson(data)
            
            Log.d(TAG, "========== API调用 ==========")
            Log.d(TAG, "Type: $type")
            Log.d(TAG, "Data: $jsonData")
            
            val bundle = Bundle().apply {
                putString("type", type)
                putString("data", jsonData)
            }
            
            val result = context.contentResolver.call(
                calendarUri,
                "thirdPartyApi",
                null,
                bundle
            )
            
            val jsonResult = result?.getString("result")
            Log.d(TAG, "Result: $jsonResult")
            Log.d(TAG, "==============================\n")
            
            if (jsonResult != null) {
                gson.fromJson(jsonResult, ApiResponse::class.java)
            } else {
                Log.e(TAG, "API返回结果为空")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "API调用异常: ${e.message}", e)
            null
        }
    }
    
    // ==================== 提醒接口 ====================
    
    /**
     * 添加提醒（简化模式）
     */
    fun addReminder(
        title: String,
        startTime: Long,
        description: String? = null,
        alarmTime: Int = 0,
        repeatType: String = "none",
        repeatDays: String? = null,
        repeatCount: Int? = null,
        repeatUntil: Long? = null,
        state: Int = 0,
        hasAlarm: Int = 1
    ): Long? {
        val data = mutableMapOf<String, Any>(
            "title" to title,
            "start_time" to startTime,
            "alarm_time" to alarmTime,
            "repeat_type" to repeatType,
            "state" to state,
            "has_alarm" to hasAlarm
        )
        
        description?.let { data["description"] = it }
        repeatDays?.let { data["repeat_days"] = it }
        repeatCount?.let { data["repeat_count"] = it }
        repeatUntil?.let { data["repeat_until"] = it }
        
        val response = callApi("reminder_add", data)
        return if (response?.success == true) {
            val dataMap = response.data as? Map<*, *>
            (dataMap?.get("id") as? Double)?.toLong()
        } else {
            Log.e(TAG, "添加提醒失败: ${response?.error}")
            null
        }
    }
    
    /**
     * 删除提醒
     */
    fun deleteReminder(id: Long): Boolean {
        val data = mapOf("id" to id)
        val response = callApi("reminder_delete", data)
        return response?.success == true
    }
    
    /**
     * 更新提醒（简化模式）
     */
    fun updateReminder(
        id: Long,
        title: String,
        startTime: Long,
        description: String? = null,
        alarmTime: Int = 0,
        repeatType: String = "none",
        repeatDays: String? = null,
        repeatCount: Int? = null,
        repeatUntil: Long? = null,
        state: Int = 0,
        hasAlarm: Int = 1
    ): Boolean {
        val data = mutableMapOf<String, Any>(
            "id" to id,
            "title" to title,
            "start_time" to startTime,
            "alarm_time" to alarmTime,
            "repeat_type" to repeatType,
            "state" to state,
            "has_alarm" to hasAlarm
        )
        
        description?.let { data["description"] = it }
        repeatDays?.let { data["repeat_days"] = it }
        repeatCount?.let { data["repeat_count"] = it }
        repeatUntil?.let { data["repeat_until"] = it }
        
        val response = callApi("reminder_update", data)
        return response?.success == true
    }
    
    /**
     * 查询提醒 - 按ID
     */
    fun queryReminderById(id: Long): QueryResult? {
        val data = mapOf("id" to id)
        val response = callApi("reminder_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    /**
     * 查询提醒 - 按标题模糊搜索
     */
    fun queryReminderByTitle(title: String): QueryResult? {
        val data = mapOf("title" to title)
        val response = callApi("reminder_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    /**
     * 查询提醒 - 按时间范围
     */
    fun queryReminderByTimeRange(startTime: Long, endTime: Long? = null): QueryResult? {
        val data = mutableMapOf<String, Any>("start_time" to startTime)
        endTime?.let { data["end_time"] = it }
        
        val response = callApi("reminder_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    /**
     * 查询所有提醒
     */
    fun queryAllReminders(): QueryResult? {
        val data = emptyMap<String, Any>()
        val response = callApi("reminder_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    // ==================== 课程接口 ====================
    
    /**
     * 添加课程
     * @param name 课程名称
     * @param timeRule 时间规则，格式："星期-节次" 或 "星期-开始节次-结束节次"
     *                 例如："1-1-2"(周一第1-2节), "3-4"(周三第4节)
     * @param weeks 上课周数，逗号分隔，如"1,2,3,4,5"。不传则使用所有周
     * @param location 上课地点
     * @param teacher 教师
     * @param color 课程颜色，格式"#RRGGBB"
     * @param hasAlarm 是否提醒，0=关闭, 1=开启
     */
    fun addCurriculum(
        name: String,
        timeRule: String,
        weeks: String? = null,
        location: String? = null,
        teacher: String? = null,
        color: String? = null,
        hasAlarm: Int = 1
    ): Long? {
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "time_rule" to timeRule,
            "has_alarm" to hasAlarm
        )
        
        weeks?.let { data["weeks"] = it }
        location?.let { data["location"] = it }
        teacher?.let { data["teacher"] = it }
        color?.let { data["color"] = it }
        
        val response = callApi("curriculum_add", data)
        return if (response?.success == true) {
            val dataMap = response.data as? Map<*, *>
            (dataMap?.get("id") as? Double)?.toLong()
        } else {
            Log.e(TAG, "添加课程失败: ${response?.error}")
            null
        }
    }
    
    /**
     * 删除课程
     */
    fun deleteCurriculum(id: Long): Boolean {
        val data = mapOf("id" to id)
        val response = callApi("curriculum_delete", data)
        return response?.success == true
    }
    
    /**
     * 更新课程
     */
    fun updateCurriculum(
        id: Long,
        name: String,
        timeRule: String,
        weeks: String? = null,
        location: String? = null,
        teacher: String? = null,
        color: String? = null,
        hasAlarm: Int = 1
    ): Boolean {
        val data = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "time_rule" to timeRule,
            "has_alarm" to hasAlarm
        )
        
        weeks?.let { data["weeks"] = it }
        location?.let { data["location"] = it }
        teacher?.let { data["teacher"] = it }
        color?.let { data["color"] = it }
        
        val response = callApi("curriculum_update", data)
        return response?.success == true
    }
    
    /**
     * 查询课程 - 按ID
     */
    fun queryCurriculumById(id: Long): QueryResult? {
        val data = mapOf("id" to id)
        val response = callApi("curriculum_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    /**
     * 查询课程 - 按名称模糊搜索
     */
    fun queryCurriculumByName(name: String): QueryResult? {
        val data = mapOf("name" to name)
        val response = callApi("curriculum_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    /**
     * 查询课程 - 按时间范围
     */
    fun queryCurriculumByTimeRange(startTime: Long, endTime: Long? = null): QueryResult? {
        val data = mutableMapOf<String, Any>("start_time" to startTime)
        endTime?.let { data["end_time"] = it }
        
        val response = callApi("curriculum_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    /**
     * 查询所有课程
     */
    fun queryAllCurriculums(): QueryResult? {
        val data = emptyMap<String, Any>()
        val response = callApi("curriculum_query", data)
        return if (response?.success == true) {
            parseQueryResult(response.data)
        } else {
            null
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 解析查询结果
     */
    private fun parseQueryResult(data: Any?): QueryResult? {
        return try {
            val dataMap = data as? Map<*, *>
            val count = (dataMap?.get("count") as? Double)?.toInt() ?: 0
            val items = when {
                dataMap?.containsKey("reminders") == true -> {
                    dataMap["reminders"] as? List<*> ?: emptyList<Any>()
                }
                dataMap?.containsKey("curriculums") == true -> {
                    dataMap["curriculums"] as? List<*> ?: emptyList<Any>()
                }
                else -> emptyList<Any>()
            }
            
            QueryResult(
                count = count,
                items = items.mapNotNull { it as? Map<*, *> }
            )
        } catch (e: Exception) {
            Log.e(TAG, "解析查询结果失败: ${e.message}", e)
            null
        }
    }
    
    // ==================== 数据类 ====================
    
    /**
     * API响应
     */
    data class ApiResponse(
        val success: Boolean,
        val message: String? = null,
        val data: Any? = null,
        val error: String? = null
    )
    
    /**
     * 查询结果
     */
    data class QueryResult(
        val count: Int,
        val items: List<Map<*, *>>
    )
}
