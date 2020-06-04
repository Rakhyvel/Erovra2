package com.josephs_projects.erovra2.gui;

import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.InputListener;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.erovra2.Erovra2;

public class GUIWrapper {
	public Tuple position;
	List<GUIObject> objects = new ArrayList<>();
	
	public GUIWrapper(Tuple position) {
		this.position = position;
	}
	
	public void addGUIObject(GUIObject object) {
		objects.add(object);
		Erovra2.world.add(object);
		object.wrapper = this;
	}
	
	public void removeGUIObject(GUIObject object) {
		objects.remove(object);
		Erovra2.world.remove(object);
	}
	
	public void setShown(boolean shown) {
		for(GUIObject o : objects) {
			o.shown = shown;
		}
	}
}

abstract class GUIObject implements InputListener, Renderable{
	protected Tuple position;
	protected boolean shown = false;
	protected GUIWrapper wrapper;
	
	public GUIObject(Tuple position) {
		this.position = position;
	}
}
