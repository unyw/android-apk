package org.unyw


import android.os.Build
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest


val systemArch
    get() = fun () : String {
        for (androidArch in Build.SUPPORTED_ABIS) when (androidArch) {
            "arm64-v8a"   ->  return "aarch64"
            "armeabi-v7a" ->  return "armv7"
            "x86_64"      ->  return "x86_64"
            "x86"         ->  return "x86"
        }
        return "armhf" // If nothing is found, try with the most common one
    }()



fun checkMD5(md5: String?, updateFile: File?): Boolean {
    try {
        if (TextUtils.isEmpty(md5) || md5 == null || updateFile == null) return false
        val calculatedDigest = calculateMD5(updateFile) ?: return false
        return calculatedDigest.equals(md5, ignoreCase = true)
    }catch(ignored : Error){}
    return false
}

fun calculateMD5(updateFile: File): String? {
    val digest= MessageDigest.getInstance("MD5")

    val input = FileInputStream(updateFile)

    val buffer = ByteArray(8192)
    var read = input.read(buffer)
    while (read > 0) {
        digest.update(buffer, 0, read)
        read = input.read(buffer)
    }
    val md5sum = digest.digest()
    val bigInt = BigInteger(1, md5sum)
    var output = bigInt.toString(16)
    // Fill to 32 chars
    output = String.format("%32s", output).replace(' ', '0')
    input.close()
    return output
}