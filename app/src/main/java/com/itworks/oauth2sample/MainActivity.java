package com.itworks.oauth2sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import itworks.oauth2.OAuthToken;

/**
 * Created by bryang on 6/3/15.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView vwAuthToken = (TextView)findViewById(R.id.auth_token);
        Bundle bundle = getIntent().getExtras();

        final OAuthToken authToken = (OAuthToken)bundle.getSerializable("OAuthToken");
        vwAuthToken.setText(authToken.getAccessToken());


    }


}
