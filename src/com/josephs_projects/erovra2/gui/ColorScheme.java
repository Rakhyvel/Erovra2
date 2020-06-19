package com.josephs_projects.erovra2.gui;

import java.awt.Color;

public class ColorScheme {
	public final Color backgroundColor;
	public final Color borderColor;
	public final Color highlightColor;
	public final Color textColor;
	public final Color disabledTextColor;
	
	public ColorScheme(Color backgroundColor, Color borderColor, Color highlightColor, Color textColor, Color disabledTextColor) {
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
		this.highlightColor = highlightColor;
		this.textColor = textColor;
		this.disabledTextColor = disabledTextColor;
	}
	
}
