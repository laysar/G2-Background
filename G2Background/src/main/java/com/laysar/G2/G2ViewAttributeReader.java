package com.laysar.G2;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.laysar..G2.G2Background;
import com.laysar.G2.R;

final class G2ViewAttributeReader {
	private G2ViewAttributeReader() {
	}
	
	static void Apply(Context Context, AttributeSet AttributeSet, int DefStyleAttr, G2Background Background) {
		Resources.Theme Theme = Context.getTheme();
		TypedArray Attributes = Context.obtainStyledAttributes(AttributeSet, R.styleable.G2ViewAttributes, DefStyleAttr, 0);
		try {
			ApplyFillAttributes(Attributes, Theme, Background);
			ApplyProfileAttributes(Attributes, Background);
			ApplyCornerAttributes(Attributes, Background);
			ApplyStrokeAttributes(Attributes, Theme, Background);
			ApplyRippleAttributes(Attributes, Theme, Background);
			ApplyClipAttributes(Attributes, Background);
		} finally {
			Attributes.recycle();
		}
	}
	
	private static void ApplyFillAttributes(TypedArray Attributes, Resources.Theme Theme, G2Background Background) {
		Integer FillColor = G2ColorResolver.ResolveColorAttribute(Attributes, R.styleable.G2ViewAttributes_G2FillColor, Theme);
		if (FillColor != null) {
			G2ColorResolver.ApplyResolvedFillColor(Background, FillColor);
		}
		String Alpha = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2Alpha);
		if (Alpha != null) {
			Background.setAlpha(Alpha);
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2FillGradient)) {
			int[] GradientColors = G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2ViewAttributes_G2FillGradient, Theme, 2);
			if (GradientColors != null) {
				G2ColorResolver.ApplyResolvedFillGradient(Background, GradientColors);
			}
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2FillGradientOrientation)) {
			String OrientationValue = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2FillGradientOrientation);
			Float Angle = G2GradientValueParser.ParseOrientation(OrientationValue);
			Background.setFillGradientOrientation(Angle == null ? 0.0f : Angle);
		}
		Integer FillGradientRepeatCount = ReadG2IntegerAttribute(Attributes, R.styleable.G2ViewAttributes_G2FillGradientRepeatCount, 1);
		if (FillGradientRepeatCount != null) {
			Background.setFillGradientRepeatCount(FillGradientRepeatCount);
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2FillOrderedBlend)) {
			int[] BlendColors = G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2ViewAttributes_G2FillOrderedBlend, Theme, 1);
			if (BlendColors != null) {
				G2ColorResolver.ApplyResolvedFillOrderedBlend(Background, BlendColors);
			}
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2FillAverageBlend)) {
			int[] BlendColors = G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2ViewAttributes_G2FillAverageBlend, Theme, 1);
			if (BlendColors != null) {
				G2ColorResolver.ApplyResolvedFillAverageBlend(Background, BlendColors);
			}
		}
	}
	
	private static void ApplyProfileAttributes(TypedArray Attributes, G2Background Background) {
		String RoundedRectangleExtendedFraction = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2RoundedRectangleExtendedFraction);
		if (RoundedRectangleExtendedFraction != null) {
			Background.setRoundedRectangleExtendedFraction(RoundedRectangleExtendedFraction);
		}
		String RoundedRectangleArcFraction = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2RoundedRectangleArcFraction);
		if (RoundedRectangleArcFraction != null) {
			Background.setRoundedRectangleArcFraction(RoundedRectangleArcFraction);
		}
		String RoundedRectangleBezierCurvatureScale = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2RoundedRectangleBezierCurvatureScale);
		if (RoundedRectangleBezierCurvatureScale != null) {
			Background.setRoundedRectangleBezierCurvatureScale(RoundedRectangleBezierCurvatureScale);
		}
		String RoundedRectangleArcCurvatureScale = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2RoundedRectangleArcCurvatureScale);
		if (RoundedRectangleArcCurvatureScale != null) {
			Background.setRoundedRectangleArcCurvatureScale(RoundedRectangleArcCurvatureScale);
		}
		String CapsuleExtendedFraction = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CapsuleExtendedFraction);
		if (CapsuleExtendedFraction != null) {
			Background.setCapsuleExtendedFraction(CapsuleExtendedFraction);
		}
		String CapsuleArcFraction = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CapsuleArcFraction);
		if (CapsuleArcFraction != null) {
			Background.setCapsuleArcFraction(CapsuleArcFraction);
		}
	}
	
	private static void ApplyCornerAttributes(TypedArray Attributes, G2Background Background) {
		String Corners = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2Corners);
		if (Corners != null) {
			Background.setCorners(Corners);
		}
		String CornerTopLeft = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerTopLeft);
		if (CornerTopLeft != null) {
			Background.setCornerTopLeft(CornerTopLeft);
		}
		String CornerTopRight = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerTopRight);
		if (CornerTopRight != null) {
			Background.setCornerTopRight(CornerTopRight);
		}
		String CornerBottomLeft = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerBottomLeft);
		if (CornerBottomLeft != null) {
			Background.setCornerBottomLeft(CornerBottomLeft);
		}
		String CornerBottomRight = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerBottomRight);
		if (CornerBottomRight != null) {
			Background.setCornerBottomRight(CornerBottomRight);
		}
		String CornerTopStart = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerTopStart);
		if (CornerTopStart != null) {
			Background.setCornerTopStart(CornerTopStart);
		}
		String CornerTopEnd = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerTopEnd);
		if (CornerTopEnd != null) {
			Background.setCornerTopEnd(CornerTopEnd);
		}
		String CornerBottomStart = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerBottomStart);
		if (CornerBottomStart != null) {
			Background.setCornerBottomStart(CornerBottomStart);
		}
		String CornerBottomEnd = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2CornerBottomEnd);
		if (CornerBottomEnd != null) {
			Background.setCornerBottomEnd(CornerBottomEnd);
		}
	}
	
	private static void ApplyStrokeAttributes(TypedArray Attributes, Resources.Theme Theme, G2Background Background) {
		Integer StrokeColor = G2ColorResolver.ResolveColorAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeColor, Theme);
		if (StrokeColor != null) {
			G2ColorResolver.ApplyResolvedStrokeColor(Background, StrokeColor);
		}
		String StrokeWidth = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeWidth);
		if (StrokeWidth != null) {
			Background.setStrokeWidth(StrokeWidth);
		}
		String StrokeAlpha = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeAlpha);
		if (StrokeAlpha != null) {
			Background.setStrokeAlpha(StrokeAlpha);
		}
		Integer StrokePosition = ReadG2EnumAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokePosition, G2Background.CENTER);
		if (StrokePosition != null) {
			Background.setStrokePosition(SanitizeStrokePosition(StrokePosition));
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2StrokeGradient)) {
			int[] GradientColors = G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeGradient, Theme, 2);
			if (GradientColors != null) {
				G2ColorResolver.ApplyResolvedStrokeGradient(Background, GradientColors);
			}
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2StrokeGradientOrientation)) {
			String OrientationValue = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeGradientOrientation);
			Float Angle = G2GradientValueParser.ParseOrientation(OrientationValue);
			Background.setStrokeGradientOrientation(Angle == null ? 0.0f : Angle);
		}
		Integer StrokeGradientRepeatCount = ReadG2IntegerAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeGradientRepeatCount, 1);
		if (StrokeGradientRepeatCount != null) {
			Background.setStrokeGradientRepeatCount(StrokeGradientRepeatCount);
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2StrokeOrderedBlend)) {
			int[] BlendColors = G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeOrderedBlend, Theme, 1);
			if (BlendColors != null) {
				G2ColorResolver.ApplyResolvedStrokeOrderedBlend(Background, BlendColors);
			}
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2StrokeAverageBlend)) {
			int[] BlendColors = G2ColorResolver.ResolveColorListAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeAverageBlend, Theme, 1);
			if (BlendColors != null) {
				G2ColorResolver.ApplyResolvedStrokeAverageBlend(Background, BlendColors);
			}
		}
		Integer StrokeSegmentCount = ReadG2IntegerAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeSegmentCount, 1);
		if (StrokeSegmentCount != null) {
			Background.setStrokeSegmentCount(StrokeSegmentCount);
		}
		String StrokeGapLength = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeGapLength);
		if (StrokeGapLength != null) {
			Background.setStrokeGapLength(StrokeGapLength);
		}
		if (Attributes.hasValue(R.styleable.G2ViewAttributes_G2StrokePatternAngle)) {
			String PatternAngleValue = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokePatternAngle);
			Float PatternAngle = G2GradientValueParser.ParseAngle(PatternAngleValue);
			if (PatternAngle != null) {
				Background.setStrokePatternAngle(PatternAngle);
			}
		}
		Integer StrokeSegmentCap = ReadG2EnumAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeSegmentCap, G2Background.SHARP);
		if (StrokeSegmentCap != null) {
			Background.setStrokeSegmentCap(SanitizeStrokeSegmentCap(StrokeSegmentCap));
		}
		Integer StrokeLayer = ReadG2EnumAttribute(Attributes, R.styleable.G2ViewAttributes_G2StrokeLayer, G2Background.BELOW_CONTENT);
		if (StrokeLayer != null) {
			Background.setStrokeLayer(SanitizeStrokeLayer(StrokeLayer));
		}
	}
	
	private static void ApplyRippleAttributes(TypedArray Attributes, Resources.Theme Theme, G2Background Background) {
		Integer RippleColor = G2ColorResolver.ResolveColorAttribute(Attributes, R.styleable.G2ViewAttributes_G2RippleColor, Theme);
		if (RippleColor != null) {
			G2ColorResolver.ApplyResolvedRippleColor(Background, RippleColor);
		}
		String RippleAlpha = ReadG2StringAttribute(Attributes, R.styleable.G2ViewAttributes_G2RippleAlpha);
		if (RippleAlpha != null) {
			Background.setRippleAlpha(RippleAlpha);
		}
	}
	
	private static void ApplyClipAttributes(TypedArray Attributes, G2Background Background) {
		Boolean ClipToOutline = ReadG2BooleanAttribute(Attributes, R.styleable.G2ViewAttributes_G2ClipToOutline);
		if (ClipToOutline != null) {
			Background.setClipToG2Outline(ClipToOutline);
		}
	}
	
	private static String ReadG2StringAttribute(TypedArray Attributes, int Index) {
		return G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static String ReadTypedValueAsString(TypedArray Attributes, int Index, TypedValue Value) {
		return G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static String ReadStringFallback(TypedArray Attributes, int Index) {
		return G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static String ReadReferencedValueAsString(TypedArray Attributes, int Index) {
		return G2ValueResolver.ResolveScalarAttribute(Attributes, Index);
	}
	
	private static Float ReadDimensionAttribute(TypedArray Attributes, int Index) {
		return G2ValueResolver.ResolveDimensionAttribute(Attributes, Index);
	}
	
	private static Boolean ReadG2BooleanAttribute(TypedArray Attributes, int Index) {
		return G2ValueResolver.ResolveBooleanAttribute(Attributes, Index);
	}
	
	private static Integer ReadG2IntegerAttribute(TypedArray Attributes, int Index, int DefaultValue) {
		return G2ValueResolver.ResolveIntegerAttribute(Attributes, Index);
	}

	private static Integer ReadG2EnumAttribute(TypedArray Attributes, int Index, int DefaultValue) {
		return G2ValueResolver.ResolveEnumAttribute(Attributes, Index, DefaultValue);
	}
	
	private static int SanitizeStrokePosition(int Position) {
		if (Position == G2Background.INSIDE || Position == G2Background.CENTER || Position == G2Background.OUTSIDE) {
			return Position;
		}
		return G2Background.CENTER;
	}

	private static int SanitizeStrokeSegmentCap(int Cap) {
		return Cap == G2Background.ROUND ? G2Background.ROUND : G2Background.SHARP;
	}

	private static int SanitizeStrokeLayer(int Layer) {
		if (Layer == G2Background.BELOW_CONTENT || Layer == G2Background.ABOVE_CONTENT) {
			return Layer;
		}
		return G2Background.BELOW_CONTENT;
	}
}