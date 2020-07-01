package com.josephs_projects.erovra2.gui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.apricotLibrary.interfaces.InputListener;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.erovra2.Erovra2;

public class GUIWrapper extends GUIObject {
	List<GUIObject> objects = new ArrayList<>();

	public GUIWrapper(Tuple position) {
		super(position);
		Erovra2.world.add(this);
	}

	public void addGUIObject(GUIObject object) {
		objects.add(object);
		object.wrapper = this;
		updatePosition(position);
	}

	public void removeGUIObject(GUIObject object) {
		objects.remove(object);
		Erovra2.world.remove(object);
	}

	public void setShown(boolean shown) {
		this.shown = shown;
		for (GUIObject o : objects) {
			o.setShown(shown);
		}
	}

	@Override
	public void render(Graphics2D g) {
	}

	@Override
	public void input(InputEvent e) {

	}

	@Override
	public void remove() {
		for (GUIObject o : objects) {
			o.remove();
		}
		Erovra2.world.remove(this);
	}

	@Override
	public int getRenderOrder() {
		return renderOrder;
	}

	@Override
	public int height() {
		if(!shown) {
			return 0;
		}
		int height = padding;
		int rowHeight = 0; // Used for determining the width of the column
		for (GUIObject o : objects) {
			// Check for vertical overflow
			if(!o.shown)
				continue;
			if (position.y + height + o.height() > Erovra2.apricot.height()) {
				height = padding;
			}
			height += o.height();
			rowHeight = Math.max(rowHeight, o.height());
		}
		return 100;
	}

	@Override
	public int width() {
		if(!shown) {
			return 0;
		}
		int width = 0;
		for (GUIObject o : objects) {
			width = Math.max(width, o.width());
		}
		return width + padding;
	}

	public void updatePosition(Tuple position) {
		this.position = position;
		int height = padding;
		int width = padding; // Used for overall width of Wrapper
		int colWidth = 0; // Used for determining the width of the column
		for (GUIObject o : objects) {
			// Check for vertical overflow
			if(!o.shown)
				continue;
			o.position = new Tuple(position.x + width, position.y + height);
			if (position.y + height + o.height() > Erovra2.apricot.height()) {
				height = padding;
				width += colWidth;
				colWidth = 0;
			}
			o.updatePosition(new Tuple(position.x + width, position.y + height));
			height += o.height();
			colWidth = Math.max(colWidth, o.width());
		}
	}
}

abstract class GUIObject implements InputListener, Renderable {
	public Tuple position;
	public int padding = 7;
	public int margin = 4;
	public boolean shown = false;
	protected GUIWrapper wrapper;
	public int renderOrder;

	public GUIObject(Tuple position) {
		this.position = position;
	}

	public void updatePosition(Tuple position) {
		this.position = position;
	}

	public void setShown(boolean shown) {
		this.shown = shown;
	}

	public abstract int height();

	public abstract int width();
}
