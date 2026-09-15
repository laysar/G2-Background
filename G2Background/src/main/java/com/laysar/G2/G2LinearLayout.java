package com.laysar.G2;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.laysar..G2.G2Background;

public final class G2LinearLayout extends LinearLayout implements G2ViewRenderHost {
	private G2Background Background;
	private boolean G2Attached;
	private G2ViewRenderDelegate RenderDelegate;
	private boolean RequestedWillNotDraw;
	
	public G2LinearLayout(Context Context) {
		super(Context);
		InitializeG2(Context, null, 0);
	}
	
	public G2LinearLayout(Context Context, AttributeSet AttributeSet) {
		super(Context, AttributeSet);
		InitializeG2(Context, AttributeSet, 0);
	}
	
	public G2LinearLayout(Context Context, AttributeSet AttributeSet, int DefStyleAttr) {
		super(Context, AttributeSet, DefStyleAttr);
		InitializeG2(Context, AttributeSet, DefStyleAttr);
	}
	
	@Override
	public void InstallG2RenderDelegate(G2ViewRenderDelegate Delegate) {
		if (RenderDelegate != Delegate) {
			RenderDelegate = Delegate;
			if (Delegate != null) {
				super.setWillNotDraw(false);
			} else {
				super.setWillNotDraw(RequestedWillNotDraw);
			}
			invalidate();
		}
	}
	
	@Override
	public void RemoveG2RenderDelegate(G2ViewRenderDelegate Delegate) {
		if (RenderDelegate == Delegate) {
			RenderDelegate = null;
			super.setWillNotDraw(RequestedWillNotDraw);
			invalidate();
		}
	}

	@Override
	public void setWillNotDraw(boolean WillNotDraw) {
		RequestedWillNotDraw = WillNotDraw;
		if (RenderDelegate != null) {
			super.setWillNotDraw(false);
		} else {
			super.setWillNotDraw(WillNotDraw);
		}
	}
	
	@Override
	public void draw(Canvas Canvas) {
		G2ViewRenderDelegate Delegate = RenderDelegate;
		int ContentSaveCount = 0;
		if (Delegate != null) {
			ContentSaveCount = Delegate.BeginDraw(Canvas, getWidth(), getHeight(), getLayoutDirection());
		}
		try {
			super.draw(Canvas);
		} finally {
			if (Delegate != null) {
				Delegate.EndDraw(Canvas, ContentSaveCount);
			}
		}
	}
	
	private void InitializeG2(Context Context, AttributeSet AttributeSet, int DefStyleAttr) {
		if (Background != null) {
			return;
		}
		RequestedWillNotDraw = willNotDraw();
		Background = new G2Background(Context);
		if (AttributeSet != null) {
			G2ViewAttributeReader.Apply(Context, AttributeSet, DefStyleAttr, Background);
		}
		if (!G2Attached) {
			Background.Attach(this);
			G2Attached = true;
		}
	}
}