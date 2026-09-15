package com.laysar.G2;

import java.util.Arrays;

final class G2ColorBlend {
	private G2ColorBlend() {
	}

	static Integer ResolveOrderedBlend(int[] Colors) {
		if (Colors == null || Colors.length == 0) {
			return null;
		}
		double CompositeAlpha = 0.0;
		double CompositePremultipliedR = 0.0;
		double CompositePremultipliedG = 0.0;
		double CompositePremultipliedB = 0.0;
		for (int ColorValue : Colors) {
			double InputAlpha = ((ColorValue >>> 24) & 0xff) / 255.0;
			if (InputAlpha == 0.0) {
				continue;
			}
			double InputLinearR = DecodeSRGBChannel((ColorValue >>> 16) & 0xff);
			double InputLinearG = DecodeSRGBChannel((ColorValue >>> 8) & 0xff);
			double InputLinearB = DecodeSRGBChannel(ColorValue & 0xff);
			double InverseInputAlpha = 1.0 - InputAlpha;
			CompositePremultipliedR = InputLinearR * InputAlpha + CompositePremultipliedR * InverseInputAlpha;
			CompositePremultipliedG = InputLinearG * InputAlpha + CompositePremultipliedG * InverseInputAlpha;
			CompositePremultipliedB = InputLinearB * InputAlpha + CompositePremultipliedB * InverseInputAlpha;
			CompositeAlpha = InputAlpha + CompositeAlpha * InverseInputAlpha;
		}
		if (CompositeAlpha <= 0.0) {
			return null;
		}
		return PackOpaqueSRGB(CompositePremultipliedR / CompositeAlpha, CompositePremultipliedG / CompositeAlpha, CompositePremultipliedB / CompositeAlpha);
	}

	static Integer ResolveAverageBlend(int[] Colors) {
		if (Colors == null || Colors.length == 0) {
			return null;
		}
		int[] SortedColors = Arrays.copyOf(Colors, Colors.length);
		Arrays.sort(SortedColors);
		double TotalWeight = 0.0;
		double WeightedR = 0.0;
		double WeightedG = 0.0;
		double WeightedB = 0.0;
		for (int ColorValue : SortedColors) {
			double Weight = ((ColorValue >>> 24) & 0xff) / 255.0;
			if (Weight == 0.0) {
				continue;
			}
			WeightedR += DecodeSRGBChannel((ColorValue >>> 16) & 0xff) * Weight;
			WeightedG += DecodeSRGBChannel((ColorValue >>> 8) & 0xff) * Weight;
			WeightedB += DecodeSRGBChannel(ColorValue & 0xff) * Weight;
			TotalWeight += Weight;
		}
		if (TotalWeight <= 0.0) {
			return null;
		}
		return PackOpaqueSRGB(WeightedR / TotalWeight, WeightedG / TotalWeight, WeightedB / TotalWeight);
	}

	private static double DecodeSRGBChannel(int Channel) {
		double Encoded = Channel / 255.0;
		if (Encoded <= 0.04045) {
			return Encoded / 12.92;
		}
		return StrictMath.pow((Encoded + 0.055) / 1.055, 2.4);
	}

	private static int EncodeSRGBChannel(double Linear) {
		double SafeLinear = ClampUnit(Linear);
		double Encoded;
		if (SafeLinear <= 0.0031308) {
			Encoded = 12.92 * SafeLinear;
		} else {
			Encoded = 1.055 * StrictMath.pow(SafeLinear, 1.0 / 2.4) - 0.055;
		}
		long Rounded = Math.round(ClampUnit(Encoded) * 255.0);
		if (Rounded < 0L) {
			return 0;
		}
		if (Rounded > 255L) {
			return 255;
		}
		return (int) Rounded;
	}

	private static int PackOpaqueSRGB(double LinearR, double LinearG, double LinearB) {
		int Red = EncodeSRGBChannel(LinearR);
		int Green = EncodeSRGBChannel(LinearG);
		int Blue = EncodeSRGBChannel(LinearB);
		return 0xff000000 | Red << 16 | Green << 8 | Blue;
	}

	private static double ClampUnit(double Value) {
		if (Value < 0.0) {
			return 0.0;
		}
		if (Value > 1.0) {
			return 1.0;
		}
		return Value;
	}
}