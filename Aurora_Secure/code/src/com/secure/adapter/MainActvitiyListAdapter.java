package com.secure.adapter;

import java.util.List;
import com.aurora.secure.R;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.secure.activity.MainActivity;
import com.secure.data.AppInfo;
import com.secure.data.BaseData;
import com.secure.data.MainActivityItemData;
import com.secure.model.AppInfoModel;
import com.secure.model.ConfigModel;
import com.secure.utils.ApkUtils;
import com.secure.utils.StorageUtil;
import com.secure.utils.StringUtils;
import com.secure.utils.Utils;
import com.secure.viewcache.PermissionDetailItemCache;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class MainActvitiyListAdapter extends ArrayAdapter<BaseData> {      
	private AbsListView.LayoutParams convertViewLayoutParams;	
	public MainActvitiyListAdapter(Activity activity,
			List<BaseData> listData) {		
		super(activity, 0, listData);
		convertViewLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
	}

	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {			
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.main_activity_grid_item, parent, false);			
		}
		
		int parentHeight = parent.getHeight();
		if(parentHeight >0){
			convertViewLayoutParams.height = parentHeight/3;
			convertView.setLayoutParams(convertViewLayoutParams);
		}
		
		if(getCount()<=position){
			return convertView;
		}

		MainActivityItemData item =(MainActivityItemData)getItem(position);
		ImageView Icon = (ImageView)convertView.findViewById(R.id.Icon);
		TextView titleName = (TextView)convertView.findViewById(R.id.titleName);
		TextView hintText = (TextView)convertView.findViewById(R.id.hintText);
		Icon.setImageResource(item.getIconRes());
		titleName.setText(item.getItemName());
		hintText.setText(String.format(item.getHintStrTail(),item.getHintStrFront()));
		hintText.setVisibility(item.getVilibileFlag());
		
		if(position == 3){
			if(StorageUtil.getInstance(getContext()).isInternalSDMemorySizeAvailable()){
				if(isSpaceLow1G()){
					hintText.setSelected(true);//这样设就会被高亮变红
				}else{
					hintText.setSelected(false);//这样设就不会被高亮变红
				}
			}else{
				hintText.setText(R.string.storage_not_available);				
			}					
		}else{
			hintText.setSelected(false);//这样设就不会被高亮变红
		}
		return convertView;
	}
	
	/**
	 * 存储空间是否少于1G
	 * @return
	 */
	private boolean isSpaceLow1G(){	
    	long internalAvailable = StorageUtil.getInstance(getContext()).getAvailableInternalMemorySize();
		long externalAvailable = StorageUtil.getInstance(getContext()).getAvailableExternalMemorySize();
		if(internalAvailable == StorageUtil.ERROR) internalAvailable = 0;
		if(externalAvailable == StorageUtil.ERROR) externalAvailable = 0;				
		long available = internalAvailable+externalAvailable;
		
		if(available<1024*1024*1024){
			return true;
		}else{
			return false;
		}
	}
}