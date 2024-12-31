package com.example.doanmess.helper

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AESUtils {
    // Tạo khóa AES ngẫu nhiên
    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(128) // Kích thước khóa: 128 bit
        return keyGenerator.generateKey()
    }

    // Chuyển đổi khóa từ chuỗi Base64 thành SecretKey
    fun decodeBase64ToSecretKey(base64Key: String): SecretKey {
        val decodedKey = Base64.decode(base64Key, Base64.DEFAULT) // Sửa lại flag đúng
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    // Mã hóa dữ liệu
    fun encryptMessage(message: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(message.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT) // Sửa lại flag đúng
    }

    // Giải mã dữ liệu
    fun decryptMessage(encryptedMessage: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT) // Sửa lại flag đúng
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }
}

//if (type == "text") {
//    val base64Key = "q+xZ9yXk5F8WlKsbJb4sHg=="
//
//    // Tạo đối tượng AESUtils
//    val aesUtils = AESUtils()
//
//    // Giải mã khóa từ chuỗi Base64 thành SecretKey
//    val secretKey = aesUtils.decodeBase64ToSecretKey(base64Key)
//
//    // Kiểm tra nếu `content` là null hoặc không hợp lệ
//    val contentStr = content?.toString() ?: "Invalid content"
//
//    // Giải mã nội dung với try-catch
//    val txt = try {
//        aesUtils.decryptMessage(contentStr, secretKey) // Gọi hàm giải mã từ lớp AESUtils
//    } catch (e: Exception) {
//        e.printStackTrace() // Ghi log lỗi nếu có
//        null // Trả về null nếu xảy ra lỗi
//    }
//
//    // Kiểm tra kết quả giải mã
//    if (txt != null) {
//        // Gán nội dung giải mã thành công cho `content`
//        content = txt
//    } else {
//        // Thực hiện hành động khi giải mã thất bại
//        println("Decryption failed. Content remains encrypted or invalid.")
//    }
//}