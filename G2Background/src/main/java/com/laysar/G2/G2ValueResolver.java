package com.laysar.G2;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import java.math.BigDecimal;

import com.laysar..G2UnitParser;

final class G2ValueResolver {
	private static final UnitParser.UnitPolicy CORNER_POLICY = UnitParser.fromAllowedUnits(
			UnitParser.UnitType.UNITLESS,
			UnitParser.UnitType.PIXEL,
			UnitParser.UnitType.DENSITY_INDEPENDENT_PIXEL,
			UnitParser.UnitType.SCALE_INDEPENDENT_PIXEL,
			UnitParser.UnitType.POINT,
			UnitParser.UnitType.PICA,
			UnitParser.UnitType.INCH,
			UnitParser.UnitType.CENTIMETER,
			UnitParser.UnitType.MILLIMETER,
			UnitParser.UnitType.QUARTER_MILLIMETER,
			UnitParser.UnitType.PERCENT
	);
	private static final UnitParser.UnitPolicy STROKE_LENGTH_POLICY = UnitParser.fromAllowedUnits(
			UnitParser.UnitType.UNITLESS,
			UnitParser.UnitType.PIXEL,
			UnitParser.UnitType.DENSITY_INDEPENDENT_PIXEL,
			UnitParser.UnitType.SCALE_INDEPENDENT_PIXEL,
			UnitParser.UnitType.POINT,
			UnitParser.UnitType.PICA,
			UnitParser.UnitType.INCH,
			UnitParser.UnitType.CENTIMETER,
			UnitParser.UnitType.MILLIMETER,
			UnitParser.UnitType.QUARTER_MILLIMETER
	);
	private static final UnitParser.UnitPolicy UNIT_INTERVAL_POLICY = UnitParser.fromAllowedUnits(
			UnitParser.UnitType.UNITLESS,
			UnitParser.UnitType.PERCENT
	);
	private static final UnitParser.UnitPolicy POSITIVE_SCALE_POLICY = UnitParser.fromAllowedUnits(
			UnitParser.UnitType.UNITLESS
	);

	private G2ValueResolver() {
	}

	static G2CornerValue ResolveCorner(Context Context, String Value) {
		return ResolveCornerResult(UnitParser.toPXResult(Context, Value, CORNER_POLICY));
	}

	static G2CornerValue ResolveCorner(Resources Resources, String Value) {
		return ResolveCornerResult(UnitParser.toPXResultFromResources(Resources, Value, CORNER_POLICY));
	}

	static Float ResolveNonNegativeLength(Context Context, String Value) {
		return ResolveNonNegativeLengthResult(UnitParser.toPXResult(Context, Value, STROKE_LENGTH_POLICY));
	}

	static Float ResolveNonNegativeLength(Resources Resources, String Value) {
		return ResolveNonNegativeLengthResult(UnitParser.toPXResultFromResources(Resources, Value, STROKE_LENGTH_POLICY));
	}

	static Float ResolveUnitInterval(Context Context, String Value) {
		return ResolveUnitIntervalResult(UnitParser.toPXResult(Context, Value, UNIT_INTERVAL_POLICY));
	}

	static Float ResolveUnitInterval(Resources Resources, String Value) {
		return ResolveUnitIntervalResult(UnitParser.toPXResultFromResources(Resources, Value, UNIT_INTERVAL_POLICY));
	}

	static Float ResolvePositiveScale(Context Context, String Value) {
		return ResolvePositiveScaleResult(UnitParser.toPXResult(Context, Value, POSITIVE_SCALE_POLICY));
	}

	static Float ResolvePositiveScale(Resources Resources, String Value) {
		return ResolvePositiveScaleResult(UnitParser.toPXResultFromResources(Resources, Value, POSITIVE_SCALE_POLICY));
	}

	static G2CornerValue ResolveCornerResult(UnitParser.UnitResult Result) {
		if (!isValidUnitResult(Result)) {
			return null;
		}
		if (Result.getUnitType() == UnitParser.UnitType.PERCENT) {
			return G2CornerValue.Percentage(Result.getValue());
		}
		return G2CornerValue.Absolute(Result.getValue());
	}

	static Float ResolveNonNegativeLengthResult(UnitParser.UnitResult Result) {
		if (!isValidUnitResult(Result)) {
			return null;
		}
		return Math.max(0.0f, Result.getValue());
	}

	static Float ResolveUnitIntervalResult(UnitParser.UnitResult Result) {
		if (!isValidUnitResult(Result)) {
			return null;
		}
		float ResolvedValue = Result.getValue();
		if (ResolvedValue < 0.0f) {
			return 0.0f;
		}
		if (ResolvedValue > 1.0f) {
			return 1.0f;
		}
		return ResolvedValue;
	}

	static Float ResolvePositiveScaleResult(UnitParser.UnitResult Result) {
		if (!isValidUnitResult(Result)) {
			return null;
		}
		return Math.max(0.0001f, Result.getValue());
	}

	static String ResolveResource(Context Context, int ResourceId) {
		if (Context == null || ResourceId == 0) {
			return null;
		}
		return ResolveScalarResource(Context.getResources(), ResourceId);
	}

	static String ResolveAttribute(Context Context, int AttributeId) {
		TypedValue ResolvedValue = ResolveThemeAttribute(Context, AttributeId);
		if (ResolvedValue == null) {
			return null;
		}
		return ResolveScalarValue(Context.getResources(), ResolvedValue);
	}

	static String ResolveScalarAttribute(TypedArray Attributes, int Index) {
		if (Attributes == null || !Attributes.hasValue(Index)) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			if (!Attributes.getValue(Index, ResolvedValue)) {
				return null;
			}
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
		return ResolveScalarValue(Attributes.getResources(), ResolvedValue);
	}

	static Float ResolveDimensionAttribute(TypedArray Attributes, int Index) {
		if (Attributes == null || !Attributes.hasValue(Index)) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			if (!Attributes.getValue(Index, ResolvedValue) || ResolvedValue.type != TypedValue.TYPE_DIMENSION) {
				return null;
			}
			float ResolvedDimension = Attributes.getDimension(Index, 0.0f);
			return isFinite(ResolvedDimension) ? ResolvedDimension : null;
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
	}

	static Boolean ResolveBooleanAttribute(TypedArray Attributes, int Index) {
		if (Attributes == null || !Attributes.hasValue(Index)) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			if (!Attributes.getValue(Index, ResolvedValue)) {
				return null;
			}
			if (ResolvedValue.type == TypedValue.TYPE_INT_BOOLEAN) {
				return ResolvedValue.data != 0;
			}
			if (ResolvedValue.type == TypedValue.TYPE_REFERENCE) {
				int ResourceId = getReferencedResourceId(ResolvedValue);
				Resources ResourcesObject = Attributes.getResources();
				if (!isResourceType(ResourcesObject, ResourceId, "bool")) {
					return null;
				}
				return ResourcesObject.getBoolean(ResourceId);
			}
			return null;
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
	}

	static Integer ResolveIntegerAttribute(TypedArray Attributes, int Index) {
		if (Attributes == null || !Attributes.hasValue(Index)) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			if (!Attributes.getValue(Index, ResolvedValue)) {
				return null;
			}
			if (isIntegerScalarType(ResolvedValue.type)) {
				return ResolvedValue.data;
			}
			if (ResolvedValue.type == TypedValue.TYPE_REFERENCE) {
				int ResourceId = getReferencedResourceId(ResolvedValue);
				Resources ResourcesObject = Attributes.getResources();
				if (!isResourceType(ResourcesObject, ResourceId, "integer")) {
					return null;
				}
				return ResourcesObject.getInteger(ResourceId);
			}
			return null;
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
	}

	static Integer ResolveEnumAttribute(TypedArray Attributes, int Index, int DefaultValue) {
		if (Attributes == null || !Attributes.hasValue(Index)) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			if (!Attributes.getValue(Index, ResolvedValue) || !isIntegerScalarType(ResolvedValue.type)) {
				return null;
			}
			return Attributes.getInt(Index, DefaultValue);
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
	}

	static String toPxString(float Value) {
		String FormattedValue = FormatNumber(Value);
		return FormattedValue == null ? null : FormattedValue + "px";
	}

	private static String ResolveScalarResource(Resources ResourcesObject, int ResourceId) {
		if (ResourcesObject == null || ResourceId == 0) {
			return null;
		}
		String ResourceType = getResourceType(ResourcesObject, ResourceId);
		if (!"string".equals(ResourceType) && !"dimen".equals(ResourceType) && !"fraction".equals(ResourceType) && !"integer".equals(ResourceType)) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			ResourcesObject.getValue(ResourceId, ResolvedValue, true);
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
		return ResolveScalarValue(ResourcesObject, ResolvedValue);
	}

	private static String ResolveScalarValue(Resources ResourcesObject, TypedValue ResolvedValue) {
		if (ResourcesObject == null || ResolvedValue == null) {
			return null;
		}
		if (ResolvedValue.type == TypedValue.TYPE_STRING) {
			return ResolvedValue.string == null ? null : ResolvedValue.string.toString().trim();
		}
		if (ResolvedValue.type == TypedValue.TYPE_DIMENSION) {
			try {
				return toPxString(TypedValue.complexToDimension(ResolvedValue.data, ResourcesObject.getDisplayMetrics()));
			} catch (RuntimeException ignored_exception) {
				return null;
			}
		}
		if (ResolvedValue.type == TypedValue.TYPE_FRACTION) {
			if ((ResolvedValue.data & TypedValue.COMPLEX_UNIT_MASK) != TypedValue.COMPLEX_UNIT_FRACTION) {
				return null;
			}
			float Fraction;
			try {
				Fraction = TypedValue.complexToFraction(ResolvedValue.data, 1.0f, 1.0f);
			} catch (RuntimeException ignored_exception) {
				return null;
			}
			String FormattedFraction = FormatNumber(Fraction * 100.0f);
			return FormattedFraction == null ? null : FormattedFraction + "%";
		}
		if (ResolvedValue.type == TypedValue.TYPE_FLOAT) {
			return FormatNumber(ResolvedValue.getFloat());
		}
		if (ResolvedValue.type == TypedValue.TYPE_REFERENCE) {
			return ResolveScalarResource(ResourcesObject, getReferencedResourceId(ResolvedValue));
		}
		if (isIntegerScalarType(ResolvedValue.type)) {
			return Integer.toString(ResolvedValue.data);
		}
		return null;
	}

	private static TypedValue ResolveThemeAttribute(Context Context, int AttributeId) {
		if (!isContextResourceType(Context, AttributeId, "attr")) {
			return null;
		}
		Resources.Theme Theme = Context.getTheme();
		if (Theme == null) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			return Theme.resolveAttribute(AttributeId, ResolvedValue, true) ? ResolvedValue : null;
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
	}

	private static String FormatNumber(float Value) {
		if (!isFinite(Value)) {
			return null;
		}
		return new BigDecimal(Float.toString(Value)).toPlainString();
	}

	private static boolean isContextResourceType(Context Context, int ResourceId, String ExpectedType) {
		return Context != null && isResourceType(Context.getResources(), ResourceId, ExpectedType);
	}

	private static boolean isResourceType(Resources ResourcesObject, int ResourceId, String ExpectedType) {
		return ExpectedType != null && ExpectedType.equals(getResourceType(ResourcesObject, ResourceId));
	}

	private static String getResourceType(Resources ResourcesObject, int ResourceId) {
		if (ResourcesObject == null || ResourceId == 0) {
			return null;
		}
		try {
			return ResourcesObject.getResourceTypeName(ResourceId);
		} catch (Resources.NotFoundException ignored_exception) {
			return null;
		} catch (RuntimeException ignored_exception) {
			return null;
		}
	}

	private static int getReferencedResourceId(TypedValue ResolvedValue) {
		if (ResolvedValue == null) {
			return 0;
		}
		if (ResolvedValue.resourceId != 0) {
			return ResolvedValue.resourceId;
		}
		return ResolvedValue.type == TypedValue.TYPE_REFERENCE ? ResolvedValue.data : 0;
	}

	private static boolean isIntegerScalarType(int Type) {
		return Type == TypedValue.TYPE_INT_DEC || Type == TypedValue.TYPE_INT_HEX;
	}

	private static boolean isValidUnitResult(UnitParser.UnitResult Result) {
		return Result != null && Result.isValid() && Result.isUnitResolved() && isFinite(Result.getValue());
	}

	private static boolean isFinite(float Value) {
		return !Float.isNaN(Value) && !Float.isInfinite(Value);
	}
}