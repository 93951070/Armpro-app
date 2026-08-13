package armadillo.studio.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class ViewPonyRunning extends View {
    private Paint paint;
    private Paint dustPaint;
    private float bounceOffset = 0f;
    private float legPhase = 0f;
    private float dustOffset = 0f;
    private ValueAnimator animator;
    private int ponyColor;
    private static final int PONY_COLOR = 0xFFFF6B8A;
    private static final int DUST_COLOR = 0x33FF6B8A;

    public ViewPonyRunning(Context context) {
        super(context);
        init();
    }

    public ViewPonyRunning(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewPonyRunning(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ponyColor = PONY_COLOR;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ponyColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        dustPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dustPaint.setColor(DUST_COLOR);
        dustPaint.setStyle(Paint.Style.FILL);

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            bounceOffset = (float) Math.sin(progress * Math.PI * 2) * 6f;
            legPhase = progress;
            dustOffset = (dustOffset + 3f) % 40f;
            invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == VISIBLE) {
            animator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.cancel();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            animator.start();
        } else {
            animator.cancel();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = dpToPx(60);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f - 5f;
        float unit = w / 8f;

        canvas.save();
        canvas.translate(0, -bounceOffset);

        // Body (ellipse)
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(cx - unit * 2.2f, cy - unit * 0.8f, cx + unit * 2.2f, cy + unit * 0.8f, paint);

        // Head (circle)
        float headCx = cx + unit * 2.0f;
        float headCy = cy - unit * 1.0f;
        canvas.drawCircle(headCx, headCy, unit * 1.0f, paint);

        // Snout
        canvas.drawOval(headCx + unit * 0.5f, headCy - unit * 0.1f, headCx + unit * 1.3f, headCy + unit * 0.5f, paint);

        // Ear
        Path ear = new Path();
        ear.moveTo(headCx - unit * 0.3f, headCy - unit * 0.9f);
        ear.lineTo(headCx - unit * 0.1f, headCy - unit * 1.5f);
        ear.lineTo(headCx + unit * 0.2f, headCy - unit * 0.8f);
        ear.close();
        canvas.drawPath(ear, paint);

        // Eye
        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.WHITE);
        eyePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(headCx + unit * 0.3f, headCy - unit * 0.2f, unit * 0.25f, eyePaint);
        eyePaint.setColor(Color.parseColor("#2C2C2C"));
        canvas.drawCircle(headCx + unit * 0.35f, headCy - unit * 0.2f, unit * 0.12f, eyePaint);

        // Mane (curved shape on neck)
        paint.setStyle(Paint.Style.FILL);
        Path mane = new Path();
        mane.moveTo(cx + unit * 0.5f, cy - unit * 1.0f);
        mane.cubicTo(cx + unit * 0.3f, cy - unit * 1.8f, cx - unit * 0.3f, cy - unit * 1.5f, cx - unit * 0.5f, cy - unit * 0.8f);
        mane.cubicTo(cx - unit * 0.2f, cy - unit * 1.2f, cx + unit * 0.2f, cy - unit * 1.0f, cx + unit * 0.5f, cy - unit * 1.0f);
        canvas.drawPath(mane, paint);

        // Tail
        Path tail = new Path();
        float tailWave = (float) Math.sin(legPhase * Math.PI * 2) * 4f;
        tail.moveTo(cx - unit * 2.2f, cy - unit * 0.3f);
        tail.cubicTo(cx - unit * 3.0f, cy - unit * 0.5f + tailWave, cx - unit * 3.2f, cy + unit * 0.3f + tailWave, cx - unit * 2.8f, cy + unit * 0.8f + tailWave);
        tail.cubicTo(cx - unit * 2.5f, cy + unit * 0.3f, cx - unit * 2.3f, cy + unit * 0.1f, cx - unit * 2.2f, cy - unit * 0.3f);
        canvas.drawPath(tail, paint);

        // Legs (4 legs with running animation)
        float legWidth = unit * 0.35f;
        paint.setStrokeWidth(legWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        // Leg positions
        float[] legX = {cx - unit * 1.4f, cx - unit * 0.5f, cx + unit * 0.5f, cx + unit * 1.4f};
        for (int i = 0; i < 4; i++) {
            float phase = legPhase + (i * 0.25f);
            float legSwing = (float) Math.sin(phase * Math.PI * 2) * unit * 0.5f;
            float legBounce = Math.abs((float) Math.sin(phase * Math.PI * 2)) * unit * 0.3f;
            float startX = legX[i];
            float startY = cy + unit * 0.6f;
            float endX = startX + legSwing;
            float endY = startY + unit * 1.2f - legBounce;

            // Upper leg
            canvas.drawLine(startX, startY, (startX + endX) / 2, (startY + endY) / 2 - unit * 0.2f, paint);
            // Lower leg
            canvas.drawLine((startX + endX) / 2, (startY + endY) / 2 - unit * 0.2f, endX, endY, paint);
        }

        canvas.restore();

        // Dust particles behind pony (bottom area)
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 5; i++) {
            float dx = (dustOffset + i * 10f) % 50f;
            float dustX = cx - unit * 2.5f - dx;
            float dustY = cy + unit * 1.8f;
            float dustSize = unit * 0.15f * (1f - dx / 50f);
            int alpha = (int) (80 * (1f - dx / 50f));
            dustPaint.setColor(Color.argb(alpha, 255, 107, 138));
            canvas.drawCircle(dustX, dustY, dustSize, dustPaint);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
