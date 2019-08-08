package at.looksy.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewRobotoLight extends TextView {
	public TextViewRobotoLight(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createFont();
	}

	public TextViewRobotoLight(Context context, AttributeSet attrs) {
		super(context, attrs);
		createFont();
	}

	public TextViewRobotoLight(Context context) {
		super(context);
		createFont();
	}

	public void createFont() {
		Typeface font = Typeface.createFromAsset(
				getContext().getAssets(), "Roboto-Light.ttf");
		setTypeface(font);
	}
}
