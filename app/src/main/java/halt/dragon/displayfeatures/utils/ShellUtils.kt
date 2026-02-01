package halt.dragon.displayfeatures.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object ShellUtils {

    fun checkRootAccess(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            p.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun wrapCommandWithSu(command: String): String {
        return "su -c \"$command\""
    }

    fun execRootCmd(command: String): String? {
        var process: Process? = null
        var os: DataOutputStream? = null
        var reader: BufferedReader? = null
        return try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                os?.close()
                reader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            process?.destroy()
        }
    }

    // Simpler version for single line read/write
    fun writeToFile(path: String, value: String): Boolean {
        // Many sysfs nodes require changing permissions or careful writing.
        // We stick to the basic echo for now.
        val cmd = "echo \"$value\" > \"$path\""
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun readFromFile(path: String): String {
        val cmd = "cat \"$path\""
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine() // usually just one line
            process.waitFor()
            result ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
