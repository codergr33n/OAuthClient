package itworks.oauth2;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by bryang on 6/3/15.
 * OAuthToken
 */
public class OAuthToken implements Serializable{
    private final Long expiresIn;
    private final Long expiresAt;
    private final String tokenType;
    private final String refreshToken;
    private final String accessToken;


    public OAuthToken(Long expiresIn, String tokenType, String refreshToken, String accessToken) {
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.expiresAt = (expiresIn * 1000) + System.currentTimeMillis();
    }


    public Long getExpiresIn() {
        return expiresIn;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public String getTokenType() {
        return tokenType;
    }


    public String getRefreshToken() {
        return refreshToken;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() >= this.getExpiresAt()) ? true : false;
    }



}
