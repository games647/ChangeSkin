package com.github.games647.changeskin.core.model.skin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VerifyUtil {

    private static final String SIGNATURE_ALG = "SHA1withRSA";
    private static PublicKey publicKey;

    static {
        URL keyUrl = VerifyUtil.class.getResource("/yggdrasil_session_pubkey.der");
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(readAllBytes(keyUrl));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(spec);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Error reading public key", e);
        }
    }

    public static boolean isValid(String value, String encodedSignature) throws GeneralSecurityException {
        Signature signature = Signature.getInstance(SIGNATURE_ALG);
        signature.initVerify(publicKey);
        signature.update(value.getBytes());
        return signature.verify(Base64.getDecoder().decode(encodedSignature));
    }

    private static byte[] readAllBytes(URL url) throws IOException {
        try (
                InputStream inputStream = url.openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        ) {
            int nRead;
            byte[] data = new byte[Short.MAX_VALUE];

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        }
    }
}
