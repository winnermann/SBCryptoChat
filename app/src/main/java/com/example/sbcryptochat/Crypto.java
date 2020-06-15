package com.example.sbcryptochat;


import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

// этот класс будет заниматься шифрованием и дешифрованием наших сообщений
public class Crypto {
    //секретное слово
    private static final String pass = "медвежьяпипирочка";
    //объявить переменную для секретного ключа
    private static SecretKeySpec keySpec;
    //этот блок кода делает из секретного слова секретный ключ keySpec
    static {
        try {


        //этот алгоритм (SHA-256) из секретного слова делает хэш секретного слова
        //хэш - это строка из которой можно получить секретное слово
        //мишаня => kfghkfhgukdshgudshuasi
        //мишаня => kfghkfhgukdshgudshuasi
        //kfghkfhgukdshgudshuasi => ???
        //хэширование - односторонняя операция
        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
        //преобразовать слово в массив байтов. Пример: skillbox => [s, k, i, l, l, b, o, x]
        byte[] bytes = pass.getBytes();
        //передать массив байтов bytes в shaDigest, чтобы произвести операцию хэширования
        //передать массив байтов от нуля до его длины (bytes.length)
        shaDigest.update(bytes, 0, bytes.length);
        //возврвщает массив байт хэша
        byte[] hash = shaDigest.digest();
        //сделать спецификацию секретного ключа (keySpec) из хэша (hash)
        //передаем ключ (hash) и тип алгоритма симметричного шифрования "AES"
        //симметричное - это значит что используя "AES" мы можем и зашифровать, и расшифровать
        keySpec = new SecretKeySpec(hash, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    };
    //Создали объект шифра
    //Настроили его
    //Зашифровали строку
    //Завернули строку в Base64
    //Объявим функцию которая будет заниматья шифрованием
    public static String encrypt (String unencryptedText) throws Exception{
        //из незашифрованного текста используя секретный ключ получить зашифрованный
        //создать объект который будет отвечать за алгоритм шифрования, будет делать шифрование
        //создать алгоритм шифрования "AES"
        Cipher cipher = Cipher.getInstance("AES");
        //настроить (инициализировать) cipher
        //Cipher.DECRYPT_MODE - переключить в режим шифрования, keySpec - подставить ключ шифрования
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        //зашифруем нашу строчку unencryptedText
        //просто передать строку мы не можем, нужно передать массив байтов unencryptedText.getBytes()
        //doFinal-применить финальное шифрование
        byte[] encrypted = cipher.doFinal(unencryptedText.getBytes());
        //наш сервер работает по простому протоколу, кот. принимает на вход только Строки
        //привести к строке массив байт для отправки на сервер
        //Base64 переводит все нечитаемые символы в читаемые %;*?;*?*? => asdf
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    //Объявим обратный метод который будет заниматья расшифрованием
    public static String decrypt(String decryptedText) throws  Exception{
        //операция обратная Base64.encodeToString(encrypted, Base64.DEFAULT)
        byte[] ciphered = Base64.decode(decryptedText, Base64.DEFAULT);
        //создать алгоритм шифрования "AES"
        Cipher cipher = Cipher.getInstance("AES");
        //настроить на режи дешифровки
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        //получить массив байт в котором хранится наш текст
        byte[] rawText = cipher.doFinal(ciphered);
        //преобразовать массив байт с текстом в строку с кодировкой UTF-8
        return new String(rawText, "UTF-8");
    }

}
