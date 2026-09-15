package com.laysar.G2;

import android.graphics.Path;
import android.graphics.RectF;

final class G2PathBuilder {
	static final int ExactOutlineNone = 0;
	static final int ExactOutlineRectangle = 1;
	static final int ExactOutlineOval = 2;
	private static final double Epsilon = 0.000001;
	
	private G2PathBuilder() {
	}
	
	static int getExactOutlineType(float Width, float Height, float TopLeft, float TopRight, float BottomLeft, float BottomRight) {
		if (Width <= 0.0f || Height <= 0.0f) {
			return ExactOutlineNone;
		}
		double W = Width;
		double H = Height;
		double MaximumRadius = Math.min(W, H) * 0.5;
		double TL = clamp(TopLeft, 0.0, MaximumRadius);
		double TR = clamp(TopRight, 0.0, MaximumRadius);
		double BL = clamp(BottomLeft, 0.0, MaximumRadius);
		double BR = clamp(BottomRight, 0.0, MaximumRadius);
		if (isZero(TL) && isZero(TR) && isZero(BL) && isZero(BR)) {
			return ExactOutlineRectangle;
		}
		if (areEqual(W, H) && areEqual(TL, TR) && areEqual(TL, BL) && areEqual(TL, BR) && areEqual(TL, W * 0.5)) {
			return ExactOutlineOval;
		}
		return ExactOutlineNone;
	}
	
	static void build(Path Path, float Width, float Height, float TopLeft, float TopRight, float BottomLeft, float BottomRight, G2Profile Profile, G2Profile CapsuleProfile) {
		build(Path, 0.0f, 0.0f, Width, Height, TopLeft, TopRight, BottomLeft, BottomRight, Profile, CapsuleProfile, null);
	}
	
	static void build(Path Path, float Width, float Height, float TopLeft, float TopRight, float BottomLeft, float BottomRight, G2Profile Profile, G2Profile CapsuleProfile, RectF ArcRect) {
		build(Path, 0.0f, 0.0f, Width, Height, TopLeft, TopRight, BottomLeft, BottomRight, Profile, CapsuleProfile, ArcRect);
	}
	
	static void build(Path Path, float Left, float Top, float Right, float Bottom, float TopLeft, float TopRight, float BottomLeft, float BottomRight, G2Profile Profile, G2Profile CapsuleProfile) {
		build(Path, Left, Top, Right, Bottom, TopLeft, TopRight, BottomLeft, BottomRight, Profile, CapsuleProfile, null);
	}
	
	static void build(Path Path, float Left, float Top, float Right, float Bottom, float TopLeft, float TopRight, float BottomLeft, float BottomRight, G2Profile Profile, G2Profile CapsuleProfile, RectF ArcRect) {
		Path.reset();
		
		float Width = Right - Left;
		float Height = Bottom - Top;
		
		if (Width <= 0.0f || Height <= 0.0f) {
			return;
		}
		
		double W = Width;
		double H = Height;
		double MaximumRadius = Math.min(W, H) * 0.5;
		double TL = clamp(TopLeft, 0.0, MaximumRadius);
		double TR = clamp(TopRight, 0.0, MaximumRadius);
		double BL = clamp(BottomLeft, 0.0, MaximumRadius);
		double BR = clamp(BottomRight, 0.0, MaximumRadius);
		
		if (isZero(TL) && isZero(TR) && isZero(BL) && isZero(BR)) {
			Path.addRect(0.0f, 0.0f, Width, Height, android.graphics.Path.Direction.CW);
			if (Left != 0.0f || Top != 0.0f) {
				Path.offset(Left, Top);
			}
			return;
		}
		
		if (areEqual(TL, TR) && areEqual(TL, BL) && areEqual(TL, BR)) {
			double Radius = TL;
			if (areEqual(W, H) && areEqual(Radius, W * 0.5)) {
				Path.addCircle((float) (W * 0.5), (float) (H * 0.5), (float) Radius, android.graphics.Path.Direction.CW);
				if (Left != 0.0f || Top != 0.0f) {
					Path.offset(Left, Top);
				}
				return;
			}
			if (W > H && areEqual(Radius, H * 0.5)) {
				buildHorizontalCapsule(Path, W, H, Profile, CapsuleProfile, ArcRect);
				if (Left != 0.0f || Top != 0.0f) {
					Path.offset(Left, Top);
				}
				return;
			}
			if (H > W && areEqual(Radius, W * 0.5)) {
				buildVerticalCapsule(Path, W, H, Profile, CapsuleProfile, ArcRect);
				if (Left != 0.0f || Top != 0.0f) {
					Path.offset(Left, Top);
				}
				return;
			}
		}
		
		buildStandardRoundedRectangle(Path, W, H, TL, TR, BL, BR, Profile, CapsuleProfile, ArcRect);
		if (Left != 0.0f || Top != 0.0f) {
			Path.offset(Left, Top);
		}
	}
	
	private static void buildStandardRoundedRectangle(Path Path, double Width, double Height, double TopLeft, double TopRight, double BottomLeft, double BottomRight, G2Profile Profile, G2Profile CapsuleProfile, RectF ArcRect) {
		double CenterX = Width * 0.5;
		double CenterY = Height * 0.5;
		double RatioTLV = nonCapsuleRatio(CenterY, TopLeft, Profile.extendedFraction);
		double RatioTLH = nonCapsuleRatio(CenterX, TopLeft, Profile.extendedFraction);
		double RatioTRH = nonCapsuleRatio(CenterX, TopRight, Profile.extendedFraction);
		double RatioTRV = nonCapsuleRatio(CenterY, TopRight, Profile.extendedFraction);
		double RatioBRV = nonCapsuleRatio(CenterY, BottomRight, Profile.extendedFraction);
		double RatioBRH = nonCapsuleRatio(CenterX, BottomRight, Profile.extendedFraction);
		double RatioBLH = nonCapsuleRatio(CenterX, BottomLeft, Profile.extendedFraction);
		double RatioBLV = nonCapsuleRatio(CenterY, BottomLeft, Profile.extendedFraction);
		double RatioTL = Math.min(RatioTLV, RatioTLH);
		double RatioTR = Math.min(RatioTRH, RatioTRV);
		double RatioBR = Math.min(RatioBRV, RatioBRH);
		double RatioBL = Math.min(RatioBLH, RatioBLV);
		double ExtFracTL = lerp(CapsuleProfile.extendedFraction, Profile.extendedFraction, RatioTL);
		double ExtFracTR = lerp(CapsuleProfile.extendedFraction, Profile.extendedFraction, RatioTR);
		double ExtFracBR = lerp(CapsuleProfile.extendedFraction, Profile.extendedFraction, RatioBR);
		double ExtFracBL = lerp(CapsuleProfile.extendedFraction, Profile.extendedFraction, RatioBL);
		double ExtFracTLV = ExtFracTL * RatioTLV;
		double ExtFracTLH = ExtFracTL * RatioTLH;
		double ExtFracTRH = ExtFracTR * RatioTRH;
		double ExtFracTRV = ExtFracTR * RatioTRV;
		double ExtFracBRV = ExtFracBR * RatioBRV;
		double ExtFracBRH = ExtFracBR * RatioBRH;
		double ExtFracBLH = ExtFracBL * RatioBLH;
		double ExtFracBLV = ExtFracBL * RatioBLV;
		double OffsetTLV = -TopLeft * ExtFracTLV;
		double OffsetTLH = -TopLeft * ExtFracTLH;
		double OffsetTRH = -TopRight * ExtFracTRH;
		double OffsetTRV = -TopRight * ExtFracTRV;
		double OffsetBRV = -BottomRight * ExtFracBRV;
		double OffsetBRH = -BottomRight * ExtFracBRH;
		double OffsetBLH = -BottomLeft * ExtFracBLH;
		double OffsetBLV = -BottomLeft * ExtFracBLV;
		double BezKScaleTLV = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioTLV);
		double BezKScaleTLH = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioTLH);
		double BezKScaleTRH = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioTRH);
		double BezKScaleTRV = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioTRV);
		double BezKScaleBRV = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioBRV);
		double BezKScaleBRH = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioBRH);
		double BezKScaleBLH = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioBLH);
		double BezKScaleBLV = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioBLV);
		double ArcFracTL = lerp(CapsuleProfile.arcFraction, Profile.arcFraction, RatioTL);
		double ArcFracTR = lerp(CapsuleProfile.arcFraction, Profile.arcFraction, RatioTR);
		double ArcFracBR = lerp(CapsuleProfile.arcFraction, Profile.arcFraction, RatioBR);
		double ArcFracBL = lerp(CapsuleProfile.arcFraction, Profile.arcFraction, RatioBL);
		double ArcKScaleTL = 1.0 + (Profile.arcCurvatureScale - 1.0) * RatioTL;
		double ArcKScaleTR = 1.0 + (Profile.arcCurvatureScale - 1.0) * RatioTR;
		double ArcKScaleBR = 1.0 + (Profile.arcCurvatureScale - 1.0) * RatioBR;
		double ArcKScaleBL = 1.0 + (Profile.arcCurvatureScale - 1.0) * RatioBL;
		G2CubicBezier BezierTLV = new G2Profile(ExtFracTLV, ArcFracTL, BezKScaleTLV, ArcKScaleTL).createBezier();
		G2CubicBezier BezierTLH = new G2Profile(ExtFracTLH, ArcFracTL, BezKScaleTLH, ArcKScaleTL).createBezier();
		G2CubicBezier BezierTRH = new G2Profile(ExtFracTRH, ArcFracTR, BezKScaleTRH, ArcKScaleTR).createBezier();
		G2CubicBezier BezierTRV = new G2Profile(ExtFracTRV, ArcFracTR, BezKScaleTRV, ArcKScaleTR).createBezier();
		G2CubicBezier BezierBRV = new G2Profile(ExtFracBRV, ArcFracBR, BezKScaleBRV, ArcKScaleBR).createBezier();
		G2CubicBezier BezierBRH = new G2Profile(ExtFracBRH, ArcFracBR, BezKScaleBRH, ArcKScaleBR).createBezier();
		G2CubicBezier BezierBLH = new G2Profile(ExtFracBLH, ArcFracBL, BezKScaleBLH, ArcKScaleBL).createBezier();
		G2CubicBezier BezierBLV = new G2Profile(ExtFracBLV, ArcFracBL, BezKScaleBLV, ArcKScaleBL).createBezier();
		double X = 0.0;
		double Y = TopLeft;
		
		Path.moveTo(toFloat(X), toFloat(Y - OffsetTLV));
		
		if (TopLeft > 0.0) {
			Path.cubicTo(toFloat(X + BezierTLV.p1.y * TopLeft), toFloat(Y - BezierTLV.p1.x * TopLeft), toFloat(X + BezierTLV.p2.y * TopLeft), toFloat(Y - BezierTLV.p2.x * TopLeft), toFloat(X + BezierTLV.p3.y * TopLeft), toFloat(Y - BezierTLV.p3.x * TopLeft));
			arcToWithScaledRadius(Path, TopLeft, TopLeft, TopLeft, 1.0 / ArcKScaleTL, Math.PI + Math.PI * 0.5 * (1.0 - ArcFracTL) * 0.5, Math.PI * 0.5 * ArcFracTL, ArcRect);
			X = TopLeft;
			Y = 0.0;
			Path.cubicTo(toFloat(X - BezierTLH.p2.x * TopLeft), toFloat(Y + BezierTLH.p2.y * TopLeft), toFloat(X - BezierTLH.p1.x * TopLeft), toFloat(Y + BezierTLH.p1.y * TopLeft), toFloat(X - Math.max(BezierTLH.p0.x * TopLeft, OffsetTLH)), toFloat(Y + BezierTLH.p0.y * TopLeft));
		}
		
		X = Width - TopRight;
		Y = 0.0;
		Path.lineTo(toFloat(X + OffsetTRH), toFloat(Y));
		
		if (TopRight > 0.0) {
			Path.cubicTo(toFloat(X + BezierTRH.p1.x * TopRight), toFloat(Y + BezierTRH.p1.y * TopRight), toFloat(X + BezierTRH.p2.x * TopRight), toFloat(Y + BezierTRH.p2.y * TopRight), toFloat(X + BezierTRH.p3.x * TopRight), toFloat(Y + BezierTRH.p3.y * TopRight));
			arcToWithScaledRadius(Path, Width - TopRight, TopRight, TopRight, 1.0 / ArcKScaleTR, -Math.PI * 0.5 + Math.PI * 0.5 * (1.0 - ArcFracTR) * 0.5, Math.PI * 0.5 * ArcFracTR, ArcRect);
			X = Width;
			Y = TopRight;
			Path.cubicTo(toFloat(X - BezierTRV.p2.y * TopRight), toFloat(Y - BezierTRV.p2.x * TopRight), toFloat(X - BezierTRV.p1.y * TopRight), toFloat(Y - BezierTRV.p1.x * TopRight), toFloat(X - BezierTRV.p0.y * TopRight), toFloat(Y - Math.max(BezierTRV.p0.x * TopRight, OffsetTRV)));
		}
		
		X = Width;
		Y = Height - BottomRight;
		Path.lineTo(toFloat(X), toFloat(Y + OffsetBRV));
		
		if (BottomRight > 0.0) {
			Path.cubicTo(toFloat(X - BezierBRV.p1.y * BottomRight), toFloat(Y + BezierBRV.p1.x * BottomRight), toFloat(X - BezierBRV.p2.y * BottomRight), toFloat(Y + BezierBRV.p2.x * BottomRight), toFloat(X - BezierBRV.p3.y * BottomRight), toFloat(Y + BezierBRV.p3.x * BottomRight));
			arcToWithScaledRadius(Path, Width - BottomRight, Height - BottomRight, BottomRight, 1.0 / ArcKScaleBR, Math.PI * 0.5 * (1.0 - ArcFracBR) * 0.5, Math.PI * 0.5 * ArcFracBR, ArcRect);
			X = Width - BottomRight;
			Y = Height;
			Path.cubicTo(toFloat(X + BezierBRH.p2.x * BottomRight), toFloat(Y - BezierBRH.p2.y * BottomRight), toFloat(X + BezierBRH.p1.x * BottomRight), toFloat(Y - BezierBRH.p1.y * BottomRight), toFloat(X + Math.max(BezierBRH.p0.x * BottomRight, OffsetBRH)), toFloat(Y - BezierBRH.p0.y * BottomRight));
		}
		
		X = BottomLeft;
		Y = Height;
		Path.lineTo(toFloat(X - OffsetBLH), toFloat(Y));
		
		if (BottomLeft > 0.0) {
			Path.cubicTo(toFloat(X - BezierBLH.p1.x * BottomLeft), toFloat(Y - BezierBLH.p1.y * BottomLeft), toFloat(X - BezierBLH.p2.x * BottomLeft), toFloat(Y - BezierBLH.p2.y * BottomLeft), toFloat(X - BezierBLH.p3.x * BottomLeft), toFloat(Y - BezierBLH.p3.y * BottomLeft));
			arcToWithScaledRadius(Path, BottomLeft, Height - BottomLeft, BottomLeft, 1.0 / ArcKScaleBL, Math.PI * 0.5 + Math.PI * 0.5 * (1.0 - ArcFracBL) * 0.5, Math.PI * 0.5 * ArcFracBL, ArcRect);
			X = 0.0;
			Y = Height - BottomLeft;
			Path.cubicTo(toFloat(X + BezierBLV.p2.y * BottomLeft), toFloat(Y + BezierBLV.p2.x * BottomLeft), toFloat(X + BezierBLV.p1.y * BottomLeft), toFloat(Y + BezierBLV.p1.x * BottomLeft), toFloat(X + BezierBLV.p0.y * BottomLeft), toFloat(Y + Math.max(BezierBLV.p0.x * BottomLeft, OffsetBLV)));
		}
		
		Path.close();
	}
	
	private static void buildHorizontalCapsule(Path Path, double Width, double Height, G2Profile Profile, G2Profile CapsuleProfile, RectF ArcRect) {
		double Radius = Height * 0.5;
		double CenterX = Width * 0.5;
		double RatioH = nonCapsuleRatio(CenterX, Radius, CapsuleProfile.extendedFraction);
		double ExtFracH = CapsuleProfile.extendedFraction * RatioH;
		double OffsetH = -Radius * ExtFracH;
		double BezKScaleH = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioH);
		double ArcFrac = CapsuleProfile.arcFraction;
		G2CubicBezier BezierH = new G2Profile(ExtFracH, ArcFrac, BezKScaleH, 1.0).createBezier().times(Radius);
		double ArcRad = Math.PI * 0.5 * ArcFrac;
		double BezRad = (Math.PI * 0.5 - ArcRad) * 0.5;
		double SweepRad = (BezRad + ArcRad) * 2.0;
		double LeftStartAngle = Math.PI * 0.5 + BezRad;
		double X = Math.cos(LeftStartAngle) * Radius + Radius;
		double Y = Math.sin(LeftStartAngle) * Radius + Radius;
		
		Path.moveTo(toFloat(X), toFloat(Y));
		arcTo(Path, Radius, Radius, Radius, LeftStartAngle, SweepRad, ArcRect);
		
		X = Radius;
		Y = 0.0;
		Path.cubicTo(toFloat(X - BezierH.p2.x), toFloat(Y + BezierH.p2.y), toFloat(X - BezierH.p1.x), toFloat(Y + BezierH.p1.y), toFloat(X - Math.max(BezierH.p0.x, OffsetH)), toFloat(Y + BezierH.p0.y));
		
		X = Width - Radius;
		Y = 0.0;
		Path.lineTo(toFloat(X + OffsetH), toFloat(Y));
		Path.cubicTo(toFloat(X + BezierH.p1.x), toFloat(Y + BezierH.p1.y), toFloat(X + BezierH.p2.x), toFloat(Y + BezierH.p2.y), toFloat(X + BezierH.p3.x), toFloat(Y + BezierH.p3.y));
		arcTo(Path, Width - Radius, Radius, Radius, -(Math.PI * 0.5 - BezRad), SweepRad, ArcRect);
		
		X = Width - Radius;
		Y = Height;
		Path.cubicTo(toFloat(X + BezierH.p2.x), toFloat(Y - BezierH.p2.y), toFloat(X + BezierH.p1.x), toFloat(Y - BezierH.p1.y), toFloat(X + Math.max(BezierH.p0.x, OffsetH)), toFloat(Y - BezierH.p0.y));
		
		X = Radius;
		Y = Height;
		Path.lineTo(toFloat(X - OffsetH), toFloat(Y));
		Path.cubicTo(toFloat(X - BezierH.p1.x), toFloat(Y - BezierH.p1.y), toFloat(X - BezierH.p2.x), toFloat(Y - BezierH.p2.y), toFloat(X - BezierH.p3.x), toFloat(Y - BezierH.p3.y));
		Path.close();
	}
	
	private static void buildVerticalCapsule(Path Path, double Width, double Height, G2Profile Profile, G2Profile CapsuleProfile, RectF ArcRect) {
		double Radius = Width * 0.5;
		double CenterY = Height * 0.5;
		double RatioV = nonCapsuleRatio(CenterY, Radius, CapsuleProfile.extendedFraction);
		double ExtFracV = CapsuleProfile.extendedFraction * RatioV;
		double OffsetV = -Radius * ExtFracV;
		double BezKScaleV = lerp(CapsuleProfile.bezierCurvatureScale, Profile.bezierCurvatureScale, RatioV);
		double ArcFrac = CapsuleProfile.arcFraction;
		G2CubicBezier BezierV = new G2Profile(ExtFracV, ArcFrac, BezKScaleV, 1.0).createBezier().times(Radius);
		double ArcRad = Math.PI * 0.5 * ArcFrac;
		double BezRad = (Math.PI * 0.5 - ArcRad) * 0.5;
		double SweepRad = (BezRad + ArcRad) * 2.0;
		double X = 0.0;
		double Y = Radius;
		
		Path.moveTo(toFloat(X), toFloat(Y - OffsetV));
		Path.cubicTo(toFloat(X + BezierV.p1.y), toFloat(Y - BezierV.p1.x), toFloat(X + BezierV.p2.y), toFloat(Y - BezierV.p2.x), toFloat(X + BezierV.p3.y), toFloat(Y - BezierV.p3.x));
		arcTo(Path, Radius, Radius, Radius, -(Math.PI - BezRad), SweepRad, ArcRect);
		
		X = Width;
		Y = Radius;
		Path.cubicTo(toFloat(X - BezierV.p2.y), toFloat(Y - BezierV.p2.x), toFloat(X - BezierV.p1.y), toFloat(Y - BezierV.p1.x), toFloat(X - BezierV.p0.y), toFloat(Y - Math.max(BezierV.p0.x, OffsetV)));
		
		X = Width;
		Y = Height - Radius;
		Path.lineTo(toFloat(X), toFloat(Y + OffsetV));
		Path.cubicTo(toFloat(X - BezierV.p1.y), toFloat(Y + BezierV.p1.x), toFloat(X - BezierV.p2.y), toFloat(Y + BezierV.p2.x), toFloat(X - BezierV.p3.y), toFloat(Y + BezierV.p3.x));
		arcTo(Path, Width - Radius, Height - Radius, Radius, BezRad, SweepRad, ArcRect);
		
		X = 0.0;
		Y = Height - Radius;
		Path.cubicTo(toFloat(X + BezierV.p2.y), toFloat(Y + BezierV.p2.x), toFloat(X + BezierV.p1.y), toFloat(Y + BezierV.p1.x), toFloat(X + BezierV.p0.y), toFloat(Y + Math.max(BezierV.p0.x, OffsetV)));
		Path.close();
	}
	
	private static void arcToWithScaledRadius(Path Path, double CenterX, double CenterY, double Radius, double RadiusScale, double StartAngle, double SweepAngle, RectF ArcRect) {
		double CenterAngle = StartAngle + SweepAngle * 0.5;
		double ScaledCenterX = CenterX + Math.cos(CenterAngle) * Radius * (1.0 - RadiusScale);
		double ScaledCenterY = CenterY + Math.sin(CenterAngle) * Radius * (1.0 - RadiusScale);
		arcTo(Path, ScaledCenterX, ScaledCenterY, Radius * RadiusScale, StartAngle, SweepAngle, ArcRect);
	}
	
	private static void arcTo(Path Path, double CenterX, double CenterY, double Radius, double StartAngle, double SweepAngle, RectF ArcRect) {
		if (Radius <= 0.0) {
			return;
		}
		if (Math.abs(SweepAngle) <= Epsilon) {
			return;
		}
		RectF Rect = ArcRect == null ? new RectF() : ArcRect;
		Rect.set(toFloat(CenterX - Radius), toFloat(CenterY - Radius), toFloat(CenterX + Radius), toFloat(CenterY + Radius));
		Path.arcTo(Rect, toFloat(Math.toDegrees(StartAngle)), toFloat(Math.toDegrees(SweepAngle)));
	}
	
	private static double nonCapsuleRatio(double Center, double Radius, double ExtendedFraction) {
		if (Radius <= 0.0 || ExtendedFraction <= 0.0) {
			return 1.0;
		}
		return clamp(((Center / Radius) - 1.0) / ExtendedFraction, 0.0, 1.0);
	}
	
	private static double lerp(double Start, double Stop, double Fraction) {
		return Start + (Stop - Start) * Fraction;
	}
	
	private static double clamp(double Value, double Minimum, double Maximum) {
		if (Double.isNaN(Value) || Double.isInfinite(Value)) {
			return Minimum;
		}
		if (Value < Minimum) {
			return Minimum;
		}
		if (Value > Maximum) {
			return Maximum;
		}
		return Value;
	}
	
	private static boolean isZero(double Value) {
		return Math.abs(Value) <= Epsilon;
	}
	
	private static boolean areEqual(double First, double Second) {
		return Math.abs(First - Second) <= Epsilon;
	}
	
	private static float toFloat(double Value) {
		if (Double.isNaN(Value) || Double.isInfinite(Value)) {
			return 0.0f;
		}
		if (Value > Float.MAX_VALUE) {
			return Float.MAX_VALUE;
		}
		if (Value < -Float.MAX_VALUE) {
			return -Float.MAX_VALUE;
		}
		return (float) Value;
	}
}