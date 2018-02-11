package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.base.compare.BinarySearch;
import com.base.compare.ISearchable;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file International_Info.java
 * ժҪ:�޼���Ϣ
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class International_Info 
{
	/**
	 * ������
	 */
	private String iName;
	/**
	 * ������Ϣ
	 */
//	private List< Country_Info > iCountryInfo = new ArrayList< Country_Info >();
	private Country_Info[] iCountryInfo;
	
	public Country_Info[] getiCountryInfo() {
		return iCountryInfo;
	}

	public void setiCountryInfo(Country_Info[] iCountryInfo) {
		this.iCountryInfo = iCountryInfo;
	}
	public International_Info(DataInputStream aDis) throws Exception
	{
//		iName = aDis.readUTF();
//		int len = aDis.readUnsignedShort();
//		for(int i = 0; i < len; i++)
//		{
//			iCountryInfo.add(new Country_Info(aDis));
//		}
		iName = aDis.readUTF();
		int len = aDis.readUnsignedShort();
		iCountryInfo = new Country_Info[len];
		for(int i = 0; i < len; i++)
		{
			iCountryInfo[i] = new Country_Info(aDis);
		}
	}
	
	/**
	 * ���ݹ������Ų��ҹ�����Ϣ
	 * @param aSearch
	 * @param aTelNum
	 * @return
	 */
	public List<Country_Info> searchCountryInfo(ISearchable aSearch, String aTelNum)
	{
		List<Country_Info> list = new ArrayList< Country_Info >();
		List<Integer> indexList = NumAreaManager.searchInfo(aSearch, iCountryInfo, aTelNum, 0);
		/*����ͬһ���ſ��ܶ�Ӧ��ͬ����.������Ҫ��List��װ�����еĽ��*/
		if(indexList != null)
		{
			for(Integer index : indexList)
			{
				list.add(iCountryInfo[index]);
			}
			return list;
		}
		else
			return null;
	}

	/**
	 * @return the iName
	 */
	public String getiName()
	{
		return iName;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "International_Info## name=" + iName;
	}


	
	
}
