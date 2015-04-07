package com.bounce;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener {
    private static final int CONFIRMATION_REQUEST_CODE = 0;
    private GoogleApiClient mGoogleApiClient;

    private TextView mTextView;
    private ImageButton bounce;
    private DelayedConfirmationView mDelayedView;
    private View mButton;

    private boolean mCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_main);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mDelayedView = (DelayedConfirmationView)
                findViewById(R.id.delayed_confirm);

        // Programmatically hide due to visibility bug when setting value in XML
        mDelayedView.setVisibility(View.GONE);

        // Set the timer to 2 seconds
        mDelayedView.setTotalTimeMs(3000);

        // Set this activity as a listener
        mDelayedView.setListener(this);

        // Set click behavior for the button that will trigger the timer
        mButton = findViewById(R.id.trigger_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BUTTON","pressed");
                animateInDelayedViewAndStartTimer();
            }
        });
    }
    @Override
    public void onTimerFinished(View view) {
        if (mCanceled) {
            // Timer was cancelled, do nothing
            return;
        }

        // Show the success animation
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                "Lets go!");
        startActivityForResult(intent, CONFIRMATION_REQUEST_CODE);
    }

    @Override
    public void onTimerSelected(View view) {
        // Indicate that the timer should do nothing when it finishes
        mCanceled = true;

        // Show a cancellation toast
        Toast.makeText(this, "Guess we're staying here", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void animateInDelayedViewAndStartTimer() {
        // We'll translate the views one full screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Animate the trigger button off to the left
        ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(mButton, View.TRANSLATION_X, 0,
                -screenWidth);
        // Animate the delayed confirmation view in from the right
        ObjectAnimator delayedViewAnimator = ObjectAnimator.ofFloat(mDelayedView, View.TRANSLATION_X,
                screenWidth, 0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(buttonAnimator, delayedViewAnimator);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                // Show the delayed confirmation view
                mDelayedView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Start the timer
                mDelayedView.start();
            }
        });
        set.setDuration(300);
        set.start();
    }




}
