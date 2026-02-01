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
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)

            // Append a unique marker to know when the command output ends
            val marker = "---CMD_END_MARKER---"
            os.writeBytes("$command\n")
            os.writeBytes("echo \"$marker\"\n")
            os.writeBytes("exit\n")
            os.flush()

            reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            // Read until we find the marker or end of stream
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains(marker)) {
                    break
                }
                output.append(line).append("\n")
            }

            process.waitFor()
            return output.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                os?.close()
                reader?.close()
                process?.destroy()
            } catch (e: Exception) { /* ignored */ }
        }
    }

    fun writeToFile(path: String, value: String): Boolean {
        // Using execRootCmd for consistency
        val cmd = "echo \"$value\" > \"$path\""
        return execRootCmd(cmd) != null
    }

    fun readFromFile(path: String): String {
        val cmd = "cat \"$path\""
        // Return raw result (trimmed in execRootCmd), or empty string on failure
        return execRootCmd(cmd) ?: "Error: Read Failed"
    }
}
