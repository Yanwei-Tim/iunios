package com.gionee.aora.numarea.data;
import java.io.DataInputStream;
/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file Province_Info.java
 * ժҪ:ʡ����Ϣ
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class Province_Info implements Comparable< Integer >
{
	/**
	 * ʡ��ID
	 */
	private int iProvinceId;
	/**
	 * ���и���
	 */
	private int iCityCount;
	/**
	 * ʡ������
	 */
	private String iProvinceName;
	
	public Province_Info(DataInputStream aDis) throws Exception
	{
		iProvinceId = aDis.readUnsignedByte();
		iProvinceName = aDis.readUTF();
		iCityCount = aDis.readUnsignedByte();
	}
	
	

	/**
	 * @return the iProvinceId
	 */
	public int getProvinceId()
	{
		return iProvinceId;
	}



	/**
	 * @return the iCityCount
	 */
	public int getCityCount()
	{
		return iCityCount;
	}



	/**
	 * @return the iProvinceName
	 */
	public String getProvinceName()
	{
		return iProvinceName;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Province_Info## provinceid=" + iProvinceId + ",name=" + iProvinceName + ",count=" + iCityCount;
	}

	@Override
	public int compareTo(Integer another)
	{
		// TODO Auto-generated method stub
		if(iProvinceId < another)
			return -1;
		else if(iProvinceId > another)
			return 1;
		else
			return 0;
	}
	
	
}
