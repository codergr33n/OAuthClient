# Android OAuth2 Client

Android OAuth2 Client currently handled resource owner flow with a self-signed cert and client cert.

This library was written to solve a few issues with existing OAuth libraries posted.  This is sucessfully connecting to an Azure instance using the embedded certs within the application

### Who
Initial version - Bryan Green (codergreen) bryan.green@redpigeoninteractive.com

### Version
1.0.0

### Tech


Used a series of sites to guide me through the code

* http://www.programcreek.com/java-api-examples/index.php?api=javax.net.ssl.HttpsURLConnection
* http://developer.android.com/training/articles/security-ssl.html#HttpsExample
* http://chariotsolutions.com/blog/post/https-with-client-certificates-on/

### Installation

```sh
$ git clone [git-repo-url] OAuthClient

```

### Contribute
If you would like to contribute, great!  I wouldn't mind additions to the library for other OAuth2 request types.

### Usage

``` java
OAuthClient client = new OAuthClient(new URI(OAuthConstants.TokenEndpoint)
                        , "roclient"
                        , "secret"
                        ,"certs/iwg-user.pfx"
                        ,getString(R.string.client_cert_password)
                        ,"certs/iwis-ssl-root.cer"
                        ,getBaseContext());

                //retrieve a token
                OAuthToken response = client.RequestResourceOwnerPasswordAsync(mUsername, mPassword, "esuite");
```


License
----

GNU General Public License

