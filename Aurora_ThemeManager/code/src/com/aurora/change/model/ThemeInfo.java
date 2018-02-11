package com.aurora.change.model;

public class ThemeInfo {
	public String name;
	public String nameColor;
	public String isDefault;
	public String timeBlack;
	public String statusBarBlack;
	
	public ThemeInfo() {
		// TODO Auto-generated constructor stub
		this.nameColor = "#FF000000";				//"default_title_color" in colors.xml
		this.isDefault = "false";					//default false
		this.timeBlack = "false";					//default false
		this.statusBarBlack = "false";				//default false
	}
}
