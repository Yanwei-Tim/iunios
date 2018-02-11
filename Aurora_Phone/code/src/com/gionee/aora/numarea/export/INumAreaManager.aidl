package com.gionee.aora.numarea.export;
import com.gionee.aora.numarea.export.INumAreaObserver;
import com.gionee.aora.numarea.export.NumAreaInfo;
/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file INumAreaManager.aidl
 * ժҪ:��������ع�����,��Ҫ�ṩ�����ӿ�,�ɹ����ص�Service����
 *
 * @author yewei
 * @data 2011-5-20
 * @version 
 *
 */
interface INumAreaManager
{
/**
	 * ���ݺ����ȡ��������Ϣ
	 * @param aPhoneNum
	 * ����ĺ���
	 * @return
	 * ��������Ϣ
	 */
	NumAreaInfo getNumAreaInfo(String aPhoneNum);
	/**
	 * ���������ַ������Һ��������
	 * @param aArea
	 *@param aTag
	 * ����
	 * @return
	 * ��Ź��������ݵ�List
	 */
	NumAreaInfo[] getAreaNumInfo(String aArea,String aTag);
		/**
	 * ��ȡ���ú����б�
	 * @param aArea
	 * ����
	 * @return
	 * ��Ź��������ݵ�List
	 */
	NumAreaInfo[] getComAreaNumInfo();
	/**
	 * ���¹��������ݿ�
	 * @param aObserver
	 * �������½���Ĺ۲���
	 */
	void updataDB(INumAreaObserver aObserver);
	
	/**
	 * ȡ������
	 */
	void cancelUpdata();
	
    /**
	 * ע��һ���۲���
	 * @param aObserver
	 * �۲��߶���
	 * @return
	 * ����۲����б���û�д˶�����ӳɹ�.����true,�������Ϊfalse
	 */
	boolean registObserver(INumAreaObserver aObserver);
	
	/**
	 * ע��һ���۲���
	 * @param aObserver
	 * �۲��߶���
	 * @return
	 * ����۲����б����д˶���ɾ���ɹ�,����true,�������Ϊfalse
	 */
	boolean unregistObserver(INumAreaObserver aObserver);
}