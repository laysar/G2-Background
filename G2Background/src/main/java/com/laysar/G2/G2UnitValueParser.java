package com.laysar.G2;

import android.content.Context;
import android.content.res.Resources;

import com.laysar..G2UnitParser;

final class G2UnitValueParser {
	private G2UnitValueParser() {
	}
	
	static G2CornerValue ParseCorner(Context Context, String Value) {
		return G2ValueResolver.ResolveCorner(Context, Value);
	}
	
	static G2CornerValue ParseCorner(Resources Resources, String Value) {
		return G2ValueResolver.ResolveCorner(Resources, Value);
	}
	
	static Float ParseStrokeWidth(Context Context, String Value) {
		return G2ValueResolver.ResolveNonNegativeLength(Context, Value);
	}
	
	static Float ParseStrokeWidth(Resources Resources, String Value) {
		return G2ValueResolver.ResolveNonNegativeLength(Resources, Value);
	}
	
	static Float ParseStrokeGapLength(Context Context, String Value) {
		return G2ValueResolver.ResolveNonNegativeLength(Context, Value);
	}
	
	static Float ParseStrokeGapLength(Resources Resources, String Value) {
		return G2ValueResolver.ResolveNonNegativeLength(Resources, Value);
	}
	
	static Float ParseUnitInterval(Context Context, String Value) {
		return G2ValueResolver.ResolveUnitInterval(Context, Value);
	}
	
	static Float ParseUnitInterval(Resources Resources, String Value) {
		return G2ValueResolver.ResolveUnitInterval(Resources, Value);
	}
	
	static Float ParsePositiveScale(Context Context, String Value) {
		return G2ValueResolver.ResolvePositiveScale(Context, Value);
	}
	
	static Float ParsePositiveScale(Resources Resources, String Value) {
		return G2ValueResolver.ResolvePositiveScale(Resources, Value);
	}
	
	private static G2CornerValue ParseCornerResult(UnitParser.UnitResult Result) {
		return G2ValueResolver.ResolveCornerResult(Result);
	}
	
	private static Float ParseNonNegativeLengthResult(UnitParser.UnitResult Result) {
		return G2ValueResolver.ResolveNonNegativeLengthResult(Result);
	}
	
	private static Float ParseUnitIntervalResult(UnitParser.UnitResult Result) {
		return G2ValueResolver.ResolveUnitIntervalResult(Result);
	}
	
	private static Float ParsePositiveScaleResult(UnitParser.UnitResult Result) {
		return G2ValueResolver.ResolvePositiveScaleResult(Result);
	}
}