package com.laysar.G2;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface G2ViewRenderHost {
	void InstallG2RenderDelegate(G2ViewRenderDelegate Delegate);
	void RemoveG2RenderDelegate(G2ViewRenderDelegate Delegate);
}