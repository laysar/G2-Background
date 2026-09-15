package com.laysar.G2;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;

final class G2StrokePatternSolver {
	static final int RESULT_EMPTY = 0;
	static final int RESULT_FULL_PATH = 1;
	static final int RESULT_SEGMENT_PATH = 2;
	static final int RESULT_ROUND_POINTS = 3;
	private static final float ApproximationError = 0.1f;
	private static final double AngularDotTolerance = 0.000001;
	private static final int EvaluationInvalid = 0;
	private static final int EvaluationValid = 1;
	private static final int EvaluationOverClosure = 2;
	private static final int ModeSymmetricReference = 0;
	private static final int ModeForward = 1;
	private static final int SampleCacheSize = 128;
	private static final int SampleCacheMask = SampleCacheSize - 1;
	private static final double NoCrossing = Double.POSITIVE_INFINITY;
	private final PathMeasure Measure = new PathMeasure();
	private final RectF Bounds = new RectF();
	private final float[] PositionA = new float[2];
	private final float[] TangentA = new float[2];
	private final float[] PositionB = new float[2];
	private final float[] TangentB = new float[2];
	private final float[] PositionBefore = new float[2];
	private final float[] PositionAfter = new float[2];
	private final int[] SampleCacheKeys = new int[SampleCacheSize];
	private final float[] SampleCachePositionX = new float[SampleCacheSize];
	private final float[] SampleCachePositionY = new float[SampleCacheSize];
	private final float[] SampleCacheTangentX = new float[SampleCacheSize];
	private final float[] SampleCacheTangentY = new float[SampleCacheSize];
	private final boolean[] SampleCacheValid = new boolean[SampleCacheSize];
	private double Perimeter;
	private double StrokeWidth;
	private double StrokeRadius;
	private double ArcTolerance;
	private double GapTolerance;
	private double TangentDelta;
	private double DistanceSolveTolerance;
	private int EffectiveCount;
	private int SegmentCap;
	private double AnchorDistance;
	private double FirstPreviousEnd;
	private double FirstNextStart;
	private double FirstGapArc;
	private double SolvedSegmentLength;
	private double SolvedVisibleGap;
	private double EvaluationResidual;
	private boolean SearchNumericalFailure;

	int Build(Path SourcePath, float StrokeWidthValue, int RequestedSegmentCount, float RequestedVisibleGap, float PatternAngle, int SegmentCapValue, Path SegmentOutput, Path RoundPointOutput) {
		if (SegmentOutput == null || RoundPointOutput == null) {
			return RESULT_EMPTY;
		}
		SegmentOutput.reset();
		RoundPointOutput.reset();
		if (SourcePath == null || SourcePath.isEmpty() || !isFinite(StrokeWidthValue) || StrokeWidthValue <= 0.0f) {
			return RESULT_EMPTY;
		}
		resetSampleCache();
		Measure.setPath(SourcePath, true);
		Perimeter = Measure.getLength();
		StrokeWidth = StrokeWidthValue;
		StrokeRadius = StrokeWidth * 0.5;
		if (!isFinite(Perimeter) || Perimeter <= 0.0 || !isFinite(StrokeWidth) || StrokeWidth <= 0.0) {
			return RESULT_EMPTY;
		}
		ArcTolerance = Math.max(0.0001, 8.0 * Math.ulp((float) Perimeter));
		GapTolerance = Math.max(0.01, Math.max(8.0 * Math.ulp((float) Perimeter), 8.0 * Math.ulp(StrokeWidthValue)));
		TangentDelta = Math.max(ArcTolerance * 4.0, Perimeter * 0.000001);
		if (!isFinite(RequestedVisibleGap) || RequestedVisibleGap <= GapTolerance) {
			return RESULT_FULL_PATH;
		}
		if (Perimeter <= StrokeWidth + ArcTolerance) {
			return RESULT_FULL_PATH;
		}
		int SafeRequestedCount = Math.max(1, RequestedSegmentCount);
		int MaximumCountByMinimumSegment = (int) Math.floor(Perimeter / StrokeWidth);
		if (MaximumCountByMinimumSegment < 1) {
			return RESULT_FULL_PATH;
		}
		EffectiveCount = Math.min(SafeRequestedCount, MaximumCountByMinimumSegment);
		DistanceSolveTolerance = Math.max(Math.ulp((float) Perimeter), ArcTolerance / Math.max(1.0, EffectiveCount * 4.0));
		SegmentCap = SegmentCapValue == G2Background.ROUND ? G2Background.ROUND : G2Background.SHARP;
		double TopCenterDistance = findTopCenterDistance(SourcePath);
		if (!isFinite(TopCenterDistance)) {
			return RESULT_FULL_PATH;
		}
		double SafePatternAngle = isFinite(PatternAngle) ? PatternAngle : 0.0;
		double NormalizedAngle = ((SafePatternAngle % 360.0) + 360.0) % 360.0;
		AnchorDistance = normalizeDistance(TopCenterDistance + Perimeter * NormalizedAngle / 360.0);
		double RequestedGap = Math.max(0.0, RequestedVisibleGap);
		boolean RequestedFeasible = solvePatternForGap(RequestedGap);
		if (!RequestedFeasible) {
			if (SearchNumericalFailure) {
				return RESULT_FULL_PATH;
			}
			double ExpandedWidth = Math.max(0.0, (double) Bounds.right - Bounds.left) + StrokeWidth;
			double ExpandedHeight = Math.max(0.0, (double) Bounds.bottom - Bounds.top) + StrokeWidth;
			double GeometricGapUpperBound = Math.hypot(ExpandedWidth, ExpandedHeight);
			if (!isFinite(GeometricGapUpperBound) || GeometricGapUpperBound <= GapTolerance || !solveMaximumFeasibleGap(RequestedGap, GeometricGapUpperBound)) {
				return RESULT_FULL_PATH;
			}
		} else {
			SolvedVisibleGap = RequestedGap;
		}
		if (SolvedVisibleGap <= GapTolerance) {
			return RESULT_FULL_PATH;
		}
		return buildAndValidate(SegmentOutput, RoundPointOutput);
	}

	private boolean solvePatternForGap(double TargetVisibleGap) {
		SearchNumericalFailure = false;
		if (!isFinite(TargetVisibleGap) || TargetVisibleGap < 0.0) {
			return false;
		}
		double LowerSegmentLength = SegmentCap == G2Background.SHARP ? StrokeWidth : 0.0;
		double HalfGap = solveFirstReferenceHalfGap(TargetVisibleGap, LowerSegmentLength);
		if (!isFinite(HalfGap)) {
			return false;
		}
		FirstPreviousEnd = AnchorDistance - HalfGap;
		FirstNextStart = AnchorDistance + HalfGap;
		FirstGapArc = HalfGap * 2.0;
		double UpperSegmentLength = Math.max(LowerSegmentLength, (Perimeter - FirstGapArc) / EffectiveCount);
		if (!isFinite(UpperSegmentLength) || UpperSegmentLength < LowerSegmentLength - ArcTolerance) {
			return false;
		}
		if (EffectiveCount == 1) {
			double SegmentLength = Perimeter - FirstGapArc;
			if (!isFinite(SegmentLength) || SegmentLength < LowerSegmentLength - ArcTolerance) {
				return false;
			}
			SolvedSegmentLength = Math.max(0.0, SegmentLength);
			SolvedVisibleGap = TargetVisibleGap;
			return true;
		}
		int LowerEvaluation = evaluateSequence(LowerSegmentLength, TargetVisibleGap);
		if (LowerEvaluation != EvaluationValid || EvaluationResidual > ArcTolerance) {
			return false;
		}
		double LowerLength = LowerSegmentLength;
		double LowerResidual = EvaluationResidual;
		double BestLength = LowerLength;
		double BestResidualMagnitude = Math.abs(LowerResidual);
		if (BestResidualMagnitude <= ArcTolerance) {
			SolvedSegmentLength = BestLength;
			SolvedVisibleGap = TargetVisibleGap;
			return true;
		}
		double UpperLength = UpperSegmentLength;
		int UpperEvaluation = evaluateSequence(UpperLength, TargetVisibleGap);
		if (UpperEvaluation == EvaluationValid && EvaluationResidual < -ArcTolerance) {
			return false;
		}
		if (UpperEvaluation == EvaluationValid && Math.abs(EvaluationResidual) < BestResidualMagnitude) {
			BestLength = UpperLength;
			BestResidualMagnitude = Math.abs(EvaluationResidual);
		}
		int Iterations = iterationCount(UpperLength - LowerLength, DistanceSolveTolerance);
		for (int Index = 0; Index < Iterations; Index++) {
			double MiddleLength = (LowerLength + UpperLength) * 0.5;
			int Evaluation = evaluateSequence(MiddleLength, TargetVisibleGap);
			if (Evaluation == EvaluationValid) {
				double Residual = EvaluationResidual;
				double Magnitude = Math.abs(Residual);
				if (Magnitude < BestResidualMagnitude) {
					BestResidualMagnitude = Magnitude;
					BestLength = MiddleLength;
				}
				if (Magnitude <= ArcTolerance) {
					BestLength = MiddleLength;
					BestResidualMagnitude = Magnitude;
					break;
				}
				if (Residual > 0.0) {
					UpperLength = MiddleLength;
				} else {
					LowerLength = MiddleLength;
				}
			} else if (Evaluation == EvaluationOverClosure) {
				UpperLength = MiddleLength;
			} else {
				return false;
			}
			if (UpperLength - LowerLength <= DistanceSolveTolerance) {
				break;
			}
		}
		if (BestResidualMagnitude > ArcTolerance) {
			int FinalEvaluation = evaluateSequence((LowerLength + UpperLength) * 0.5, TargetVisibleGap);
			if (FinalEvaluation == EvaluationValid && Math.abs(EvaluationResidual) < BestResidualMagnitude) {
				BestLength = (LowerLength + UpperLength) * 0.5;
				BestResidualMagnitude = Math.abs(EvaluationResidual);
			}
		}
		if (BestResidualMagnitude > ArcTolerance) {
			return false;
		}
		SolvedSegmentLength = BestLength;
		SolvedVisibleGap = TargetVisibleGap;
		return true;
	}

	private int evaluateSequence(double SegmentLength, double TargetVisibleGap) {
		if (!isFinite(SegmentLength) || SegmentLength < 0.0) {
			return EvaluationInvalid;
		}
		double ClosureTarget = FirstPreviousEnd + Perimeter;
		double CurrentStart = FirstNextStart;
		for (int SegmentIndex = 0; SegmentIndex < EffectiveCount - 1; SegmentIndex++) {
			double CurrentEnd = CurrentStart + SegmentLength;
			int RemainingSegmentCount = EffectiveCount - SegmentIndex - 1;
			double MaximumForwardArc = ClosureTarget - CurrentEnd - RemainingSegmentCount * SegmentLength;
			if (!isFinite(MaximumForwardArc)) {
				return EvaluationInvalid;
			}
			if (MaximumForwardArc < -ArcTolerance) {
				EvaluationResidual = -MaximumForwardArc;
				return EvaluationOverClosure;
			}
			double GapArc = solveForwardGap(CurrentEnd, TargetVisibleGap, Math.max(0.0, MaximumForwardArc));
			if (!isFinite(GapArc)) {
				if (SearchNumericalFailure) {
					return EvaluationInvalid;
				}
				EvaluationResidual = Math.max(ArcTolerance, -MaximumForwardArc + ArcTolerance);
				return EvaluationOverClosure;
			}
			CurrentStart = CurrentEnd + GapArc;
		}
		double FinalEnd = CurrentStart + SegmentLength;
		EvaluationResidual = FinalEnd - ClosureTarget;
		return isFinite(EvaluationResidual) ? EvaluationValid : EvaluationInvalid;
	}

	private double solveFirstReferenceHalfGap(double TargetVisibleGap, double LowerSegmentLength) {
		if (TargetVisibleGap <= GapTolerance) {
			return 0.0;
		}
		double MaximumHalfGap = Math.max(0.0, (Perimeter - EffectiveCount * LowerSegmentLength) * 0.5);
		if (MaximumHalfGap <= 0.0) {
			return Double.NaN;
		}
		return solveOrderedFirstCrossing(ModeSymmetricReference, AnchorDistance, TargetVisibleGap, MaximumHalfGap);
	}

	private double solveForwardGap(double PreviousSegmentEnd, double TargetVisibleGap, double MaximumForwardArc) {
		if (TargetVisibleGap <= GapTolerance) {
			return 0.0;
		}
		if (!isFinite(MaximumForwardArc) || MaximumForwardArc <= 0.0) {
			return Double.NaN;
		}
		return solveOrderedFirstCrossing(ModeForward, PreviousSegmentEnd, TargetVisibleGap, MaximumForwardArc);
	}

	private double solveOrderedFirstCrossing(int Mode, double BaseDistance, double TargetVisibleGap, double MaximumParameter) {
		SearchNumericalFailure = false;
		if (!isFinite(BaseDistance) || !isFinite(TargetVisibleGap) || !isFinite(MaximumParameter) || TargetVisibleGap < 0.0 || MaximumParameter < 0.0) {
			SearchNumericalFailure = true;
			return Double.NaN;
		}
		if (TargetVisibleGap <= GapTolerance) {
			return 0.0;
		}
		if (MaximumParameter <= 0.0) {
			return Double.NaN;
		}
		double PreferredCellSpan = Math.max(StrokeWidth * 0.5, Math.max(Perimeter / 128.0, DistanceSolveTolerance * 8.0));
		int InitialCellCount = (int) Math.ceil(MaximumParameter / PreferredCellSpan);
		InitialCellCount = Math.max(16, Math.min(128, InitialCellCount));
		double CellWidth = MaximumParameter / InitialCellCount;
		if (!isFinite(CellWidth) || CellWidth <= 0.0) {
			SearchNumericalFailure = true;
			return Double.NaN;
		}
		double LeftParameter = 0.0;
		double LeftDistance = evaluateSearchDistance(Mode, BaseDistance, LeftParameter);
		if (!isFinite(LeftDistance)) {
			SearchNumericalFailure = true;
			return Double.NaN;
		}
		for (int CellIndex = 0; CellIndex < InitialCellCount; CellIndex++) {
			double RightParameter = CellIndex == InitialCellCount - 1 ? MaximumParameter : CellWidth * (CellIndex + 1);
			double RightDistance = evaluateSearchDistance(Mode, BaseDistance, RightParameter);
			if (!isFinite(RightDistance)) {
				SearchNumericalFailure = true;
				return Double.NaN;
			}
			double Result = searchCrossingCell(Mode, BaseDistance, TargetVisibleGap, LeftParameter, RightParameter, LeftDistance, RightDistance, 0);
			if (Double.isNaN(Result)) {
				SearchNumericalFailure = true;
				return Double.NaN;
			}
			if (isFinite(Result)) {
				return validateSearchResult(Mode, BaseDistance, TargetVisibleGap, MaximumParameter, Result);
			}
			LeftParameter = RightParameter;
			LeftDistance = RightDistance;
		}
		return Double.NaN;
	}

	private double searchCrossingCell(int Mode, double BaseDistance, double TargetVisibleGap, double A, double B, double LeftDistance, double RightDistance, int Depth) {
		if (LeftDistance >= TargetVisibleGap && LeftDistance - TargetVisibleGap <= GapTolerance) {
			return A;
		}
		double TotalMotionBound = calculateTotalMotionBound(Mode, BaseDistance, A, B);
		if (!isFinite(TotalMotionBound)) {
			return Double.NaN;
		}
		double PossibleDistanceUpperBound = Math.min(LeftDistance, RightDistance) + TotalMotionBound;
		if (PossibleDistanceUpperBound < TargetVisibleGap - GapTolerance) {
			return NoCrossing;
		}
		double LeafMotionTolerance = GapTolerance * 0.25;
		if (TotalMotionBound <= LeafMotionTolerance || B - A <= DistanceSolveTolerance || Depth >= 64) {
			return searchCrossingLeaf(Mode, BaseDistance, TargetVisibleGap, A, B, LeftDistance, RightDistance);
		}
		double Middle = (A + B) * 0.5;
		double MiddleDistance = evaluateSearchDistance(Mode, BaseDistance, Middle);
		if (!isFinite(MiddleDistance)) {
			return Double.NaN;
		}
		double LeftResult = searchCrossingCell(Mode, BaseDistance, TargetVisibleGap, A, Middle, LeftDistance, MiddleDistance, Depth + 1);
		if (Double.isNaN(LeftResult) || isFinite(LeftResult)) {
			return LeftResult;
		}
		return searchCrossingCell(Mode, BaseDistance, TargetVisibleGap, Middle, B, MiddleDistance, RightDistance, Depth + 1);
	}

	private double searchCrossingLeaf(int Mode, double BaseDistance, double TargetVisibleGap, double A, double B, double LeftDistance, double RightDistance) {
		double Quarter = (B - A) * 0.25;
		double Parameter1 = A + Quarter;
		double Parameter2 = A + Quarter * 2.0;
		double Parameter3 = A + Quarter * 3.0;
		double Distance1 = evaluateSearchDistance(Mode, BaseDistance, Parameter1);
		double Distance2 = evaluateSearchDistance(Mode, BaseDistance, Parameter2);
		double Distance3 = evaluateSearchDistance(Mode, BaseDistance, Parameter3);
		if (!isFinite(Distance1) || !isFinite(Distance2) || !isFinite(Distance3)) {
			return Double.NaN;
		}
		if (LeftDistance >= TargetVisibleGap && LeftDistance - TargetVisibleGap <= GapTolerance) {
			return A;
		}
		double Crossing = refineOrderedBracket(Mode, BaseDistance, TargetVisibleGap, A, Parameter1, LeftDistance, Distance1);
		if (isFinite(Crossing)) {
			return Crossing;
		}
		Crossing = refineOrderedBracket(Mode, BaseDistance, TargetVisibleGap, Parameter1, Parameter2, Distance1, Distance2);
		if (isFinite(Crossing)) {
			return Crossing;
		}
		Crossing = refineOrderedBracket(Mode, BaseDistance, TargetVisibleGap, Parameter2, Parameter3, Distance2, Distance3);
		if (isFinite(Crossing)) {
			return Crossing;
		}
		Crossing = refineOrderedBracket(Mode, BaseDistance, TargetVisibleGap, Parameter3, B, Distance3, RightDistance);
		if (isFinite(Crossing)) {
			return Crossing;
		}
		if (LeftDistance < TargetVisibleGap && Distance1 < TargetVisibleGap && Distance2 < TargetVisibleGap && Distance3 < TargetVisibleGap && RightDistance < TargetVisibleGap) {
			double MaximumDistance = LeftDistance;
			double MaximumParameter = A;
			if (Distance1 > MaximumDistance) {
				MaximumDistance = Distance1;
				MaximumParameter = Parameter1;
			}
			if (Distance2 > MaximumDistance) {
				MaximumDistance = Distance2;
				MaximumParameter = Parameter2;
			}
			if (Distance3 > MaximumDistance) {
				MaximumDistance = Distance3;
				MaximumParameter = Parameter3;
			}
			if (RightDistance > MaximumDistance) {
				MaximumDistance = RightDistance;
				MaximumParameter = B;
			}
			if (TargetVisibleGap - MaximumDistance <= GapTolerance) {
				return MaximumParameter;
			}
		}
		return NoCrossing;
	}

	private double refineOrderedBracket(int Mode, double BaseDistance, double TargetVisibleGap, double A, double B, double LeftDistance, double RightDistance) {
		if (LeftDistance >= TargetVisibleGap || RightDistance < TargetVisibleGap) {
			return NoCrossing;
		}
		double Lower = A;
		double Upper = B;
		double LowerDistance = LeftDistance;
		double UpperDistance = RightDistance;
		double RefinementTolerance = Math.max(0.0000001, ArcTolerance / Math.max(1.0, EffectiveCount * 64.0));
		for (int Index = 0; Index < 64 && Upper - Lower > RefinementTolerance; Index++) {
			double Middle = (Lower + Upper) * 0.5;
			double MiddleDistance = evaluateSearchDistance(Mode, BaseDistance, Middle);
			if (!isFinite(MiddleDistance)) {
				return Double.NaN;
			}
			if (MiddleDistance >= TargetVisibleGap) {
				Upper = Middle;
				UpperDistance = MiddleDistance;
			} else {
				Lower = Middle;
				LowerDistance = MiddleDistance;
			}
		}
		if (LowerDistance >= TargetVisibleGap && LowerDistance - TargetVisibleGap <= GapTolerance) {
			return Lower;
		}
		double DistanceSpan = UpperDistance - LowerDistance;
		if (DistanceSpan > 0.0) {
			double Fraction = (TargetVisibleGap - LowerDistance) / DistanceSpan;
			double Candidate = Lower + (Upper - Lower) * Math.max(0.0, Math.min(1.0, Fraction));
			double CandidateDistance = evaluateSearchDistance(Mode, BaseDistance, Candidate);
			if (isFinite(CandidateDistance) && Math.abs(CandidateDistance - TargetVisibleGap) <= GapTolerance) {
				return Candidate;
			}
		}
		if (UpperDistance >= TargetVisibleGap && UpperDistance - TargetVisibleGap <= GapTolerance) {
			return Upper;
		}
		return NoCrossing;
	}

	private double acceptOrderedOvershoot(int Mode, double BaseDistance, double TargetVisibleGap, double A, double B, double LeftDistance, double RightDistance) {
		if (LeftDistance >= TargetVisibleGap || RightDistance < TargetVisibleGap || RightDistance - TargetVisibleGap > GapTolerance) {
			return NoCrossing;
		}
		double MotionBound = calculateTotalMotionBound(Mode, BaseDistance, A, B);
		if (!isFinite(MotionBound)) {
			return Double.NaN;
		}
		return MotionBound <= GapTolerance ? B : NoCrossing;
	}

	private double evaluateSearchDistance(int Mode, double BaseDistance, double Parameter) {
		if (Mode == ModeSymmetricReference) {
			return visibleGapDistance(BaseDistance - Parameter, BaseDistance + Parameter);
		}
		if (Mode == ModeForward) {
			return visibleGapDistance(BaseDistance, BaseDistance + Parameter);
		}
		return Double.NaN;
	}

	private double calculateTotalMotionBound(int Mode, double BaseDistance, double A, double B) {
		if (!isFinite(A) || !isFinite(B) || B < A) {
			return Double.NaN;
		}
		if (Mode == ModeForward) {
			return calculateOneCapMotionBound(BaseDistance + A, BaseDistance + B, B - A, true);
		}
		if (Mode == ModeSymmetricReference) {
			double PreviousBound = calculateOneCapMotionBound(BaseDistance - A, BaseDistance - B, B - A, false);
			double NextBound = calculateOneCapMotionBound(BaseDistance + A, BaseDistance + B, B - A, true);
			return isFinite(PreviousBound) && isFinite(NextBound) ? PreviousBound + NextBound : Double.NaN;
		}
		return Double.NaN;
	}

	private double calculateOneCapMotionBound(double DistanceA, double DistanceB, double ParameterSpan, boolean ForwardTraversal) {
		if (!sample(DistanceA, PositionA, TangentA)) {
			return Double.NaN;
		}
		double TangentAngleA = Math.atan2(TangentA[1], TangentA[0]);
		if (!sample(DistanceB, PositionB, TangentB)) {
			return Double.NaN;
		}
		double TangentAngleB = Math.atan2(TangentB[1], TangentB[0]);
		if (!isFinite(TangentAngleA) || !isFinite(TangentAngleB)) {
			return Double.NaN;
		}
		double TangentTurn = ForwardTraversal ? normalizeAngle(TangentAngleB - TangentAngleA) : normalizeAngle(TangentAngleA - TangentAngleB);
		double RotationMotionBound = 2.0 * StrokeRadius * Math.sin(Math.min(TangentTurn, Math.PI) * 0.5);
		double Result = ParameterSpan + RotationMotionBound;
		return isFinite(Result) && Result >= 0.0 ? Result : Double.NaN;
	}

	private double validateSearchResult(int Mode, double BaseDistance, double TargetVisibleGap, double MaximumParameter, double Result) {
		if (!isFinite(Result) || Result < -ArcTolerance || Result > MaximumParameter + ArcTolerance) {
			return Double.NaN;
		}
		double SafeResult = Math.max(0.0, Math.min(MaximumParameter, Result));
		double ActualDistance = evaluateSearchDistance(Mode, BaseDistance, SafeResult);
		if (!isFinite(ActualDistance) || Math.abs(ActualDistance - TargetVisibleGap) > GapTolerance) {
			return Double.NaN;
		}
		return SafeResult;
	}

	private boolean solveMaximumFeasibleGap(double RequestedGap, double GeometricGapUpperBound) {
		double MinimumSegmentLength = SegmentCap == G2Background.SHARP ? StrokeWidth : 0.0;
		double LowerGap = 0.0;
		double UpperGap = Math.min(RequestedGap, GeometricGapUpperBound);
		if (!isFinite(UpperGap) || UpperGap <= GapTolerance) {
			return false;
		}
		double BestGap = 0.0;
		double BestResidualMagnitude = Double.POSITIVE_INFINITY;
		int LowerEvaluation = evaluateMinimumLengthGap(LowerGap, MinimumSegmentLength);
		if (LowerEvaluation == EvaluationInvalid || LowerEvaluation == EvaluationOverClosure) {
			return false;
		}
		if (Math.abs(EvaluationResidual) < BestResidualMagnitude) {
			BestResidualMagnitude = Math.abs(EvaluationResidual);
			BestGap = LowerGap;
		}
		int Iterations = 64;
		for (int Index = 0; Index < Iterations; Index++) {
			double MiddleGap = (LowerGap + UpperGap) * 0.5;
			int Evaluation = evaluateMinimumLengthGap(MiddleGap, MinimumSegmentLength);
			if (Evaluation == EvaluationInvalid) {
				return false;
			}
			if (Evaluation == EvaluationValid) {
				double ResidualMagnitude = Math.abs(EvaluationResidual);
				if (ResidualMagnitude < BestResidualMagnitude) {
					BestResidualMagnitude = ResidualMagnitude;
					BestGap = MiddleGap;
				}
				if (ResidualMagnitude <= ArcTolerance) {
					BestGap = MiddleGap;
					BestResidualMagnitude = ResidualMagnitude;
					break;
				}
				if (EvaluationResidual < 0.0) {
					LowerGap = MiddleGap;
				} else {
					UpperGap = MiddleGap;
				}
			} else {
				UpperGap = MiddleGap;
			}
		}
		if (BestGap <= GapTolerance || BestResidualMagnitude > ArcTolerance) {
			return false;
		}
		int FinalEvaluation = evaluateMinimumLengthGap(BestGap, MinimumSegmentLength);
		if (FinalEvaluation != EvaluationValid || Math.abs(EvaluationResidual) > ArcTolerance) {
			return false;
		}
		SolvedVisibleGap = BestGap;
		SolvedSegmentLength = SegmentCap == G2Background.ROUND ? 0.0 : StrokeWidth;
		return true;
	}

	private int evaluateMinimumLengthGap(double TargetVisibleGap, double MinimumSegmentLength) {
		double HalfGap = solveFirstReferenceHalfGap(TargetVisibleGap, MinimumSegmentLength);
		if (!isFinite(HalfGap)) {
			return SearchNumericalFailure ? EvaluationInvalid : EvaluationOverClosure;
		}
		FirstPreviousEnd = AnchorDistance - HalfGap;
		FirstNextStart = AnchorDistance + HalfGap;
		FirstGapArc = HalfGap * 2.0;
		return evaluateSequence(MinimumSegmentLength, TargetVisibleGap);
	}

	private int buildAndValidate(Path SegmentOutput, Path RoundPointOutput) {
		SegmentOutput.reset();
		RoundPointOutput.reset();
		double ClosureTarget = FirstPreviousEnd + Perimeter;
		double CurrentStart = FirstNextStart;
		boolean RoundPoints = SegmentCap == G2Background.ROUND && SolvedSegmentLength <= ArcTolerance;
		int SegmentCount = 0;
		int GapCount = 1;
		for (int SegmentIndex = 0; SegmentIndex < EffectiveCount; SegmentIndex++) {
			double CurrentEnd = CurrentStart + SolvedSegmentLength;
			if (!isFinite(CurrentStart) || !isFinite(CurrentEnd) || CurrentEnd < CurrentStart - ArcTolerance || CurrentStart < FirstNextStart - ArcTolerance || CurrentEnd > ClosureTarget + ArcTolerance) {
				return resetAndFullPath(SegmentOutput, RoundPointOutput);
			}
			if (RoundPoints) {
				if (!sample(CurrentStart, PositionA, TangentA)) {
					return resetAndFullPath(SegmentOutput, RoundPointOutput);
				}
				RoundPointOutput.addCircle(PositionA[0], PositionA[1], (float) StrokeRadius, Path.Direction.CW);
			} else if (!appendInterval(CurrentStart, SolvedSegmentLength, SegmentOutput)) {
				return resetAndFullPath(SegmentOutput, RoundPointOutput);
			}
			SegmentCount++;
			if (SegmentIndex < EffectiveCount - 1) {
				int RemainingSegmentCount = EffectiveCount - SegmentIndex - 1;
				double MaximumForwardArc = ClosureTarget - CurrentEnd - RemainingSegmentCount * SolvedSegmentLength;
				double GapArc = solveForwardGap(CurrentEnd, SolvedVisibleGap, Math.max(0.0, MaximumForwardArc));
				if (!isFinite(GapArc) || GapArc < -ArcTolerance) {
					return resetAndFullPath(SegmentOutput, RoundPointOutput);
				}
				double NextStart = CurrentEnd + GapArc;
				double ActualVisibleGap = visibleGapDistance(CurrentEnd, NextStart);
				if (!isFinite(ActualVisibleGap) || Math.abs(ActualVisibleGap - SolvedVisibleGap) > GapTolerance) {
					return resetAndFullPath(SegmentOutput, RoundPointOutput);
				}
				CurrentStart = NextStart;
				GapCount++;
			} else {
				double Residual = CurrentEnd - ClosureTarget;
				if (!isFinite(Residual) || Math.abs(Residual) > ArcTolerance) {
					return resetAndFullPath(SegmentOutput, RoundPointOutput);
				}
				double FirstActualGap = visibleGapDistance(CurrentEnd, FirstNextStart);
				if (!isFinite(FirstActualGap) || Math.abs(FirstActualGap - SolvedVisibleGap) > GapTolerance) {
					return resetAndFullPath(SegmentOutput, RoundPointOutput);
				}
			}
		}
		if (SegmentCount != EffectiveCount || GapCount != EffectiveCount) {
			return resetAndFullPath(SegmentOutput, RoundPointOutput);
		}
		double FirstGapMidpoint = (FirstPreviousEnd + FirstNextStart) * 0.5;
		if (Math.abs(FirstGapMidpoint - AnchorDistance) > ArcTolerance) {
			return resetAndFullPath(SegmentOutput, RoundPointOutput);
		}
		if (RoundPoints) {
			if (!SegmentOutput.isEmpty() || RoundPointOutput.isEmpty()) {
				return resetAndFullPath(SegmentOutput, RoundPointOutput);
			}
			return RESULT_ROUND_POINTS;
		}
		if (SegmentOutput.isEmpty() || !RoundPointOutput.isEmpty()) {
			return resetAndFullPath(SegmentOutput, RoundPointOutput);
		}
		return RESULT_SEGMENT_PATH;
	}

	private int resetAndFullPath(Path SegmentOutput, Path RoundPointOutput) {
		SegmentOutput.reset();
		RoundPointOutput.reset();
		return RESULT_FULL_PATH;
	}

	private boolean appendInterval(double StartDistance, double Length, Path Destination) {
		if (!isFinite(StartDistance) || !isFinite(Length) || Length <= 0.0 || Length > Perimeter + ArcTolerance) {
			return false;
		}
		double Start = normalizeDistance(StartDistance);
		double End = Start + Length;
		if (End <= Perimeter) {
			return Measure.getSegment((float) Start, (float) End, Destination, true);
		}
		if (!Measure.getSegment((float) Start, (float) Perimeter, Destination, true)) {
			return false;
		}
		double Remaining = End - Perimeter;
		return Measure.getSegment(0.0f, (float) Remaining, Destination, false);
	}

	private double visibleGapDistance(double PreviousSegmentEndDistance, double NextSegmentStartDistance) {
		if (!sample(PreviousSegmentEndDistance, PositionA, TangentA) || !sample(NextSegmentStartDistance, PositionB, TangentB)) {
			return Double.NaN;
		}
		if (SegmentCap == G2Background.ROUND) {
			return roundArcDistance();
		}
		return sharpFaceDistance();
	}

	private boolean sample(double Distance, float[] Position, float[] Tangent) {
		double NormalizedDistance = normalizeDistance(Distance);
		float SampleDistance = (float) NormalizedDistance;
		int Key = Float.floatToIntBits(SampleDistance);
		int CacheIndex = sampleCacheIndex(Key);
		if (SampleCacheValid[CacheIndex] && SampleCacheKeys[CacheIndex] == Key) {
			Position[0] = SampleCachePositionX[CacheIndex];
			Position[1] = SampleCachePositionY[CacheIndex];
			Tangent[0] = SampleCacheTangentX[CacheIndex];
			Tangent[1] = SampleCacheTangentY[CacheIndex];
			return true;
		}
		if (!Measure.getPosTan(SampleDistance, Position, Tangent)) {
			return false;
		}
		double TangentLength = Math.hypot(Tangent[0], Tangent[1]);
		if (isFinite(TangentLength) && TangentLength > 0.0) {
			Tangent[0] = (float) (Tangent[0] / TangentLength);
			Tangent[1] = (float) (Tangent[1] / TangentLength);
			storeSample(CacheIndex, Key, Position, Tangent);
			return true;
		}
		double BeforeDistance = normalizeDistance(Distance - TangentDelta);
		double AfterDistance = normalizeDistance(Distance + TangentDelta);
		if (!Measure.getPosTan((float) BeforeDistance, PositionBefore, null) || !Measure.getPosTan((float) AfterDistance, PositionAfter, null)) {
			return false;
		}
		double DeltaX = PositionAfter[0] - PositionBefore[0];
		double DeltaY = PositionAfter[1] - PositionBefore[1];
		double DifferenceLength = Math.hypot(DeltaX, DeltaY);
		if (!isFinite(DifferenceLength) || DifferenceLength <= 0.0) {
			return false;
		}
		Tangent[0] = (float) (DeltaX / DifferenceLength);
		Tangent[1] = (float) (DeltaY / DifferenceLength);
		storeSample(CacheIndex, Key, Position, Tangent);
		return true;
	}

	private void resetSampleCache() {
		for (int Index = 0; Index < SampleCacheSize; Index++) {
			SampleCacheValid[Index] = false;
		}
	}

	private static int sampleCacheIndex(int Key) {
		int Hash = Key ^ Key >>> 16;
		Hash *= 0x7feb352d;
		Hash ^= Hash >>> 15;
		return Hash & SampleCacheMask;
	}

	private void storeSample(int CacheIndex, int Key, float[] Position, float[] Tangent) {
		SampleCacheKeys[CacheIndex] = Key;
		SampleCachePositionX[CacheIndex] = Position[0];
		SampleCachePositionY[CacheIndex] = Position[1];
		SampleCacheTangentX[CacheIndex] = Tangent[0];
		SampleCacheTangentY[CacheIndex] = Tangent[1];
		SampleCacheValid[CacheIndex] = true;
	}

	private double sharpFaceDistance() {
		double NormalAX = -TangentA[1];
		double NormalAY = TangentA[0];
		double NormalBX = -TangentB[1];
		double NormalBY = TangentB[0];
		double A0X = PositionA[0] - StrokeRadius * NormalAX;
		double A0Y = PositionA[1] - StrokeRadius * NormalAY;
		double A1X = PositionA[0] + StrokeRadius * NormalAX;
		double A1Y = PositionA[1] + StrokeRadius * NormalAY;
		double B0X = PositionB[0] - StrokeRadius * NormalBX;
		double B0Y = PositionB[1] - StrokeRadius * NormalBY;
		double B1X = PositionB[0] + StrokeRadius * NormalBX;
		double B1Y = PositionB[1] + StrokeRadius * NormalBY;
		return segmentDistance(A0X, A0Y, A1X, A1Y, B0X, B0Y, B1X, B1Y);
	}

	private double roundArcDistance() {
		double AX = PositionA[0];
		double AY = PositionA[1];
		double BX = PositionB[0];
		double BY = PositionB[1];
		double CenterDeltaX = BX - AX;
		double CenterDeltaY = BY - AY;
		double CenterDistance = Math.hypot(CenterDeltaX, CenterDeltaY);
		if (!isFinite(CenterDistance)) {
			return Double.NaN;
		}
		if (CenterDistance <= ArcTolerance) {
			return 0.0;
		}
		double Minimum = Double.POSITIVE_INFINITY;
		if (CenterDistance <= StrokeWidth + GapTolerance) {
			double HalfDistance = CenterDistance * 0.5;
			double HeightSquared = StrokeRadius * StrokeRadius - HalfDistance * HalfDistance;
			if (HeightSquared >= -GapTolerance * GapTolerance) {
				double Height = Math.sqrt(Math.max(0.0, HeightSquared));
				double UnitX = CenterDeltaX / CenterDistance;
				double UnitY = CenterDeltaY / CenterDistance;
				double MiddleX = (AX + BX) * 0.5;
				double MiddleY = (AY + BY) * 0.5;
				double PerpendicularX = -UnitY;
				double PerpendicularY = UnitX;
				double Intersection1X = MiddleX + PerpendicularX * Height;
				double Intersection1Y = MiddleY + PerpendicularY * Height;
				double Intersection2X = MiddleX - PerpendicularX * Height;
				double Intersection2Y = MiddleY - PerpendicularY * Height;
				if (belongsToPreviousRoundArc((Intersection1X - AX) / StrokeRadius, (Intersection1Y - AY) / StrokeRadius) && belongsToNextRoundArc((Intersection1X - BX) / StrokeRadius, (Intersection1Y - BY) / StrokeRadius)) {
					return 0.0;
				}
				if (belongsToPreviousRoundArc((Intersection2X - AX) / StrokeRadius, (Intersection2Y - AY) / StrokeRadius) && belongsToNextRoundArc((Intersection2X - BX) / StrokeRadius, (Intersection2Y - BY) / StrokeRadius)) {
					return 0.0;
				}
			}
		}
		double UnitCenterX = CenterDeltaX / CenterDistance;
		double UnitCenterY = CenterDeltaY / CenterDistance;
		if (belongsToPreviousRoundArc(UnitCenterX, UnitCenterY) && belongsToNextRoundArc(-UnitCenterX, -UnitCenterY)) {
			Minimum = Math.max(0.0, CenterDistance - StrokeWidth);
		}
		double NormalAX = -TangentA[1];
		double NormalAY = TangentA[0];
		double NormalBX = -TangentB[1];
		double NormalBY = TangentB[0];
		Minimum = Math.min(Minimum, pointToNextRoundArc(AX + StrokeRadius * NormalAX, AY + StrokeRadius * NormalAY));
		Minimum = Math.min(Minimum, pointToNextRoundArc(AX - StrokeRadius * NormalAX, AY - StrokeRadius * NormalAY));
		Minimum = Math.min(Minimum, pointToPreviousRoundArc(BX + StrokeRadius * NormalBX, BY + StrokeRadius * NormalBY));
		Minimum = Math.min(Minimum, pointToPreviousRoundArc(BX - StrokeRadius * NormalBX, BY - StrokeRadius * NormalBY));
		return isFinite(Minimum) ? Math.max(0.0, Minimum) : Double.NaN;
	}

	private double pointToPreviousRoundArc(double PointX, double PointY) {
		return pointToConstrainedArc(PointX, PointY, PositionA[0], PositionA[1], TangentA[0], TangentA[1], true);
	}

	private double pointToNextRoundArc(double PointX, double PointY) {
		return pointToConstrainedArc(PointX, PointY, PositionB[0], PositionB[1], TangentB[0], TangentB[1], false);
	}

	private double pointToConstrainedArc(double PointX, double PointY, double CenterX, double CenterY, double TangentX, double TangentY, boolean PreviousArc) {
		double DeltaX = PointX - CenterX;
		double DeltaY = PointY - CenterY;
		double RadialLength = Math.hypot(DeltaX, DeltaY);
		if (!isFinite(RadialLength)) {
			return Double.NaN;
		}
		if (RadialLength > ArcTolerance) {
			double DirectionX = DeltaX / RadialLength;
			double DirectionY = DeltaY / RadialLength;
			double Dot = DirectionX * TangentX + DirectionY * TangentY;
			boolean Allowed = PreviousArc ? Dot >= -AngularDotTolerance : Dot <= AngularDotTolerance;
			if (Allowed) {
				return Math.abs(RadialLength - StrokeRadius);
			}
		}
		double NormalX = -TangentY;
		double NormalY = TangentX;
		double Endpoint1X = CenterX + StrokeRadius * NormalX;
		double Endpoint1Y = CenterY + StrokeRadius * NormalY;
		double Endpoint2X = CenterX - StrokeRadius * NormalX;
		double Endpoint2Y = CenterY - StrokeRadius * NormalY;
		return Math.min(Math.hypot(PointX - Endpoint1X, PointY - Endpoint1Y), Math.hypot(PointX - Endpoint2X, PointY - Endpoint2Y));
	}

	private boolean belongsToPreviousRoundArc(double DirectionX, double DirectionY) {
		return DirectionX * TangentA[0] + DirectionY * TangentA[1] >= -AngularDotTolerance;
	}

	private boolean belongsToNextRoundArc(double DirectionX, double DirectionY) {
		return DirectionX * TangentB[0] + DirectionY * TangentB[1] <= AngularDotTolerance;
	}

	private double segmentDistance(double A0X, double A0Y, double A1X, double A1Y, double B0X, double B0Y, double B1X, double B1Y) {
		if (segmentsIntersect(A0X, A0Y, A1X, A1Y, B0X, B0Y, B1X, B1Y)) {
			return 0.0;
		}
		double Minimum = pointToSegmentDistance(A0X, A0Y, B0X, B0Y, B1X, B1Y);
		Minimum = Math.min(Minimum, pointToSegmentDistance(A1X, A1Y, B0X, B0Y, B1X, B1Y));
		Minimum = Math.min(Minimum, pointToSegmentDistance(B0X, B0Y, A0X, A0Y, A1X, A1Y));
		Minimum = Math.min(Minimum, pointToSegmentDistance(B1X, B1Y, A0X, A0Y, A1X, A1Y));
		return Minimum;
	}

	private boolean segmentsIntersect(double A0X, double A0Y, double A1X, double A1Y, double B0X, double B0Y, double B1X, double B1Y) {
		double Tolerance = Math.max(0.000000000001, GapTolerance * Math.max(1.0, StrokeWidth));
		double O1 = orientation(A0X, A0Y, A1X, A1Y, B0X, B0Y);
		double O2 = orientation(A0X, A0Y, A1X, A1Y, B1X, B1Y);
		double O3 = orientation(B0X, B0Y, B1X, B1Y, A0X, A0Y);
		double O4 = orientation(B0X, B0Y, B1X, B1Y, A1X, A1Y);
		if ((O1 > Tolerance && O2 < -Tolerance || O1 < -Tolerance && O2 > Tolerance) && (O3 > Tolerance && O4 < -Tolerance || O3 < -Tolerance && O4 > Tolerance)) {
			return true;
		}
		return Math.abs(O1) <= Tolerance && pointOnSegment(B0X, B0Y, A0X, A0Y, A1X, A1Y) || Math.abs(O2) <= Tolerance && pointOnSegment(B1X, B1Y, A0X, A0Y, A1X, A1Y) || Math.abs(O3) <= Tolerance && pointOnSegment(A0X, A0Y, B0X, B0Y, B1X, B1Y) || Math.abs(O4) <= Tolerance && pointOnSegment(A1X, A1Y, B0X, B0Y, B1X, B1Y);
	}

	private boolean pointOnSegment(double PointX, double PointY, double StartX, double StartY, double EndX, double EndY) {
		double Tolerance = GapTolerance;
		return PointX >= Math.min(StartX, EndX) - Tolerance && PointX <= Math.max(StartX, EndX) + Tolerance && PointY >= Math.min(StartY, EndY) - Tolerance && PointY <= Math.max(StartY, EndY) + Tolerance;
	}

	private static double orientation(double A0X, double A0Y, double A1X, double A1Y, double PointX, double PointY) {
		return (A1X - A0X) * (PointY - A0Y) - (A1Y - A0Y) * (PointX - A0X);
	}

	private static double pointToSegmentDistance(double PointX, double PointY, double StartX, double StartY, double EndX, double EndY) {
		double DeltaX = EndX - StartX;
		double DeltaY = EndY - StartY;
		double Denominator = DeltaX * DeltaX + DeltaY * DeltaY;
		if (Denominator <= 0.0) {
			return Math.hypot(PointX - StartX, PointY - StartY);
		}
		double Projection = ((PointX - StartX) * DeltaX + (PointY - StartY) * DeltaY) / Denominator;
		Projection = Math.max(0.0, Math.min(1.0, Projection));
		double ClosestX = StartX + Projection * DeltaX;
		double ClosestY = StartY + Projection * DeltaY;
		return Math.hypot(PointX - ClosestX, PointY - ClosestY);
	}

	private double findTopCenterDistance(Path SourcePath) {
		SourcePath.computeBounds(Bounds, true);
		double TargetX = Bounds.centerX();
		double TargetY = Bounds.top;
		float[] Approximation = SourcePath.approximate(ApproximationError);
		double BestDistanceSquared = Double.POSITIVE_INFINITY;
		double BestFraction = Double.POSITIVE_INFINITY;
		boolean FoundCandidate = false;
		if (Approximation != null && Approximation.length >= 6) {
			for (int Index = 0; Index + 5 < Approximation.length; Index += 3) {
				double Fraction0 = Approximation[Index];
				double X0 = Approximation[Index + 1];
				double Y0 = Approximation[Index + 2];
				double Fraction1 = Approximation[Index + 3];
				double X1 = Approximation[Index + 4];
				double Y1 = Approximation[Index + 5];
				if (!isFinite(Fraction0) || !isFinite(X0) || !isFinite(Y0) || !isFinite(Fraction1) || !isFinite(X1) || !isFinite(Y1) || Fraction1 < Fraction0) {
					continue;
				}
				double DeltaX = X1 - X0;
				double DeltaY = Y1 - Y0;
				double Denominator = DeltaX * DeltaX + DeltaY * DeltaY;
				double Projection = Denominator <= 0.0 ? 0.0 : ((TargetX - X0) * DeltaX + (TargetY - Y0) * DeltaY) / Denominator;
				Projection = Math.max(0.0, Math.min(1.0, Projection));
				double ProjectedX = X0 + DeltaX * Projection;
				double ProjectedY = Y0 + DeltaY * Projection;
				double DistanceX = ProjectedX - TargetX;
				double DistanceY = ProjectedY - TargetY;
				double DistanceSquared = DistanceX * DistanceX + DistanceY * DistanceY;
				double CandidateFraction = Fraction0 + (Fraction1 - Fraction0) * Projection;
				if (isBetterAnchorCandidate(DistanceSquared, CandidateFraction, BestDistanceSquared, BestFraction)) {
					BestDistanceSquared = DistanceSquared;
					BestFraction = CandidateFraction;
					FoundCandidate = true;
				}
			}
		}
		if (FoundCandidate) {
			return normalizeDistance(BestFraction * Perimeter);
		}
		BestDistanceSquared = Double.POSITIVE_INFINITY;
		double BestDistance = Double.NaN;
		for (int Index = 0; Index < 1024; Index++) {
			double SampleDistance = Perimeter * Index / 1024.0;
			if (!Measure.getPosTan((float) SampleDistance, PositionA, null)) {
				continue;
			}
			double DistanceX = PositionA[0] - TargetX;
			double DistanceY = PositionA[1] - TargetY;
			double DistanceSquared = DistanceX * DistanceX + DistanceY * DistanceY;
			if (DistanceSquared < BestDistanceSquared) {
				BestDistanceSquared = DistanceSquared;
				BestDistance = SampleDistance;
			}
		}
		return BestDistance;
	}

	private static boolean isBetterAnchorCandidate(double DistanceSquared, double CandidateFraction, double BestDistanceSquared, double BestFraction) {
		if (!isFinite(BestDistanceSquared)) {
			return true;
		}
		double Scale = Math.max(Math.abs(DistanceSquared), Math.abs(BestDistanceSquared));
		double Tolerance = Math.max(0.000000000001, Math.ulp(Scale) * 8.0);
		if (DistanceSquared < BestDistanceSquared - Tolerance) {
			return true;
		}
		return Math.abs(DistanceSquared - BestDistanceSquared) <= Tolerance && CandidateFraction < BestFraction;
	}

	private double normalizeDistance(double Distance) {
		return ((Distance % Perimeter) + Perimeter) % Perimeter;
	}

	private static double normalizeAngle(double Angle) {
		double FullTurn = Math.PI * 2.0;
		return ((Angle % FullTurn) + FullTurn) % FullTurn;
	}

	private static int iterationCount(double SearchRange, double Tolerance) {
		if (!isFinite(SearchRange) || !isFinite(Tolerance) || SearchRange <= 0.0 || Tolerance <= 0.0) {
			return 8;
		}
		double Ratio = SearchRange / Tolerance;
		int Count = Ratio <= 1.0 ? 8 : (int) Math.ceil(Math.log(Ratio) / Math.log(2.0)) + 2;
		return Math.max(8, Math.min(64, Count));
	}

	private static boolean isFinite(float Value) {
		return !Float.isNaN(Value) && !Float.isInfinite(Value);
	}

	private static boolean isFinite(double Value) {
		return !Double.isNaN(Value) && !Double.isInfinite(Value);
	}
}