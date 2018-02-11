package com.gionee.aora.numarea.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file Powerboot_BroadCast.java
 * ժҪ:������������������ط���Ĺ㲥����
 *
 * @author jiangf
 * @data 2011-6-28
 * @version 
 *
 */
public class Powerboot_BroadCast extends BroadcastReceiver{
	
	public static final String ACTION = "android.intent.action.BOOT_COMPLETED";

@Override
public void onReceive(Context context, Intent intent) {

	if (intent.getAction().equals(ACTION)) {
		Intent Powerboot_Intent = new Intent(context, NumAreaService.class);
		Powerboot_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(Powerboot_Intent);
	}
}}
