package com.laysar.G2;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class G2ViewRenderDelegate implements Drawable.Callback {
	private static final int NoClipSentinel = -1;
	private static final int NoDrawSentinel = -2;
	private View Host;
	private G2Drawable Drawable;
	private int StrokeLayer;
	private boolean ContentClipEnabled;
	private boolean DrawStrokeAbove;
	private boolean Released;
	
	G2ViewRenderDelegate(View HostView, G2Drawable CurrentDrawable, int Layer, boolean ClipEnabled) {
		Host = HostView;
		Drawable = CurrentDrawable;
		StrokeLayer = sanitizeStrokeLayer(Layer);
		ContentClipEnabled = ClipEnabled;
		if (Drawable != null) {
			Drawable.setCallback(this);
		}
	}
	
	public int BeginDraw(Canvas Canvas, int Width, int Height, int LayoutDirection) {
		if (Released || Canvas == null || Drawable == null || Width <= 0 || Height <= 0) {
			DrawStrokeAbove = false;
			return NoDrawSentinel;
		}
		Rect Bounds = Drawable.getBounds();
		if (Bounds.left != 0 || Bounds.top != 0 || Bounds.right != Width || Bounds.bottom != Height) {
			Drawable.setBounds(0, 0, Width, Height);
		}
		Drawable.setResolvedLayoutDirection(LayoutDirection);
		Drawable.DrawFill(Canvas);
		DrawStrokeAbove = StrokeLayer == G2Background.ABOVE_CONTENT;
		if (!DrawStrokeAbove) {
			Drawable.DrawStroke(Canvas);
		}
		if (!ContentClipEnabled) {
			return NoClipSentinel;
		}
		int SaveCount = Canvas.save();
		try {
			Drawable.ClipToFillPath(Canvas);
		} catch (RuntimeException Exception) {
			Canvas.restoreToCount(SaveCount);
			throw Exception;
		}
		return SaveCount;
	}
	
	public void EndDraw(Canvas Canvas, int ContentSaveCount) {
		if (ContentSaveCount == NoDrawSentinel || Canvas == null) {
			return;
		}
		if (ContentSaveCount != NoClipSentinel) {
			Canvas.restoreToCount(ContentSaveCount);
		}
		if (DrawStrokeAbove && !Released && Drawable != null) {
			Drawable.DrawStroke(Canvas);
		}
	}
	
	void UpdateStrokeLayer(int Layer) {
		int SafeLayer = sanitizeStrokeLayer(Layer);
		if (StrokeLayer != SafeLayer) {
			StrokeLayer = SafeLayer;
			invalidateHost();
		}
	}
	
	void UpdateContentClipEnabled(boolean Enabled) {
		if (ContentClipEnabled != Enabled) {
			ContentClipEnabled = Enabled;
			invalidateHost();
		}
	}
	
	void Release() {
		if (Released) {
			return;
		}
		Released = true;
		G2Drawable CurrentDrawable = Drawable;
		Drawable = null;
		if (CurrentDrawable != null) {
			CurrentDrawable.setCallback(null);
		}
		Host = null;
		DrawStrokeAbove = false;
	}
	
	@Override
	public void invalidateDrawable(Drawable Who) {
		if (!Released && Who == Drawable) {
			invalidateHost();
		}
	}
	
	@Override
	public void scheduleDrawable(Drawable Who, Runnable What, long When) {
		View CurrentHost = Host;
		if (!Released && Who == Drawable && CurrentHost != null && What != null) {
			//			CurrentHost.postAtTime(What, When);
		}
	}
	
	@Override
	public void unscheduleDrawable(Drawable Who, Runnable What) {
		View CurrentHost = Host;
		if (!Released && Who == Drawable && CurrentHost != null && What != null) {
			CurrentHost.removeCallbacks(What);
		}
	}
	
	private void invalidateHost() {
		View CurrentHost = Host;
		if (!Released && CurrentHost != null) {
			CurrentHost.invalidate();
		}
	}
	
	private static int sanitizeStrokeLayer(int Layer) {
		return Layer == G2Background.ABOVE_CONTENT ? G2Background.ABOVE_CONTENT : G2Background.BELOW_CONTENT;
	}
}