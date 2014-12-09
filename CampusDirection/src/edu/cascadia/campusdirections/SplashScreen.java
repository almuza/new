// Campus Direction quick splash screen
package edu.cascadia.campusdirections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
 
public class SplashScreen extends Activity {
 
    // Splash screen timer set for 3 seconds
    private static int SPLASH_TIME_OUT = 3000;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
 
        new Handler().postDelayed(new Runnable() {
 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent splashscreen = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(splashscreen);
 
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
 
}