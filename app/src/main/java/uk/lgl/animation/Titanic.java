package uk.lgl.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.animation.LinearInterpolator;

import uk.lgl.modmenu.FloatingModMenuService;

public class Titanic {

    //Right to left: LEFT 2000, RIGHT 0
    //Left to right: LEFT 0, RIGHT 2000
    float LEFT = 2000;
    float RIGHT = 0;
    long setDuration = 20000;

    private AnimatorSet animatorSet;
    private Animator.AnimatorListener animatorListener;

    public Animator.AnimatorListener getAnimatorListener() {
        return animatorListener;
    }

    public void setAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.animatorListener = animatorListener;
    }

    public void start(final TitanicTextView textView) {

        final Runnable animate = new Runnable() {
            @Override
            public void run() {

                textView.setSinking(true);

                // horizontal animation. 200 = wave.png width


                ObjectAnimator maskXAnimator = ObjectAnimator.ofFloat(textView, "maskX", LEFT, RIGHT);
                maskXAnimator.setRepeatCount(ValueAnimator.INFINITE);
                //maskXAnimator.setDuration(1000);
                maskXAnimator.setDuration(setDuration); //So it does not repeat too often. You may need to increase value above too
                maskXAnimator.setStartDelay(0);

                int h = textView.getHeight();

                // vertical animation
                // maskY = 0 -> wave vertically centered
                // repeat mode REVERSE to go back and forth
                ObjectAnimator maskYAnimator = ObjectAnimator.ofFloat(textView, "maskY", h / 2, -h / 2);
                maskYAnimator.setRepeatCount(ValueAnimator.INFINITE);
                maskYAnimator.setRepeatMode(ValueAnimator.REVERSE);
                maskYAnimator.setDuration(10000);
                maskYAnimator.setStartDelay(0);

                // now play both animations together
                animatorSet = new AnimatorSet();
                animatorSet.playTogether(maskXAnimator, maskYAnimator);
                animatorSet.setInterpolator(new LinearInterpolator());
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        textView.setSinking(false);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            textView.postInvalidate();
                        } else {
                            textView.postInvalidateOnAnimation();
                        }

                        animatorSet = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });


                if (animatorListener != null) {
                    animatorSet.addListener(animatorListener);
                }

                animatorSet.start();
            }
        };

        if (!textView.isSetUp()) {
            textView.setAnimationSetupCallback(new AnimationSetupCallback() {
                @Override
                public void onSetupAnimation(final TitanicTextView target) {
                    animate.run();
                }

                @Override
                public void onSetupAnimation(TitanicButton Button) {

                }
            });
        } else {
            animate.run();
        }
    }

    public void start(final TitanicButton Button) {

        final Runnable animate = new Runnable() {
            @Override
            public void run() {

                Button.setSinking(true);

                // horizontal animation. 200 = wave.png width

                //Right to left: 2000, 0
                //Left to right: 0, 2000
                ObjectAnimator maskXAnimator = ObjectAnimator.ofFloat(Button, "maskX", LEFT, RIGHT);
                maskXAnimator.setRepeatCount(ValueAnimator.INFINITE);
                //maskXAnimator.setDuration(1000);
                maskXAnimator.setDuration(setDuration); //So it does not repeat too often. You may need to increase value above too
                maskXAnimator.setStartDelay(0);

                int h = Button.getHeight();

                // vertical animation
                // maskY = 0 -> wave vertically centered
                // repeat mode REVERSE to go back and forth
                ObjectAnimator maskYAnimator = ObjectAnimator.ofFloat(Button, "maskY", h / 2, -h / 2);
                maskYAnimator.setRepeatCount(ValueAnimator.INFINITE);
                maskYAnimator.setRepeatMode(ValueAnimator.REVERSE);
                maskYAnimator.setDuration(10000);
                maskYAnimator.setStartDelay(0);

                // now play both animations together
                animatorSet = new AnimatorSet();
                animatorSet.playTogether(maskXAnimator, maskYAnimator);
                animatorSet.setInterpolator(new LinearInterpolator());
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Button.setSinking(false);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            Button.postInvalidate();
                        } else {
                            Button.postInvalidateOnAnimation();
                        }

                        animatorSet = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });


                if (animatorListener != null) {
                    animatorSet.addListener(animatorListener);
                }

                animatorSet.start();
            }
        };

        if (!Button.isSetUp()) {
            Button.setAnimationSetupCallback(new AnimationSetupCallback() {
                @Override
                public void onSetupAnimation(final TitanicTextView target) {

                }

                @Override
                public void onSetupAnimation(TitanicButton Button) {
                    animate.run();
                }
            });
        } else {
            animate.run();
        }
    }

    public void cancel() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
    }
}