/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.contacts.list;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.list.ContactTileAdapter.ContactEntry;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * A ContactTile displays the contact's picture overlayed with their name
 */
public class ContactTileView extends FrameLayout {
    private final static String TAG = ContactTileView.class.getSimpleName();

    private Uri mLookupUri;
    private ImageView mPhoto;
    private QuickContactBadge mQuickContact;
    private TextView mName;
    private TextView mStatus;
    private TextView mPhoneLabel;
    private TextView mPhoneNumber;
    private ContactPhotoManager mPhotoManager = null;
    private View mPushState;
    private View mHorizontalDivider;
    private Listener mListener;
	// gionee xuhz 20121206 add for GIUI2.0 start
    private ImageView mCollectIcon;
    private String mDisplayName;
	// gionee xuhz 20121206 add for GIUI2.0 end
    
    // gionee xuhz 20130117 add for CR00765218 start
	private int mSimIndex;
	// gionee xuhz 20130117 add for CR00765218 end
	
    public ContactTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mName = (TextView) findViewById(R.id.contact_tile_name);

        mQuickContact = (QuickContactBadge) findViewById(R.id.contact_tile_quick);
        mPhoto = (ImageView) findViewById(R.id.contact_tile_image);
        mStatus = (TextView) findViewById(R.id.contact_tile_status);
        mPhoneLabel = (TextView) findViewById(R.id.contact_tile_phone_type);
        mPhoneNumber = (TextView) findViewById(R.id.contact_tile_phone_number);
        mPushState = findViewById(R.id.contact_tile_push_state);
        mHorizontalDivider = findViewById(R.id.contact_tile_horizontal_divider);

    	// gionee xuhz 20121206 add for GIUI2.0 start
    	if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
    		mCollectIcon = (ImageView) findViewById(R.id.gn_contacts_collect_icon);
    	}
    	// gionee xuhz 20121206 add for GIUI2.0 end
    	
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(ContactTileView.this);
                }
            }
        };

        
    	// gionee xuhz 20121206 add for GIUI2.0 start
        OnLongClickListener longClicklistener = new OnLongClickListener() {
			public boolean onLongClick(View v) {
				return mListener.onLongClick(ContactTileView.this);
			}
        };
    	// gionee xuhz 20121206 add for GIUI2.0 end
    	
        if(mPushState != null) {
            mPushState.setOnClickListener(listener);
            
        	// gionee xuhz 20121206 add for GIUI2.0 start
        	if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
        		mPushState.setOnLongClickListener(longClicklistener);
        	}
        	// gionee xuhz 20121206 add for GIUI2.0 end
        } else {
            setOnClickListener(listener);
            
            //Goinee:huangzy 20130320 modify for CR00786812 start
            if (ContactsApplication.sIsGroupMemberLongClickSupport) {
                // gionee xuhz 20130117 add for CR00765218 start
                setOnLongClickListener(longClicklistener);
                // gionee xuhz 20130117 add for CR00765218 end            	
            }
            //Goinee:huangzy 20130320 modify for CR00786812 end
        }
    }

    public void setPhotoManager(ContactPhotoManager photoManager) {
        mPhotoManager = photoManager;
    }

    /**
     * Populates the data members to be displayed from the
     * fields in {@link ContactEntry}
     */
    public void loadFromContact(ContactEntry entry) {
        if (entry != null) {
            mName.setText(entry.name);
            mLookupUri = entry.lookupKey;

            if (mStatus != null) {
                if (entry.status == null) {
                    mStatus.setVisibility(View.GONE);
                } else {
                    mStatus.setText(entry.status);
                    mStatus.setCompoundDrawablesWithIntrinsicBounds(entry.presenceIcon,
                            null, null, null);
                    mStatus.setVisibility(View.VISIBLE);
                }
            }

            if (mPhoneLabel != null) {
                mPhoneLabel.setText(entry.phoneLabel);
            }

            if (mPhoneNumber != null) {
                // TODO: Format number correctly
                mPhoneNumber.setText(entry.phoneNumber);
            }

            setVisibility(View.VISIBLE);

            if (mPhotoManager != null) {
                if (mPhoto != null) {
                    mPhotoManager.loadPhoto(mPhoto, entry.photoUri, isDefaultIconHires(),
                            isDarkTheme());

                    if (mQuickContact != null) {
                        mQuickContact.assignContactUri(mLookupUri);
                    }
                } else if (mQuickContact != null) {
                    mQuickContact.assignContactUri(mLookupUri);
                    mPhotoManager.loadPhoto(mQuickContact, entry.photoUri, isDefaultIconHires(),
                            isDarkTheme());
                }

            } else {
                Log.w(TAG, "contactPhotoManager not set");
            }

            if (mPushState != null) {
                mPushState.setContentDescription(entry.name);
            } else if (mQuickContact != null) {
                mQuickContact.setContentDescription(entry.name);
            }
        } else {
            setVisibility(View.INVISIBLE);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setHorizontalDividerVisibility(int visibility) {
        /*if (mHorizontalDivider != null) mHorizontalDivider.setVisibility(visibility);*/
    }

    public Uri getLookupUri() {
        return mLookupUri;
    }

    public String getDisplayName() {
        return mDisplayName;
    }
    
    protected boolean isDefaultIconHires() {
        return false;
    }

    protected boolean isDarkTheme() {
        return false;
    }

    public interface Listener {
        void onClick(ContactTileView contactTileView);
    	// gionee xuhz 20121206 add for GIUI2.0 start
		boolean onLongClick(ContactTileView contactTileView);
    	// gionee xuhz 20121206 add for GIUI2.0 end
    }
    
    //************************follow lines are Gionee*********************
    public void gnLoadFromContact(ContactEntry entry, int childIndex, boolean fouceTagNull) {

        if (entry != null) {
        	// gionee xuhz 20130117 add for CR00765218 start
        	setSimIndex(entry.indexSim);
        	// gionee xuhz 20130117 add for CR00765218 end
        	mDisplayName = entry.name;
            mName.setText(entry.name);
            mLookupUri = entry.lookupKey;

            if (mStatus != null) {
                if (entry.status == null) {
                    mStatus.setVisibility(View.GONE);
                } else {
                    mStatus.setText(entry.status);
                    mStatus.setCompoundDrawablesWithIntrinsicBounds(entry.presenceIcon,
                            null, null, null);
                    mStatus.setVisibility(View.VISIBLE);
                }
            }
            
            if (mPhoneNumber != null) {
                mPhoneNumber.setText(entry.phoneNumber);
            }

            if (mPhoneLabel != null) {
                mPhoneLabel.setText(entry.area);
            }

            setVisibility(View.VISIBLE);

        	// gionee xuhz 20121206 modify for GIUI2.0 start
        	if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
    			if (mCollectIcon != null) {
            		if (entry.photoUri != null && entry.photoUri.toString().equals("content://add_starred")) {
            			mCollectIcon.setVisibility(View.GONE);
            		} else {
            			mCollectIcon.setVisibility(View.VISIBLE);
            		}
    			}
        	}
        	// gionee xuhz 20121206 modify for GIUI2.0 end
        	
            if (mHidePhoto) {
            	if (mPhoto != null) {
            		mPhoto.setVisibility(View.GONE);
            	} else if (mQuickContact != null) {
            		mQuickContact.setVisibility(View.GONE);
            	}

            	if (mName != null) {
            		mName.setPadding(ResConstant.sListItemTextLeftPaddingNoPhoto, 0, 0, 0);
                }
            } else {
            	if (mPhotoManager != null) {
                	//Gionee:huangzy 20121212 modify for CR00742116 start
                	ImageView photo = mPhoto != null ? mPhoto : mQuickContact;
                	if (mQuickContact != null) {
                        mQuickContact.assignContactUri(mLookupUri);
                    }
                	
                    if (null != photo) {
                    	ContactPhotoManager.setContactPhotoViewTag(photo, entry.name, childIndex, fouceTagNull);

                    	if (entry.photoId > 0) {
                        	mPhotoManager.loadPhoto(photo, entry.photoId, isDefaultIconHires(),
                        			ContactsApplication.sIsGnDarkStyle);
                        } else {
                        	mPhotoManager.loadPhoto(photo, entry.photoUri, isDefaultIconHires(),
                        			ContactsApplication.sIsGnDarkStyle);	
                        }
                    }
                    //Gionee:huangzy 20121212 modify for CR00742116 end
                } else {
                    Log.w(TAG, "contactPhotoManager not set");
                }            	
            }

            if (mPushState != null) {
                mPushState.setContentDescription(entry.name);
            } else if (mQuickContact != null) {
                mQuickContact.setContentDescription(entry.name);
            }
        } else {
            setVisibility(View.INVISIBLE);
        }
    }
    
    private boolean mHidePhoto = false;
    public void setHidePhoto(boolean hide) {
    	mHidePhoto = hide;
    }

	// gionee xuhz 20130117 add for CR00765218 start
	public void setSimIndex(int simIndex) {
		this.mSimIndex = simIndex;
	}

	public int getSimIndex() {
		return mSimIndex;
	}
	// gionee xuhz 20130117 add for CR00765218 end
}