package com.laysar.G2;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UnitParser {
	@UnitSelection
	public static final int RAW_VALUE_ONLY = UnitType.UNITLESS;
	@UnitSelection
	public static final int ALL_UNITS = UnitType.UNITLESS | UnitType.PIXEL | UnitType.DENSITY_INDEPENDENT_PIXEL | UnitType.SCALE_INDEPENDENT_PIXEL | UnitType.POINT | UnitType.PICA | UnitType.INCH | UnitType.CENTIMETER | UnitType.MILLIMETER | UnitType.QUARTER_MILLIMETER | UnitType.PERCENT | UnitType.DEGREE;
	@UnitSelection
	public static final int ANDROID_UNITS = UnitType.PIXEL | UnitType.DENSITY_INDEPENDENT_PIXEL | UnitType.SCALE_INDEPENDENT_PIXEL;
	@UnitSelection
	public static final int PRINT_UNITS = UnitType.POINT | UnitType.PICA;
	@UnitSelection
	public static final int PHYSICAL_LENGTH_UNITS = UnitType.INCH | UnitType.CENTIMETER | UnitType.MILLIMETER | UnitType.QUARTER_MILLIMETER;
	private static final String LOG_TAG = "UnitParser";
	private static final String UNIT_PIXEL = "px";
	private static final String UNIT_DENSITY_INDEPENDENT_PIXEL = "dp";
	private static final String UNIT_SCALE_INDEPENDENT_PIXEL = "sp";
	private static final String UNIT_POINT = "pt";
	private static final String UNIT_PICA = "pc";
	private static final String UNIT_INCH = "in";
	private static final String UNIT_CENTIMETER = "cm";
	private static final String UNIT_MILLIMETER = "mm";
	private static final String UNIT_QUARTER_MILLIMETER = "qmm";
	private static final String UNIT_PERCENT = "\u0025";
	private static final String UNIT_DEGREE = "\u00B0";
	private static final float PICA_TO_POINT = 12.0f;
	private static final float CENTIMETER_TO_MILLIMETER = 10.0f;
	private static final float QUARTER_MILLIMETER_TO_MILLIMETER = 0.25f;
	private static final Pattern VALUE_PATTERN = Pattern.compile("^([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))([a-zA-Z]*|\u0025|\u00B0)$");
	private static final int UNIT_TYPE_COUNT = 12;
	private static final UnitPolicy DEFAULT_UNIT_POLICY = new UnitPolicy(true, ALL_UNITS);
	private static final InputConstraints DEFAULT_INPUT_CONSTRAINTS = new InputConstraints(DEFAULT_UNIT_POLICY, DEFAULT_UNIT_POLICY, false, 0.0f, false, 0.0f, 0, 0, new float[UNIT_TYPE_COUNT], new float[UNIT_TYPE_COUNT]);
	private static final InputConstraints INVALID_INPUT_CONSTRAINTS = new InputConstraints(DEFAULT_UNIT_POLICY, DEFAULT_UNIT_POLICY, false, 0.0f, false, 0.0f, 0, 0, new float[UNIT_TYPE_COUNT], new float[UNIT_TYPE_COUNT]);
	private static final UnitPolicy INVALID_UNIT_POLICY = new UnitPolicy(true, 0);
	private static final MetricsResolver<Context> CONTEXT_METRICS_RESOLVER = new MetricsResolver<Context>() {
		@Override
		public DisplayMetrics getMetrics(Context AppContext) {
			return UnitParser.getMetrics(AppContext);
		}
	};
	private static final MetricsResolver<Resources> RESOURCES_METRICS_RESOLVER = new MetricsResolver<Resources>() {
		@Override
		public DisplayMetrics getMetrics(Resources AppResources) {
			return UnitParser.getMetrics(AppResources);
		}
	};
	
	private UnitParser() {
	}

	public static float toPX(Context AppContext, String RawValue) {
		return toPXResult(AppContext, RawValue).getValue();
	}

	public static float toPX(Context AppContext, String RawValue, UnitPolicy Policy) {
		return toPXResult(AppContext, RawValue, Policy).getValue();
	}

	public static float toPX(Context AppContext, String RawValue, UnitPolicy FirstPolicy, UnitPolicy SecondPolicy) {
		return toPXResult(AppContext, RawValue, FirstPolicy, SecondPolicy).getValue();
	}

	public static float toPXWithConstraints(Context AppContext, String RawValue, InputConstraints Constraints) {
		return toPXResultWithConstraints(AppContext, RawValue, Constraints).getValue();
	}

	public static float toPXFromResources(Resources AppResources, String RawValue) {
		return toPXResultFromResources(AppResources, RawValue).getValue();
	}
	
	public static float toPXFromResources(Resources AppResources, String RawValue, UnitPolicy Policy) {
		return toPXResultFromResources(AppResources, RawValue, Policy).getValue();
	}
	
	public static float toPXFromResources(Resources AppResources, String RawValue, UnitPolicy FirstPolicy, UnitPolicy SecondPolicy) {
		return toPXResultFromResources(AppResources, RawValue, FirstPolicy, SecondPolicy).getValue();
	}
	
	public static float toPXFromResourcesWithConstraints(Resources AppResources, String RawValue, InputConstraints Constraints) {
		return toPXResultFromResourcesWithConstraints(AppResources, RawValue, Constraints).getValue();
	}
	
	public static UnitResult toPXResult(Context AppContext, String RawValue) {
		return toPXResultInternal(AppContext, CONTEXT_METRICS_RESOLVER, RawValue, DEFAULT_UNIT_POLICY, DEFAULT_UNIT_POLICY, DEFAULT_INPUT_CONSTRAINTS);
	}
	
	public static UnitResult toPXResult(Context AppContext, String RawValue, UnitPolicy Policy) {
		if (Policy == null) {
			return new UnitResult(toErrorValue("Unit policy must not be null"), 0, false);
		}
		
		if (Policy == INVALID_UNIT_POLICY) {
			return new UnitResult(0.0f, 0, false);
		}
		
		return toPXResultInternal(AppContext, CONTEXT_METRICS_RESOLVER, RawValue, Policy, DEFAULT_UNIT_POLICY, DEFAULT_INPUT_CONSTRAINTS);
	}
	
	public static UnitResult toPXResult(Context AppContext, String RawValue, UnitPolicy FirstPolicy, UnitPolicy SecondPolicy) {
		return toPXResultInternal(AppContext, CONTEXT_METRICS_RESOLVER, RawValue, FirstPolicy, SecondPolicy, DEFAULT_INPUT_CONSTRAINTS);
	}
	
	public static UnitResult toPXResultWithConstraints(Context AppContext, String RawValue, InputConstraints Constraints) {
		if (Constraints == null) {
			return new UnitResult(toErrorValue("Input constraints must not be null"), 0, false);
		}
		
		if (Constraints == INVALID_INPUT_CONSTRAINTS) {
			return new UnitResult(0.0f, 0, false);
		}
		
		return toPXResultInternal(AppContext, CONTEXT_METRICS_RESOLVER, RawValue, Constraints.FirstUnitPolicy, Constraints.SecondUnitPolicy, Constraints);
	}
	
	public static UnitResult toPXResultFromResources(Resources AppResources, String RawValue) {
		return toPXResultInternal(AppResources, RESOURCES_METRICS_RESOLVER, RawValue, DEFAULT_UNIT_POLICY, DEFAULT_UNIT_POLICY, DEFAULT_INPUT_CONSTRAINTS);
	}
	
	public static UnitResult toPXResultFromResources(Resources AppResources, String RawValue, UnitPolicy Policy) {
		if (Policy == null) {
			return new UnitResult(toErrorValue("Unit policy must not be null"), 0, false);
		}
		
		if (Policy == INVALID_UNIT_POLICY) {
			return new UnitResult(0.0f, 0, false);
		}
		
		return toPXResultInternal(AppResources, RESOURCES_METRICS_RESOLVER, RawValue, Policy, DEFAULT_UNIT_POLICY, DEFAULT_INPUT_CONSTRAINTS);
	}
	
	public static UnitResult toPXResultFromResources(Resources AppResources, String RawValue, UnitPolicy FirstPolicy, UnitPolicy SecondPolicy) {
		return toPXResultInternal(AppResources, RESOURCES_METRICS_RESOLVER, RawValue, FirstPolicy, SecondPolicy, DEFAULT_INPUT_CONSTRAINTS);
	}
	
	public static UnitResult toPXResultFromResourcesWithConstraints(Resources AppResources, String RawValue, InputConstraints Constraints) {
		if (Constraints == null) {
			return new UnitResult(toErrorValue("Input constraints must not be null"), 0, false);
		}
		
		if (Constraints == INVALID_INPUT_CONSTRAINTS) {
			return new UnitResult(0.0f, 0, false);
		}
		
		return toPXResultInternal(AppResources, RESOURCES_METRICS_RESOLVER, RawValue, Constraints.FirstUnitPolicy, Constraints.SecondUnitPolicy, Constraints);
	}
	
	private static <MetricsOwnerType> UnitResult toPXResultInternal(MetricsOwnerType MetricsOwner, MetricsResolver<MetricsOwnerType> SelectedMetricsResolver, String RawValue, UnitPolicy FirstPolicy, UnitPolicy SecondPolicy, InputConstraints Constraints) {
		if (FirstPolicy == null) {
			return new UnitResult(toErrorValue("First unit policy must not be null"), 0, false);
		}
		
		if (SecondPolicy == null) {
			return new UnitResult(toErrorValue("Second unit policy must not be null"), 0, false);
		}
		
		if (FirstPolicy == INVALID_UNIT_POLICY) {
			return new UnitResult(0.0f, 0, false);
		}
		
		if (SecondPolicy == INVALID_UNIT_POLICY) {
			return new UnitResult(0.0f, 0, false);
		}
		
		if (Constraints == null) {
			return new UnitResult(toErrorValue("Input constraints must not be null"), 0, false);
		}
		
		if (Constraints == INVALID_INPUT_CONSTRAINTS) {
			return new UnitResult(0.0f, 0, false);
		}
		
		if (RawValue == null) {
			return new UnitResult(toErrorValue("Value must not be null"), 0, false);
		}
		
		String CleanValue = RawValue.trim();
		
		if (CleanValue.isEmpty()) {
			return new UnitResult(toErrorValue("Value must not be empty"), 0, false);
		}
		
		Matcher ValueMatcher = VALUE_PATTERN.matcher(CleanValue);
		
		if (!ValueMatcher.matches()) {
			return new UnitResult(toErrorValue("Value has an invalid numeric portion or unit suffix"), 0, false);
		}
		
		float Number;
		
		try {
			Number = Float.parseFloat(ValueMatcher.group(1));
		} catch (NumberFormatException Cause) {
			return new UnitResult(toErrorValue("Value has an invalid numeric portion", Cause), 0, false);
		}
		
		if (!isFinite(Number)) {
			return new UnitResult(toErrorValue("Value numeric portion must be finite"), 0, false);
		}
		
		String Unit = ValueMatcher.group(2).toLowerCase(Locale.US);
		@UnitSelection int CurrentUnitType = getUnitType(Unit);
		
		if (CurrentUnitType == 0) {
			return new UnitResult(toErrorValue("Unsupported unit: " + Unit), 0, false);
		}
		
		boolean FirstPolicyAllowsUnit = FirstPolicy.isUnitAllowed(CurrentUnitType);
		boolean SecondPolicyAllowsUnit = SecondPolicy.isUnitAllowed(CurrentUnitType);
		String UnitLabel = Unit.isEmpty() ? "unitless" : Unit;
		
		if (!FirstPolicyAllowsUnit || !SecondPolicyAllowsUnit) {
			return new UnitResult(toErrorValue("Unit is not allowed by the current policy: " + UnitLabel), CurrentUnitType, false);
		}
		
		boolean HasMinimum = Constraints.hasMinimum(CurrentUnitType);
		
		if (HasMinimum) {
			float Minimum = Constraints.getMinimum(CurrentUnitType);
			
			if (Number < Minimum) {
				return new UnitResult(toErrorValue("Input value is below the minimum allowed for unit: " + UnitLabel), CurrentUnitType, false);
			}
		}
		
		boolean HasMaximum = Constraints.hasMaximum(CurrentUnitType);
		
		if (HasMaximum) {
			float Maximum = Constraints.getMaximum(CurrentUnitType);
			
			if (Number > Maximum) {
				return new UnitResult(toErrorValue("Input value exceeds the maximum allowed for unit: " + UnitLabel), CurrentUnitType, false);
			}
		}
		
		switch (CurrentUnitType) {
		case UnitType.UNITLESS:
		case UnitType.PIXEL:
		case UnitType.DEGREE:
			return new UnitResult(Number, CurrentUnitType, true);
		case UnitType.PERCENT:
			float PercentResult = Number / 100.0f;
			
			if (!isFinite(PercentResult)) {
				return new UnitResult(toErrorValue("Converted value must be finite"), CurrentUnitType, false);
			}
			
			return new UnitResult(PercentResult, CurrentUnitType, true);
		case UnitType.DENSITY_INDEPENDENT_PIXEL:
		case UnitType.SCALE_INDEPENDENT_PIXEL:
		case UnitType.POINT:
		case UnitType.PICA:
		case UnitType.INCH:
		case UnitType.CENTIMETER:
		case UnitType.MILLIMETER:
		case UnitType.QUARTER_MILLIMETER:
			break;
		default:
			return new UnitResult(toErrorValue("Unsupported unit: " + Unit), CurrentUnitType, false);
		}
		
		if (SelectedMetricsResolver == null) {
			return new UnitResult(toErrorValue("Metrics resolver must not be null for unit conversion"), CurrentUnitType, false);
		}
		
		DisplayMetrics Metrics = SelectedMetricsResolver.getMetrics(MetricsOwner);
		
		if (Metrics == null) {
			return new UnitResult(0.0f, CurrentUnitType, false);
		}
		
		float Pixels;
		
		switch (CurrentUnitType) {
		case UnitType.DENSITY_INDEPENDENT_PIXEL:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Number, Metrics);
			break;
		case UnitType.SCALE_INDEPENDENT_PIXEL:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Number, Metrics);
			break;
		case UnitType.POINT:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, Number, Metrics);
			break;
		case UnitType.PICA:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, Number * PICA_TO_POINT, Metrics);
			break;
		case UnitType.INCH:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, Number, Metrics);
			break;
		case UnitType.CENTIMETER:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, Number * CENTIMETER_TO_MILLIMETER, Metrics);
			break;
		case UnitType.MILLIMETER:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, Number, Metrics);
			break;
		case UnitType.QUARTER_MILLIMETER:
			Pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, Number * QUARTER_MILLIMETER_TO_MILLIMETER, Metrics);
			break;
		default:
			return new UnitResult(toErrorValue("Unsupported unit: " + Unit), CurrentUnitType, false);
		}
		
		if (!isFinite(Pixels)) {
			return new UnitResult(toErrorValue("Converted pixel value must be finite"), CurrentUnitType, false);
		}
		
		return new UnitResult(Pixels, CurrentUnitType, true);
	}
	
	public static UnitPolicy fromAllowedUnits(@UnitSelection int... UnitSelections) {
		return fromUnitSelections(true, UnitSelections);
	}
	
	public static UnitPolicy fromRejectedUnits(@UnitSelection int... UnitSelections) {
		return fromUnitSelections(false, UnitSelections);
	}
	
	private static UnitPolicy fromUnitSelections(boolean AllowedMode, @UnitSelection int... UnitSelections) {
		if (UnitSelections == null) {
			toErrorValue("Unit selections must not be null");
			return INVALID_UNIT_POLICY;
		}
		
		@UnitSelection int ResolvedUnitMask = 0;
		
		for (@UnitSelection int CurrentUnitSelection : UnitSelections) {
			if ((CurrentUnitSelection & ~ALL_UNITS) != 0) {
				toErrorValue("Unit selection contains unsupported bits: " + CurrentUnitSelection);
				return INVALID_UNIT_POLICY;
			}
			
			ResolvedUnitMask |= CurrentUnitSelection;
		}
		
		return new UnitPolicy(AllowedMode, ResolvedUnitMask);
	}
	
	private static @UnitSelection int getUnitType(String Unit) {
		switch (Unit) {
		case "":
			return UnitType.UNITLESS;
		case UNIT_PIXEL:
			return UnitType.PIXEL;
		case UNIT_DENSITY_INDEPENDENT_PIXEL:
			return UnitType.DENSITY_INDEPENDENT_PIXEL;
		case UNIT_SCALE_INDEPENDENT_PIXEL:
			return UnitType.SCALE_INDEPENDENT_PIXEL;
		case UNIT_POINT:
			return UnitType.POINT;
		case UNIT_PICA:
			return UnitType.PICA;
		case UNIT_INCH:
			return UnitType.INCH;
		case UNIT_CENTIMETER:
			return UnitType.CENTIMETER;
		case UNIT_MILLIMETER:
			return UnitType.MILLIMETER;
		case UNIT_QUARTER_MILLIMETER:
			return UnitType.QUARTER_MILLIMETER;
		case UNIT_PERCENT:
			return UnitType.PERCENT;
		case UNIT_DEGREE:
			return UnitType.DEGREE;
		default:
			return 0;
		}
	}
	
	private static boolean isSingleUnitType(@UnitSelection int UnitType) {
		return UnitType != 0 && (UnitType & ~ALL_UNITS) == 0 && (UnitType & (UnitType - 1)) == 0;
	}
	
	private static int getUnitIndex(@UnitSelection int UnitType) {
		return Integer.numberOfTrailingZeros(UnitType);
	}
	
	private static float toErrorValue(String Message) {
		Log.e(LOG_TAG, Message);
		return 0.0f;
	}
	
	private static float toErrorValue(String Message, Throwable Cause) {
		Log.e(LOG_TAG, Message, Cause);
		return 0.0f;
	}
	
	private static boolean isFinite(float FloatValue) {
		return !Float.isNaN(FloatValue) && !Float.isInfinite(FloatValue);
	}
	
	private static DisplayMetrics getMetrics(Context AppContext) {
		if (AppContext == null) {
			toErrorValue("Context must not be null for unit conversion");
			return null;
		}
		
		Resources AppResources = AppContext.getResources();
		
		if (AppResources == null) {
			toErrorValue("Context resources must not be null for unit conversion");
			return null;
		}
		
		DisplayMetrics Metrics = AppResources.getDisplayMetrics();
		
		if (Metrics == null) {
			toErrorValue("Display metrics must not be null for unit conversion");
			return null;
		}
		
		return Metrics;
	}
	
	private static DisplayMetrics getMetrics(Resources AppResources) {
		if (AppResources == null) {
			toErrorValue("Resources must not be null for unit conversion");
			return null;
		}
		
		DisplayMetrics Metrics = AppResources.getDisplayMetrics();
		
		if (Metrics == null) {
			toErrorValue("Display metrics must not be null for unit conversion");
			return null;
		}
		
		return Metrics;
	}
	
	@IntDef(flag = true, value = {UnitType.UNITLESS, UnitType.PIXEL, UnitType.DENSITY_INDEPENDENT_PIXEL, UnitType.SCALE_INDEPENDENT_PIXEL, UnitType.POINT, UnitType.PICA, UnitType.INCH, UnitType.CENTIMETER, UnitType.MILLIMETER, UnitType.QUARTER_MILLIMETER, UnitType.PERCENT, UnitType.DEGREE})
	@Retention(RetentionPolicy.SOURCE)
	public @interface UnitSelection {
	}
	
	private interface MetricsResolver<MetricsOwnerType> {
		DisplayMetrics getMetrics(MetricsOwnerType MetricsOwner);
	}
	
	public static final class UnitType {
		@UnitSelection
		public static final int UNITLESS = 1 << 0;
		@UnitSelection
		public static final int PIXEL = 1 << 1;
		@UnitSelection
		public static final int DENSITY_INDEPENDENT_PIXEL = 1 << 2;
		@UnitSelection
		public static final int SCALE_INDEPENDENT_PIXEL = 1 << 3;
		@UnitSelection
		public static final int POINT = 1 << 4;
		@UnitSelection
		public static final int PICA = 1 << 5;
		@UnitSelection
		public static final int INCH = 1 << 6;
		@UnitSelection
		public static final int CENTIMETER = 1 << 7;
		@UnitSelection
		public static final int MILLIMETER = 1 << 8;
		@UnitSelection
		public static final int QUARTER_MILLIMETER = 1 << 9;
		@UnitSelection
		public static final int PERCENT = 1 << 10;
		@UnitSelection
		public static final int DEGREE = 1 << 11;
		
		private UnitType() {
		}
	}
	
	public static final class UnitPolicy {
		private final boolean AllowedMode;
		@UnitSelection
		private final int UnitMask;
		
		private UnitPolicy(boolean AllowedMode, @UnitSelection int UnitMask) {
			this.AllowedMode = AllowedMode;
			this.UnitMask = UnitMask;
		}
		
		boolean isUnitAllowed(@UnitSelection int CurrentUnitType) {
			boolean ContainsUnit = (UnitMask & CurrentUnitType) == CurrentUnitType;
			
			if (AllowedMode) {
				return ContainsUnit;
			}
			
			return !ContainsUnit;
		}
	}
	
	public static final class UnitResult {
		private final float Value;
		private final int UnitType;
		private final boolean Valid;
		
		private UnitResult(float Value, int UnitType, boolean Valid) {
			this.Value = Value;
			this.UnitType = UnitType;
			this.Valid = Valid;
		}
		
		public float getValue() {
			return Value;
		}
		
		public int getUnitType() {
			return UnitType;
		}
		
		public boolean isValid() {
			return Valid;
		}
		
		public boolean isUnitResolved() {
			return UnitType != 0;
		}
	}
	
	public static final class InputConstraints {
		private final UnitPolicy FirstUnitPolicy;
		private final UnitPolicy SecondUnitPolicy;
		private final boolean HasGeneralMinimum;
		private final float GeneralMinimum;
		private final boolean HasGeneralMaximum;
		private final float GeneralMaximum;
		@UnitSelection
		private final int UnitMinimumMask;
		@UnitSelection
		private final int UnitMaximumMask;
		private final float[] UnitMinimumValues;
		private final float[] UnitMaximumValues;
		
		private InputConstraints(UnitPolicy FirstUnitPolicy, UnitPolicy SecondUnitPolicy, boolean HasGeneralMinimum, float GeneralMinimum, boolean HasGeneralMaximum, float GeneralMaximum, @UnitSelection int UnitMinimumMask, @UnitSelection int UnitMaximumMask, float[] UnitMinimumValues, float[] UnitMaximumValues) {
			this.FirstUnitPolicy = FirstUnitPolicy;
			this.SecondUnitPolicy = SecondUnitPolicy;
			this.HasGeneralMinimum = HasGeneralMinimum;
			this.GeneralMinimum = GeneralMinimum;
			this.HasGeneralMaximum = HasGeneralMaximum;
			this.GeneralMaximum = GeneralMaximum;
			this.UnitMinimumMask = UnitMinimumMask;
			this.UnitMaximumMask = UnitMaximumMask;
			this.UnitMinimumValues = UnitMinimumValues.clone();
			this.UnitMaximumValues = UnitMaximumValues.clone();
		}
		
		private boolean hasMinimum(@UnitSelection int CurrentUnitType) {
			if (!isSingleUnitType(CurrentUnitType)) {
				return false;
			}
			
			if ((UnitMinimumMask & CurrentUnitType) == CurrentUnitType) {
				return true;
			}
			
			return HasGeneralMinimum;
		}
		
		private float getMinimum(@UnitSelection int CurrentUnitType) {
			if (!isSingleUnitType(CurrentUnitType)) {
				return 0.0f;
			}
			
			if ((UnitMinimumMask & CurrentUnitType) == CurrentUnitType) {
				return UnitMinimumValues[getUnitIndex(CurrentUnitType)];
			}
			
			if (HasGeneralMinimum) {
				return GeneralMinimum;
			}
			
			return 0.0f;
		}
		
		private boolean hasMaximum(@UnitSelection int CurrentUnitType) {
			if (!isSingleUnitType(CurrentUnitType)) {
				return false;
			}
			
			if ((UnitMaximumMask & CurrentUnitType) == CurrentUnitType) {
				return true;
			}
			
			return HasGeneralMaximum;
		}
		
		private float getMaximum(@UnitSelection int CurrentUnitType) {
			if (!isSingleUnitType(CurrentUnitType)) {
				return 0.0f;
			}
			
			if ((UnitMaximumMask & CurrentUnitType) == CurrentUnitType) {
				return UnitMaximumValues[getUnitIndex(CurrentUnitType)];
			}
			
			if (HasGeneralMaximum) {
				return GeneralMaximum;
			}
			
			return 0.0f;
		}
		
		public static final class Builder {
			private final float[] UnitMinimumValues;
			private final float[] UnitMaximumValues;
			private UnitPolicy FirstUnitPolicy;
			private UnitPolicy SecondUnitPolicy;
			private boolean HasGeneralMinimum;
			private float GeneralMinimum;
			private boolean HasGeneralMaximum;
			private float GeneralMaximum;
			@UnitSelection
			private int UnitMinimumMask;
			@UnitSelection
			private int UnitMaximumMask;
			private boolean ConfigurationValid;
			
			public Builder() {
				FirstUnitPolicy = DEFAULT_UNIT_POLICY;
				SecondUnitPolicy = DEFAULT_UNIT_POLICY;
				HasGeneralMinimum = false;
				GeneralMinimum = 0.0f;
				HasGeneralMaximum = false;
				GeneralMaximum = 0.0f;
				UnitMinimumMask = 0;
				UnitMaximumMask = 0;
				UnitMinimumValues = new float[UNIT_TYPE_COUNT];
				UnitMaximumValues = new float[UNIT_TYPE_COUNT];
				ConfigurationValid = true;
			}
			
			public Builder setGeneralMinimum(float Minimum) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isFinite(Minimum)) {
					toErrorValue("General minimum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				HasGeneralMinimum = true;
				GeneralMinimum = Minimum;
				return this;
			}
			
			public Builder unsetGeneralMinimum() {
				if (!ConfigurationValid) {
					return this;
				}
				
				HasGeneralMinimum = false;
				GeneralMinimum = 0.0f;
				return this;
			}
			
			public Builder setGeneralMaximum(float Maximum) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isFinite(Maximum)) {
					toErrorValue("General maximum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				HasGeneralMaximum = true;
				GeneralMaximum = Maximum;
				return this;
			}
			
			public Builder unsetGeneralMaximum() {
				if (!ConfigurationValid) {
					return this;
				}
				
				HasGeneralMaximum = false;
				GeneralMaximum = 0.0f;
				return this;
			}
			
			public Builder setGeneralRange(float Minimum, float Maximum) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isFinite(Minimum)) {
					toErrorValue("General minimum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				if (!isFinite(Maximum)) {
					toErrorValue("General maximum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				if (Minimum > Maximum) {
					toErrorValue("General minimum must not be greater than general maximum");
					ConfigurationValid = false;
					return this;
				}
				
				HasGeneralMinimum = true;
				GeneralMinimum = Minimum;
				HasGeneralMaximum = true;
				GeneralMaximum = Maximum;
				return this;
			}
			
			public Builder unsetGeneralRange() {
				if (!ConfigurationValid) {
					return this;
				}
				
				HasGeneralMinimum = false;
				GeneralMinimum = 0.0f;
				HasGeneralMaximum = false;
				GeneralMaximum = 0.0f;
				return this;
			}
			
			public Builder setUnitMinimum(@UnitSelection int UnitType, float Minimum) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isSingleUnitType(UnitType)) {
					toErrorValue("Unit type must represent exactly one supported unit: " + UnitType);
					ConfigurationValid = false;
					return this;
				}
				
				if (!isFinite(Minimum)) {
					toErrorValue("Unit minimum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				int UnitIndex = getUnitIndex(UnitType);
				UnitMinimumValues[UnitIndex] = Minimum;
				UnitMinimumMask |= UnitType;
				return this;
			}
			
			public Builder unsetUnitMinimum(@UnitSelection int UnitType) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isSingleUnitType(UnitType)) {
					toErrorValue("Unit type must represent exactly one supported unit: " + UnitType);
					ConfigurationValid = false;
					return this;
				}
				
				int UnitIndex = getUnitIndex(UnitType);
				UnitMinimumMask &= ~UnitType;
				UnitMinimumValues[UnitIndex] = 0.0f;
				return this;
			}
			
			public Builder setUnitMaximum(@UnitSelection int UnitType, float Maximum) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isSingleUnitType(UnitType)) {
					toErrorValue("Unit type must represent exactly one supported unit: " + UnitType);
					ConfigurationValid = false;
					return this;
				}
				
				if (!isFinite(Maximum)) {
					toErrorValue("Unit maximum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				int UnitIndex = getUnitIndex(UnitType);
				UnitMaximumValues[UnitIndex] = Maximum;
				UnitMaximumMask |= UnitType;
				return this;
			}
			
			public Builder unsetUnitMaximum(@UnitSelection int UnitType) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isSingleUnitType(UnitType)) {
					toErrorValue("Unit type must represent exactly one supported unit: " + UnitType);
					ConfigurationValid = false;
					return this;
				}
				
				int UnitIndex = getUnitIndex(UnitType);
				UnitMaximumMask &= ~UnitType;
				UnitMaximumValues[UnitIndex] = 0.0f;
				return this;
			}
			
			public Builder setUnitRange(@UnitSelection int UnitType, float Minimum, float Maximum) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isSingleUnitType(UnitType)) {
					toErrorValue("Unit type must represent exactly one supported unit: " + UnitType);
					ConfigurationValid = false;
					return this;
				}
				
				if (!isFinite(Minimum)) {
					toErrorValue("Unit minimum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				if (!isFinite(Maximum)) {
					toErrorValue("Unit maximum must be finite");
					ConfigurationValid = false;
					return this;
				}
				
				if (Minimum > Maximum) {
					toErrorValue("Unit minimum must not be greater than unit maximum for unit type: " + UnitType);
					ConfigurationValid = false;
					return this;
				}
				
				int UnitIndex = getUnitIndex(UnitType);
				UnitMinimumValues[UnitIndex] = Minimum;
				UnitMaximumValues[UnitIndex] = Maximum;
				UnitMinimumMask |= UnitType;
				UnitMaximumMask |= UnitType;
				return this;
			}
			
			public Builder unsetUnitRange(@UnitSelection int UnitType) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (!isSingleUnitType(UnitType)) {
					toErrorValue("Unit type must represent exactly one supported unit: " + UnitType);
					ConfigurationValid = false;
					return this;
				}
				
				int UnitIndex = getUnitIndex(UnitType);
				UnitMinimumMask &= ~UnitType;
				UnitMaximumMask &= ~UnitType;
				UnitMinimumValues[UnitIndex] = 0.0f;
				UnitMaximumValues[UnitIndex] = 0.0f;
				return this;
			}
			
			public Builder setFirstUnitPolicy(UnitPolicy FirstUnitPolicy) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (FirstUnitPolicy == null) {
					toErrorValue("First unit policy must not be null");
					ConfigurationValid = false;
					return this;
				}
				
				if (FirstUnitPolicy == INVALID_UNIT_POLICY) {
					ConfigurationValid = false;
					return this;
				}
				
				this.FirstUnitPolicy = FirstUnitPolicy;
				return this;
			}
			
			public Builder setSecondUnitPolicy(UnitPolicy SecondUnitPolicy) {
				if (!ConfigurationValid) {
					return this;
				}
				
				if (SecondUnitPolicy == null) {
					toErrorValue("Second unit policy must not be null");
					ConfigurationValid = false;
					return this;
				}
				
				if (SecondUnitPolicy == INVALID_UNIT_POLICY) {
					ConfigurationValid = false;
					return this;
				}
				
				this.SecondUnitPolicy = SecondUnitPolicy;
				return this;
			}
			
			public InputConstraints toConstraints() {
				if (!ConfigurationValid) {
					return INVALID_INPUT_CONSTRAINTS;
				}
				
				if (HasGeneralMinimum && HasGeneralMaximum && GeneralMinimum > GeneralMaximum) {
					toErrorValue("General minimum must not be greater than general maximum");
					ConfigurationValid = false;
					return INVALID_INPUT_CONSTRAINTS;
				}
				
				for (int UnitIndex = 0; UnitIndex < UNIT_TYPE_COUNT; UnitIndex++) {
					@UnitSelection int CurrentUnitType = 1 << UnitIndex;
					boolean HasUnitMinimum = (UnitMinimumMask & CurrentUnitType) == CurrentUnitType;
					boolean HasUnitMaximum = (UnitMaximumMask & CurrentUnitType) == CurrentUnitType;
					
					if (HasUnitMinimum && HasUnitMaximum && UnitMinimumValues[UnitIndex] > UnitMaximumValues[UnitIndex]) {
						toErrorValue("Unit minimum must not be greater than unit maximum for unit type: " + CurrentUnitType);
						ConfigurationValid = false;
						return INVALID_INPUT_CONSTRAINTS;
					}
				}
				
				int ConflictingUnitType = getInvalidEffectiveRangeUnitType();
				
				if (ConflictingUnitType != 0) {
					toErrorValue("Input constraints produce an invalid effective range for unit type: " + ConflictingUnitType);
					ConfigurationValid = false;
					return INVALID_INPUT_CONSTRAINTS;
				}
				
				return new InputConstraints(FirstUnitPolicy, SecondUnitPolicy, HasGeneralMinimum, GeneralMinimum, HasGeneralMaximum, GeneralMaximum, UnitMinimumMask, UnitMaximumMask, UnitMinimumValues, UnitMaximumValues);
			}
			
			private int getInvalidEffectiveRangeUnitType() {
				for (int UnitIndex = 0; UnitIndex < UNIT_TYPE_COUNT; UnitIndex++) {
					@UnitSelection int CurrentUnitType = 1 << UnitIndex;
					boolean CurrentHasMinimum = (UnitMinimumMask & CurrentUnitType) == CurrentUnitType || HasGeneralMinimum;
					boolean CurrentHasMaximum = (UnitMaximumMask & CurrentUnitType) == CurrentUnitType || HasGeneralMaximum;
					
					if (!CurrentHasMinimum || !CurrentHasMaximum) {
						continue;
					}
					
					float EffectiveMinimum;
					
					if ((UnitMinimumMask & CurrentUnitType) == CurrentUnitType) {
						EffectiveMinimum = UnitMinimumValues[UnitIndex];
					} else {
						EffectiveMinimum = GeneralMinimum;
					}
					
					float EffectiveMaximum;
					
					if ((UnitMaximumMask & CurrentUnitType) == CurrentUnitType) {
						EffectiveMaximum = UnitMaximumValues[UnitIndex];
					} else {
						EffectiveMaximum = GeneralMaximum;
					}
					
					if (EffectiveMinimum > EffectiveMaximum) {
						return CurrentUnitType;
					}
				}
				
				return 0;
			}
		}
	}
}