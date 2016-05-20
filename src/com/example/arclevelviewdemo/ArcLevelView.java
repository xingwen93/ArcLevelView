package com.example.arclevelviewdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * onDraw方法需要优化，尽可能的减少对象的创建
 * @author Administrator
 *
 */
public class ArcLevelView extends View {
	public static final String TAG = ArcLevelView.class.getSimpleName();

	private Paint mArcPaint = new Paint();
	private Paint mProgressArcPaint = new Paint();
	private RectF mExternalRingRect;
	private float mExternalRingWidth;
	private RectF mInternalRingRect;
	private float mInternalRingWidth;
	private float mLeftAndRightPadding;

	private Paint mLevelPaint = new Paint();
	private int mExternalLevelCount = 21;
	private int mInternalLevelCount = 12;
	private float mBigLevelWidth, mBigLevelLength;
	private float mSmallLevelWidth, mSmallLevelLength;

	private Paint mExternLevelTextPaint = new Paint();
	private Paint mInternalLevelTextPaint = new Paint();
	private Rect mTextRect = new Rect();

	private Paint mBottomTextPaint = new Paint();
	private String[] mExternalBigLevelTexts = { "1万", "5万" };
	private float[] mExternalBigLevelValues = { 0.0f, 10000.0f, 50000.0f, 300000.0f, Integer.MAX_VALUE >> 2 };
	private String[] mInternalBigLevelTexts = { "7.0", "7.2", "7.4", "7.6" };
	private String mStartText = "总金额", mCenterText = "预期年化", mEndText = "30万";
	private Rect mStartTextRect = new Rect(), mCenterTextRect = new Rect(), mEndTextRect = new Rect();
	private float mBottomTextTopPadding; // 上面三个文本的paddingTop值

	private Paint mArrowPaint = new Paint();
	private Path mArrowPath = new Path();
	private float mArrowCircleRadius;
	private float mArrowLength;

	private float mExternalRingRadius, mExternalTextRadius;
	private float mInternalRingRadius, mInternalTextRadius;

	private float mMaxProgress = 300000;
	private float mCurrentProgress;
	private float mProgressPercent;
	private float mAnimationProgressPercent; // 动态变化的
	private float mArrowAngle;
	private float mAnimationAngle; // 动态变化的

	private float mStartAngle = 160;
	private float mSweepAngle = 220;

	private Block[] mBlocks = new Block[4];
	private Block mCurrentBlock;
	private int mWidth, mHeight;
	private int mViewRadius;
	private float mCenterX, mCenterY;

	private String mInterestRateText = mInternalBigLevelTexts[0];
	
	public ArcLevelView(Context context) {
		this(context, null);
	}

	public ArcLevelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPadding(0, 0, 0, 0);
		init();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode != heightMode) {
			widthMode = heightMode = MeasureSpec.AT_MOST;
			mWidth = mHeight = mViewRadius * 2;
		} else {
			if (widthMode == MeasureSpec.EXACTLY) {
				mWidth = Math.max(mViewRadius * 2, widthSize);
			} else if (widthMode == MeasureSpec.AT_MOST) {
				mWidth = mViewRadius * 2;
			}

			if (heightMode == MeasureSpec.EXACTLY) {
				mHeight = Math.max(mViewRadius * 2, heightSize);
			} else if (widthMode == MeasureSpec.AT_MOST) {
				mHeight = mViewRadius * 2;
			}
		}

		mWidth = mHeight = Math.min(mWidth, mHeight);

		mCenterX = mCenterY = mViewRadius = mWidth / 2;

		float halfPadding = mLeftAndRightPadding / 2;
		mExternalRingRadius = mViewRadius - halfPadding - mExternalRingWidth / 2; // 相对于(mCenterX,
																					// mCenterY)点的半径
		if (mExternalRingRect == null) {
			mExternalRingRect = new RectF(halfPadding, halfPadding, mWidth - halfPadding, mHeight - halfPadding);
		}

		mInternalRingRadius = (mViewRadius - halfPadding) / 2 - mInternalRingWidth / 2; // 相对于(mCenterX,
																						// mCenterY)点的半径,
																						// 这个半径是长度值
		if (mInternalRingRect == null) {
			float iLeft = mViewRadius - mInternalRingRadius + mInternalRingWidth / 2;
			float iTop = mViewRadius - mInternalRingRadius + mInternalRingWidth / 2;
			float iRight = iLeft + mInternalRingRadius * 2 - mInternalRingWidth / 2;
			float iBottom = iTop + mInternalRingRadius * 2 - mInternalRingWidth / 2;
			mInternalRingRect = new RectF(iLeft, iTop, iRight, iBottom);
		}

		mExternalTextRadius = mExternalRingRadius - mBigLevelLength - dpToPx(8);
		mInternalTextRadius = mInternalRingRadius - mBigLevelLength - dpToPx(8);

		mArrowCircleRadius = dpToPx(4);
		mArrowLength = mInternalRingRadius / 2 + dpToPx(8);

		setMeasuredDimension(mWidth, mHeight);
	}

	private void init() {
		mViewRadius = dpToPx(140);
		mExternalRingWidth = dpToPx(9);
		mInternalRingWidth = dpToPx(3);
		mBigLevelWidth = dpToPx(3);
		mSmallLevelWidth = dpToPx(2);
		mBigLevelLength = dpToPx(5);
		mSmallLevelLength = dpToPx(3);
		mBottomTextTopPadding = dpToPx(24);

		mArcPaint.setAntiAlias(true);
		mArcPaint.setDither(true);
		mArcPaint.setStyle(Style.STROKE);
		mArcPaint.setStrokeCap(Cap.ROUND);

		mExternLevelTextPaint.setAntiAlias(true);
		mExternLevelTextPaint.setDither(true);
		mExternLevelTextPaint.setColor(Color.WHITE);
		mExternLevelTextPaint.setTextSize(dpToPx(12));
		mExternLevelTextPaint.setStyle(Paint.Style.STROKE);

		mInternalLevelTextPaint.setAntiAlias(true);
		mInternalLevelTextPaint.setDither(true);
		mInternalLevelTextPaint.setColor(Color.WHITE);
		mInternalLevelTextPaint.setTextSize(dpToPx(12));
		mInternalLevelTextPaint.setStyle(Paint.Style.STROKE);

		mBottomTextPaint.setAntiAlias(true);
		mBottomTextPaint.setDither(true);
		mBottomTextPaint.setColor(Color.WHITE);
		mBottomTextPaint.setTextSize(dpToPx(12));
		mBottomTextPaint.setStyle(Paint.Style.STROKE);
		mBottomTextPaint.getTextBounds(mStartText, 0, mStartText.length(), mStartTextRect);
		mBottomTextPaint.getTextBounds(mCenterText, 0, mStartText.length(), mCenterTextRect);
		mBottomTextPaint.getTextBounds(mEndText, 0, mStartText.length(), mEndTextRect);
		mLeftAndRightPadding = Math.max(mStartTextRect.width(), mEndTextRect.width());

		mLevelPaint.setAntiAlias(true);
		mLevelPaint.setDither(true);
		mLevelPaint.setStyle(Style.STROKE);
		mLevelPaint.setColor(Color.WHITE);

		mProgressArcPaint.setAntiAlias(true);
		mProgressArcPaint.setDither(true);
		mProgressArcPaint.setStyle(Style.STROKE);
		mProgressArcPaint.setStrokeCap(Cap.ROUND);
		mProgressArcPaint.setStrokeWidth(mExternalRingWidth);
		mProgressArcPaint.setColor(Color.WHITE);

		mArrowPaint.setAntiAlias(true);
		mArrowPaint.setDither(true);
		mArrowPaint.setStyle(Style.FILL);
		mArrowPaint.setColor(Color.WHITE);

		mAnimationAngle = mStartAngle;

		initAngleBlocks();

	}

	private void initAngleBlocks() {
		for (int i = 0; i < mBlocks.length; i++) {
			mBlocks[i] = new Block();
		}

		final float averageAngle = (float) mSweepAngle / mExternalLevelCount;
		int blockIndex = 0;

		mBlocks[0].startValue = mExternalBigLevelValues[0];
		mBlocks[0].startAngle = mStartAngle;
		for (int level = 0; level < mExternalLevelCount + 1; level++) {
			if (level != 0 && level % 7 == 0) {
				mBlocks[blockIndex].endValue = mExternalBigLevelValues[blockIndex + 1];
				mBlocks[blockIndex].endAngle = mStartAngle + level * averageAngle;
				mBlocks[blockIndex + 1].startValue = mExternalBigLevelValues[blockIndex + 1];
				mBlocks[blockIndex + 1].startAngle = mStartAngle + level * averageAngle;
				blockIndex++;
			}
		}
		mBlocks[blockIndex].endValue = mExternalBigLevelValues[blockIndex + 1];
		mBlocks[blockIndex].endAngle = Integer.MAX_VALUE >> 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawArcs(canvas);
		drawExternalLevels(canvas);
		drawExternalLevelTexts(canvas);
		drawInternalLevels(canvas);
		drawInternalLevelTexts(canvas);
		drawBottomTexts(canvas);
		drawPointerArrow(canvas);
		drawProgress(canvas);
	}

	/**
	 * 画圆弧
	 * @param canvas
	 */
	private void drawArcs(Canvas canvas) {
		mArcPaint.setColor(Color.parseColor("#FE6765"));
		mArcPaint.setStrokeWidth(mExternalRingWidth);
		canvas.drawArc(mExternalRingRect, mStartAngle, mSweepAngle, false, mArcPaint);

		mArcPaint.setColor(Color.WHITE);
		mArcPaint.setStrokeWidth(mInternalRingWidth);
		canvas.drawArc(mInternalRingRect, mStartAngle, mSweepAngle, false, mArcPaint);
	}

	/**
	 * 画外部刻度
	 * @param canvas
	 */
	private void drawExternalLevels(Canvas canvas) {
		Point start, stop;
		float startLevelRadius, endLevelRadius;
		final float averageAngle = mSweepAngle / mExternalLevelCount;
		for (int level = 0; level < mExternalLevelCount + 1; level++) {
			if (level % 7 == 0) {
				mLevelPaint.setStrokeWidth(mBigLevelWidth);
				startLevelRadius = mExternalRingRadius;
				endLevelRadius = startLevelRadius - mBigLevelLength;
			} else {
				mLevelPaint.setStrokeWidth(mSmallLevelWidth);
				startLevelRadius = mExternalRingRadius;
				endLevelRadius = startLevelRadius - mSmallLevelLength;
			}
			float levelAngle = mStartAngle + averageAngle * level;
			start = getCoordinatePoint(startLevelRadius, levelAngle);
			stop = getCoordinatePoint(endLevelRadius, levelAngle);
			canvas.drawLine(start.x, start.y, stop.x, stop.y, mLevelPaint);
		}
	}

	/**
	 * 画内部刻度
	 * @param canvas
	 */
	private void drawInternalLevels(Canvas canvas) {
		Point start, stop;
		float startLevelRadius, endLevelRadius;
		final float averageAngle = mSweepAngle / mInternalLevelCount;
		for (int level = 0; level < mInternalLevelCount + 1; level++) {
			if (level % 4 == 0) {
				mLevelPaint.setStrokeWidth(mBigLevelWidth);
				startLevelRadius = mInternalRingRadius - mInternalRingWidth;
				endLevelRadius = startLevelRadius - mBigLevelLength;
			} else {
				mLevelPaint.setStrokeWidth(mSmallLevelWidth);
				startLevelRadius = mInternalRingRadius - mInternalRingWidth;
				endLevelRadius = startLevelRadius - mSmallLevelLength;
			}
			float levelAngle = mStartAngle + averageAngle * level;
			start = getCoordinatePoint(startLevelRadius, levelAngle);
			stop = getCoordinatePoint(endLevelRadius, levelAngle);
			canvas.drawLine(start.x, start.y, stop.x, stop.y, mLevelPaint);
		}
	}

	/**
	 * 画外部刻度文本
	 * @param canvas
	 */
	private void drawExternalLevelTexts(Canvas canvas) {
		final float averageAngle = (float) mSweepAngle / mExternalLevelCount;
		for (int level = 0, textIndex = 0; level < mExternalLevelCount + 1; level++) {
			if (level != 0 && level != mExternalLevelCount && level % 7 == 0) {
				float levelAngle = mStartAngle + averageAngle * level;
				String text = mExternalBigLevelTexts[textIndex];
				mExternLevelTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
				if (levelAngle % 360 > 135 && levelAngle % 360 < 225) {
					mExternLevelTextPaint.setTextAlign(Paint.Align.LEFT);
				} else if ((levelAngle % 360 >= 0 && levelAngle % 360 < 45) || (levelAngle % 360 > 315 && levelAngle % 360 <= 360)) {
					mExternLevelTextPaint.setTextAlign(Paint.Align.RIGHT);
				} else {
					mExternLevelTextPaint.setTextAlign(Paint.Align.CENTER);
				}
				Point textPoint = getCoordinatePoint(mExternalTextRadius, levelAngle);
				canvas.drawText(text, textPoint.x, textPoint.y + mTextRect.height(), mExternLevelTextPaint);
				textIndex++;
			}
		}
	}

	/**
	 * 画内部圆弧刻度文本
	 * @param canvas
	 */
	private void drawInternalLevelTexts(Canvas canvas) {
		final float averageAngle = (float) mSweepAngle / mInternalLevelCount;
		for (int level = 0, textIndex = 0; level < mInternalLevelCount + 1; level++) {
			if (level % 4 == 0) {
				float levelAngle = mStartAngle + averageAngle * level;
				String text = mInternalBigLevelTexts[textIndex];
				// if (Math.abs(levelAngle - mArrowAngle) < 1.0f) {
				// mInternalLevelTextPaint.setColor(Color.parseColor("#FDFF00"));
				// } else {
				// mInternalLevelTextPaint.setColor(Color.WHITE);
				// }

				if (mInterestRateText.equals(text)) {
					mInternalLevelTextPaint.setColor(Color.parseColor("#FDFF00"));
				} else {
					mInternalLevelTextPaint.setColor(Color.WHITE);
				}

				mInternalLevelTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
				if (levelAngle % 360 > 135 && levelAngle % 360 < 225) {
					mInternalLevelTextPaint.setTextAlign(Paint.Align.LEFT);
				} else if ((levelAngle % 360 >= 0 && levelAngle % 360 < 45) || (levelAngle % 360 > 315 && levelAngle % 360 <= 360)) {
					mInternalLevelTextPaint.setTextAlign(Paint.Align.RIGHT);
				} else {
					mInternalLevelTextPaint.setTextAlign(Paint.Align.CENTER);
				}
				Point textPoint = getCoordinatePoint(mInternalTextRadius, levelAngle);
				if (level == 0 || level == mInternalLevelCount) {
					canvas.drawText(text, textPoint.x, textPoint.y + mTextRect.height() / 2, mInternalLevelTextPaint);
				} else {
					canvas.drawText(text, textPoint.x, textPoint.y + mTextRect.height(), mInternalLevelTextPaint);
				}
				textIndex++;
			}
		}
	}

	/**
	 * 画底部文本
	 * @param canvas
	 */
	private void drawBottomTexts(Canvas canvas) {
		mBottomTextPaint.setTextAlign(Paint.Align.CENTER);

		// 绘制左边的文本
		mBottomTextPaint.setTextSize(dpToPx(12));
		Point startTextPoint = getCoordinatePoint(mExternalRingRadius, mStartAngle);
		canvas.drawText(mStartText, startTextPoint.x, startTextPoint.y + mBottomTextTopPadding, mBottomTextPaint);

		// 绘制中间的文本
		mBottomTextPaint.setTextSize(dpToPx(14));
		float y = startTextPoint.y + (mCenterTextRect.width() - mStartTextRect.width()) / 2;
		canvas.drawText(mCenterText, mViewRadius, y + mBottomTextTopPadding, mBottomTextPaint);

		// 绘制右边的文本
		Point endTextPoint = getCoordinatePoint(mExternalRingRadius, mStartAngle + mSweepAngle);
		mBottomTextPaint.setTextSize(dpToPx(14));
		canvas.drawText(mEndText, endTextPoint.x, endTextPoint.y + mBottomTextTopPadding, mBottomTextPaint);
	}

	/**
	 * 画进度圆弧
	 * @param canvas
	 */
	private void drawProgress(Canvas canvas) {
		float progressAngle = mAnimationProgressPercent * mSweepAngle;
		mProgressArcPaint.setStrokeWidth(mExternalRingWidth);
		canvas.drawArc(mExternalRingRect, mStartAngle, progressAngle, false, mProgressArcPaint);
	}

	/**
	 * 画中心的指针箭头
	 * @param canvas
	 */
	private void drawPointerArrow(Canvas canvas) {
		mArrowPath.reset();
		Point p1 = getCoordinatePoint(mArrowCircleRadius / 2, mAnimationAngle + 90);
		mArrowPath.moveTo(p1.x, p1.y);
		Point p2 = getCoordinatePoint(mArrowCircleRadius / 2, mAnimationAngle - 90);
		mArrowPath.lineTo(p2.x, p2.y);
		Point p3 = getCoordinatePoint(mArrowLength, mAnimationAngle);
		mArrowPath.lineTo(p3.x, p3.y);
		mArrowPath.close();
		canvas.drawPath(mArrowPath, mArrowPaint);
		canvas.drawCircle(mCenterX, mCenterY, mArrowCircleRadius, mArrowPaint);
	}

	public void setMaxProgress(int maxProgress) {
		mMaxProgress = maxProgress;
	}

	public float getMaxProgress() {
		return mMaxProgress;
	}

	public void clear() {
		mCurrentProgress = 0;
		mInterestRateText = mInternalBigLevelTexts[0];
	}

	public void setCurrentProgress(float currentProgress) {
		if (mCurrentProgress != currentProgress && currentProgress >= 0) {
			mCurrentProgress = currentProgress > mMaxProgress ? mMaxProgress : currentProgress;
			mCurrentBlock = getCurrentBlock();
			setProgressPercent(calculateProgressPercent());
		}
	}

	public float getCurrentProgress() {
		return mCurrentProgress;
	}
	
	private void setProgressPercent(float percent) {
		mProgressPercent = percent;
	}

	private float calculateBlockProgressPercent() {
		float blockProgressPercent = (mCurrentProgress - mCurrentBlock.startValue) 
				/ (mCurrentBlock.endValue - mCurrentBlock.startValue);
		return blockProgressPercent;
	}

	private float calculateProgressPercent() {
		float blockAngle = calculateBlockProgressPercent() * (mCurrentBlock.endAngle - mCurrentBlock.startAngle);
		float percent = (mCurrentBlock.startAngle - mStartAngle + blockAngle) / mSweepAngle;
		return percent;
	}

	private void setAnimationProgressPercent(float percent) {
		mAnimationProgressPercent = percent;
		final float averageAngle = (mArrowAngle - mStartAngle) / mProgressPercent;
		mAnimationAngle = mStartAngle + mAnimationProgressPercent * averageAngle;
//		mArrowAngle = mStartAngle + mProgressPercent * mSweepAngle;
//		for (int i = 0; i < mBlocks.length; i++) {
//			Block block = mBlocks[i];
//			if (block.startAngle <= mArrowAngle && mArrowAngle < block.endAngle) {
//				mArrowAngle = block.startAngle;
//			}
//		}
//		Log.w(TAG, "mArrowAngle: " + mArrowAngle);
		postInvalidate();
	}

	public void setInterestRate(String text) {
		final float averageAngle = (float) mSweepAngle / mInternalLevelCount;
		for (int level = 0, textIndex = 0; level < mInternalLevelCount + 1; level++) {
			if (level % 4 == 0) {
				if (text.contains(mInternalBigLevelTexts[textIndex])) {
					mArrowAngle = mStartAngle + averageAngle * level;
					Log.w(TAG, "mArrowAngle: " + mArrowAngle);
					mInterestRateText = text;
					return;
				} else {
					textIndex++;
				}
			}
		}
		// Log.w(TAG, "mArrowAngle: " + mArrowAngle);
	}

	/**
	 * 进度加载动画
	 */
	public void startProgressAnimation() {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				ValueAnimator anim = ValueAnimator.ofFloat(0f, calculateProgressPercent());
				anim.setDuration(500);
				anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						float value = (Float) animation.getAnimatedValue();
						setAnimationProgressPercent(value);
					}
				});
				anim.start();
			}
		}, 200);

	}

	/**
	 * 设置底部文本的paddingTop
	 * @param px
	 */
	public void setBottomTextPaddingTop(float px) {
		if (px > mBottomTextTopPadding) {
			mBottomTextTopPadding = px;
		}
		postInvalidate();
	}

	/**
	 * 获取底部文本底下的空白区域的高度
	 * @return
	 */
	public float getBottomSpaceHeight() {
		Point startPoint = getCoordinatePoint(mExternalRingRadius, mStartAngle);
		Point endPoint = getCoordinatePoint(mExternalRingRadius, mStartAngle + mSweepAngle);
		int height = mHeight;
		int y = (int) Math.max(startPoint.y, endPoint.y);
		int textHeight = Math.max(mStartTextRect.height(), mEndTextRect.height());
		return height - y - mBottomTextTopPadding - textHeight;
	}

	/**
	 * 计算当前所在区间
	 * @return
	 */
	private Block getCurrentBlock() {
		for (int i = 0; i < mBlocks.length; i++) {
			Block block = mBlocks[i];
			if (block.startValue <= mCurrentProgress && mCurrentProgress < block.endValue) {
				return block;
			}
		}
		return null;
	}

	/**
	 * 根据相对于(centerX, centerY)的半径长的圆周和角度计算圆周上一点相对于View原点(0, 0)的坐标位置
	 * @param radius
	 * @param levelAngle
	 * @return
	 */
	private Point getCoordinatePoint(float radius, float levelAngle) {
		Point point = new Point();
		double radians = Math.toRadians(levelAngle);
		if (levelAngle < 90) {
			point.x = (float) (mViewRadius + Math.cos(radians) * radius);
			point.y = (float) (mViewRadius + Math.sin(radians) * radius);
		} else if (levelAngle == 90) {
			point.x = mViewRadius;
			point.y = mViewRadius + radius;
		} else if (levelAngle > 90 && levelAngle < 180) {
			radians = Math.PI * (180 - levelAngle) / 180.0;
			point.x = (float) (mViewRadius - Math.cos(radians) * radius);
			point.y = (float) (mViewRadius + Math.sin(radians) * radius);
		} else if (levelAngle == 180) {
			point.x = mViewRadius - radius;
			point.y = mViewRadius;
		} else if (levelAngle > 180 && levelAngle < 270) {
			radians = Math.PI * (levelAngle - 180) / 180.0;
			point.x = (float) (mViewRadius - Math.cos(radians) * radius);
			point.y = (float) (mViewRadius - Math.sin(radians) * radius);
		} else if (levelAngle == 270) {
			point.x = mViewRadius;
			point.y = mViewRadius - radius;
		} else {
			radians = Math.PI * (360 - levelAngle) / 180.0;
			point.x = (float) (mViewRadius + Math.cos(radians) * radius);
			point.y = (float) (mViewRadius - Math.sin(radians) * radius);
		}
		return point;
	}

	private int dpToPx(float dp) {
		return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
	}

	private final class Point {
		float x, y;
	}

	private final class Block {
		float startValue, endValue;
		float startAngle, endAngle;
	}
}
