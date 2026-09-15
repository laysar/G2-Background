package com.laysar.G2;

final class G2Profile {
    static final double DefaultRoundedRectangleExtendedFraction = 0.5286651;
    static final double DefaultRoundedRectangleArcFraction = 5.0 / 9.0;
    static final double DefaultRoundedRectangleBezierCurvatureScale = 1.0732051;
    static final double DefaultRoundedRectangleArcCurvatureScale = 1.0732051;
    static final double DefaultCapsuleExtendedFraction = 0.5286651 * 0.75;
    static final double DefaultCapsuleArcFraction = 0.0;
    static final double DefaultCapsuleBezierCurvatureScale = 1.0;
    static final double DefaultCapsuleArcCurvatureScale = 1.0;
    private static final double MinimumDivisor = 0.000000001;

    final double extendedFraction;
    final double arcFraction;
    final double bezierCurvatureScale;
    final double arcCurvatureScale;

    G2Profile(double ExtendedFraction, double ArcFraction, double BezierCurvatureScale, double ArcCurvatureScale) {
        extendedFraction = ExtendedFraction;
        arcFraction = ArcFraction;
        bezierCurvatureScale = BezierCurvatureScale;
        arcCurvatureScale = ArcCurvatureScale;
    }

    static G2Profile roundedRectangleDefault() {
        return new G2Profile(DefaultRoundedRectangleExtendedFraction, DefaultRoundedRectangleArcFraction, DefaultRoundedRectangleBezierCurvatureScale, DefaultRoundedRectangleArcCurvatureScale);
    }

    static G2Profile capsuleDefault() {
        return new G2Profile(DefaultCapsuleExtendedFraction, DefaultCapsuleArcFraction, DefaultCapsuleBezierCurvatureScale, DefaultCapsuleArcCurvatureScale);
    }

    static G2Profile lerp(G2Profile Start, G2Profile Stop, double Fraction) {
        double SafeFraction = clamp(Fraction, 0.0, 1.0);
        return new G2Profile(
                lerp(Start.extendedFraction, Stop.extendedFraction, SafeFraction),
                lerp(Start.arcFraction, Stop.arcFraction, SafeFraction),
                lerp(Start.bezierCurvatureScale, Stop.bezierCurvatureScale, SafeFraction),
                lerp(Start.arcCurvatureScale, Stop.arcCurvatureScale, SafeFraction)
        );
    }

    G2CubicBezier createBezier() {
        double ArcRadians = Math.PI * 0.5 * arcFraction;
        double BezierRadians = (Math.PI * 0.5 - ArcRadians) * 0.5;
        double Sin = Math.sin(BezierRadians);
        double Cos = Math.cos(BezierRadians);

        if (bezierCurvatureScale == 1.0 && arcCurvatureScale == 1.0) {
            double HalfTan = Sin / (1.0 + Cos);
            return new G2CubicBezier(
                    new G2Point(-extendedFraction, 0.0),
                    new G2Point((1.0 - 1.5 / (1.0 + Cos)) * HalfTan, 0.0),
                    new G2Point(HalfTan, 0.0),
                    new G2Point(Sin, 1.0 - Cos)
            );
        }

        double RadiusScale = 1.0 / arcCurvatureScale;
        double SqrtHalf = 1.0 / Math.sqrt(2.0);
        G2Point ArcCenter = new G2Point(0.0, 1.0).plus(new G2Point(SqrtHalf, -SqrtHalf).times(1.0 - RadiusScale));
        G2Point ArcStartPoint = ArcCenter.plus(new G2Point(Sin, -Cos).times(RadiusScale));
        return generateG2ContinuousBezierWithZeroStartCurvature(
                new G2Point(-extendedFraction, 0.0),
                ArcStartPoint,
                new G2Point(1.0, 0.0),
                new G2Point(Cos, Sin),
                bezierCurvatureScale
        );
    }

    private static G2CubicBezier generateG2ContinuousBezierWithZeroStartCurvature(G2Point Start, G2Point End, G2Point StartTangent, G2Point EndTangent, double EndCurvature) {
        double B = StartTangent.x * EndTangent.y - StartTangent.y * EndTangent.x;
        double Distance = Math.hypot(End.x - Start.x, End.y - Start.y);

        if (Math.abs(B) < MinimumDivisor || Distance < MinimumDivisor) {
            double Handle = Distance / 3.0;
            return new G2CubicBezier(
                    Start,
                    Start.plus(StartTangent.times(Handle)),
                    End.minus(EndTangent.times(Handle)),
                    End
            );
        }

        double A2 = 1.5 * EndCurvature;
        double Dx = End.x - Start.x;
        double Dy = End.y - Start.y;
        double C1 = -Dy * StartTangent.x + Dx * StartTangent.y;
        double C2 = Dy * EndTangent.x - Dx * EndTangent.y;
        double Lambda0 = -C2 / B - A2 * C1 * C1 / B / B / B;
        double Lambda3 = -C1 / B;
        G2Point P1 = Start.plus(new G2Point(Math.max(Lambda0 * StartTangent.x, 0.0), Math.max(Lambda0 * StartTangent.y, 0.0)));
        G2Point P2 = End.minus(new G2Point(Math.max(Lambda3 * EndTangent.x, 0.0), Math.max(Lambda3 * EndTangent.y, 0.0)));
        return new G2CubicBezier(Start, P1, P2, End);
    }

    private static double lerp(double Start, double Stop, double Fraction) {
        return Start + (Stop - Start) * Fraction;
    }

    private static double clamp(double Value, double Minimum, double Maximum) {
        if (Value < Minimum) {
            return Minimum;
        }
        if (Value > Maximum) {
            return Maximum;
        }
        return Value;
    }
}