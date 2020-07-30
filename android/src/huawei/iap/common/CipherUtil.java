package huawei.iap.common;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import huawei.iap.HuaweiIapModule;


public class CipherUtil {
    private static final String SIGN_ALGORITHMS = "SHA256WithRSA";

    /**
     * the method to check the signature for the data returned from the interface
     * @param content Unsigned data
     * @param sign the signature for content
     * @return boolean
     */
    public static boolean doCheck(String content, String sign) {
        if (HuaweiIapModule.API_KEY.equalsIgnoreCase("")) {
            Log.e(HuaweiIapModule.TAG, "api-key is null");
            return false;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(HuaweiIapModule.API_KEY, Base64.DEFAULT);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(content.getBytes("utf-8"));

            return signature.verify(Base64.decode(sign, Base64.DEFAULT));

        } catch (NoSuchAlgorithmException e) {
            Log.e(HuaweiIapModule.TAG, "doCheck NoSuchAlgorithmException" + e);
        } catch (InvalidKeySpecException e) {
            Log.e(HuaweiIapModule.TAG, "doCheck InvalidKeySpecException" + e);
        } catch (InvalidKeyException e) {
            Log.e(HuaweiIapModule.TAG, "doCheck InvalidKeyException" + e);
        } catch (SignatureException e) {
            Log.e(HuaweiIapModule.TAG, "doCheck SignatureException" + e);
        } catch (UnsupportedEncodingException e) {
            Log.e(HuaweiIapModule.TAG, "doCheck UnsupportedEncodingException" + e);
        }

        return false;
    }
}
