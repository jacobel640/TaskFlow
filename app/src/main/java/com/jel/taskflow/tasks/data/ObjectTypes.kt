package com.jel.taskflow.tasks.data

import java.util.Date

fun main() {
    val taskClass1 = TaskClass(
        title = "",
        content = "",
        created = 0L,
        updated = 0L
    )
    val taskClass2 = TaskClass(
        title = "",
        content = "",
        created = 0L,
        updated = 0L
    )

    println(taskClass1 == taskClass2)

    val taskData1 = TaskData(
        title = "",
        content = "",
        created = 0L,
        updated = 0L
    )
    val taskData2 = TaskData(
        title = "",
        content = "",
        created = 0L,
        updated = 0L
    )

    println(taskData1 == taskData2)

    // anonymous object (-unnamed object)
    val someObject = object {
        val name = "jacob"
        val lastName = "elcharar"
        val age = 26
    }

    // anonymous object (-unnamed object - inheritance example)
    val anotherObject = object: Sensor() {
        override val name = "jacob"
        override fun startListening(listener: () -> Unit) {
            val onClick = {
                listener()
            }
        }

        val lastName = "elcharar"
        val age = 26
    }

    val email = Email("jacobel640@gmail.com")
}

class TaskClass(
    private var title: String,
    private var content: String,
    private var created: Long,
    private var updated: Long
) {
    var createdString = TaskObject.longDateToString(created)
    var updatedString = TaskObject.longDateToString(created)

    fun getTitle() = title
    fun getContent() = content
    fun getCreated() = created
    fun getUpdated() = updated

    fun setCreated(created: Long) {
        this.created = created
        this.createdString = TaskObject.longDateToString(created)
    }

    fun setUpdated(updated: Long) {
        this.updated = updated
        this.updatedString = TaskObject.longDateToString(updated)
    }
}

data class TaskData(
    private val title: String,
    private val content: String,
    private val created: Long,
    private val updated: Long
) {
    val createdString: String
        get() = TaskObject.longDateToString(created)
    val updatedString: String
        get() = TaskObject.longDateToString(updated)
    fun getTitle() = title
    fun getContent() = content
    fun getCreated() = created
    fun getUpdated() = updated
}

object TaskObject {
    fun longDateToString(date: Long): String {
        return Date(date).toString()
    }
}

enum class ResultStatus {
    OK,
    BAD_REQUEST,
    NOT_FOUND
}

enum class ResultCode(val code: Int, val message: String) {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found")
}

sealed class NetworkResult(val isServerCall: Boolean) {
    data class Success(val data: String): NetworkResult(true)
    data class Error(val message: String, val resultCode: ResultCode): NetworkResult(true)
    data object Empty: NetworkResult(false)
}

sealed interface NetworkResultInterface {
    data class Success(val data: String): NetworkResultInterface
    data class Error(val message: String, val resultCode: ResultCode): NetworkResultInterface
    data object Empty: NetworkResultInterface
}

abstract class Sensor {
    abstract val name: String // must implemented in child sensors
    abstract fun startListening(listener: () -> Unit) // must implemented in child sensors
    open fun stopListening() { // in this case 'open' means 'optional' (- optional implementation...)
        // write the default implementation
    }
}
/*                 /\ /\ --- \/ \/                 */
class HeartRateSensor: Sensor() {
    override val name = "Heart rate sensor"

    override fun startListening(listener: () -> Unit) {
        val onClick = {
            listener()
        }
    }
}
/*                 /\ /\ --- \/ \/                 */
open class ProximitySensor: Sensor() { // 'open' allows other objects to inherit from this 'non abstract' class
    override val name = "Proximity sensor"

    override fun startListening(listener: () -> Unit) {
        val onStart = {
            listener()
        }
    }

    override fun stopListening() {
//        super.stopListening()
        // override the default implementation (optional)
    }
}
/*                 /\ /\ --- \/ \/                 */
class CustomProximitySensor: ProximitySensor() {
    override val name = "Custom ${super.name}"
}

@JvmInline
value class Email(val address: String) {
    init {
        if (!address.contains("@")) {
            throw IllegalArgumentException("Invalid email format!")
        }
    }
}

annotation class DoNotDoThis

@DoNotDoThis
class SillyClassExample() {

}