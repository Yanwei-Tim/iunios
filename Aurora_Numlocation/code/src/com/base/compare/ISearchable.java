package com.base.compare;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file ISearchable.java
 * ժҪ:��ѯ�ӿ�
 *
 * @author yewei
 * @data 2011-6-3
 * @version 
 *
 * @param <T>
 */
public interface ISearchable<T extends Comparable< T >>
{
	/**
	 * ��ʼ����
	 * @param aData
	 * Ҫ���ҵ�����Դ
	 * @param aKey
	 * Ҫ�ҵ�������
	 * @param start
	 * ������Դ�����￪ʼ��
	 * @return
	 * Ҫ������������Դ�е�λ��,û����Ϊ-1
	 */
	public int search(T[] aData,T aKey,int start);
}
