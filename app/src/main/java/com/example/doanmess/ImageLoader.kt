package com.example.doanmess

import android.app.Activity
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

class ImageLoader(private val cont: Activity) {
    val mutex = Mutex()
    suspend fun checkFile(Path: String, uid: String): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = File(cont.filesDir, "uid_avatar.json")
            val gson = Gson()
            val list: MutableMap<String, String> = if (file.exists()) {
                val bufferedReader = BufferedReader(FileReader(file))
                val type = object : TypeToken<MutableMap<String, String>>() {}.type
                gson.fromJson(bufferedReader, type)
            } else {
                mutableMapOf()
            }

            var res: String = ""
            if (list.containsKey(uid) && list[uid] == Path) {
                res = "${cont.filesDir}/$uid.jpg"
            } else {
                list[uid] = Path
                val json = gson.toJson(list)
                saveFile(file, json)
                downloadImage(Path, uid)
                res = "${cont.filesDir}/$uid.jpg"
            }
            return@withContext res
        }


    }

    fun downloadImage(urlPath: String, uid: String){
        val url = URL(urlPath)
        val connection = url.openConnection()
        connection.connect()

        val input: InputStream = connection.getInputStream()
        val output = FileOutputStream(File(cont.filesDir, "$uid.jpg"))
        copyStream(input, output)
    }

    fun copyStream(input: InputStream, output: OutputStream) {
        input.use { inputStream ->
            output.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun saveFile(file: File, json: String) {
        FileWriter(file).use { it.write(json) }
    }
}