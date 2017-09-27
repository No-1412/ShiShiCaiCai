package RTPower;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author ngh AES128 算法
 * <p/>
 * CBC 模式
 * <p/>
 * PKCS7Padding 填充模式
 * <p/>
 * CBC模式需要添加一个参数iv
 * <p/>
 * 介于java 不支持PKCS7Padding，只支持PKCS5Padding 但是PKCS7Padding 和 PKCS5Padding 没有什么区别
 * 要实现在java端用PKCS7Padding填充，需要用到bouncycastle组件来实现
 */
public class RTAES {

    /**
     * 公共密匙，与服务器保持一致
     */
    private IvParameterSpec ivspec;
    private SecretKeySpec keyspec;
    private Cipher cipher;

    /**
     * 设置加解密key
     *
     * @param IvString 要求16位
     * @param KeyString 要求16位
     */
    public void SetKey(String IvString, String KeyString) {

        ivspec = new IvParameterSpec(IvString.getBytes());//偏移量

        keyspec = new SecretKeySpec(KeyString.getBytes(), "AES");//生成密钥
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.err.println(e.getMessage());
        }
        // TODO Auto-generated catch block

    }

    /**
     * AES加密
     *
     * @param text 字符串
     * @return 输出加密结果（其中做了空位补足，也就是结果解密后结尾有空格）
     * @throws Exception
     */
    public byte[] encrypt(String text) throws Exception {

        if (text == null || text.length() == 0) {
            throw new Exception("传入字符串为空");
        }

        byte[] encrypted = null;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

            encrypted = cipher.doFinal(padString(text).getBytes());
        } catch (Exception e) {
            throw new Exception("[encrypt] " + e.getMessage());
        }

        return encrypted;
    }

    /**
     * AES解密
     *
     * @param code 加密字符串
     * @return 解密后的字符串，注意结尾有空格，要处理掉使用
     * @throws Exception
     */
    public byte[] decrypt(String code) throws Exception {
        if (code == null || code.length() == 0) {
            throw new Exception("Empty string");
        }

        byte[] decrypted = null;

        try {
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);//<span style="font-family: Simsun;font-size:16px; ">用密钥和一组算法参数初始化此 Cipher。</span>

            decrypted = cipher.doFinal(hexToBytes(code));
        } catch (Exception e) {
            throw new Exception("[decrypt] " + e.getMessage());
        }
        return decrypted;
    }

    /**
     *
     * @param data
     * @return
     */
    public String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        }

        int len = data.length;
        String str = "";
        for (int i = 0; i < len; i++) {
            if ((data[i] & 0xFF) < 16) {
                str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
            } else {
                str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
            }
        }
        return str;
    }

    public byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    private String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }

        return source;
    }

}
