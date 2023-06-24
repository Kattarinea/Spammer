package com.example.spammer;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {

    public Key publicKey = null;
    public Key secretKey = null;
    SecretKeySpec sks = null;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String generateKey()
    {
        try
        {
            KeyPairGenerator keyPairGenerator =  KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            publicKey = keyPair.getPublic();
            secretKey = keyPair.getPrivate();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new String(Base64.getEncoder().encode(publicKey.getEncoded()));
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public  String encryptData(SecretKeySpec key, String data)
    {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encr = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encr);

        } catch (Exception e) { }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String decryptData(String data, SecretKeySpec key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] textBytes = cipher.doFinal(Base64.getDecoder().decode(data));
            String text = new String(textBytes);
            return text;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SecretKeySpec getKeyFromEncrKey(String key)
    {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKeySpec originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return originalKey;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    public String secretKeySpec() throws NoSuchAlgorithmException {

        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("ih86hk4go0@5fqzx".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {

        }
        return new String(Base64.getEncoder().encode(sks.getEncoded()));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public  String encryptSecKey(String key,String sessionKey_str) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        byte[] keyBytes = android.util.Base64.decode(key, android.util.Base64.DEFAULT); //построение java.security.PublicKey из приходящей строки
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes); //ключ, закодированный по стандарту X.509
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicKeyNew = keyFactory.generatePublic(x509KeySpec);//Генерирует объект открытого ключа на основе предоставленной спецификации ключа (ключевого материала)
        byte[] encodedBytes = null;

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKeyNew);
        try {
            encodedBytes = cipher.doFinal(sessionKey_str.getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return java.util.Base64.getEncoder().encodeToString(encodedBytes);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String decryptSecKey(String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        byte[] encrypted_bytes = java.util.Base64.getDecoder().decode(key);
        byte[] decr=null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decr = cipher.doFinal(encrypted_bytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new String(decr);
    }


}


