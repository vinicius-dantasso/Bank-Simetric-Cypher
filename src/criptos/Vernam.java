package criptos;

public class Vernam {
    
    public static String encrypt(String text, String key) {

        StringBuilder ciphertext = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char encryptedChar = (char) (text.charAt(i) ^ key.charAt(i % key.length()));
            ciphertext.append(encryptedChar);
        }
        return ciphertext.toString();

    }

    public static String decrypt(String ciphertext, String key) {
        return encrypt(ciphertext, key);
    }

}
