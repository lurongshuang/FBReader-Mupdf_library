package yuku.ambilwarna.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class AmbilWarnaPrefWidgetView extends View {
	Paint paint;
	float rectSize;
	float strokeWidth;

	boolean drawCross;

	public AmbilWarnaPrefWidgetView(Context context, AttributeSet attrs) {
		super(context, attrs);

		float density = context.getResources().getDisplayMetrics().density;
		rectSize = (float)Math.floor(24.f * density + 0.5f);
		strokeWidth = (float)Math.floor(1.f * density + 0.5f);

		paint = new Paint();
		paint.setColor(0xffffffff);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(strokeWidth);
	}

	public void showCross(boolean show) {
		drawCross = show;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

//		canvas.drawRect(strokeWidth, strokeWidth, rectSize - strokeWidth, rectSize - strokeWidth, paint);
		canvas.drawCircle(50, 500, 50, paint);
		RectF rect = new RectF(100, 700, 170, 800);
		canvas.drawRoundRect(rect, 30, 20, paint);
		if (drawCross) {
//			canvas.drawLine(strokeWidth, strokeWidth, rectSize - strokeWidth, rectSize - strokeWidth, paint);
//			canvas.drawLine(strokeWidth, rectSize - strokeWidth, rectSize - strokeWidth, strokeWidth, paint);
//			RectF oval = new RectF(350, 500, 450, 700);
//			 canvas.drawOval(oval, paint);
		}

	}
}
