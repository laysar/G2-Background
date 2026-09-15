package com.laysar.G2;

import android.content.Context;

public final class G2Value {
	private G2Value() {
	}

	public static String Resource(Context Context, int ResourceId) {
		String ResolvedValue = G2ValueResolver.ResolveResource(Context, ResourceId);
		if (ResolvedValue == null) {
			throw new IllegalArgumentException("Invalid G2 scalar resource.");
		}
		return ResolvedValue;
	}

	public static String Attribute(Context Context, int AttributeId) {
		String ResolvedValue = G2ValueResolver.ResolveAttribute(Context, AttributeId);
		if (ResolvedValue == null) {
			throw new IllegalArgumentException("Invalid G2 scalar attribute.");
		}
		return ResolvedValue;
	}
}