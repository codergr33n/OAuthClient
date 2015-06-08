package itworks.oauth2;


import android.content.Context;
import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


/**
 * Created by bryang on 6/3/15.
 * OAuthClient using a client cert, CA certificate, and a custom Trust manager
 */
public class OAuthClient {

    private final URI endpoint;
    private final String clientId;
    private final String secret;

    private final Context context;

    private String clientCertPassword;
    private String clientCertAssetPath;
    private String caCertAssetPath;

    private static final String TAG = OAuthClient.class.getName();


    /**
     * Creates an OAuthClient to handle IdentityServer communication
     * @param endpoint String representing a token endpoint
     * @param clientId String representing the client id param for identity server
     * @param secret String representing the secret param for identity server
     * @param clientCertAssetPath String representing the location of the client cert ex. ["certs/mycert.pfx"]
     * @param clientCertPassword String representing the client cert password
     * @param caCertAssetPath String representing the custom CA cert
     * @param context An initialized application context
     */
    public OAuthClient(URI endpoint, String clientId, String secret, String clientCertAssetPath,
                       String clientCertPassword, String caCertAssetPath,Context context) {

        this.endpoint = endpoint;
        this.clientId = clientId;
        this.secret = secret;

        this.clientCertAssetPath = clientCertAssetPath;
        this.clientCertPassword = clientCertPassword;
        this.caCertAssetPath = caCertAssetPath;
        this.context = context;
    }

    /**
     * Request an accessToken using ResourceOwner Flow with IdentityServer 3
     * @param username A String username
     * @param password A String password
     * @param scopes A String representing requested scopes
     * @return an OAuthToken
     */
    public OAuthToken RequestResourceOwnerPasswordAsync(String username, String password, String scopes) {


        OAuthToken oAuthToken = null;

        Map<String, String> mMap = new HashMap<String,String>();
        mMap.put(OAuthConstants.GRANT_TYPE,
                OAuthConstants.RESOURCE_OWNER_GRANT_TYPE);
        mMap.put(OAuthConstants.USERNAME,
                username);
        mMap.put(OAuthConstants.PASSWORD,
                password);
        mMap.put(OAuthConstants.SCOPE,
                scopes);




        //get an ssl context
        SSLContext sslContext = getSSlContext();

        try {

            URL url = endpoint.toURL();
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            ((javax.net.ssl.HttpsURLConnection)conn).setSSLSocketFactory(sslContext.getSocketFactory());

            //set the base header properties
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            //convert client id and secret to base64 encoding and add to the header
            byte[] authHeader = (clientId + ":" + secret).getBytes("UTF-8");
            String authHeaderEntry = Base64.encodeToString(authHeader, Base64.URL_SAFE);
            conn.setRequestProperty(OAuthConstants.AUTHORIZATION,"Basic " + authHeaderEntry);

            conn.setConnectTimeout(1500);
            conn.setReadTimeout(1500);
            conn.setRequestProperty("User-Agent", "ItWorksOAuthClient/1.0");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);

            //set the post data to the outputStream: http://www.programcreek.com/java-api-examples/index.php?api=javax.net.ssl.HttpsURLConnection
            String postData = BuildPostData(mMap);
            conn.setFixedLengthStreamingMode(postData.getBytes().length);

            DataOutputStream postOut=new DataOutputStream(conn.getOutputStream());
            postOut.writeBytes(postData);
            postOut.flush();
            postOut.close();

            //check the response code
            int responseCode = conn.getResponseCode();
            Log.d(TAG,"ResponseCode: " + responseCode);

            if(responseCode == HttpsURLConnection.HTTP_OK)
            {
                InputStream responseStream = conn.getInputStream();
                String response = ConvertToString(responseStream);

                JSONObject jsonResponse = new JSONObject(response);
                oAuthToken = new OAuthToken(jsonResponse.getLong("expires_in")
                        ,jsonResponse.getString("token_type")
                        ,jsonResponse.optString("refresh_token",null)
                        ,jsonResponse.getString("access_token"));

                Log.d(TAG, response);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return oAuthToken;

    }

    /**
     * Creates an SSLContext from a client cert and a custom CA
     * @return An initialized SSLContext for SSL/TLS communication
     */
    private SSLContext getSSlContext() {

        SSLContext sslContext = null;

        try {
            //Loading the client cert into a keymanager
            KeyStore keyStore =
                    CertUtil.getInstance(context).loadPKCS12KeyStoreFromAssets(clientCertAssetPath, clientCertPassword);

            //setup the keymanager : http://chariotsolutions.com/blog/post/https-with-client-certificates-on/
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, clientCertPassword.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();


            //load the root certificate : https://developer.android.com/training/articles/security-ssl.html
            Certificate ca = CertUtil.getInstance(context).loadCaCertificateFromAssets(caCertAssetPath);

            //create a custom TrustManager
            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            trustStore.setCertificateEntry("ca", ca);
            TrustManager[] trustManagers = {new CustomTrustManager(trustStore)};

            //This is needed once self signed cert is not here
            // Create a TrustManager that trusts the CAs in our KeyStore
            //String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            //TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            //tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

        } catch (Exception e) {

        }

        return sslContext;
    }


    /**
     * Converts a namevaluepair to String
     * @param params Key/Pair list of params to change
     * @return A URL encoded querystring representation of data
     * @throws UnsupportedEncodingException
     */
    private String BuildPostData(Map<String,String> params) throws UnsupportedEncodingException {
        String postData="";
        for (  String key : params.keySet()) {
            postData+="&" + key + "="+ params.get(key);
        }
        postData=postData.substring(1);

        return postData;
    }

    /**
     * Converts an InputStream to String
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static String ConvertToString(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            return "";
        }

        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];
            int available = 0;

            while ((available = bufferedInputStream.read(buffer)) >= 0) {
                byteArrayOutputStream.write(buffer, 0, available);
            }

            return byteArrayOutputStream.toString();

        } finally {
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
        }
    }

}
