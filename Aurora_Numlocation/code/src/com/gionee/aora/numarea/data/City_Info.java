package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file City_Info.java
 * ժҪ:������Ϣ
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class City_Info implements Comparable< Integer >
{
	/**
	 * ����ID
	 */
	private int iCityId;
	/**
	 * ʡ��ID 
	 */
	private int iProvinceId;
	/**
	 * ����
	 */
	private String iAreaCode;
	/**
	 * ��������
	 */
	private String iCityName;
	
	public City_Info(DataInputStream aDis) throws Exception
	{
		iCityId = aDis.readUnsignedShort();
		iProvinceId = aDis.readUnsignedByte();
		iCityName = aDis.readUTF();
		iAreaCode = aDis.readUTF();
	}
	
	

	/**
	 * @return the iCityId
	 */
	public int getiCityId()
	{
		return iCityId;
	}



	/**
	 * @return the iProvinceId
	 */
	public int getiProvinceId()
	{
		return iProvinceId;
	}



	/**
	 * @return the iAreaCode
	 */
	public String getiAreaCode()
	{
		return iAreaCode;
	}



	/**
	 * @return the iCityName
	 */
	public String getiCityName()
	{
		return iCityName;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "City_Info## cityid=" + iCityId + ",name=" + iCityName + ",provinceid=" + iProvinceId + "areacode=" + iAreaCode;
	}

	@Override
	public int compareTo(Integer another)
	{
		// TODO Auto-generated method stub
		if(iCityId < another)
			return -1;
		else if(iCityId > another)
			return 1;
		else
			return 0;
	}
	
	
}
