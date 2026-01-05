package com.subu.OpenScheme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.subu.weddingcraft.R;

public class ChooseTheme extends AppCompatActivity {
    private GestureDetector gestureDetector;
    private ImageView imageView;
    private int currentImageIndex = 0;
    private final int[] imageResIds = {
            R.drawable.royal_theme_01,
            R.drawable.elegant_theme_01
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_activity_choose_theme);

        String cardType = getIntent().getStringExtra("type");

        imageView = findViewById(R.id.themeViewImage);
        gestureDetector = new GestureDetector(this, new GestureListener());

        findViewById(R.id.main).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });


        findViewById(R.id.btnSelectTheme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseTheme.this, GenerateWedCard.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", cardType);
                switch (currentImageIndex){
                    case 0:
                        intent.putExtra("theme", "royal");
                        break;
                    case 1:
                        intent.putExtra("theme", "elegant");
                        break;
                }
                startActivity(intent);
            }
        });
    }



    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true; // Must return true to detect gestures
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            assert e1 != null;
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private void onSwipeRight() {
        slideImage(true);
    }

    private void onSwipeLeft() {
        slideImage(false);
    }

    private void slideImage(boolean toRight) {
        int width = imageView.getWidth();
        Animation outAnim = new TranslateAnimation(0, toRight ? width : -width, 0, 0);
        outAnim.setDuration(300);
        outAnim.setFillAfter(false);

        Animation inAnim = new TranslateAnimation(toRight? -width: width, 0, 0, 0);
        inAnim.setDuration(300);
        inAnim.setFillAfter(true);


        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change the image
                currentImageIndex = (currentImageIndex + (toRight ? -1 : 1) + imageResIds.length) % imageResIds.length;
                imageView.setImageResource(imageResIds[currentImageIndex]);

                // Start the "in" animation
                imageView.startAnimation(inAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        imageView.startAnimation(outAnim);
    }
}