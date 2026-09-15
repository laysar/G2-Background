package com.laysar.G2;

import android.graphics.Outline;
import android.graphics.Path;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

final class G2OutlineProvider extends ViewOutlineProvider {
	private final Path OutlinePath = new Path();
	private G2Drawable Drawable;
	
	G2OutlineProvider(G2Drawable Current) {
		Drawable = Current;
	}
	
	void setDrawable(G2Drawable Current) {
		Drawable = Current;
	}
	
	boolean canClipView(View View) {
		if (Drawable == null || View == null) {
			return false;
		}
		if (Build.VERSION.SDK_INT >= 33) {
			return true;
		}
		if (View.getWidth() <= 0 || View.getHeight() <= 0) {
			return false;
		}
		Outline TestOutline = new Outline();
		getOutline(View, TestOutline);
		return TestOutline.canClip();
	}
	
	@Override
	public void getOutline(View View, Outline Outline) {
		if (Drawable == null || View == null || Outline == null) {
			return;
		}
		int Width = View.getWidth();
		int Height = View.getHeight();
		if (Width <= 0 || Height <= 0) {
			Outline.setEmpty();
			Outline.setAlpha(0.0f);
			return;
		}
		int LayoutDirection = View.getLayoutDirection();
		if (Build.VERSION.SDK_INT >= 33) {
			Drawable.buildLocalPath(OutlinePath, Width, Height, LayoutDirection);
			Outline.setPath(OutlinePath);
			Outline.setAlpha(1.0f);
			return;
		}
		int ExactOutlineType = Drawable.getExactOutlineType(Width, Height, LayoutDirection);
		if (ExactOutlineType == G2PathBuilder.ExactOutlineRectangle) {
			Outline.setRect(0, 0, Width, Height);
			Outline.setAlpha(1.0f);
			return;
		}
		if (ExactOutlineType == G2PathBuilder.ExactOutlineOval) {
			Outline.setOval(0, 0, Width, Height);
			Outline.setAlpha(1.0f);
			return;
		}
		if (Build.VERSION.SDK_INT >= 30) {
			Drawable.buildLocalPath(OutlinePath, Width, Height, LayoutDirection);
			Outline.setPath(OutlinePath);
			Outline.setAlpha(1.0f);
			return;
		}
		Outline.setEmpty();
		Outline.setAlpha(0.0f);
	}
}