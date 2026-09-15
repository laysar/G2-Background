package com.laysar.G2;

import android.content.Context;
import android.content.res.Resources;

public final class G2Color {
	private G2Color() {
	}

	public static long Resource(Context Context, int ResourceId) {
		if (Context == null) {
			throw new IllegalArgumentException("Context is required for G2 color resource resolution.");
		}
		Integer ResolvedColor = G2ColorResolver.ResolveColorResource(Context.getResources(), Context.getTheme(), ResourceId);
		if (ResolvedColor == null) {
			throw new IllegalArgumentException("Invalid G2 color resource.");
		}
		return android.graphics.Color.pack(ResolvedColor);
	}

	public static long Attribute(Context Context, int AttributeId) {
		if (Context == null) {
			throw new IllegalArgumentException("Context is required for G2 color attribute resolution.");
		}
		Integer ResolvedColor = G2ColorResolver.ResolveThemeAttribute(Context.getResources(), Context.getTheme(), AttributeId);
		if (ResolvedColor == null) {
			throw new IllegalArgumentException("Invalid G2 color attribute.");
		}
		return android.graphics.Color.pack(ResolvedColor);
	}

	public static long Alpha(int Color, String Alpha) {
		Float ParsedAlpha = G2ValueResolver.ResolveUnitInterval(Resources.getSystem(), Alpha);
		if (ParsedAlpha == null) {
			throw new IllegalArgumentException("Invalid G2 color alpha.");
		}
		return PackAlpha(Color, ParsedAlpha);
	}

	public static long Alpha(Context Context, int Color, String Alpha) {
		if (Context == null) {
			throw new IllegalArgumentException("Context is required for G2 color source resolution.");
		}
		Resources ResourcesObject = Context.getResources();
		Integer ResolvedColor = G2ColorResolver.ResolveColorSource(ResourcesObject, Context.getTheme(), Color);
		if (ResolvedColor == null) {
			throw new IllegalArgumentException("Invalid G2 color source.");
		}
		Float ParsedAlpha = G2ValueResolver.ResolveUnitInterval(ResourcesObject, Alpha);
		if (ParsedAlpha == null) {
			throw new IllegalArgumentException("Invalid G2 color alpha.");
		}
		return PackAlpha(ResolvedColor, ParsedAlpha);
	}

	private static long PackAlpha(int Color, float Alpha) {
		int AlphaByte = Math.round(Alpha * 255.0f);
		int FinalColor = (Color & 0x00ffffff) | (AlphaByte << 24);
		return android.graphics.Color.pack(FinalColor);
	}
}