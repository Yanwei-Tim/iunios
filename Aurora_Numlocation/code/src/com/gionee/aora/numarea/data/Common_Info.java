package com.gionee.aora.numarea.data;

import java.io.DataInputStream;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file Common_Info.java
 * ժҪ:���ú�����Ϣ
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class Common_Info implements Comparable< String >
{
	/**
	 * ����
	 */
	private String iTelNum;
	/**
	 * ����
	 */
	private String iName;
	/**
	 * ������
	 */
	private String iGroupName;
	
	public Common_Info(String aGroupName,String aTelNum,String aName)
	{
		iGroupName = aGroupName;
		iTelNum = aTelNum;
		iName = aName;
//		NumAreaManager.LOG.print(this);
	}
	
	

	/**
	 * @return the iTelNum
	 */
	public String getiTelNum()
	{
		return iTelNum;
	}



	/**
	 * @return the iName
	 */
	public String getiName()
	{
		return iName;
	}



	/**
	 * @return the iGroupName
	 */
	public String getiGroupName()
	{
		return iGroupName;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Common_Info## groupname=" + iGroupName + ",telnum=" + iTelNum + ",name=" + iName;
	}

	@Override
	public int compareTo(String another)
	{
		// TODO Auto-generated method stub
		if(iTelNum.equals(another))
			return 0;
		else 
			return 1;
		
	}
	
	
}
