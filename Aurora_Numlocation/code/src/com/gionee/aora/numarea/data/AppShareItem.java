package com.gionee.aora.numarea.data;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file AppShareItem.java
 * ժҪ: �������
 *
 * @author yinzw
 * @data 2011-7-7
 * @version 
 *
 */
public class AppShareItem {
	
	/**Сͼ��*/
	protected Bitmap icon;
	
	/**����*/
	protected String packageName;
	/**���id*/
	protected String id;
	
	/**ActivityInfo*/
	ActivityInfo info;

	/**ActivityInfo
	 * @return
	 */
	public ActivityInfo getInfo() {
		return info;
	}

	/**ActivityInfo
	 * @param info
	 */
	public void setInfo(ActivityInfo info) {
		this.info = info;
	}
	
	/**
	 * ��ȡСͼ��
	 * @return
	 */
	public Bitmap getIcon() {
		return icon;
	}

	/**
	 * ����Сͼ��
	 * @param icon
	 */
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	/**
	 * get id
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * set id
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * ��ȡ����
	 * @return
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * ���ð���
	 * @param packageName
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AppShareItem)
			return packageName.equals(((AppShareItem) o).getPackageName());
		return false;
	}
}
