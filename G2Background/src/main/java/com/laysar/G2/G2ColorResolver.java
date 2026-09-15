package com.laysar.G2;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class G2ColorResolver {
	private G2ColorResolver() {
	}

	public static Integer ResolveColorSource(Resources ResourcesObject, Resources.Theme Theme, int ColorSource) {
		if (ResourcesObject == null) {
			return ColorSource;
		}
		String ResourceType;
		try {
			ResourceType = ResourcesObject.getResourceTypeName(ColorSource);
		} catch (Resources.NotFoundException Ignored) {
			return ColorSource;
		}
		if ("color".equals(ResourceType)) {
			return ResolveColorResource(ResourcesObject, Theme, ColorSource);
		}
		if ("attr".equals(ResourceType)) {
			return ResolveThemeAttribute(ResourcesObject, Theme, ColorSource);
		}
		return null;
	}

	public static int[] ResolveColorSources(Resources ResourcesObject, Resources.Theme Theme, int[] ColorSources) {
		if (ColorSources == null) {
			return null;
		}
		int[] ResolvedColors = new int[ColorSources.length];
		for (int Index = 0; Index < ColorSources.length; Index++) {
			Integer ResolvedColor = ResolveColorSource(ResourcesObject, Theme, ColorSources[Index]);
			if (ResolvedColor == null) {
				return null;
			}
			ResolvedColors[Index] = ResolvedColor;
		}
		return ResolvedColors;
	}

	static int[] ResolveColorListSources(Resources ResourcesObject, Resources.Theme Theme, int[] ColorSources, int MinimumColorCount) {
		if (ColorSources == null || MinimumColorCount < 1) {
			return null;
		}
		if (ColorSources.length == 1 && ResourcesObject != null) {
			String ResourceType = getResourceTypeName(ResourcesObject, ColorSources[0]);
			if ("array".equals(ResourceType)) {
				return ResolveColorArray(ResourcesObject, Theme, ColorSources[0], MinimumColorCount);
			}
		}
		if (ColorSources.length < MinimumColorCount) {
			return null;
		}
		return ResolveColorSources(ResourcesObject, Theme, ColorSources);
	}

	static Integer ResolveColorValue(Resources ResourcesObject, Resources.Theme Theme, long Color) {
		if (Color == (long) ((int) Color)) {
			return ResolveColorSource(ResourcesObject, Theme, (int) Color);
		}
		if ((Color & 0xffffffffL) != 0L) {
			return null;
		}
		try {
			int ResolvedColor = android.graphics.Color.toArgb(Color);
			if (android.graphics.Color.pack(ResolvedColor) != Color) {
				return null;
			}
			return ResolvedColor;
		} catch (IllegalArgumentException ignored_exception) {
			return null;
		}
	}

	static int[] ResolveColorListValues(Resources ResourcesObject, Resources.Theme Theme, long FirstColor, long[] RemainingColors, int MinimumColorCount) {
		if (RemainingColors == null || MinimumColorCount < 1) {
			return null;
		}
		int ColorCount = RemainingColors.length + 1;
		if (ColorCount == 1 && FirstColor == (long) ((int) FirstColor) && ResourcesObject != null) {
			int ColorSource = (int) FirstColor;
			String ResourceType = getResourceTypeName(ResourcesObject, ColorSource);
			if ("array".equals(ResourceType)) {
				return ResolveColorArray(ResourcesObject, Theme, ColorSource, MinimumColorCount);
			}
		}
		if (ColorCount < MinimumColorCount) {
			return null;
		}
		int[] ResolvedColors = new int[ColorCount];
		Integer ResolvedColor = ResolveColorValue(ResourcesObject, Theme, FirstColor);
		if (ResolvedColor == null) {
			return null;
		}
		ResolvedColors[0] = ResolvedColor;
		for (int item_index = 0; item_index < RemainingColors.length; item_index++) {
			ResolvedColor = ResolveColorValue(ResourcesObject, Theme, RemainingColors[item_index]);
			if (ResolvedColor == null) {
				return null;
			}
			ResolvedColors[item_index + 1] = ResolvedColor;
		}
		return ResolvedColors;
	}

	public static Integer ResolveColorAttribute(TypedArray Attributes, int Index, Resources.Theme Theme) {
		if (Attributes == null || !Attributes.hasValue(Index)) {
			return null;
		}
		TypedValue TypedColor = new TypedValue();
		try {
			if (!Attributes.getValue(Index, TypedColor)) {
				return null;
			}
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
		return ResolveTypedColorValue(Attributes.getResources(), Theme, TypedColor);
	}

	public static int[] ResolveColorListAttribute(TypedArray Attributes, int Index, Resources.Theme Theme, int MinimumColorCount) {
		if (Attributes == null || MinimumColorCount < 1 || !Attributes.hasValue(Index)) {
			return null;
		}
		Resources ResourcesObject = Attributes.getResources();
		if (ResourcesObject == null) {
			return null;
		}
		TypedValue ListValue = new TypedValue();
		try {
			if (!Attributes.getValue(Index, ListValue)) {
				return null;
			}
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
		int ResourceId = getReferencedResourceId(ListValue);
		if (ResourceId != 0) {
			String ResourceType = getResourceTypeName(ResourcesObject, ResourceId);
			if ("array".equals(ResourceType)) {
				return ResolveColorArray(ResourcesObject, Theme, ResourceId, MinimumColorCount);
			}
			if ("string".equals(ResourceType)) {
				String ColorList = ReadStringAttribute(Attributes, Index);
				return ParseColorList(ColorList, MinimumColorCount);
			}
			return null;
		}
		if (ListValue.type != TypedValue.TYPE_STRING) {
			return null;
		}
		String ColorList = ReadStringAttribute(Attributes, Index);
		return ParseColorList(ColorList, MinimumColorCount);
	}

	public static void ApplyResolvedFillColor(G2Background Background, int Color) {
		if (Background != null) {
			Background.ApplyResolvedFillColor(Color);
		}
	}

	public static void ApplyResolvedFillGradient(G2Background Background, int[] Colors) {
		if (Background != null) {
			Background.ApplyResolvedFillGradient(Colors);
		}
	}

	public static void ApplyResolvedFillOrderedBlend(G2Background Background, int[] Colors) {
		if (Background != null) {
			Background.ApplyResolvedFillOrderedBlend(Colors);
		}
	}

	public static void ApplyResolvedFillAverageBlend(G2Background Background, int[] Colors) {
		if (Background != null) {
			Background.ApplyResolvedFillAverageBlend(Colors);
		}
	}

	public static void ApplyResolvedStrokeColor(G2Background Background, int Color) {
		if (Background != null) {
			Background.ApplyResolvedStrokeColor(Color);
		}
	}

	public static void ApplyResolvedStrokeGradient(G2Background Background, int[] Colors) {
		if (Background != null) {
			Background.ApplyResolvedStrokeGradient(Colors);
		}
	}

	public static void ApplyResolvedStrokeOrderedBlend(G2Background Background, int[] Colors) {
		if (Background != null) {
			Background.ApplyResolvedStrokeOrderedBlend(Colors);
		}
	}

	public static void ApplyResolvedStrokeAverageBlend(G2Background Background, int[] Colors) {
		if (Background != null) {
			Background.ApplyResolvedStrokeAverageBlend(Colors);
		}
	}

	public static void ApplyResolvedRippleColor(G2Background Background, int Color) {
		if (Background != null) {
			Background.ApplyResolvedRippleColor(Color);
		}
	}

	private static Integer ResolveTypedColorValue(Resources ResourcesObject, Resources.Theme Theme, TypedValue ColorValue) {
		if (ColorValue == null) {
			return null;
		}
		if (isColorType(ColorValue.type)) {
			return ColorValue.data;
		}
		if (ColorValue.type == TypedValue.TYPE_ATTRIBUTE) {
			return ResolveThemeAttribute(ResourcesObject, Theme, ColorValue.data);
		}
		int ResourceId = getReferencedResourceId(ColorValue);
		if (ResourceId == 0 || ResourcesObject == null) {
			return null;
		}
		String ResourceType = getResourceTypeName(ResourcesObject, ResourceId);
		if ("color".equals(ResourceType)) {
			return ResolveColorResource(ResourcesObject, Theme, ResourceId);
		}
		if ("attr".equals(ResourceType)) {
			return ResolveThemeAttribute(ResourcesObject, Theme, ResourceId);
		}
		return null;
	}

	static Integer ResolveColorResource(Resources ResourcesObject, Resources.Theme Theme, int ResourceId) {
		if (ResourcesObject == null) {
			return null;
		}
		try {
			if (!"color".equals(ResourcesObject.getResourceTypeName(ResourceId))) {
				return null;
			}
			ColorStateList Colors = ResourcesObject.getColorStateList(ResourceId, Theme);
			return Colors == null ? null : Colors.getDefaultColor();
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
	}

	static Integer ResolveThemeAttribute(Resources ResourcesObject, Resources.Theme Theme, int AttributeId) {
		if (ResourcesObject == null || Theme == null || AttributeId == 0 || !"attr".equals(getResourceTypeName(ResourcesObject, AttributeId))) {
			return null;
		}
		TypedValue ResolvedValue = new TypedValue();
		try {
			if (!Theme.resolveAttribute(AttributeId, ResolvedValue, true)) {
				return null;
			}
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
		if (isColorType(ResolvedValue.type)) {
			return ResolvedValue.data;
		}
		int ResourceId = getReferencedResourceId(ResolvedValue);
		if (ResourceId == 0) {
			return null;
		}
		String ResourceType = getResourceTypeName(ResourcesObject, ResourceId);
		if (!"color".equals(ResourceType)) {
			return null;
		}
		return ResolveColorResource(ResourcesObject, Theme, ResourceId);
	}

	private static int[] ResolveColorArray(Resources ResourcesObject, Resources.Theme Theme, int ArrayResourceId, int MinimumColorCount) {
		TypedArray ColorArray;
		try {
			ColorArray = ResourcesObject.obtainTypedArray(ArrayResourceId);
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
		try {
			int ColorCount = ColorArray.length();
			if (ColorCount < MinimumColorCount) {
				return null;
			}
			int[] ResolvedColors = new int[ColorCount];
			for (int Index = 0; Index < ColorCount; Index++) {
				if (!ColorArray.hasValue(Index)) {
					return null;
				}
				TypedValue ColorValue = new TypedValue();
				try {
					if (!ColorArray.getValue(Index, ColorValue)) {
						return null;
					}
				} catch (Resources.NotFoundException Ignored) {
					return null;
				} catch (RuntimeException Ignored) {
					return null;
				}
				Integer ResolvedColor = ResolveTypedColorValue(ResourcesObject, Theme, ColorValue);
				if (ResolvedColor == null) {
					return null;
				}
				ResolvedColors[Index] = ResolvedColor;
			}
			return ResolvedColors;
		} finally {
			ColorArray.recycle();
		}
	}

	private static int[] ParseColorList(String ColorList, int MinimumColorCount) {
		int[] ParsedColors = G2GradientValueParser.ParseColorList(ColorList);
		if (ParsedColors == null || ParsedColors.length < MinimumColorCount) {
			return null;
		}
		return ParsedColors;
	}

	private static String ReadStringAttribute(TypedArray Attributes, int Index) {
		try {
			return Attributes.getString(Index);
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
	}

	private static String getResourceTypeName(Resources ResourcesObject, int ResourceId) {
		if (ResourcesObject == null || ResourceId == 0) {
			return null;
		}
		try {
			return ResourcesObject.getResourceTypeName(ResourceId);
		} catch (Resources.NotFoundException Ignored) {
			return null;
		} catch (RuntimeException Ignored) {
			return null;
		}
	}

	private static int getReferencedResourceId(TypedValue Value) {
		if (Value == null) {
			return 0;
		}
		if (Value.resourceId != 0) {
			return Value.resourceId;
		}
		if (Value.type == TypedValue.TYPE_REFERENCE) {
			return Value.data;
		}
		return 0;
	}

	private static boolean isColorType(int Type) {
		return Type >= TypedValue.TYPE_FIRST_COLOR_INT && Type <= TypedValue.TYPE_LAST_COLOR_INT;
	}
}