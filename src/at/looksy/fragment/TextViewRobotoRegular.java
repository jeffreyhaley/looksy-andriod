package at.looksy.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewRobotoRegular extends TextView {
	public TextViewRobotoRegular(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createFont();
	}

	public TextViewRobotoRegular(Context context, AttributeSet attrs) {
		super(context, attrs);
		createFont();
	}

	public TextViewRobotoRegular(Context context) {
		super(context);
		createFont();
	}

	public void createFont() {
		Typeface font = Typeface.createFromAsset(
				getContext().getAssets(), "Roboto-Regular.ttf");
		setTypeface(font);
	}
}
