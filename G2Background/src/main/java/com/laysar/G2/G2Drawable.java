package com.laysar.G2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Arrays;

final class G2Drawable extends Drawable {
	private final Paint FillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint StrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Path FillPath = new Path();
	private final Path StrokePath = new Path();
	private final Path StrokePatternPath = new Path();
	private final Path StrokeRoundPointPath = new Path();
	private final RectF ArcRect = new RectF();
	private final RectF StrokeRepeatLayerBounds = new RectF();
	private final G2StrokePatternSolver StrokePatternSolver = new G2StrokePatternSolver();
	private G2Profile RoundedRectangleProfile = G2Profile.roundedRectangleDefault();
	private G2Profile CapsuleProfile = G2Profile.capsuleDefault();
	private G2CornerValue BaseCorner = G2CornerValue.Absolute(0.0f);
	private G2CornerValue PhysicalTopLeft = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalTopLeft = false;
	private G2CornerValue PhysicalTopRight = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalTopRight = false;
	private G2CornerValue PhysicalBottomLeft = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalBottomLeft = false;
	private G2CornerValue PhysicalBottomRight = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalBottomRight = false;
	private G2CornerValue LogicalTopStart = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalTopStart = false;
	private G2CornerValue LogicalTopEnd = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalTopEnd = false;
	private G2CornerValue LogicalBottomStart = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalBottomStart = false;
	private G2CornerValue LogicalBottomEnd = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalBottomEnd = false;
	private int FillColor = Color.TRANSPARENT;
	private float FillAlpha = 1.0f;
	private int[] FillGradientColors = null;
	private float FillGradientAngle = 0.0f;
	private int FillGradientRepeatCount = 1;
	private Shader FillShader;
	private boolean FillShaderDirty = true;
	private int DrawableAlpha = 255;
	private int StrokeColor = Color.TRANSPARENT;
	private float StrokeWidth = 0.0f;
	private int StrokePosition = G2Background.CENTER;
	private float StrokeAlpha = 1.0f;
	private int[] StrokeGradientColors = null;
	private float StrokeGradientAngle = 0.0f;
	private int StrokeGradientRepeatCount = 1;
	private int StrokeSegmentCount = 1;
	private float StrokeGapLength = 0.0f;
	private float StrokePatternAngle = 0.0f;
	private int StrokeSegmentCap = G2Background.SHARP;
	private Shader StrokeShader;
	private boolean StrokeShaderDirty = true;
	private int RippleColor = Color.TRANSPARENT;
	private float RippleAlpha = 1.0f;
	private boolean ClipToG2Outline = false;
	private int ResolvedLayoutDirection = View.LAYOUT_DIRECTION_LTR;
	private boolean FillPathDirty = true;
	private boolean StrokePathDirty = true;
	private boolean StrokePatternDirty = true;
	private int StrokePatternRenderMode = G2StrokePatternSolver.RESULT_FULL_PATH;
	private int CachedLeft = Integer.MIN_VALUE;
	private int CachedTop = Integer.MIN_VALUE;
	private int CachedRight = Integer.MIN_VALUE;
	private int CachedBottom = Integer.MIN_VALUE;
	
	G2Drawable() {
		FillPaint.setStyle(Paint.Style.FILL);
		StrokePaint.setStyle(Paint.Style.STROKE);
	}
	
	private static int combinedAlpha(int ColorValue, float AlphaValue, int DrawableAlphaValue) {
		float Combined = Color.alpha(ColorValue) * AlphaValue * (DrawableAlphaValue / 255.0f);
		return clampInt(Math.round(Combined), 0, 255);
	}

	private static int gradientAlpha(float AlphaValue, int DrawableAlphaValue) {
		float Combined = 255.0f * AlphaValue * (DrawableAlphaValue / 255.0f);
		return clampInt(Math.round(Combined), 0, 255);
	}
	
	private static int clampInt(int Value, int Minimum, int Maximum) {
		if (Value < Minimum) {
			return Minimum;
		}
		if (Value > Maximum) {
			return Maximum;
		}
		return Value;
	}
	
	void setGeometry(G2Profile RoundedProfile, G2Profile Capsule, G2CornerValue Base, G2CornerValue TopLeft, boolean HasTopLeft, G2CornerValue TopRight, boolean HasTopRight, G2CornerValue BottomLeft, boolean HasBottomLeft, G2CornerValue BottomRight, boolean HasBottomRight, G2CornerValue TopStart, boolean HasTopStart, G2CornerValue TopEnd, boolean HasTopEnd, G2CornerValue BottomStart, boolean HasBottomStart, G2CornerValue BottomEnd, boolean HasBottomEnd) {
		boolean GeometryChanged = !profilesEqual(RoundedRectangleProfile, RoundedProfile) || !profilesEqual(CapsuleProfile, Capsule) || !BaseCorner.ContentEquals(Base) || !PhysicalTopLeft.ContentEquals(TopLeft) || HasPhysicalTopLeft != HasTopLeft || !PhysicalTopRight.ContentEquals(TopRight) || HasPhysicalTopRight != HasTopRight || !PhysicalBottomLeft.ContentEquals(BottomLeft) || HasPhysicalBottomLeft != HasBottomLeft || !PhysicalBottomRight.ContentEquals(BottomRight) || HasPhysicalBottomRight != HasBottomRight || !LogicalTopStart.ContentEquals(TopStart) || HasLogicalTopStart != HasTopStart || !LogicalTopEnd.ContentEquals(TopEnd) || HasLogicalTopEnd != HasTopEnd || !LogicalBottomStart.ContentEquals(BottomStart) || HasLogicalBottomStart != HasBottomStart || !LogicalBottomEnd.ContentEquals(BottomEnd) || HasLogicalBottomEnd != HasBottomEnd;
		RoundedRectangleProfile = RoundedProfile;
		CapsuleProfile = Capsule;
		BaseCorner = G2CornerValue.CopyOf(Base);
		PhysicalTopLeft = G2CornerValue.CopyOf(TopLeft);
		HasPhysicalTopLeft = HasTopLeft;
		PhysicalTopRight = G2CornerValue.CopyOf(TopRight);
		HasPhysicalTopRight = HasTopRight;
		PhysicalBottomLeft = G2CornerValue.CopyOf(BottomLeft);
		HasPhysicalBottomLeft = HasBottomLeft;
		PhysicalBottomRight = G2CornerValue.CopyOf(BottomRight);
		HasPhysicalBottomRight = HasBottomRight;
		LogicalTopStart = G2CornerValue.CopyOf(TopStart);
		HasLogicalTopStart = HasTopStart;
		LogicalTopEnd = G2CornerValue.CopyOf(TopEnd);
		HasLogicalTopEnd = HasTopEnd;
		LogicalBottomStart = G2CornerValue.CopyOf(BottomStart);
		HasLogicalBottomStart = HasBottomStart;
		LogicalBottomEnd = G2CornerValue.CopyOf(BottomEnd);
		HasLogicalBottomEnd = HasBottomEnd;
		if (GeometryChanged) {
			markGeometryDirty();
		}
	}
	
	void setFill(int ColorValue, float AlphaValue, int[] GradientColors, float GradientAngle, int GradientRepeatCount) {
		boolean ExistingGradientActive = FillGradientColors != null;
		boolean IncomingGradientActive = GradientColors != null && GradientColors.length >= 2;
		boolean GradientColorsChanged = IncomingGradientActive ? !Arrays.equals(FillGradientColors, GradientColors) : ExistingGradientActive;
		boolean GradientAngleChanged = FillGradientAngle != GradientAngle;
		int SafeGradientRepeatCount = Math.max(1, GradientRepeatCount);
		boolean GradientRepeatCountChanged = FillGradientRepeatCount != SafeGradientRepeatCount;
		boolean ShaderConfigurationChanged = GradientColorsChanged || (GradientAngleChanged && (ExistingGradientActive || IncomingGradientActive));
		boolean PaintChanged = FillColor != ColorValue || FillAlpha != AlphaValue;
		FillColor = ColorValue;
		FillAlpha = AlphaValue;
		if (GradientColorsChanged) {
			FillGradientColors = IncomingGradientActive ? Arrays.copyOf(GradientColors, GradientColors.length) : null;
		}
		FillGradientAngle = GradientAngle;
		FillGradientRepeatCount = SafeGradientRepeatCount;
		if (ShaderConfigurationChanged) {
			FillShaderDirty = true;
			FillShader = null;
		}
		if (PaintChanged || ShaderConfigurationChanged || GradientRepeatCountChanged && (ExistingGradientActive || IncomingGradientActive)) {
			invalidateSelf();
		}
	}
	
	void setStroke(int ColorValue, float WidthValue, int PositionValue, float AlphaValue, int[] GradientColors, float GradientAngle, int GradientRepeatCount) {
		boolean StrokeGeometryChanged = StrokeWidth != WidthValue || StrokePosition != PositionValue;
		boolean ExistingGradientActive = StrokeGradientColors != null;
		boolean IncomingGradientActive = GradientColors != null && GradientColors.length >= 2;
		boolean GradientColorsChanged = IncomingGradientActive ? !Arrays.equals(StrokeGradientColors, GradientColors) : ExistingGradientActive;
		boolean GradientAngleChanged = StrokeGradientAngle != GradientAngle;
		int SafeGradientRepeatCount = Math.max(1, GradientRepeatCount);
		boolean GradientRepeatCountChanged = StrokeGradientRepeatCount != SafeGradientRepeatCount;
		boolean ShaderConfigurationChanged = GradientColorsChanged || (GradientAngleChanged && (ExistingGradientActive || IncomingGradientActive));
		boolean StrokePaintChanged = StrokeColor != ColorValue || StrokeAlpha != AlphaValue;
		StrokeColor = ColorValue;
		StrokeWidth = WidthValue;
		StrokePosition = PositionValue;
		StrokeAlpha = AlphaValue;
		if (GradientColorsChanged) {
			StrokeGradientColors = IncomingGradientActive ? Arrays.copyOf(GradientColors, GradientColors.length) : null;
		}
		StrokeGradientAngle = GradientAngle;
		StrokeGradientRepeatCount = SafeGradientRepeatCount;
		if (ShaderConfigurationChanged) {
			StrokeShaderDirty = true;
			StrokeShader = null;
		}
		if (StrokeGeometryChanged) {
			markStrokePathDirty();
		} else if (StrokePaintChanged || ShaderConfigurationChanged || GradientRepeatCountChanged && (ExistingGradientActive || IncomingGradientActive)) {
			invalidateSelf();
		}
	}

	void setStrokePattern(int SegmentCount, float GapLength, float PatternAngle, int SegmentCap) {
		int SafeSegmentCount = Math.max(1, SegmentCount);
		float SafeGapLength = isFinite(GapLength) ? Math.max(0.0f, GapLength) : 0.0f;
		float SafePatternAngle = isFinite(PatternAngle) ? PatternAngle : 0.0f;
		int SafeSegmentCap = SegmentCap == G2Background.ROUND ? G2Background.ROUND : G2Background.SHARP;
		if (StrokeSegmentCount == SafeSegmentCount && Float.compare(StrokeGapLength, SafeGapLength) == 0 && Float.compare(StrokePatternAngle, SafePatternAngle) == 0 && StrokeSegmentCap == SafeSegmentCap) {
			return;
		}
		StrokeSegmentCount = SafeSegmentCount;
		StrokeGapLength = SafeGapLength;
		StrokePatternAngle = SafePatternAngle;
		StrokeSegmentCap = SafeSegmentCap;
		markStrokePatternDirty();
	}
	
	void setRipple(int ColorValue, float AlphaValue) {
		if (RippleColor != ColorValue || RippleAlpha != AlphaValue) {
			RippleColor = ColorValue;
			RippleAlpha = AlphaValue;
			invalidateSelf();
		}
	}
	
	void setClipToG2Outline(boolean Enabled) {
		if (ClipToG2Outline != Enabled) {
			ClipToG2Outline = Enabled;
			invalidateSelf();
		}
	}
	
	void setResolvedLayoutDirection(int LayoutDirection) {
		int SafeDirection = LayoutDirection == View.LAYOUT_DIRECTION_RTL ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
		if (ResolvedLayoutDirection != SafeDirection) {
			ResolvedLayoutDirection = SafeDirection;
			markGeometryDirty();
		}
	}
	
	@Override
	public void draw(Canvas Canvas) {
		if (Canvas == null) {
			return;
		}
		Rect Bounds = getBounds();
		if (Bounds.width() <= 0 || Bounds.height() <= 0) {
			return;
		}
		rebuildFillPathIfNeeded(Bounds);
		int SaveCount = Canvas.save();
		Canvas.translate(Bounds.left, Bounds.top);
		drawFill(Canvas, Bounds.width(), Bounds.height());
		drawStroke(Canvas, Bounds.width(), Bounds.height());
		Canvas.restoreToCount(SaveCount);
	}

	void DrawFill(Canvas Canvas) {
		if (Canvas == null) {
			return;
		}
		Rect Bounds = getBounds();
		if (Bounds.width() <= 0 || Bounds.height() <= 0) {
			return;
		}
		rebuildFillPathIfNeeded(Bounds);
		int SaveCount = Canvas.save();
		Canvas.translate(Bounds.left, Bounds.top);
		drawFill(Canvas, Bounds.width(), Bounds.height());
		Canvas.restoreToCount(SaveCount);
	}

	void DrawStroke(Canvas Canvas) {
		if (Canvas == null) {
			return;
		}
		Rect Bounds = getBounds();
		if (Bounds.width() <= 0 || Bounds.height() <= 0) {
			return;
		}
		rebuildFillPathIfNeeded(Bounds);
		int SaveCount = Canvas.save();
		Canvas.translate(Bounds.left, Bounds.top);
		drawStroke(Canvas, Bounds.width(), Bounds.height());
		Canvas.restoreToCount(SaveCount);
	}

	void ClipToFillPath(Canvas Canvas) {
		if (Canvas == null) {
			return;
		}
		Rect Bounds = getBounds();
		if (Bounds.width() <= 0 || Bounds.height() <= 0) {
			return;
		}
		rebuildFillPathIfNeeded(Bounds);
		if (Bounds.left == 0 && Bounds.top == 0) {
			Canvas.clipPath(FillPath);
			return;
		}
		Canvas.translate(Bounds.left, Bounds.top);
		Canvas.clipPath(FillPath);
		Canvas.translate(-Bounds.left, -Bounds.top);
	}

	private void drawFill(Canvas Canvas, int Width, int Height) {
		if (FillGradientColors != null && FillGradientColors.length >= 2) {
			rebuildFillShaderIfNeeded(Width, Height);
			FillPaint.setShader(FillShader);
			int AlphaValue = gradientAlpha(FillAlpha, DrawableAlpha);
			FillPaint.setAlpha(AlphaValue);
			if (AlphaValue > 0 && FillShader != null) {
				if (FillGradientRepeatCount == 1) {
					Canvas.drawPath(FillPath, FillPaint);
				} else {
					FillPaint.setAlpha(255);
					try {
						int SaveCount = Canvas.saveLayerAlpha(0.0f, 0.0f, Width, Height, AlphaValue);
						try {
							for (int RepeatIndex = 0; RepeatIndex < FillGradientRepeatCount; RepeatIndex++) {
								Canvas.drawPath(FillPath, FillPaint);
							}
						} finally {
							Canvas.restoreToCount(SaveCount);
						}
					} finally {
						FillPaint.setAlpha(AlphaValue);
					}
				}
			}
			return;
		}
		FillPaint.setShader(null);
		FillPaint.setAlpha(255);
		int FillAlphaValue = combinedAlpha(FillColor, FillAlpha, DrawableAlpha);
		if (FillAlphaValue > 0) {
			FillPaint.setColor((FillColor & 0x00ffffff) | (FillAlphaValue << 24));
			Canvas.drawPath(FillPath, FillPaint);
		}
	}
	
	@Override
	public void setAlpha(int Alpha) {
		int SafeAlpha = clampInt(Alpha, 0, 255);
		if (DrawableAlpha != SafeAlpha) {
			DrawableAlpha = SafeAlpha;
			invalidateSelf();
		}
	}
	
	@Override
	public void setColorFilter(ColorFilter ColorFilter) {
		FillPaint.setColorFilter(ColorFilter);
		StrokePaint.setColorFilter(ColorFilter);
		invalidateSelf();
	}
	
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	@Override
	protected void onBoundsChange(Rect Bounds) {
		super.onBoundsChange(Bounds);
		FillShaderDirty = true;
		StrokeShaderDirty = true;
		FillShader = null;
		StrokeShader = null;
		markGeometryDirty();
	}
	
	@Override
	public boolean onLayoutDirectionChanged(int LayoutDirection) {
		setResolvedLayoutDirection(LayoutDirection);
		return true;
	}
	
	private void rebuildFillPathIfNeeded(Rect Bounds) {
		boolean BoundsChanged = CachedLeft != Bounds.left || CachedTop != Bounds.top || CachedRight != Bounds.right || CachedBottom != Bounds.bottom;
		if (!FillPathDirty && !BoundsChanged) {
			return;
		}
		CachedLeft = Bounds.left;
		CachedTop = Bounds.top;
		CachedRight = Bounds.right;
		CachedBottom = Bounds.bottom;
		buildLocalPath(FillPath, Bounds.width(), Bounds.height(), ResolvedLayoutDirection);
		FillPathDirty = false;
		StrokePathDirty = true;
		StrokePatternDirty = true;
	}
	
	void buildLocalPath(Path Path, int Width, int Height, int LayoutDirection) {
		int SafeDirection = LayoutDirection == View.LAYOUT_DIRECTION_RTL ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
		G2PathBuilder.build(Path, Width, Height, resolveTopLeft(SafeDirection, Width, Height), resolveTopRight(SafeDirection, Width, Height), resolveBottomLeft(SafeDirection, Width, Height), resolveBottomRight(SafeDirection, Width, Height), RoundedRectangleProfile, CapsuleProfile, ArcRect);
	}
	
	int getExactOutlineType(int Width, int Height, int LayoutDirection) {
		int SafeDirection = LayoutDirection == View.LAYOUT_DIRECTION_RTL ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
		return G2PathBuilder.getExactOutlineType(Width, Height, resolveTopLeft(SafeDirection, Width, Height), resolveTopRight(SafeDirection, Width, Height), resolveBottomLeft(SafeDirection, Width, Height), resolveBottomRight(SafeDirection, Width, Height));
	}
	
	private void drawStroke(Canvas Canvas, int Width, int Height) {
		if (!isFinite(StrokeWidth) || StrokeWidth <= 0.0f) {
			return;
		}
		boolean GradientActive = StrokeGradientColors != null && StrokeGradientColors.length >= 2;
		int AlphaValue;
		if (GradientActive) {
			rebuildStrokeShaderIfNeeded(Width, Height);
			StrokePaint.setShader(StrokeShader);
			AlphaValue = gradientAlpha(StrokeAlpha, DrawableAlpha);
			StrokePaint.setAlpha(AlphaValue);
			if (AlphaValue <= 0 || StrokeShader == null) {
				return;
			}
		} else {
			StrokePaint.setShader(null);
			StrokePaint.setAlpha(255);
			AlphaValue = combinedAlpha(StrokeColor, StrokeAlpha, DrawableAlpha);
			if (AlphaValue <= 0) {
				return;
			}
			StrokePaint.setColor((StrokeColor & 0x00ffffff) | (AlphaValue << 24));
		}
		StrokePaint.setStyle(Paint.Style.STROKE);
		StrokePaint.setStrokeWidth(StrokeWidth);
		StrokePaint.setPathEffect(null);
		Path SourcePath = getStrokeSourcePath(Width, Height);
		if (SourcePath == null) {
			return;
		}
		rebuildStrokePatternIfNeeded(SourcePath);
		if (SourcePath.isEmpty()) {
			return;
		}
		if (StrokePatternRenderMode == G2StrokePatternSolver.RESULT_EMPTY) {
			StrokePaint.setStrokeCap(Paint.Cap.BUTT);
			StrokePaint.setStyle(Paint.Style.STROKE);
			return;
		}
		if (!GradientActive || StrokeGradientRepeatCount == 1) {
			DrawPreparedStrokePass(Canvas, SourcePath);
			return;
		}
		if (!ResolveStrokeRepeatLayerBounds(SourcePath)) {
			return;
		}
		StrokePaint.setAlpha(255);
		try {
			int SaveCount = Canvas.saveLayerAlpha(StrokeRepeatLayerBounds.left, StrokeRepeatLayerBounds.top, StrokeRepeatLayerBounds.right, StrokeRepeatLayerBounds.bottom, AlphaValue);
			try {
				for (int RepeatIndex = 0; RepeatIndex < StrokeGradientRepeatCount; RepeatIndex++) {
					DrawPreparedStrokePass(Canvas, SourcePath);
				}
			} finally {
				Canvas.restoreToCount(SaveCount);
			}
		} finally {
			StrokePaint.setAlpha(AlphaValue);
		}
	}

	private void DrawPreparedStrokePass(Canvas Canvas, Path SourcePath) {
		if (StrokePatternRenderMode == G2StrokePatternSolver.RESULT_FULL_PATH) {
			StrokePaint.setStrokeCap(Paint.Cap.BUTT);
			StrokePaint.setStyle(Paint.Style.STROKE);
			Canvas.drawPath(SourcePath, StrokePaint);
			return;
		}
		if (StrokePatternRenderMode == G2StrokePatternSolver.RESULT_ROUND_POINTS) {
			StrokePaint.setStrokeCap(Paint.Cap.ROUND);
			StrokePaint.setStyle(Paint.Style.FILL);
			try {
				Canvas.drawPath(StrokeRoundPointPath, StrokePaint);
			} finally {
				StrokePaint.setStyle(Paint.Style.STROKE);
			}
			return;
		}
		StrokePaint.setStyle(Paint.Style.STROKE);
		StrokePaint.setStrokeCap(StrokeSegmentCap == G2Background.ROUND ? Paint.Cap.ROUND : Paint.Cap.BUTT);
		Canvas.drawPath(StrokePatternPath, StrokePaint);
	}

	private boolean ResolveStrokeRepeatLayerBounds(Path SourcePath) {
		if (StrokePatternRenderMode == G2StrokePatternSolver.RESULT_ROUND_POINTS) {
			StrokeRoundPointPath.computeBounds(StrokeRepeatLayerBounds, true);
		} else if (StrokePatternRenderMode == G2StrokePatternSolver.RESULT_FULL_PATH) {
			SourcePath.computeBounds(StrokeRepeatLayerBounds, true);
			StrokeRepeatLayerBounds.inset(-StrokeWidth * 0.5f, -StrokeWidth * 0.5f);
		} else {
			StrokePatternPath.computeBounds(StrokeRepeatLayerBounds, true);
			StrokeRepeatLayerBounds.inset(-StrokeWidth * 0.5f, -StrokeWidth * 0.5f);
		}
		return !StrokeRepeatLayerBounds.isEmpty();
	}
	
	private Path getStrokeSourcePath(int Width, int Height) {
		if (StrokePosition == G2Background.CENTER) {
			return FillPath;
		}
		rebuildStrokePathIfNeeded(Width, Height);
		return StrokePath;
	}

	private void rebuildStrokePatternIfNeeded(Path SourcePath) {
		if (!StrokePatternDirty) {
			return;
		}
		StrokePatternRenderMode = StrokePatternSolver.Build(SourcePath, StrokeWidth, StrokeSegmentCount, StrokeGapLength, StrokePatternAngle, StrokeSegmentCap, StrokePatternPath, StrokeRoundPointPath);
		StrokePatternDirty = false;
	}

	private void rebuildFillShaderIfNeeded(int Width, int Height) {
		if (!FillShaderDirty) {
			return;
		}
		FillShader = createLinearGradient(FillGradientColors, FillGradientAngle, Width, Height);
		FillShaderDirty = false;
	}

	private void rebuildStrokeShaderIfNeeded(int Width, int Height) {
		if (!StrokeShaderDirty) {
			return;
		}
		StrokeShader = createLinearGradient(StrokeGradientColors, StrokeGradientAngle, Width, Height);
		StrokeShaderDirty = false;
	}

	private static Shader createLinearGradient(int[] Colors, float Angle, int Width, int Height) {
		if (Colors == null || Colors.length < 2 || Width <= 0 || Height <= 0) {
			return null;
		}
		float NormalizedAngle = ((Angle % 360.0f) + 360.0f) % 360.0f;
		double Radians = Math.toRadians(NormalizedAngle);
		float DirectionX = (float) Math.sin(Radians);
		float DirectionY = (float) Math.cos(Radians);
		float CenterX = Width * 0.5f;
		float CenterY = Height * 0.5f;
		float HalfLength = Math.abs(DirectionX) * Width * 0.5f + Math.abs(DirectionY) * Height * 0.5f;
		float StartX = CenterX - DirectionX * HalfLength;
		float StartY = CenterY - DirectionY * HalfLength;
		float EndX = CenterX + DirectionX * HalfLength;
		float EndY = CenterY + DirectionY * HalfLength;
		return new LinearGradient(StartX, StartY, EndX, EndY, Colors, null, Shader.TileMode.CLAMP);
	}

	private void rebuildStrokePathIfNeeded(int Width, int Height) {
		if (!StrokePathDirty) {
			return;
		}
		buildStrokePath(StrokePath, Width, Height);
		StrokePathDirty = false;
	}
	
	private void buildStrokePath(Path Path, int Width, int Height) {
		Path.reset();
		float HalfStrokeWidth = StrokeWidth * 0.5f;
		float Left;
		float Top;
		float Right;
		float Bottom;
		float RadiusAdjustment;
		if (StrokePosition == G2Background.INSIDE) {
			Left = HalfStrokeWidth;
			Top = HalfStrokeWidth;
			Right = Width - HalfStrokeWidth;
			Bottom = Height - HalfStrokeWidth;
			RadiusAdjustment = -HalfStrokeWidth;
			if (Right <= Left || Bottom <= Top) {
				return;
			}
		} else if (StrokePosition == G2Background.OUTSIDE) {
			Left = -HalfStrokeWidth;
			Top = -HalfStrokeWidth;
			Right = Width + HalfStrokeWidth;
			Bottom = Height + HalfStrokeWidth;
			RadiusAdjustment = HalfStrokeWidth;
		} else {
			Left = 0.0f;
			Top = 0.0f;
			Right = Width;
			Bottom = Height;
			RadiusAdjustment = 0.0f;
		}
		G2PathBuilder.build(Path, Left, Top, Right, Bottom, adjustStrokeRadius(resolveTopLeft(ResolvedLayoutDirection, Width, Height), RadiusAdjustment), adjustStrokeRadius(resolveTopRight(ResolvedLayoutDirection, Width, Height), RadiusAdjustment), adjustStrokeRadius(resolveBottomLeft(ResolvedLayoutDirection, Width, Height), RadiusAdjustment), adjustStrokeRadius(resolveBottomRight(ResolvedLayoutDirection, Width, Height), RadiusAdjustment), RoundedRectangleProfile, CapsuleProfile, ArcRect);
	}
	
	private float adjustStrokeRadius(float Radius, float RadiusAdjustment) {
		float AdjustedRadius = Radius + RadiusAdjustment;
		return AdjustedRadius < 0.0f ? 0.0f : AdjustedRadius;
	}
	
	private float resolveTopLeft(int LayoutDirection, int Width, int Height) {
		G2CornerValue Source;
		if (LayoutDirection == View.LAYOUT_DIRECTION_RTL) {
			Source = HasLogicalTopEnd ? LogicalTopEnd : HasPhysicalTopLeft ? PhysicalTopLeft : BaseCorner;
		} else {
			Source = HasLogicalTopStart ? LogicalTopStart : HasPhysicalTopLeft ? PhysicalTopLeft : BaseCorner;
		}
		return Source.Resolve(Width, Height);
	}
	
	private float resolveTopRight(int LayoutDirection, int Width, int Height) {
		G2CornerValue Source;
		if (LayoutDirection == View.LAYOUT_DIRECTION_RTL) {
			Source = HasLogicalTopStart ? LogicalTopStart : HasPhysicalTopRight ? PhysicalTopRight : BaseCorner;
		} else {
			Source = HasLogicalTopEnd ? LogicalTopEnd : HasPhysicalTopRight ? PhysicalTopRight : BaseCorner;
		}
		return Source.Resolve(Width, Height);
	}
	
	private float resolveBottomLeft(int LayoutDirection, int Width, int Height) {
		G2CornerValue Source;
		if (LayoutDirection == View.LAYOUT_DIRECTION_RTL) {
			Source = HasLogicalBottomEnd ? LogicalBottomEnd : HasPhysicalBottomLeft ? PhysicalBottomLeft : BaseCorner;
		} else {
			Source = HasLogicalBottomStart ? LogicalBottomStart : HasPhysicalBottomLeft ? PhysicalBottomLeft : BaseCorner;
		}
		return Source.Resolve(Width, Height);
	}
	
	private float resolveBottomRight(int LayoutDirection, int Width, int Height) {
		G2CornerValue Source;
		if (LayoutDirection == View.LAYOUT_DIRECTION_RTL) {
			Source = HasLogicalBottomStart ? LogicalBottomStart : HasPhysicalBottomRight ? PhysicalBottomRight : BaseCorner;
		} else {
			Source = HasLogicalBottomEnd ? LogicalBottomEnd : HasPhysicalBottomRight ? PhysicalBottomRight : BaseCorner;
		}
		return Source.Resolve(Width, Height);
	}
	
	private void markGeometryDirty() {
		FillPathDirty = true;
		StrokePathDirty = true;
		StrokePatternDirty = true;
		invalidateSelf();
	}
	
	private void markStrokePathDirty() {
		StrokePathDirty = true;
		StrokePatternDirty = true;
		invalidateSelf();
	}
	
	private void markStrokePatternDirty() {
		StrokePatternDirty = true;
		invalidateSelf();
	}

	private static boolean isFinite(float Value) {
		return !Float.isNaN(Value) && !Float.isInfinite(Value);
	}

	private static boolean isFinite(double Value) {
		return !Double.isNaN(Value) && !Double.isInfinite(Value);
	}

	private static boolean profilesEqual(G2Profile First, G2Profile Second) {
		return First == Second || First != null && Second != null && First.extendedFraction == Second.extendedFraction && First.arcFraction == Second.arcFraction && First.bezierCurvatureScale == Second.bezierCurvatureScale && First.arcCurvatureScale == Second.arcCurvatureScale;
	}
}