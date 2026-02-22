package com.hanu.pdfconverter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView tagline = findViewById(R.id.splash_tagline);

        // Initially invisible
        logo.setAlpha(0f);
        logo.setScaleX(0.3f);
        logo.setScaleY(0.3f);
        appName.setAlpha(0f);
        appName.setTranslationY(40f);
        tagline.setAlpha(0f);
        tagline.setTranslationY(30f);

        // Animate logo
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.3f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.3f, 1f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoAlpha, logoScaleX, logoScaleY);
        logoAnim.setDuration(700);
        logoAnim.setInterpolator(new OvershootInterpolator(1.2f));

        // Animate app name
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(appName, View.ALPHA, 0f, 1f);
        ObjectAnimator nameY = ObjectAnimator.ofFloat(appName, View.TRANSLATION_Y, 40f, 0f);
        AnimatorSet nameAnim = new AnimatorSet();
        nameAnim.playTogether(nameAlpha, nameY);
        nameAnim.setDuration(500);
        nameAnim.setStartDelay(400);
        nameAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate tagline
        ObjectAnimator tagAlpha = ObjectAnimator.ofFloat(tagline, View.ALPHA, 0f, 1f);
        ObjectAnimator tagY = ObjectAnimator.ofFloat(tagline, View.TRANSLATION_Y, 30f, 0f);
        AnimatorSet tagAnim = new AnimatorSet();
        tagAnim.playTogether(tagAlpha, tagY);
        tagAnim.setDuration(500);
        tagAnim.setStartDelay(650);
        tagAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        logoAnim.start();
        nameAnim.start();
        tagAnim.start();

        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2200);
    }
}
