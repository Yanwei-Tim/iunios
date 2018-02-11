package com.gionee.aora.numarea.export;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file IUpdataResult.java
 * ժҪ:���¹��������ݿ�Ĳ������
 *
 * @author yewei
 * @data 2011-5-20
 * @version 
 *
 */
public interface IUpdataResult
{
	/**
	 * ���³ɹ�
	 */
	final static public int RESULT_SUCCESS = 2;
	/**
	 * �������ӳ���
	 */
	final static public int RESULT_ERROR_CONNECT_FAILD = 1;
	/**
	 * �������ӳ�ʱ
	 */
	final static public int RESULT_ERROR_CONNECT_TIMEOUT = 0;
	/**
	 * �������ݴ���
	 */
	final static public int RESULT_ERROR_PARSE_DB_FAILD = 4;
	/**
	 * ȡ������
	 */
	final static public int RESULT_USER_CANCEL_UPDATA = 5;
	/**
	 * ���²������ڽ�����
	 */
	final static public int RESULT_ERROR_UPDATA_PROCESSING = 6;
	/**
	 * ��ʼ�����ݳɹ�
	 */
	final static public int RESULT_INIT_FINISH = 7;
	/**
	 * ���ݿ��Ѿ������°汾��
	 */
	final static public int RESULT_DB_IS_LAST_VERSION = 8;
	/**
	 * ���ڽ��г�ʼ��
	 */
	final static public int RESULT_INIT_PROCESSING = 9;
	/**
	 * ��������
	 */
	final static public int RESULT_DOWNLOADING = 10;
}
