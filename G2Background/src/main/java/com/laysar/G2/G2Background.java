package com.laysar.G2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.IntDef;

import com.laysar.G2.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class G2Background {
	public static final int INSIDE = 0;
	public static final int CENTER = 1;
	public static final int OUTSIDE = 2;
	public static final int BELOW_CONTENT = 0;
	public static final int ABOVE_CONTENT = 1;
	public static final int SHARP = 0;
	public static final int ROUND = 1;
	public static final float LEFT_TO_RIGHT = 90.0f;
	public static final float TOP_LEFT_TO_BOTTOM_RIGHT = 45.0f;
	public static final float TOP_TO_BOTTOM = 0.0f;
	public static final float TOP_RIGHT_TO_BOTTOM_LEFT = -45.0f;
	public static final float RIGHT_TO_LEFT = -90.0f;
	public static final float BOTTOM_RIGHT_TO_TOP_LEFT = -135.0f;
	public static final float BOTTOM_TO_TOP = 180.0f;
	public static final float BOTTOM_LEFT_TO_TOP_RIGHT = 135.0f;
	private static final Object OwnershipLock = new Object();
	private static final ViewOutlineProvider NoSystemShadowOutlineProvider = new ViewOutlineProvider() {
		@Override
		public void getOutline(View View, Outline Outline) {
			if (Outline != null) {
				Outline.setEmpty();
				Outline.setAlpha(0.0f);
			}
		}
	};
	private final Context AppContext;
	private final Resources ColorResources;
	private final Resources.Theme ColorTheme;
	private final Map<View, Attachment> Attachments = new LinkedHashMap<>();
	private G2CornerValue BaseCorner = G2CornerValue.Absolute(0.0f);
	private G2CornerValue PhysicalTopLeft = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalTopLeft = false;
	private G2CornerValue PhysicalTopRight = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalTopRight = false;
	private G2CornerValue PhysicalBottomLeft = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalBottomLeft = false;
	private G2CornerValue PhysicalBottomRight = G2CornerValue.Absolute(0.0f);
	private boolean HasPhysicalBottomRight = false;
	private G2CornerValue LogicalTopStart = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalTopStart = false;
	private G2CornerValue LogicalTopEnd = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalTopEnd = false;
	private G2CornerValue LogicalBottomStart = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalBottomStart = false;
	private G2CornerValue LogicalBottomEnd = G2CornerValue.Absolute(0.0f);
	private boolean HasLogicalBottomEnd = false;
	private float RoundedRectangleExtendedFraction = (float) G2Profile.DefaultRoundedRectangleExtendedFraction;
	private float RoundedRectangleArcFraction = (float) G2Profile.DefaultRoundedRectangleArcFraction;
	private float RoundedRectangleBezierCurvatureScale = (float) G2Profile.DefaultRoundedRectangleBezierCurvatureScale;
	private float RoundedRectangleArcCurvatureScale = (float) G2Profile.DefaultRoundedRectangleArcCurvatureScale;
	private float CapsuleExtendedFraction = (float) G2Profile.DefaultCapsuleExtendedFraction;
	private float CapsuleArcFraction = (float) G2Profile.DefaultCapsuleArcFraction;
	private int FillColor = Color.TRANSPARENT;
	private float FillAlpha = 1.0f;
	private int[] FillGradientColors = null;
	private float FillGradientAngle = 0.0f;
	private int FillGradientRepeatCount = 1;
	private int StrokeColor = Color.TRANSPARENT;
	private float StrokeWidth = 0.0f;
	private int StrokePosition = CENTER;
	private int StrokeLayer = BELOW_CONTENT;
	private float StrokeAlpha = 1.0f;
	private int[] StrokeGradientColors = null;
	private float StrokeGradientAngle = 0.0f;
	private int StrokeGradientRepeatCount = 1;
	private int StrokeSegmentCount = 1;
	private float StrokeGapLength = 0.0f;
	private float StrokePatternAngle = 0.0f;
	private int StrokeSegmentCap = SHARP;
	private int RippleColor = Color.TRANSPARENT;
	private float RippleAlpha = 1.0f;
	private boolean ClipToG2Outline = false;
	
	public G2Background(Context Context) {
		Context ApplicationContext = Context == null ? null : Context.getApplicationContext();
		AppContext = ApplicationContext == null ? Context : ApplicationContext;
		Resources OriginalResources = Context == null ? null : Context.getResources();
		ColorResources = OriginalResources;
		Resources.Theme ThemeSnapshot = null;
		if (OriginalResources != null && Context != null) {
			Resources.Theme OriginalTheme = Context.getTheme();
			if (OriginalTheme != null) {
				try {
					ThemeSnapshot = OriginalResources.newTheme();
					ThemeSnapshot.setTo(OriginalTheme);
				} catch (RuntimeException Ignored) {
					ThemeSnapshot = null;
				}
			}
		}
		ColorTheme = ThemeSnapshot;
	}
	
	private static int sanitizeStrokePosition(int Position) {
		if (Position == INSIDE || Position == CENTER || Position == OUTSIDE) {
			return Position;
		}
		return CENTER;
	}

	private static int sanitizeStrokeLayer(int Layer) {
		if (Layer == BELOW_CONTENT || Layer == ABOVE_CONTENT) {
			return Layer;
		}
		return BELOW_CONTENT;
	}

	private static int sanitizeStrokeSegmentCount(int Count) {
		return Math.max(1, Count);
	}

	private static int sanitizeStrokeSegmentCap(int Cap) {
		return Cap == ROUND ? ROUND : SHARP;
	}

	private static float sanitizeGradientOrientation(float Orientation) {
		return isFinite(Orientation) ? Orientation : 0.0f;
	}
	
	private static boolean isFinite(float Value) {
		return !Float.isNaN(Value) && !Float.isInfinite(Value);
	}
	
	public void Attach(View... Views) {
		if (Views == null) {
			return;
		}
		synchronized (OwnershipLock) {
			for (View CurrentView : Views) {
				if (CurrentView != null) {
					attachLocked(CurrentView);
				}
			}
		}
	}

	public void Detach(View... Views) {
		if (Views == null) {
			return;
		}
		synchronized (OwnershipLock) {
			for (View CurrentView : Views) {
				if (CurrentView != null) {
					detachLocked(CurrentView);
				}
			}
		}
	}

	public void Detach() {
		synchronized (OwnershipLock) {
			ArrayList<View> Snapshot = new ArrayList<>(Attachments.keySet());
			for (View CurrentView : Snapshot) {
				detachLocked(CurrentView);
			}
		}
	}

	private static OwnershipState getOrCreateOwnershipState(View View) {
		Object TagValue = View.getTag(R.id.g2_background_ownership_state);
		if (TagValue == null) {
			OwnershipState Ownership = new OwnershipState(View.getBackground(), View.getOutlineProvider(), View.getClipToOutline());
			View.setTag(R.id.g2_background_ownership_state, Ownership);
			return Ownership;
		}
		if (TagValue instanceof OwnershipState) {
			return (OwnershipState) TagValue;
		}
		throw new IllegalStateException("Corrupted internal G2 ownership tag: expected G2Background.OwnershipState.");
	}

	private void attachLocked(View View) {
		OwnershipState Ownership = getOrCreateOwnershipState(View);
		G2Background PreviousOwner = Ownership.OwnerReference.get();
		if (PreviousOwner == null) {
			cleanupOrphanedRenderDelegateLocked(View, Ownership);
		} else if (PreviousOwner == this) {
			releaseForSameOwnerReattachLocked(View, Ownership);
		} else {
			PreviousOwner.releaseForOwnershipTransferLocked(View, Ownership);
		}

		long NewGeneration = Ownership.Generation + 1L;
		Ownership.Generation = NewGeneration;
		Ownership.OwnerReference = new WeakReference<>(this);
		Ownership.ActiveRenderDelegateReference = new WeakReference<>(null);

		G2Drawable Drawable = new G2Drawable();
		applyToDrawable(View, Drawable);
		Attachment NewAttachment = new Attachment(View, Ownership, NewGeneration, Drawable);
		Attachments.put(View, NewAttachment);

		if (View instanceof G2ViewRenderHost) {
			G2ViewRenderDelegate RenderDelegate = new G2ViewRenderDelegate(View, Drawable, StrokeLayer, ClipToG2Outline);
			NewAttachment.RenderDelegate = RenderDelegate;
			((G2ViewRenderHost) View).InstallG2RenderDelegate(RenderDelegate);
			Ownership.ActiveRenderDelegateReference = new WeakReference<>(RenderDelegate);
			View.setBackground(null);
		} else {
			Ownership.ActiveRenderDelegateReference = new WeakReference<>(null);
			View.setBackground(Drawable);
		}
		updateOutlineForAttachment(View, NewAttachment);
	}

	private void detachLocked(View View) {
		Attachment ExistingAttachment = Attachments.remove(View);
		if (ExistingAttachment == null) {
			return;
		}
		removeRenderDelegate(View, ExistingAttachment);
		removeOutlineLayoutListener(View, ExistingAttachment);
		ExistingAttachment.Drawable.setCallback(null);

		Object TagValue = View.getTag(R.id.g2_background_ownership_state);
		if (TagValue != ExistingAttachment.Ownership) {
			return;
		}
		OwnershipState Ownership = ExistingAttachment.Ownership;
		if (Ownership.OwnerReference.get() != this || Ownership.Generation != ExistingAttachment.OwnershipGeneration) {
			return;
		}

		Ownership.OwnerReference = new WeakReference<>(null);
		Ownership.ActiveRenderDelegateReference = new WeakReference<>(null);
		View.setBackground(Ownership.OriginalBackground);
		View.setOutlineProvider(Ownership.OriginalOutlineProvider);
		View.setClipToOutline(Ownership.OriginalClipToOutline);
		View.invalidateOutline();
		View.setTag(R.id.g2_background_ownership_state, null);
	}

	private void releaseForSameOwnerReattachLocked(View View, OwnershipState Ownership) {
		Attachment ExistingAttachment = Attachments.remove(View);
		if (ExistingAttachment == null || ExistingAttachment.Ownership != Ownership) {
			throw new IllegalStateException("G2 ownership state mismatch during same-owner reattach.");
		}
		releaseAttachmentForOwnershipChangeLocked(View, ExistingAttachment);
	}

	private void releaseForOwnershipTransferLocked(View View, OwnershipState Ownership) {
		Attachment ExistingAttachment = Attachments.remove(View);
		if (ExistingAttachment == null || ExistingAttachment.Ownership != Ownership) {
			throw new IllegalStateException("G2 ownership state mismatch during ownership transfer.");
		}
		releaseAttachmentForOwnershipChangeLocked(View, ExistingAttachment);
	}

	private void releaseAttachmentForOwnershipChangeLocked(View View, Attachment Attachment) {
		G2ViewRenderDelegate RenderDelegate = Attachment.RenderDelegate;
		removeRenderDelegate(View, Attachment);
		removeOutlineLayoutListener(View, Attachment);
		Attachment.Drawable.setCallback(null);
		clearActiveRenderDelegateReferenceLocked(Attachment.Ownership, RenderDelegate);
	}

	private static void cleanupOrphanedRenderDelegateLocked(View View, OwnershipState Ownership) {
		G2ViewRenderDelegate RenderDelegate = Ownership.ActiveRenderDelegateReference.get();
		if (RenderDelegate != null) {
			if (View instanceof G2ViewRenderHost) {
				((G2ViewRenderHost) View).RemoveG2RenderDelegate(RenderDelegate);
			}
			RenderDelegate.Release();
		}
		Ownership.ActiveRenderDelegateReference = new WeakReference<>(null);
	}

	private static void clearActiveRenderDelegateReferenceLocked(OwnershipState Ownership, G2ViewRenderDelegate RenderDelegate) {
		if (RenderDelegate != null && Ownership.ActiveRenderDelegateReference.get() == RenderDelegate) {
			Ownership.ActiveRenderDelegateReference = new WeakReference<>(null);
		}
	}

	public void setCorners(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		BaseCorner = G2CornerValue.CopyOf(ParsedRadius);
		PhysicalTopLeft = G2CornerValue.Absolute(0.0f);
		HasPhysicalTopLeft = false;
		PhysicalTopRight = G2CornerValue.Absolute(0.0f);
		HasPhysicalTopRight = false;
		PhysicalBottomLeft = G2CornerValue.Absolute(0.0f);
		HasPhysicalBottomLeft = false;
		PhysicalBottomRight = G2CornerValue.Absolute(0.0f);
		HasPhysicalBottomRight = false;
		LogicalTopStart = G2CornerValue.Absolute(0.0f);
		HasLogicalTopStart = false;
		LogicalTopEnd = G2CornerValue.Absolute(0.0f);
		HasLogicalTopEnd = false;
		LogicalBottomStart = G2CornerValue.Absolute(0.0f);
		HasLogicalBottomStart = false;
		LogicalBottomEnd = G2CornerValue.Absolute(0.0f);
		HasLogicalBottomEnd = false;
		updateAttachedDrawables();
	}
	
	public void setPhysicalCorners(String TopLeft, String TopRight, String BottomLeft, String BottomRight) {
		G2CornerValue ParsedTopLeft = G2ValueResolver.ResolveCorner(AppContext, TopLeft);
		G2CornerValue ParsedTopRight = G2ValueResolver.ResolveCorner(AppContext, TopRight);
		G2CornerValue ParsedBottomLeft = G2ValueResolver.ResolveCorner(AppContext, BottomLeft);
		G2CornerValue ParsedBottomRight = G2ValueResolver.ResolveCorner(AppContext, BottomRight);
		if (ParsedTopLeft == null || ParsedTopRight == null || ParsedBottomLeft == null || ParsedBottomRight == null) {
			return;
		}
		PhysicalTopLeft = G2CornerValue.CopyOf(ParsedTopLeft);
		HasPhysicalTopLeft = true;
		PhysicalTopRight = G2CornerValue.CopyOf(ParsedTopRight);
		HasPhysicalTopRight = true;
		PhysicalBottomLeft = G2CornerValue.CopyOf(ParsedBottomLeft);
		HasPhysicalBottomLeft = true;
		PhysicalBottomRight = G2CornerValue.CopyOf(ParsedBottomRight);
		HasPhysicalBottomRight = true;
		updateAttachedDrawables();
	}
	
	public void setCornerTopLeft(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		PhysicalTopLeft = G2CornerValue.CopyOf(ParsedRadius);
		HasPhysicalTopLeft = true;
		updateAttachedDrawables();
	}
	
	public void setCornerTopRight(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		PhysicalTopRight = G2CornerValue.CopyOf(ParsedRadius);
		HasPhysicalTopRight = true;
		updateAttachedDrawables();
	}
	
	public void setCornerBottomLeft(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		PhysicalBottomLeft = G2CornerValue.CopyOf(ParsedRadius);
		HasPhysicalBottomLeft = true;
		updateAttachedDrawables();
	}
	
	public void setCornerBottomRight(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		PhysicalBottomRight = G2CornerValue.CopyOf(ParsedRadius);
		HasPhysicalBottomRight = true;
		updateAttachedDrawables();
	}
	
	public void setLogicalCorners(String TopStart, String TopEnd, String BottomStart, String BottomEnd) {
		G2CornerValue ParsedTopStart = G2ValueResolver.ResolveCorner(AppContext, TopStart);
		G2CornerValue ParsedTopEnd = G2ValueResolver.ResolveCorner(AppContext, TopEnd);
		G2CornerValue ParsedBottomStart = G2ValueResolver.ResolveCorner(AppContext, BottomStart);
		G2CornerValue ParsedBottomEnd = G2ValueResolver.ResolveCorner(AppContext, BottomEnd);
		if (ParsedTopStart == null || ParsedTopEnd == null || ParsedBottomStart == null || ParsedBottomEnd == null) {
			return;
		}
		LogicalTopStart = G2CornerValue.CopyOf(ParsedTopStart);
		HasLogicalTopStart = true;
		LogicalTopEnd = G2CornerValue.CopyOf(ParsedTopEnd);
		HasLogicalTopEnd = true;
		LogicalBottomStart = G2CornerValue.CopyOf(ParsedBottomStart);
		HasLogicalBottomStart = true;
		LogicalBottomEnd = G2CornerValue.CopyOf(ParsedBottomEnd);
		HasLogicalBottomEnd = true;
		updateAttachedDrawables();
	}
	
	public void setCornerTopStart(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		LogicalTopStart = G2CornerValue.CopyOf(ParsedRadius);
		HasLogicalTopStart = true;
		updateAttachedDrawables();
	}
	
	public void setCornerTopEnd(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		LogicalTopEnd = G2CornerValue.CopyOf(ParsedRadius);
		HasLogicalTopEnd = true;
		updateAttachedDrawables();
	}
	
	public void setCornerBottomStart(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		LogicalBottomStart = G2CornerValue.CopyOf(ParsedRadius);
		HasLogicalBottomStart = true;
		updateAttachedDrawables();
	}
	
	public void setCornerBottomEnd(String Radius) {
		G2CornerValue ParsedRadius = G2ValueResolver.ResolveCorner(AppContext, Radius);
		if (ParsedRadius == null) {
			return;
		}
		LogicalBottomEnd = G2CornerValue.CopyOf(ParsedRadius);
		HasLogicalBottomEnd = true;
		updateAttachedDrawables();
	}
	
	public void setRoundedRectangleExtendedFraction(String Value) {
		Float ParsedValue = G2ValueResolver.ResolveUnitInterval(AppContext, Value);
		if (ParsedValue == null) {
			return;
		}
		RoundedRectangleExtendedFraction = ParsedValue;
		updateAttachedDrawables();
	}
	
	public void setRoundedRectangleArcFraction(String Value) {
		Float ParsedValue = G2ValueResolver.ResolveUnitInterval(AppContext, Value);
		if (ParsedValue == null) {
			return;
		}
		RoundedRectangleArcFraction = ParsedValue;
		updateAttachedDrawables();
	}
	
	public void setRoundedRectangleBezierCurvatureScale(String Value) {
		Float ParsedValue = G2ValueResolver.ResolvePositiveScale(AppContext, Value);
		if (ParsedValue == null) {
			return;
		}
		RoundedRectangleBezierCurvatureScale = ParsedValue;
		updateAttachedDrawables();
	}
	
	public void setRoundedRectangleArcCurvatureScale(String Value) {
		Float ParsedValue = G2ValueResolver.ResolvePositiveScale(AppContext, Value);
		if (ParsedValue == null) {
			return;
		}
		RoundedRectangleArcCurvatureScale = ParsedValue;
		updateAttachedDrawables();
	}
	
	public void setCapsuleExtendedFraction(String Value) {
		Float ParsedValue = G2ValueResolver.ResolveUnitInterval(AppContext, Value);
		if (ParsedValue == null) {
			return;
		}
		CapsuleExtendedFraction = ParsedValue;
		updateAttachedDrawables();
	}
	
	public void setCapsuleArcFraction(String Value) {
		Float ParsedValue = G2ValueResolver.ResolveUnitInterval(AppContext, Value);
		if (ParsedValue == null) {
			return;
		}
		CapsuleArcFraction = ParsedValue;
		updateAttachedDrawables();
	}
	
	public void setFillColor(int Color) {
		Integer ResolvedColor = G2ColorResolver.ResolveColorSource(ColorResources, ColorTheme, Color);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedFillColor(ResolvedColor);
	}

	public void setFillColor(long Color) {
		Integer ResolvedColor = G2ColorResolver.ResolveColorValue(ColorResources, ColorTheme, Color);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedFillColor(ResolvedColor);
	}

	void ApplyResolvedFillColor(int Color) {
		boolean Changed = FillColor != Color || FillGradientColors != null;
		FillColor = Color;
		FillGradientColors = null;
		if (Changed) {
			updateAttachedFillAppearance();
		}
	}
	
	public void setAlpha(String Alpha) {
		Float ParsedAlpha = G2ValueResolver.ResolveUnitInterval(AppContext, Alpha);
		if (ParsedAlpha == null) {
			return;
		}
		if (FillAlpha != ParsedAlpha) {
			FillAlpha = ParsedAlpha;
			updateAttachedFillAppearance();
		}
	}
	
	public void setFillGradient(int... Colors) {
		if (Colors == null) {
			return;
		}
		int[] ResolvedColors = G2ColorResolver.ResolveColorListSources(ColorResources, ColorTheme, Colors, 2);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedFillGradient(ResolvedColors);
	}

	public void setFillGradient(long FirstColor, long... RemainingColors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListValues(ColorResources, ColorTheme, FirstColor, RemainingColors, 2);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedFillGradient(ResolvedColors);
	}

	void ApplyResolvedFillGradient(int[] Colors) {
		if (Colors == null || Colors.length < 2) {
			return;
		}
		if (Arrays.equals(FillGradientColors, Colors)) {
			return;
		}
		FillGradientColors = Arrays.copyOf(Colors, Colors.length);
		updateAttachedFillAppearance();
	}

	public void setFillGradientOrientation(float Orientation) {
		float SafeOrientation = sanitizeGradientOrientation(Orientation);
		if (Float.compare(FillGradientAngle, SafeOrientation) == 0) {
			return;
		}
		FillGradientAngle = SafeOrientation;
		if (FillGradientColors != null) {
			updateAttachedFillAppearance();
		}
	}

	public void setFillGradientRepeatCount(int Count) {
		int SafeCount = Math.max(1, Count);
		if (FillGradientRepeatCount == SafeCount) {
			return;
		}
		FillGradientRepeatCount = SafeCount;
		if (FillGradientColors != null) {
			updateAttachedFillAppearance();
		}
	}

	public void setFillOrderedBlend(int... Colors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListSources(ColorResources, ColorTheme, Colors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedFillOrderedBlend(ResolvedColors);
	}

	public void setFillOrderedBlend(long FirstColor, long... RemainingColors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListValues(ColorResources, ColorTheme, FirstColor, RemainingColors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedFillOrderedBlend(ResolvedColors);
	}

	void ApplyResolvedFillOrderedBlend(int[] Colors) {
		Integer ResolvedColor = G2ColorBlend.ResolveOrderedBlend(Colors);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedFillColor(ResolvedColor);
	}

	public void setFillAverageBlend(int... Colors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListSources(ColorResources, ColorTheme, Colors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedFillAverageBlend(ResolvedColors);
	}

	public void setFillAverageBlend(long FirstColor, long... RemainingColors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListValues(ColorResources, ColorTheme, FirstColor, RemainingColors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedFillAverageBlend(ResolvedColors);
	}

	void ApplyResolvedFillAverageBlend(int[] Colors) {
		Integer ResolvedColor = G2ColorBlend.ResolveAverageBlend(Colors);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedFillColor(ResolvedColor);
	}
	
	public void setStrokeColor(int Color) {
		Integer ResolvedColor = G2ColorResolver.ResolveColorSource(ColorResources, ColorTheme, Color);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedStrokeColor(ResolvedColor);
	}

	public void setStrokeColor(long Color) {
		Integer ResolvedColor = G2ColorResolver.ResolveColorValue(ColorResources, ColorTheme, Color);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedStrokeColor(ResolvedColor);
	}

	void ApplyResolvedStrokeColor(int Color) {
		boolean Changed = StrokeColor != Color || StrokeGradientColors != null;
		StrokeColor = Color;
		StrokeGradientColors = null;
		if (Changed) {
			updateAttachedStrokeAppearance();
		}
	}
	
	public void setStrokeWidth(String Width) {
		Float ParsedWidth = G2ValueResolver.ResolveNonNegativeLength(AppContext, Width);
		if (ParsedWidth == null) {
			return;
		}
		if (StrokeWidth != ParsedWidth) {
			StrokeWidth = ParsedWidth;
			updateAttachedStrokeAppearance();
		}
	}
	
	public void setStrokePosition(@StrokePosition int Position) {
		int SafePosition = sanitizeStrokePosition(Position);
		if (StrokePosition != SafePosition) {
			StrokePosition = SafePosition;
			updateAttachedStrokeAppearance();
		}
	}
	
	public void setStrokeLayer(@StrokeLayer int Layer) {
		int SafeLayer = sanitizeStrokeLayer(Layer);
		if (StrokeLayer == SafeLayer) {
			return;
		}
		StrokeLayer = SafeLayer;
		for (Attachment CurrentAttachment : Attachments.values()) {
			if (CurrentAttachment.RenderDelegate != null) {
				CurrentAttachment.RenderDelegate.UpdateStrokeLayer(StrokeLayer);
			}
		}
	}
	
	public void setStrokeAlpha(String Alpha) {
		Float ParsedAlpha = G2ValueResolver.ResolveUnitInterval(AppContext, Alpha);
		if (ParsedAlpha == null) {
			return;
		}
		if (StrokeAlpha != ParsedAlpha) {
			StrokeAlpha = ParsedAlpha;
			updateAttachedStrokeAppearance();
		}
	}
	
	public void setStrokeGradient(int... Colors) {
		if (Colors == null) {
			return;
		}
		int[] ResolvedColors = G2ColorResolver.ResolveColorListSources(ColorResources, ColorTheme, Colors, 2);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedStrokeGradient(ResolvedColors);
	}

	public void setStrokeGradient(long FirstColor, long... RemainingColors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListValues(ColorResources, ColorTheme, FirstColor, RemainingColors, 2);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedStrokeGradient(ResolvedColors);
	}

	void ApplyResolvedStrokeGradient(int[] Colors) {
		if (Colors == null || Colors.length < 2) {
			return;
		}
		if (Arrays.equals(StrokeGradientColors, Colors)) {
			return;
		}
		StrokeGradientColors = Arrays.copyOf(Colors, Colors.length);
		updateAttachedStrokeAppearance();
	}

	public void setStrokeGradientOrientation(float Orientation) {
		float SafeOrientation = sanitizeGradientOrientation(Orientation);
		if (Float.compare(StrokeGradientAngle, SafeOrientation) == 0) {
			return;
		}
		StrokeGradientAngle = SafeOrientation;
		if (StrokeGradientColors != null) {
			updateAttachedStrokeAppearance();
		}
	}

	public void setStrokeGradientRepeatCount(int Count) {
		int SafeCount = Math.max(1, Count);
		if (StrokeGradientRepeatCount == SafeCount) {
			return;
		}
		StrokeGradientRepeatCount = SafeCount;
		if (StrokeGradientColors != null) {
			updateAttachedStrokeAppearance();
		}
	}

	public void setStrokeOrderedBlend(int... Colors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListSources(ColorResources, ColorTheme, Colors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedStrokeOrderedBlend(ResolvedColors);
	}

	public void setStrokeOrderedBlend(long FirstColor, long... RemainingColors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListValues(ColorResources, ColorTheme, FirstColor, RemainingColors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedStrokeOrderedBlend(ResolvedColors);
	}

	void ApplyResolvedStrokeOrderedBlend(int[] Colors) {
		Integer ResolvedColor = G2ColorBlend.ResolveOrderedBlend(Colors);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedStrokeColor(ResolvedColor);
	}

	public void setStrokeAverageBlend(int... Colors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListSources(ColorResources, ColorTheme, Colors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedStrokeAverageBlend(ResolvedColors);
	}

	public void setStrokeAverageBlend(long FirstColor, long... RemainingColors) {
		int[] ResolvedColors = G2ColorResolver.ResolveColorListValues(ColorResources, ColorTheme, FirstColor, RemainingColors, 1);
		if (ResolvedColors == null) {
			return;
		}
		ApplyResolvedStrokeAverageBlend(ResolvedColors);
	}

	void ApplyResolvedStrokeAverageBlend(int[] Colors) {
		Integer ResolvedColor = G2ColorBlend.ResolveAverageBlend(Colors);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedStrokeColor(ResolvedColor);
	}

	public void setStrokeSegmentCount(int Count) {
		int SafeCount = sanitizeStrokeSegmentCount(Count);
		if (StrokeSegmentCount == SafeCount) {
			return;
		}
		StrokeSegmentCount = SafeCount;
		updateAttachedStrokePattern();
	}

	public void setStrokeGapLength(String Length) {
		Float ParsedLength = G2ValueResolver.ResolveNonNegativeLength(AppContext, Length);
		if (ParsedLength == null || Float.compare(StrokeGapLength, ParsedLength) == 0) {
			return;
		}
		StrokeGapLength = ParsedLength;
		updateAttachedStrokePattern();
	}

	public void setStrokePatternAngle(float Angle) {
		if (!isFinite(Angle) || Float.compare(StrokePatternAngle, Angle) == 0) {
			return;
		}
		StrokePatternAngle = Angle;
		updateAttachedStrokePattern();
	}

	public void setStrokeSegmentCap(@StrokeSegmentCap int Cap) {
		int SafeCap = sanitizeStrokeSegmentCap(Cap);
		if (StrokeSegmentCap == SafeCap) {
			return;
		}
		StrokeSegmentCap = SafeCap;
		updateAttachedStrokePattern();
	}
	
	public void setRippleColor(int Color) {
		Integer ResolvedColor = G2ColorResolver.ResolveColorSource(ColorResources, ColorTheme, Color);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedRippleColor(ResolvedColor);
	}

	public void setRippleColor(long Color) {
		Integer ResolvedColor = G2ColorResolver.ResolveColorValue(ColorResources, ColorTheme, Color);
		if (ResolvedColor == null) {
			return;
		}
		ApplyResolvedRippleColor(ResolvedColor);
	}

	void ApplyResolvedRippleColor(int Color) {
		RippleColor = Color;
		updateAttachedDrawables();
	}
	
	public void setRippleAlpha(String Alpha) {
		Float ParsedAlpha = G2ValueResolver.ResolveUnitInterval(AppContext, Alpha);
		if (ParsedAlpha == null) {
			return;
		}
		RippleAlpha = ParsedAlpha;
		updateAttachedDrawables();
	}
	
	public void setClipToG2Outline(boolean Enabled) {
		ClipToG2Outline = Enabled;
		for (Map.Entry<View, Attachment> Entry : Attachments.entrySet()) {
			Entry.getValue().Drawable.setClipToG2Outline(ClipToG2Outline);
			updateOutlineForAttachment(Entry.getKey(), Entry.getValue());
		}
	}

	private void updateAttachedFillAppearance() {
		for (Attachment CurrentAttachment : Attachments.values()) {
			CurrentAttachment.Drawable.setFill(FillColor, FillAlpha, FillGradientColors, FillGradientAngle, FillGradientRepeatCount);
		}
	}

	private void updateAttachedStrokeAppearance() {
		for (Attachment CurrentAttachment : Attachments.values()) {
			CurrentAttachment.Drawable.setStroke(StrokeColor, StrokeWidth, StrokePosition, StrokeAlpha, StrokeGradientColors, StrokeGradientAngle, StrokeGradientRepeatCount);
		}
	}

	private void updateAttachedStrokePattern() {
		for (Attachment CurrentAttachment : Attachments.values()) {
			CurrentAttachment.Drawable.setStrokePattern(StrokeSegmentCount, StrokeGapLength, StrokePatternAngle, StrokeSegmentCap);
		}
	}

	private void updateAttachedDrawables() {
		for (Map.Entry<View, Attachment> Entry : Attachments.entrySet()) {
			applyToDrawable(Entry.getKey(), Entry.getValue().Drawable);
			Entry.getValue().OutlineProvider.setDrawable(Entry.getValue().Drawable);
			updateOutlineForAttachment(Entry.getKey(), Entry.getValue());
		}
	}
	
	private void applyToDrawable(View View, G2Drawable Drawable) {
		Drawable.setResolvedLayoutDirection(View.getLayoutDirection());
		Drawable.setGeometry(createRoundedRectangleProfile(), createCapsuleProfile(), BaseCorner, PhysicalTopLeft, HasPhysicalTopLeft, PhysicalTopRight, HasPhysicalTopRight, PhysicalBottomLeft, HasPhysicalBottomLeft, PhysicalBottomRight, HasPhysicalBottomRight, LogicalTopStart, HasLogicalTopStart, LogicalTopEnd, HasLogicalTopEnd, LogicalBottomStart, HasLogicalBottomStart, LogicalBottomEnd, HasLogicalBottomEnd);
		Drawable.setFill(FillColor, FillAlpha, FillGradientColors, FillGradientAngle, FillGradientRepeatCount);
		Drawable.setStroke(StrokeColor, StrokeWidth, StrokePosition, StrokeAlpha, StrokeGradientColors, StrokeGradientAngle, StrokeGradientRepeatCount);
		Drawable.setStrokePattern(StrokeSegmentCount, StrokeGapLength, StrokePatternAngle, StrokeSegmentCap);
		Drawable.setRipple(RippleColor, RippleAlpha);
		Drawable.setClipToG2Outline(ClipToG2Outline);
	}
	
	private void updateOutlineForAttachment(View View, Attachment Attachment) {
		if (Attachment.RenderDelegate != null) {
			removeOutlineLayoutListener(View, Attachment);
			Attachment.RenderDelegate.UpdateContentClipEnabled(ClipToG2Outline);
			View.setOutlineProvider(NoSystemShadowOutlineProvider);
			View.setClipToOutline(false);
			Attachment.OutlineStateChanged = true;
			View.invalidateOutline();
			return;
		}
		if (ClipToG2Outline) {
			ensureOutlineLayoutListener(View, Attachment);
			if (Attachment.OutlineProvider.canClipView(View)) {
				View.setOutlineProvider(Attachment.OutlineProvider);
				View.setClipToOutline(true);
				Attachment.OutlineStateChanged = true;
				View.invalidateOutline();
			} else {
				View.setOutlineProvider(Attachment.Ownership.OriginalOutlineProvider);
				View.setClipToOutline(false);
				Attachment.OutlineStateChanged = true;
				View.invalidateOutline();
			}
		} else {
			restoreOutlineForAttachment(View, Attachment);
			removeOutlineLayoutListener(View, Attachment);
		}
	}

	private void removeRenderDelegate(View View, Attachment Attachment) {
		G2ViewRenderDelegate RenderDelegate = Attachment.RenderDelegate;
		if (RenderDelegate == null) {
			return;
		}
		if (View instanceof G2ViewRenderHost) {
			((G2ViewRenderHost) View).RemoveG2RenderDelegate(RenderDelegate);
		}
		RenderDelegate.Release();
		Attachment.RenderDelegate = null;
	}

	private void restoreOutlineForAttachment(View View, Attachment Attachment) {
		if (Attachment.OutlineStateChanged) {
			View.setOutlineProvider(Attachment.Ownership.OriginalOutlineProvider);
			View.setClipToOutline(Attachment.Ownership.OriginalClipToOutline);
			Attachment.OutlineStateChanged = false;
			View.invalidateOutline();
		}
	}

	private void ensureOutlineLayoutListener(View View, Attachment Attachment) {
		if (!Attachment.OutlineLayoutListenerAttached) {
			View.addOnLayoutChangeListener(Attachment.OutlineLayoutChangeListener);
			Attachment.OutlineLayoutListenerAttached = true;
		}
	}

	private void removeOutlineLayoutListener(View View, Attachment Attachment) {
		if (Attachment.OutlineLayoutListenerAttached) {
			View.removeOnLayoutChangeListener(Attachment.OutlineLayoutChangeListener);
			Attachment.OutlineLayoutListenerAttached = false;
		}
	}

	private G2Profile createRoundedRectangleProfile() {
		return new G2Profile(RoundedRectangleExtendedFraction, RoundedRectangleArcFraction, RoundedRectangleBezierCurvatureScale, RoundedRectangleArcCurvatureScale);
	}
	
	private G2Profile createCapsuleProfile() {
		return new G2Profile(CapsuleExtendedFraction, CapsuleArcFraction, G2Profile.DefaultCapsuleBezierCurvatureScale, G2Profile.DefaultCapsuleArcCurvatureScale);
	}
	
	@IntDef({INSIDE, CENTER, OUTSIDE})
	@Retention(RetentionPolicy.SOURCE)
	public @interface StrokePosition {
	}

	@IntDef({BELOW_CONTENT, ABOVE_CONTENT})
	@Retention(RetentionPolicy.SOURCE)
	public @interface StrokeLayer {
	}

	@IntDef({SHARP, ROUND})
	@Retention(RetentionPolicy.SOURCE)
	public @interface StrokeSegmentCap {
	}
	
	private static final class OwnershipState {
		final Drawable OriginalBackground;
		final ViewOutlineProvider OriginalOutlineProvider;
		final boolean OriginalClipToOutline;
		WeakReference<G2Background> OwnerReference;
		WeakReference<G2ViewRenderDelegate> ActiveRenderDelegateReference;
		long Generation;

		OwnershipState(Drawable Background, ViewOutlineProvider OutlineProvider, boolean ClipToOutline) {
			OriginalBackground = Background;
			OriginalOutlineProvider = OutlineProvider;
			OriginalClipToOutline = ClipToOutline;
			OwnerReference = new WeakReference<>(null);
			ActiveRenderDelegateReference = new WeakReference<>(null);
			Generation = 0L;
		}
	}

	private final class Attachment {
		final OwnershipState Ownership;
		final long OwnershipGeneration;
		final G2OutlineProvider OutlineProvider;
		final View.OnLayoutChangeListener OutlineLayoutChangeListener;
		G2Drawable Drawable;
		G2ViewRenderDelegate RenderDelegate;
		boolean OutlineStateChanged;
		boolean OutlineLayoutListenerAttached;

		Attachment(View View, OwnershipState CurrentOwnership, long Generation, G2Drawable Current) {
			Ownership = CurrentOwnership;
			OwnershipGeneration = Generation;
			Drawable = Current;
			OutlineProvider = new G2OutlineProvider(Current);
			OutlineStateChanged = View.getOutlineProvider() != CurrentOwnership.OriginalOutlineProvider || View.getClipToOutline() != CurrentOwnership.OriginalClipToOutline;
			OutlineLayoutChangeListener = new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View ChangedView, int Left, int Top, int Right, int Bottom, int OldLeft, int OldTop, int OldRight, int OldBottom) {
					updateOutlineForAttachment(ChangedView, Attachment.this);
				}
			};
		}
	}
}