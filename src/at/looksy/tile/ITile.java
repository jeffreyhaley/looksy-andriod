package at.looksy.tile;

import android.view.View;
import at.looksy.dataitem.DataItem;

public interface ITile {
	
	public View getLayout();

	public DataItem getData();
	
	public void recycleBitmap();
	
}
