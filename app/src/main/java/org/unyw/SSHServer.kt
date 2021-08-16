package org.unyw

import android.util.Log
import com.jcraft.jsch.*
import java.util.*

import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


/*
        prefs = getPreferences(Context.MODE_PRIVATE)

    private var installStarted
        get() = prefs.getBoolean("org.unyw.install.installstarted", false)
        set(value) = prefs.edit().putBoolean("org.unyw.install.installstarted", value).apply()

 */

/*

        val ssh = SSHConnectionManager("192.168.1.11", "root", "sunhine", 2001)
        ssh.open()
        Log.d("SSSH",ssh.runCommand("ls /"))
        Log.d("SSSH",ssh.runCommand("date"))
        Log.d("SSSH",ssh.runCommand("ls /"))
        Log.d("SSSH",ssh.runCommand("date"))

        ssh.close()
 */
val MAX_SSH_CONNECTIONS = 50
val sshLock = Semaphore(MAX_SSH_CONNECTIONS)
var ssh: SSHConnectionManager? = null

fun startSSH(ms: MainService){
    sshLock.acquire(MAX_SSH_CONNECTIONS)
    val newSsh = SSHConnectionManager("localhost", "root", ms.tokens["UNYW_TOKEN_SSH"]!!, 12082)
    newSsh.open()
    ssh = newSsh
    sshLock.release(MAX_SSH_CONNECTIONS)
}


class SSHTimeoutException : Exception()


class SSHConnectionManager(private val hostname: String, private val username: String, private val password: String, private val port : Int = 22) {

    private lateinit var session : Session
    private lateinit var mChannel : Channel

    fun open (){
        while(true){
            try{

                val jSch = JSch();

                session = jSch.getSession(username, hostname, port);
                val config = Properties();
                config.put("StrictHostKeyChecking", "no");  // not recommended
                session.setConfig(config);
                Log.d("SSH_PASSWORD", password)
                session.setPassword(password);

                session.timeout = 500
                session.connect();
                Log.d("UNYW_SSH", "Connected!");
                return

            }catch (e : Exception){
                Thread.sleep(500)
                Log.e("UNYW_SSH", "Connection failed: " + e.toString());
            }
        }


    }


    fun runCommand(command: String, timeout : Long = 5_000): String? {
        val maxTime = System.currentTimeMillis() + timeout

        var ret: String? = ""
        if (!session.isConnected) open()
        var channel = session.openChannel("exec") as ChannelExec
        channel.setCommand(". /etc/profile; export DISPLAY=:3200; PS1='\\w\\\$ ' ;$command")
        channel.setEnv("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin")
        channel.inputStream = null
        val out = PrintStream(channel.outputStream)
        val `in`: InputStream = channel.inputStream // channel.getInputStream();
        channel.connect()

        // you can also send input to your running process like so:
        // String someInputToProcess = "something";
        // out.println(someInputToProcess);
        // out.flush();
        ret = getChannelOutput(channel, `in`, maxTime)
        channel.disconnect()
        println("Finished sending commands!")
        return ret
    }


    private fun getChannelOutput(channel: Channel?, `in`: InputStream, maxTime: Long): String? {
        val buffer = ByteArray(4096)
        val strBuilder = StringBuilder()
        val line = ""
        while (true) {
            if(System.currentTimeMillis() > maxTime){
                Log.e("UNYW_SSH", "TIMEOUT")
                channel!!.sendSignal("2")
                channel!!.sendSignal("9")
                channel!!.sendSignal("KILL")
                throw SSHTimeoutException()
            }
            while (`in`.available() > 0) {
                if(System.currentTimeMillis() > maxTime){
                    Log.e("UNYW_SSH", "TIMEOUT")
                    channel!!.sendSignal("2");
                    channel!!.sendSignal("9")
                    channel!!.sendSignal("KILL")
                    throw SSHTimeoutException()
                }
                val i: Int = `in`.read(buffer, 0, 4096)
                if (i < 0) break
                strBuilder.append(String(buffer, 0, i))
                //println(line)
            }
            if (line.contains("logout")) break
            if (channel!!.isClosed) break

            try {
                Thread.sleep(100)
            } catch (ee: Exception) { }
        }
        return strBuilder.toString()
    }

    fun close() {
        session.disconnect()
        println("Disconnected channel and session")
    }


}