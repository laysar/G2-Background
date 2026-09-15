package com.laysar.G2;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Arrays;

public final class G2BackgroundDrawable extends Drawable {
	private G2BackgroundDrawableState State;
	private com.laysar.G2.G2Drawable Delegate;
	private boolean Mutated;
	private int DrawableAlpha = 255;
	private ColorFilter ColorFilter;
	private final Drawable.Callback DelegateCallback = new Drawable.Callback() {
		@Override
		public void invalidateDrawable(Drawable Drawable) {
			invalidateSelf();
		}
		
		@Override
		public void scheduleDrawable(Drawable Drawable, Runnable Runnable, long When) {
			scheduleSelf(Runnable, When);
		}
		
		@Override
		public void unscheduleDrawable(Drawable Drawable, Runnable Runnable) {
			unscheduleSelf(Runnable);
		}
	};
	
	public G2BackgroundDrawable() {
		this(new G2BackgroundDrawableState());
	}
	
	private G2BackgroundDrawable(G2BackgroundDrawableState SourceState) {
		State = new G2BackgroundDrawableState(SourceState);
		CreateDelegateFromState();
	}
	
	@Override
	public void inflate(Resources Resources, XmlPullParser Parser, AttributeSet AttributeSet, Resources.Theme Theme) throws XmlPullParserException, IOException {
		super.inflate(Resources, Parser, AttributeSet, Theme);
		TypedArray Attributes = Theme == null ? Resources.obtainAttributes(AttributeSet, R.styleable.G2BackgroundDrawable) : Theme.obtainStyledAttributes(AttributeSet, R.styleable.G2BackgroundDrawable, 0, 0);
		try {
			State.ChangingConfigurations |= Attributes.getChangingConfigurations();
			ApplyFillAttributes(Attributes, Theme);
			ApplyProfileAttributes(Attributes);
			ApplyCornerAttributes(Attributes);
			ApplyStrokeAttributes(Attributes, Theme);
			ApplyRippleAttributes(Attributes, Theme);
			ApplyClipAttributes(Attributes);
		} finally {
			Attributes.recycle();
		}
		ApplyStateToDelegate();
		invalidateSelf();
	}
	
	@Override
	public void draw(Canvas Canvas) {
		SyncDelegateBounds();
		Delegate.draw(Canvas);
	}
	
	@Override
	public void setAlpha(int Alpha) {
		DrawableAlpha = clampInt(Alpha, 0, 255);
		Delegate.setAlpha(DrawableAlpha);
		invalidateSelf();
	}
	
	@Override
	public void setColorFilter(ColorFilter Filter) {
		ColorFilter = Filter;
		Delegate.setColorFilter(Filter);
		invalidateSelf();
	}
	
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	@Override
	protected void onBoundsChange(Rect Bounds) {
		super.onBoundsChange(Bounds);
		Delegate.setBounds(Bounds);
	}
	
	@Override
	public boolean onLayoutDirectionChanged(int LayoutDirection) {
		Delegate.setResolvedLayoutDirection(LayoutDirection);
		invalidateSelf();
		return true;
	}
	
	@Override
	public int getChangingConfigurations() {
		return super.getChangingConfigurations() | State.getChangingConfigurations();
	}
	
	@Override
	public ConstantState getConstantState() {
		State.ChangingConfigurations = getChangingConfigurations();
		return State;
	}
	
	@Override
	public Drawable mutate() {
		if (!Mutated && super.mutate() == this) {
			State = new G2BackgroundDrawableState(State);
			Mutated = true;
		}
		return this;
	}
	
	private void CreateDelegateFromState() {
		Delegate = new com.laysar.G2.G2Drawable();
		Delegate.setCallback(DelegateCallback);
		SyncDelegateBounds();
		Delegate.setAlpha(DrawableAlpha);
		Delegate.setColorFilter(ColorFilter);
		Delegate.setResolvedLayoutDirection(getLayoutDirection());
		ApplyStateToDelegate();
	}
	
	private void SyncDelegateBounds() {
		Rect Bounds = getBounds();
		Rect DelegateBounds = Delegate.getBounds();
		if (DelegateBounds.left != Bounds.left || DelegateBounds.top != Bounds.top || DelegateBounds.right != Bounds.right || DelegateBounds.bottom != Bounds.bottom) {
			Delegate.setBounds(Bounds);
		}
	}
	
	private void ApplyStateToDelegate() {
		Delegate.setGeometry(CreateRoundedRectangleProfile(), CreateCapsuleProfile(), State.BaseCorner, State.PhysicalTopLeft, State.HasPhysicalTopLeft, State.PhysicalTopRight, State.HasPhysicalTopRight, State.PhysicalBottomLeft, State.HasPhysicalBottomLeft, State.PhysicalBottomRight, State.HasPhysicalBottomRight, State.LogicalTopStart, State.HasLogicalTopStart, State.LogicalTopEnd, State.HasLogicalTopEnd, State.LogicalBottomStart, State.HasLogicalBottomStart, State.LogicalBottomEnd, State.HasLogicalBottomEnd);
		Delegate.setFill(State.FillColor, State.FillAlpha, State.FillGradientColors, State.FillGradientAngle, State.FillGradientRepeatCount);
		Delegate.setStroke(State.StrokeColor, State.StrokeWidth, State.StrokePosition, State.StrokeAlpha, State.StrokeGradientColors, State.StrokeGradientAngle, State.StrokeGradientRepeatCount);
		Delegate.setStrokePattern(State.StrokeSegmentCount, State.StrokeGapLength, State.StrokePatternAngle, State.StrokeSegmentCap);
		Delegate.setRipple(State.RippleColor, State.RippleAlpha);
		Delegate.setClipToG2Outline(State.ClipToG2Outline);
		Delegate.setAlpha(DrawableAlpha);
		Delegate.setColorFilter(ColorFilter);
		Delegate.setBounds(getBounds());
	}
	
	private com.laysar.G2.G2Profile CreateRoundedRectangleProfile() {
		return new com.laysar.G2.G2Profile(State.RoundedRectangleExtendedFraction, State.RoundedRectangleArcFraction, State.RoundedRectangleBezierCurvatureScale, State.RoundedRectangleArcCurvatureScale);
	}
	
	private com.laysar.G2.G2Profile CreateCapsuleProfile() {
		return new com.laysar.G2.G2Profile(State.CapsuleExtendedFraction, State.CapsuleArcFraction, com.laysar.G2.G2Profile.DefaultCapsuleBezierCurvatureScale, com.laysar.G2.G2Profile.DefaultCapsuleArcCurvatureScale);
	}
	
	private void ApplyFillAttributes(TypedArray Attributes, Resources.Theme Theme) {
		Integer FillColor = com.laysar.G2.G2ColorResolver.ResolveColorAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2FillColor, Theme);
		if (FillColor != null) {
			State.FillColor = FillColor;
			State.FillGradientColors = null;
		}
		String Alpha = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2Alpha);
		if (Alpha != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseUnitInterval(Attributes.getResources(), Alpha);
			if (Value != null) {
				State.FillAlpha = Value;
			}
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2FillGradient)) {
			int[] GradientColors = com.laysar.G2.G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2FillGradient, Theme, 2);
			if (GradientColors != null) {
				State.FillGradientColors = GradientColors;
			}
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2FillGradientOrientation)) {
			String Orientation = ReadG2ColorListStringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2FillGradientOrientation);
			Float Angle = com.laysar.G2.G2GradientValueParser.ParseOrientation(Orientation);
			State.FillGradientAngle = Angle == null ? 0.0f : Angle;
		}
		Integer FillGradientRepeatCount = ReadG2IntegerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2FillGradientRepeatCount, 1);
		if (FillGradientRepeatCount != null) {
			State.FillGradientRepeatCount = Math.max(1, FillGradientRepeatCount);
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2FillOrderedBlend)) {
			int[] BlendColors = com.laysar.G2.G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2FillOrderedBlend, Theme, 1);
			Integer ResolvedColor = com.laysar.G2.G2ColorBlend.ResolveOrderedBlend(BlendColors);
			if (ResolvedColor != null) {
				State.FillColor = ResolvedColor;
				State.FillGradientColors = null;
			}
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2FillAverageBlend)) {
			int[] BlendColors = com.laysar.G2.G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2FillAverageBlend, Theme, 1);
			Integer ResolvedColor = com.laysar.G2.G2ColorBlend.ResolveAverageBlend(BlendColors);
			if (ResolvedColor != null) {
				State.FillColor = ResolvedColor;
				State.FillGradientColors = null;
			}
		}
	}
	
	private void ApplyProfileAttributes(TypedArray Attributes) {
		String RoundedRectangleExtendedFraction = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2RoundedRectangleExtendedFraction);
		if (RoundedRectangleExtendedFraction != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseUnitInterval(Attributes.getResources(), RoundedRectangleExtendedFraction);
			if (Value != null) {
				State.RoundedRectangleExtendedFraction = Value;
			}
		}
		String RoundedRectangleArcFraction = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2RoundedRectangleArcFraction);
		if (RoundedRectangleArcFraction != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseUnitInterval(Attributes.getResources(), RoundedRectangleArcFraction);
			if (Value != null) {
				State.RoundedRectangleArcFraction = Value;
			}
		}
		String RoundedRectangleBezierCurvatureScale = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2RoundedRectangleBezierCurvatureScale);
		if (RoundedRectangleBezierCurvatureScale != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParsePositiveScale(Attributes.getResources(), RoundedRectangleBezierCurvatureScale);
			if (Value != null) {
				State.RoundedRectangleBezierCurvatureScale = Value;
			}
		}
		String RoundedRectangleArcCurvatureScale = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2RoundedRectangleArcCurvatureScale);
		if (RoundedRectangleArcCurvatureScale != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParsePositiveScale(Attributes.getResources(), RoundedRectangleArcCurvatureScale);
			if (Value != null) {
				State.RoundedRectangleArcCurvatureScale = Value;
			}
		}
		String CapsuleExtendedFraction = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CapsuleExtendedFraction);
		if (CapsuleExtendedFraction != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseUnitInterval(Attributes.getResources(), CapsuleExtendedFraction);
			if (Value != null) {
				State.CapsuleExtendedFraction = Value;
			}
		}
		String CapsuleArcFraction = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CapsuleArcFraction);
		if (CapsuleArcFraction != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseUnitInterval(Attributes.getResources(), CapsuleArcFraction);
			if (Value != null) {
				State.CapsuleArcFraction = Value;
			}
		}
	}
	
	private void ApplyCornerAttributes(TypedArray Attributes) {
		String Corners = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2Corners);
		if (Corners != null) {
			com.laysar.G2.G2CornerValue Value = com.laysar.G2.G2UnitValueParser.ParseCorner(Attributes.getResources(), Corners);
			if (Value != null) {
				State.BaseCorner = com.laysar.G2.G2CornerValue.CopyOf(Value);
				State.PhysicalTopLeft = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasPhysicalTopLeft = false;
				State.PhysicalTopRight = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasPhysicalTopRight = false;
				State.PhysicalBottomLeft = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasPhysicalBottomLeft = false;
				State.PhysicalBottomRight = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasPhysicalBottomRight = false;
				State.LogicalTopStart = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasLogicalTopStart = false;
				State.LogicalTopEnd = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasLogicalTopEnd = false;
				State.LogicalBottomStart = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasLogicalBottomStart = false;
				State.LogicalBottomEnd = com.laysar.G2.G2CornerValue.Absolute(0.0f);
				State.HasLogicalBottomEnd = false;
			}
		}
		ApplyPhysicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerTopLeft, 0);
		ApplyPhysicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerTopRight, 1);
		ApplyPhysicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerBottomLeft, 2);
		ApplyPhysicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerBottomRight, 3);
		ApplyLogicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerTopStart, 0);
		ApplyLogicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerTopEnd, 1);
		ApplyLogicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerBottomStart, 2);
		ApplyLogicalCornerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2CornerBottomEnd, 3);
	}
	
	private void ApplyPhysicalCornerAttribute(TypedArray Attributes, int Index, int CornerIndex) {
		String Corner = ReadG2StringAttribute(Attributes, Index);
		if (Corner == null) {
			return;
		}
		com.laysar.G2.G2CornerValue Value = com.laysar.G2.G2UnitValueParser.ParseCorner(Attributes.getResources(), Corner);
		if (Value == null) {
			return;
		}
		if (CornerIndex == 0) {
			State.PhysicalTopLeft = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasPhysicalTopLeft = true;
		} else if (CornerIndex == 1) {
			State.PhysicalTopRight = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasPhysicalTopRight = true;
		} else if (CornerIndex == 2) {
			State.PhysicalBottomLeft = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasPhysicalBottomLeft = true;
		} else if (CornerIndex == 3) {
			State.PhysicalBottomRight = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasPhysicalBottomRight = true;
		}
	}
	
	private void ApplyLogicalCornerAttribute(TypedArray Attributes, int Index, int CornerIndex) {
		String Corner = ReadG2StringAttribute(Attributes, Index);
		if (Corner == null) {
			return;
		}
		com.laysar.G2.G2CornerValue Value = com.laysar.G2.G2UnitValueParser.ParseCorner(Attributes.getResources(), Corner);
		if (Value == null) {
			return;
		}
		if (CornerIndex == 0) {
			State.LogicalTopStart = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasLogicalTopStart = true;
		} else if (CornerIndex == 1) {
			State.LogicalTopEnd = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasLogicalTopEnd = true;
		} else if (CornerIndex == 2) {
			State.LogicalBottomStart = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasLogicalBottomStart = true;
		} else if (CornerIndex == 3) {
			State.LogicalBottomEnd = com.laysar.G2.G2CornerValue.CopyOf(Value);
			State.HasLogicalBottomEnd = true;
		}
	}
	
	private void ApplyStrokeAttributes(TypedArray Attributes, Resources.Theme Theme) {
		Integer StrokeColor = com.laysar.G2.G2ColorResolver.ResolveColorAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeColor, Theme);
		if (StrokeColor != null) {
			State.StrokeColor = StrokeColor;
			State.StrokeGradientColors = null;
		}
		String StrokeWidth = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeWidth);
		if (StrokeWidth != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseStrokeWidth(Attributes.getResources(), StrokeWidth);
			if (Value != null) {
				State.StrokeWidth = Value;
			}
		}
		String StrokeAlpha = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeAlpha);
		if (StrokeAlpha != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseUnitInterval(Attributes.getResources(), StrokeAlpha);
			if (Value != null) {
				State.StrokeAlpha = Value;
			}
		}
		Integer StrokePosition = ReadG2EnumAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokePosition);
		if (StrokePosition != null) {
			State.StrokePosition = sanitizeStrokePosition(StrokePosition);
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2StrokeGradient)) {
			int[] GradientColors = com.laysar.G2.G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeGradient, Theme, 2);
			if (GradientColors != null) {
				State.StrokeGradientColors = GradientColors;
			}
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2StrokeGradientOrientation)) {
			String Orientation = ReadG2ColorListStringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeGradientOrientation);
			Float Angle = com.laysar.G2.G2GradientValueParser.ParseOrientation(Orientation);
			State.StrokeGradientAngle = Angle == null ? 0.0f : Angle;
		}
		Integer StrokeGradientRepeatCount = ReadG2IntegerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeGradientRepeatCount, 1);
		if (StrokeGradientRepeatCount != null) {
			State.StrokeGradientRepeatCount = Math.max(1, StrokeGradientRepeatCount);
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2StrokeOrderedBlend)) {
			int[] BlendColors = com.laysar.G2.G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeOrderedBlend, Theme, 1);
			Integer ResolvedColor = com.laysar.G2.G2ColorBlend.ResolveOrderedBlend(BlendColors);
			if (ResolvedColor != null) {
				State.StrokeColor = ResolvedColor;
				State.StrokeGradientColors = null;
			}
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2StrokeAverageBlend)) {
			int[] BlendColors = com.laysar.G2.G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeAverageBlend, Theme, 1);
			Integer ResolvedColor = com.laysar.G2.G2ColorBlend.ResolveAverageBlend(BlendColors);
			if (ResolvedColor != null) {
				State.StrokeColor = ResolvedColor;
				State.StrokeGradientColors = null;
			}
		}
		Integer StrokeSegmentCount = ReadG2IntegerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeSegmentCount, 1);
		if (StrokeSegmentCount != null) {
			State.StrokeSegmentCount = Math.max(1, StrokeSegmentCount);
		}
		String StrokeGapLength = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeGapLength);
		if (StrokeGapLength != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseStrokeGapLength(Attributes.getResources(), StrokeGapLength);
			if (Value != null) {
				State.StrokeGapLength = Value;
			}
		}
		if (Attributes.hasValue(R.styleable.G2BackgroundDrawable_G2StrokePatternAngle)) {
			String PatternAngleValue = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokePatternAngle);
			Float PatternAngle = com.laysar.G2.G2GradientValueParser.ParseAngle(PatternAngleValue);
			if (PatternAngle != null) {
				State.StrokePatternAngle = PatternAngle;
			}
		}
		Integer StrokeSegmentCap = ReadG2IntegerAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2StrokeSegmentCap, G2Background.SHARP);
		if (StrokeSegmentCap != null) {
			State.StrokeSegmentCap = sanitizeStrokeSegmentCap(StrokeSegmentCap);
		}
	}
	
	private void ApplyRippleAttributes(TypedArray Attributes, Resources.Theme Theme) {
		Integer RippleColor = com.laysar.G2.G2ColorResolver.ResolveColorAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2RippleColor, Theme);
		if (RippleColor != null) {
			State.RippleColor = RippleColor;
		}
		String RippleAlpha = ReadG2StringAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2RippleAlpha);
		if (RippleAlpha != null) {
			Float Value = com.laysar.G2.G2UnitValueParser.ParseUnitInterval(Attributes.getResources(), RippleAlpha);
			if (Value != null) {
				State.RippleAlpha = Value;
			}
		}
	}
	
	private void ApplyClipAttributes(TypedArray Attributes) {
		Boolean ClipToOutline = ReadG2BooleanAttribute(Attributes, R.styleable.G2BackgroundDrawable_G2ClipToOutline);
		if (ClipToOutline != null) {
			State.ClipToG2Outline = ClipToOutline;
		}
	}
	
	private static String ReadG2StringAttribute(TypedArray Attributes, int Index) {
		return com.laysar.G2.G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}

	private static String ReadG2ColorListStringAttribute(TypedArray Attributes, int Index) {
		return com.laysar.G2.G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static String ReadTypedValueAsString(TypedArray Attributes, int Index, TypedValue Value, Resources ResourcesObject) {
		return com.laysar.G2.G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static String ReadStringFallback(TypedArray Attributes, int Index, Resources ResourcesObject) {
		return com.laysar.G2.G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static String ReadReferencedValueAsString(TypedArray Attributes, int Index, Resources ResourcesObject) {
		return com.laysar.G2.G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static Float ReadDimensionAttribute(TypedArray Attributes, int Index) {
		return com.laysar.G2.G2ValueResolver.ResolveDimensionAttribute(Attributes, Index);
	}
	
	private static String ToPxString(float Value) {
		return com.laysar.G2.G2ValueResolver.toPxString(Value);
	}
	
	private static Integer ReadG2ColorAttribute(TypedArray Attributes, int Index) {
		if (!Attributes.hasValue(Index)) {
			return null;
		}
		try {
			ColorStateList Colors = Attributes.getColorStateList(Index);
			if (Colors != null) {
				return Colors.getDefaultColor();
			}
		} catch (Resources.NotFoundException Ignored) {
		} catch (RuntimeException Ignored) {
		}
		try {
			return Attributes.getColor(Index, 0);
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
	}
	
	private static Boolean ReadG2BooleanAttribute(TypedArray Attributes, int Index) {
		return com.laysar.G2.G2ValueResolver.ResolveBooleanAttribute(Attributes, Index);
	}
	
	private static Integer ReadG2IntegerAttribute(TypedArray Attributes, int Index, int DefaultValue) {
		return com.laysar.G2.G2ValueResolver.ResolveIntegerAttribute(Attributes, Index);
	}

	private static Integer ReadG2EnumAttribute(TypedArray Attributes, int Index) {
		return com.laysar.G2.G2ValueResolver.ResolveEnumAttribute(Attributes, Index, G2Background.CENTER);
	}
	
	private static int sanitizeStrokePosition(int Position) {
		if (Position == G2Background.INSIDE || Position == G2Background.CENTER || Position == G2Background.OUTSIDE) {
			return Position;
		}
		return G2Background.CENTER;
	}

	private static int sanitizeStrokeSegmentCap(int Cap) {
		return Cap == G2Background.ROUND ? G2Background.ROUND : G2Background.SHARP;
	}
	
	private static boolean isFinite(float Value) {
		return !Float.isNaN(Value) && !Float.isInfinite(Value);
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
	
	private static final class G2BackgroundDrawableState extends ConstantState {
		int ChangingConfigurations;
		com.laysar.G2.G2CornerValue BaseCorner = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		com.laysar.G2.G2CornerValue PhysicalTopLeft = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasPhysicalTopLeft = false;
		com.laysar.G2.G2CornerValue PhysicalTopRight = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasPhysicalTopRight = false;
		com.laysar.G2.G2CornerValue PhysicalBottomLeft = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasPhysicalBottomLeft = false;
		com.laysar.G2.G2CornerValue PhysicalBottomRight = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasPhysicalBottomRight = false;
		com.laysar.G2.G2CornerValue LogicalTopStart = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasLogicalTopStart = false;
		com.laysar.G2.G2CornerValue LogicalTopEnd = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasLogicalTopEnd = false;
		com.laysar.G2.G2CornerValue LogicalBottomStart = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasLogicalBottomStart = false;
		com.laysar.G2.G2CornerValue LogicalBottomEnd = com.laysar.G2.G2CornerValue.Absolute(0.0f);
		boolean HasLogicalBottomEnd = false;
		float RoundedRectangleExtendedFraction = (float) com.laysar.G2.G2Profile.DefaultRoundedRectangleExtendedFraction;
		float RoundedRectangleArcFraction = (float) com.laysar.G2.G2Profile.DefaultRoundedRectangleArcFraction;
		float RoundedRectangleBezierCurvatureScale = (float) com.laysar.G2.G2Profile.DefaultRoundedRectangleBezierCurvatureScale;
		float RoundedRectangleArcCurvatureScale = (float) com.laysar.G2.G2Profile.DefaultRoundedRectangleArcCurvatureScale;
		float CapsuleExtendedFraction = (float) com.laysar.G2.G2Profile.DefaultCapsuleExtendedFraction;
		float CapsuleArcFraction = (float) com.laysar.G2.G2Profile.DefaultCapsuleArcFraction;
		int FillColor = Color.TRANSPARENT;
		float FillAlpha = 1.0f;
		int[] FillGradientColors = null;
		float FillGradientAngle = 0.0f;
		int FillGradientRepeatCount = 1;
		int StrokeColor = Color.TRANSPARENT;
		float StrokeWidth = 0.0f;
		int StrokePosition = G2Background.CENTER;
		float StrokeAlpha = 1.0f;
		int[] StrokeGradientColors = null;
		float StrokeGradientAngle = 0.0f;
		int StrokeGradientRepeatCount = 1;
		int StrokeSegmentCount = 1;
		float StrokeGapLength = 0.0f;
		float StrokePatternAngle = 0.0f;
		int StrokeSegmentCap = G2Background.SHARP;
		int RippleColor = Color.TRANSPARENT;
		float RippleAlpha = 1.0f;
		boolean ClipToG2Outline = false;
		
		G2BackgroundDrawableState() {
		}
		
		G2BackgroundDrawableState(G2BackgroundDrawableState Source) {
			ChangingConfigurations = Source.ChangingConfigurations;
			BaseCorner = com.laysar.G2.G2CornerValue.CopyOf(Source.BaseCorner);
			PhysicalTopLeft = com.laysar.G2.G2CornerValue.CopyOf(Source.PhysicalTopLeft);
			HasPhysicalTopLeft = Source.HasPhysicalTopLeft;
			PhysicalTopRight = com.laysar.G2.G2CornerValue.CopyOf(Source.PhysicalTopRight);
			HasPhysicalTopRight = Source.HasPhysicalTopRight;
			PhysicalBottomLeft = com.laysar.G2.G2CornerValue.CopyOf(Source.PhysicalBottomLeft);
			HasPhysicalBottomLeft = Source.HasPhysicalBottomLeft;
			PhysicalBottomRight = com.laysar.G2.G2CornerValue.CopyOf(Source.PhysicalBottomRight);
			HasPhysicalBottomRight = Source.HasPhysicalBottomRight;
			LogicalTopStart = com.laysar.G2.G2CornerValue.CopyOf(Source.LogicalTopStart);
			HasLogicalTopStart = Source.HasLogicalTopStart;
			LogicalTopEnd = com.laysar.G2.G2CornerValue.CopyOf(Source.LogicalTopEnd);
			HasLogicalTopEnd = Source.HasLogicalTopEnd;
			LogicalBottomStart = com.laysar.G2.G2CornerValue.CopyOf(Source.LogicalBottomStart);
			HasLogicalBottomStart = Source.HasLogicalBottomStart;
			LogicalBottomEnd = com.laysar.G2.G2CornerValue.CopyOf(Source.LogicalBottomEnd);
			HasLogicalBottomEnd = Source.HasLogicalBottomEnd;
			RoundedRectangleExtendedFraction = Source.RoundedRectangleExtendedFraction;
			RoundedRectangleArcFraction = Source.RoundedRectangleArcFraction;
			RoundedRectangleBezierCurvatureScale = Source.RoundedRectangleBezierCurvatureScale;
			RoundedRectangleArcCurvatureScale = Source.RoundedRectangleArcCurvatureScale;
			CapsuleExtendedFraction = Source.CapsuleExtendedFraction;
			CapsuleArcFraction = Source.CapsuleArcFraction;
			FillColor = Source.FillColor;
			FillAlpha = Source.FillAlpha;
			FillGradientColors = Source.FillGradientColors == null ? null : Arrays.copyOf(Source.FillGradientColors, Source.FillGradientColors.length);
			FillGradientAngle = Source.FillGradientAngle;
			FillGradientRepeatCount = Source.FillGradientRepeatCount;
			StrokeColor = Source.StrokeColor;
			StrokeWidth = Source.StrokeWidth;
			StrokePosition = Source.StrokePosition;
			StrokeAlpha = Source.StrokeAlpha;
			StrokeGradientColors = Source.StrokeGradientColors == null ? null : Arrays.copyOf(Source.StrokeGradientColors, Source.StrokeGradientColors.length);
			StrokeGradientAngle = Source.StrokeGradientAngle;
			StrokeGradientRepeatCount = Source.StrokeGradientRepeatCount;
			StrokeSegmentCount = Source.StrokeSegmentCount;
			StrokeGapLength = Source.StrokeGapLength;
			StrokePatternAngle = Source.StrokePatternAngle;
			StrokeSegmentCap = Source.StrokeSegmentCap;
			RippleColor = Source.RippleColor;
			RippleAlpha = Source.RippleAlpha;
			ClipToG2Outline = Source.ClipToG2Outline;
		}
		
		@Override
		public Drawable newDrawable() {
			return new G2BackgroundDrawable(this);
		}
		
		@Override
		public Drawable newDrawable(Resources Resources) {
			return new G2BackgroundDrawable(this);
		}
		
		@Override
		public int getChangingConfigurations() {
			return ChangingConfigurations;
		}
	}
}