package com.laysar.G2;

import androidx.annotation.RestrictTo;

import java.util.regex.Pattern;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class G2GradientValueParser {
	private static final String DegreeSymbol = "\u00B0";
	private static final Pattern AnglePattern = Pattern.compile(
			"^[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:" + DegreeSymbol + ")?$"
	);

	private G2GradientValueParser() {
	}

	public static int[] ParseColors(String Value) {
		int[] Colors = ParseColorList(Value);
		return Colors != null && Colors.length >= 2 ? Colors : null;
	}

	public static int[] ParseColorList(String Value) {
		if (Value == null) {
			return null;
		}
		String Normalized = Value.trim();
		if (Normalized.length() == 0) {
			return null;
		}
		String[] Tokens = Normalized.split(",", -1);
		if (Tokens.length == 0) {
			return null;
		}
		int[] Colors = new int[Tokens.length];
		for (int Index = 0; Index < Tokens.length; Index++) {
			String Token = Tokens[Index].trim();
			if (!isValidColorToken(Token)) {
				return null;
			}
			long Parsed = Long.parseLong(Token.substring(1), 16);
			Colors[Index] = Token.length() == 7 ? (int) (0xff000000L | Parsed) : (int) Parsed;
		}
		return Colors;
	}

	public static Float ParseAngle(String Value) {
		if (Value == null) {
			return null;
		}
		String Normalized = Value.trim();
		if (!AnglePattern.matcher(Normalized).matches()) {
			return null;
		}
		if (Normalized.endsWith(DegreeSymbol)) {
			Normalized = Normalized.substring(0, Normalized.length() - DegreeSymbol.length());
		}
		try {
			float Parsed = Float.parseFloat(Normalized);
			return isFinite(Parsed) ? Parsed : null;
		} catch (NumberFormatException Ignored) {
			return null;
		}
	}

	public static Float ParseOrientation(String Value) {
		if (Value == null) {
			return null;
		}
		String Normalized = Value.trim();
		if ("LeftToRight".equals(Normalized)) {
			return G2Background.LEFT_TO_RIGHT;
		}
		if ("TopLeftToBottomRight".equals(Normalized)) {
			return G2Background.TOP_LEFT_TO_BOTTOM_RIGHT;
		}
		if ("TopToBottom".equals(Normalized)) {
			return G2Background.TOP_TO_BOTTOM;
		}
		if ("TopRightToBottomLeft".equals(Normalized)) {
			return G2Background.TOP_RIGHT_TO_BOTTOM_LEFT;
		}
		if ("RightToLeft".equals(Normalized)) {
			return G2Background.RIGHT_TO_LEFT;
		}
		if ("BottomRightToTopLeft".equals(Normalized)) {
			return G2Background.BOTTOM_RIGHT_TO_TOP_LEFT;
		}
		if ("BottomToTop".equals(Normalized)) {
			return G2Background.BOTTOM_TO_TOP;
		}
		if ("BottomLeftToTopRight".equals(Normalized)) {
			return G2Background.BOTTOM_LEFT_TO_TOP_RIGHT;
		}
		return ParseAngle(Normalized);
	}


	private static boolean isValidColorToken(String Value) {
		if (Value == null || (Value.length() != 7 && Value.length() != 9) || Value.charAt(0) != '#') {
			return false;
		}
		for (int Index = 1; Index < Value.length(); Index++) {
			char Character = Value.charAt(Index);
			boolean Decimal = Character >= '0' && Character <= '9';
			boolean Lower = Character >= 'a' && Character <= 'f';
			boolean Upper = Character >= 'A' && Character <= 'F';
			if (!Decimal && !Lower && !Upper) {
				return false;
			}
		}
		return true;
	}

	private static boolean isFinite(float Value) {
		return !Float.isNaN(Value) && !Float.isInfinite(Value);
	}
}