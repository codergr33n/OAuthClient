package itworks.oauth2;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by bryang on 6/4/15.
 * Utility class for reading Certs
 */
public class CertUtil {


    private AssetManager assetManager;
    private Context context = null;

    private static CertUtil theInstance = null;

    private CertUtil(Context context) {
        this.context = context;
        this.assetManager = context.getAssets();
    }

    public static CertUtil getInstance(Context context) {
        if(theInstance == null) {
            theInstance = new CertUtil(context);
        }
        return theInstance;
    }

    /**
     * Reads a CA cert from the Assets folder
     * @param certificateFile
     * @return An initialized Certificate
     */
    public Certificate loadCaCertificateFromAssets(String certificateFile) {

        CertificateFactory certificateFactory = null;
        InputStream inputStream = null;
        Certificate ca = null;
        try {

            inputStream = assetManager.open(certificateFile);
            certificateFactory = CertificateFactory.getInstance("X.509");
            ca = certificateFactory.generateCertificate(inputStream);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } catch (Exception e) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                // ignore
            }
        }

        return ca;
    }

    /**
     * Reads a client cert from the Assets folder
     * @param certificateFile
     * @param clientCertPassword
     * @return An initialized KeyStore
     * @throws Exception
     */
    public KeyStore loadPKCS12KeyStoreFromAssets(String certificateFile, String clientCertPassword) throws Exception {
        KeyStore keyStore = null;
        InputStream inputStream = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            inputStream = assetManager.open(certificateFile);
            keyStore.load(inputStream, clientCertPassword.toCharArray());

        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                // ignore
            }
        }
        return keyStore;
    }
}
