/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /media/myspace/6577/gionee_packages/packages/mtk/packages_4.0/apps/NumArea/src/com/gionee/aora/numarea/export/INumAreaManager.aidl
 */
package com.gionee.aora.numarea.export;
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
public interface INumAreaManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.gionee.aora.numarea.export.INumAreaManager
{
private static final java.lang.String DESCRIPTOR = "com.gionee.aora.numarea.export.INumAreaManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.gionee.aora.numarea.export.INumAreaManager interface,
 * generating a proxy if needed.
 */
public static com.gionee.aora.numarea.export.INumAreaManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.gionee.aora.numarea.export.INumAreaManager))) {
return ((com.gionee.aora.numarea.export.INumAreaManager)iin);
}
return new com.gionee.aora.numarea.export.INumAreaManager.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getNumAreaInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.gionee.aora.numarea.export.NumAreaInfo _result = this.getNumAreaInfo(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getAreaNumInfo:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
com.gionee.aora.numarea.export.NumAreaInfo[] _result = this.getAreaNumInfo(_arg0, _arg1);
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_getComAreaNumInfo:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.NumAreaInfo[] _result = this.getComAreaNumInfo();
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_updataDB:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.INumAreaObserver _arg0;
_arg0 = com.gionee.aora.numarea.export.INumAreaObserver.Stub.asInterface(data.readStrongBinder());
this.updataDB(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_cancelUpdata:
{
data.enforceInterface(DESCRIPTOR);
this.cancelUpdata();
reply.writeNoException();
return true;
}
case TRANSACTION_registObserver:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.INumAreaObserver _arg0;
_arg0 = com.gionee.aora.numarea.export.INumAreaObserver.Stub.asInterface(data.readStrongBinder());
boolean _result = this.registObserver(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_unregistObserver:
{
data.enforceInterface(DESCRIPTOR);
com.gionee.aora.numarea.export.INumAreaObserver _arg0;
_arg0 = com.gionee.aora.numarea.export.INumAreaObserver.Stub.asInterface(data.readStrongBinder());
boolean _result = this.unregistObserver(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.gionee.aora.numarea.export.INumAreaManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
	 * ���ݺ����ȡ��������Ϣ
	 * @param aPhoneNum
	 * ����ĺ���
	 * @return
	 * ��������Ϣ
	 */
public com.gionee.aora.numarea.export.NumAreaInfo getNumAreaInfo(java.lang.String aPhoneNum) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.gionee.aora.numarea.export.NumAreaInfo _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(aPhoneNum);
mRemote.transact(Stub.TRANSACTION_getNumAreaInfo, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.gionee.aora.numarea.export.NumAreaInfo.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * ���������ַ������Һ��������
	 * @param aArea
	 * ����
	 * @return
	 * ��Ź��������ݵ�����
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getAreaNumInfo(java.lang.String aArea, java.lang.String aTag) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.gionee.aora.numarea.export.NumAreaInfo[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(aArea);
_data.writeString(aTag);
mRemote.transact(Stub.TRANSACTION_getAreaNumInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(com.gionee.aora.numarea.export.NumAreaInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * ��ȡ���ú����б�
	 * @param aArea
	 * ����
	 * @return
	 * ��Ź��������ݵ�����
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getComAreaNumInfo() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.gionee.aora.numarea.export.NumAreaInfo[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getComAreaNumInfo, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(com.gionee.aora.numarea.export.NumAreaInfo.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * ���¹��������ݿ�
	 * @param aObserver
	 * �������½���Ĺ۲���
	 */
public void updataDB(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((aObserver!=null))?(aObserver.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_updataDB, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * ȡ������
	 */
public void cancelUpdata() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelUpdata, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * ע��һ���۲���
	 * @param aObserver
	 * �۲��߶���
	 * @return
	 * ����۲����б���û�д˶�����ӳɹ�.����true,�������Ϊfalse
	 */
public boolean registObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((aObserver!=null))?(aObserver.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registObserver, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * ע��һ���۲���
	 * @param aObserver
	 * �۲��߶���
	 * @return
	 * ����۲����б����д˶���ɾ���ɹ�,����true,�������Ϊfalse
	 */
public boolean unregistObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((aObserver!=null))?(aObserver.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregistObserver, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getNumAreaInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getAreaNumInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getComAreaNumInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_updataDB = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_cancelUpdata = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_registObserver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_unregistObserver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
/**
	 * ���ݺ����ȡ��������Ϣ
	 * @param aPhoneNum
	 * ����ĺ���
	 * @return
	 * ��������Ϣ
	 */
public com.gionee.aora.numarea.export.NumAreaInfo getNumAreaInfo(java.lang.String aPhoneNum) throws android.os.RemoteException;
/**
	 * ���������ַ������Һ��������
	 * @param aArea
	 * ����
	 * @return
	 * ��Ź��������ݵ�����
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getAreaNumInfo(java.lang.String aArea, java.lang.String aTag) throws android.os.RemoteException;
/**
	 * ��ȡ���ú����б�
	 * @param aArea
	 * ����
	 * @return
	 * ��Ź��������ݵ�����
	 */
public com.gionee.aora.numarea.export.NumAreaInfo[] getComAreaNumInfo() throws android.os.RemoteException;
/**
	 * ���¹��������ݿ�
	 * @param aObserver
	 * �������½���Ĺ۲���
	 */
public void updataDB(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException;
/**
	 * ȡ������
	 */
public void cancelUpdata() throws android.os.RemoteException;
/**
	 * ע��һ���۲���
	 * @param aObserver
	 * �۲��߶���
	 * @return
	 * ����۲����б���û�д˶�����ӳɹ�.����true,�������Ϊfalse
	 */
public boolean registObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException;
/**
	 * ע��һ���۲���
	 * @param aObserver
	 * �۲��߶���
	 * @return
	 * ����۲����б����д˶���ɾ���ɹ�,����true,�������Ϊfalse
	 */
public boolean unregistObserver(com.gionee.aora.numarea.export.INumAreaObserver aObserver) throws android.os.RemoteException;
}
