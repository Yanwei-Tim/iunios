package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file Country_Info.java
 * ժҪ:����������Ϣ
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class Country_Info implements Comparable< String >
{
	/**
	 * ��������
	 */
	private String iCountryName;
	/**
	 * ��������
	 */
	private String iTelCode;
	
	public Country_Info(DataInputStream aDis) throws Exception
	{
		iCountryName = aDis.readUTF();
		iTelCode = aDis.readUTF();
	}

	
	
	/**
	 * @return the iCountryName
	 */
	public String getiCountryName()
	{
		return iCountryName;
	}



	/**
	 * @return the iTelCode
	 */
	public String getiTelCode()
	{
		return iTelCode;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Country_Info## countryName=" + iCountryName + ",tel=" + iTelCode;
	}

	@Override
	public int compareTo(String another)
	{
		// TODO Auto-generated method stub
		if(iTelCode.equals(another))
			return 0;
		else 
			return 1;
	}
	
	
}
