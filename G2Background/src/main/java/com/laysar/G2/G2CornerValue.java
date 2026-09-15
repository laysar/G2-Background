package com.laysar.G2;

final class G2CornerValue {
	private final float Value;
	private final boolean Percentage;

	private G2CornerValue(float CurrentValue, boolean IsPercentage) {
		Value = CurrentValue;
		Percentage = IsPercentage;
	}

	static G2CornerValue Absolute(float Value) {
		float SafeValue = isFinite(Value) && Value >= 0.0f ? Value : 0.0f;
		return new G2CornerValue(SafeValue, false);
	}

	static G2CornerValue Percentage(float Fraction) {
		float SafeFraction;
		if (!isFinite(Fraction) || Fraction < 0.0f) {
			SafeFraction = 0.0f;
		} else if (Fraction > 1.0f) {
			SafeFraction = 1.0f;
		} else {
			SafeFraction = Fraction;
		}
		return new G2CornerValue(SafeFraction, true);
	}

	static G2CornerValue CopyOf(G2CornerValue Source) {
		if (Source == null) {
			return Absolute(0.0f);
		}
		return new G2CornerValue(Source.Value, Source.Percentage);
	}

	float Resolve(int Width, int Height) {
		if (!Percentage) {
			return Value;
		}
		if (Width <= 0 || Height <= 0) {
			return 0.0f;
		}
		float MaximumCornerRadius = Math.max(0.0f, Math.min(Width, Height) * 0.5f);
		return Value * MaximumCornerRadius;
	}

	boolean ContentEquals(G2CornerValue Other) {
		return Other != null && Percentage == Other.Percentage && Float.compare(Value, Other.Value) == 0;
	}

	private static boolean isFinite(float Value) {
		return !Float.isNaN(Value) && !Float.isInfinite(Value);
	}
}