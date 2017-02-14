package com.github.jordicurto.autochecker.util;

import android.content.Context;

public abstract class ContextKeeper {

	protected Context mContext;

	public ContextKeeper(Context context) {
		mContext = context;
	}

	public Context getContext() {
		return mContext;
	}	
}
