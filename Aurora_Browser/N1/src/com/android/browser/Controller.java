/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.browser;


import android.view.View.OnClickListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Browser;
import android.provider.BrowserContract;
import android.provider.BrowserContract.Images;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Intents.Insert;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraEditText;

import com.android.browser.BookmarksFolderActivity.FolderInfo;
import com.android.browser.IntentHandler.UrlData;
import com.android.browser.UI.ComboViews;
import com.android.browser.provider.BrowserProvider2.Thumbnails;
import com.android.browser.provider.SnapshotProvider.Snapshots;
import com.android.phase1.AuroraDataController;
import com.android.phase1.cinema.AddBookmarkViewAnim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controller for browser
 */
public class Controller
        implements WebViewController, UiController, ActivityController {

    private static final String LOGTAG = "Controller";
    private static final String SEND_APP_ID_EXTRA =
        "android.speech.extras.SEND_APPLICATION_ID_EXTRA";
    private static final String INCOGNITO_URI = "browser:incognito";

    // public message ids
    public final static int LOAD_URL = 1001;
    public final static int STOP_LOAD = 1002;

    // Message Ids
    private static final int FOCUS_NODE_HREF = 102;
    private static final int RELEASE_WAKELOCK = 107;

    static final int UPDATE_BOOKMARK_THUMBNAIL = 108;
    static final int SAVE_BOOKMARK = 109;
    
    

    private static final int OPEN_BOOKMARKS = 201;

    private static final int EMPTY_MENU = -1;

    // activity requestCode
    final static int COMBO_VIEW = 1;
    final static int PREFERENCES_PAGE = 3;
    final static int FILE_SELECTED = 4;
    final static int VOICE_RESULT = 6;
    final static int SELECT_FOLDER_RESULT = 7;

    private final static int WAKELOCK_TIMEOUT = 5 * 60 * 1000; // 5 minutes

    // As the ids are dynamically created, we can't guarantee that they will
    // be in sequence, so this static array maps ids to a window number.
    final static private int[] WINDOW_SHORTCUT_ID_ARRAY =
    { R.id.window_one_menu_id, R.id.window_two_menu_id,
      R.id.window_three_menu_id, R.id.window_four_menu_id,
      R.id.window_five_menu_id, R.id.window_six_menu_id,
      R.id.window_seven_menu_id, R.id.window_eight_menu_id };

    // "source" parameter for Google search through search key
    final static String GOOGLE_SEARCH_SOURCE_SEARCHKEY = "browser-key";
    // "source" parameter for Google search through simplily type
    final static String GOOGLE_SEARCH_SOURCE_TYPE = "browser-type";

    // "no-crash-recovery" parameter in intent to suppress crash recovery
    final static String NO_CRASH_RECOVERY = "no-crash-recovery";

    // A bitmap that is re-used in createScreenshot as scratch space
    private static Bitmap sThumbnailBitmap;

    private Activity mActivity;
    private UI mUi;
    private TabControl mTabControl;
    private BrowserSettings mSettings;
    private WebViewFactory mFactory;

    private WakeLock mWakeLock;

    private UrlHandler mUrlHandler;
    private UploadHandler mUploadHandler;
    private IntentHandler mIntentHandler;
    private PageDialogsHandler mPageDialogsHandler;
    private NetworkStateHandler mNetworkHandler;

    private Message mAutoFillSetupMessage;

    private boolean mShouldShowErrorConsole;

    private SystemAllowGeolocationOrigins mSystemAllowGeolocationOrigins;

    // FIXME, temp address onPrepareMenu performance problem.
    // When we move everything out of view, we should rewrite this.
    private int mCurrentMenuState = 0;
    private int mMenuState = R.id.MAIN_MENU;
    private int mOldMenuState = EMPTY_MENU;
    private Menu mCachedMenu;

    private boolean mMenuIsDown;

    // For select and find, we keep track of the ActionMode so that
    // finish() can be called as desired.
    private ActionMode mActionMode;

    /**
     * Only meaningful when mOptionsMenuOpen is true.  This variable keeps track
     * of whether the configuration has changed.  The first onMenuOpened call
     * after a configuration change is simply a reopening of the same menu
     * (i.e. mIconView did not change).
     */
    private boolean mConfigChanged;

    /**
     * Keeps track of whether the options menu is open. This is important in
     * determining whether to show or hide the title bar overlay
     */
    private boolean mOptionsMenuOpen;

    /**
     * Whether or not the options menu is in its bigger, popup menu form. When
     * true, we want the title bar overlay to be gone. When false, we do not.
     * Only meaningful if mOptionsMenuOpen is true.
     */
    private boolean mExtendedMenuOpen;

    private boolean mActivityPaused = true;
    private boolean mLoadStopped;

    private Handler mHandler;
    // Checks to see when the bookmarks database has changed, and updates the
    // Tabs' notion of whether they represent bookmarked sites.
    private ContentObserver mBookmarksObserver;
    private CrashRecoveryHandler mCrashRecoveryHandler;

    private boolean mBlockEvents;

    private String mVoiceResult;
    
    //--------------------------------
    ArrayList<String> tabsUrl;

    
    private TextView tvSaveFolder;
    protected String selectedFolderName;
    protected long selectedFolderId;
    
    private ImageView ivBookmarkThumb;
    private HashMap<String,String> urlTitleMap = new HashMap<String, String>();
    private HashMap<String, SoftReference<Bitmap>> iconMap = new HashMap<String, SoftReference<Bitmap>>();
    private ImageView toolbarBookmark;
    private LinearLayout addBookmarkContainer;
    private LinearLayout addBookmarkView;
    private AddBookmarkViewAnim bookmarkViewAnim;
    private AuroraEditText etBookmarkTitle;
    private AuroraEditText etBookmarkUrl;
    private Button btnCancleBkmk;
    private Button btnSaveBkmk;
    //添加书签对话框是否下移并且隐藏
    private boolean addBkmkViewDownHide;
    private boolean addBkmkViewTransHide;
    private boolean registeredReceiver;
    
    public int addbookmarkBg;
    public int addbookmarkSrc;
    
    public static boolean screenPortrait = true;
    
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(addBookmarkViewIsShowing()) {
				String action = intent.getAction();
				if(action.equals(Intent.ACTION_INPUT_METHOD_SHOW)) {
					if(!AddBookmarkViewAnim.keyboardShowing) {
						if(BaseUi.isScreenOriatationPortrait(mActivity)) {
							bookmarkViewAnim.startAnim(AddBookmarkViewAnim.NORMAL_UP);
						}
						AddBookmarkViewAnim.keyboardShowing = true;
					}
				}else if(action.equals(Intent.ACTION_INPUT_METHOD_HIDE)) {
					if(addBkmkViewTransHide) {
						AddBookmarkViewAnim.keyboardShowing = false;
						addBkmkViewTransHide = false;
						return;
					}
					if(AddBookmarkViewAnim.keyboardShowing) {
						if(BaseUi.isScreenOriatationPortrait(mActivity)) {
							bookmarkViewAnim.startAnim(AddBookmarkViewAnim.NORMAL_DOWN);
						}
						AddBookmarkViewAnim.keyboardShowing = false;
						addBkmkViewDownHide = false;
					}
				}
			}
		}
		
	};
    
    public Controller(Activity browser) {
    	tabsUrl=new ArrayList<String>();
        mActivity = browser;
        mSettings = BrowserSettings.getInstance();
        mTabControl = new TabControl(this);
        mSettings.setController(this);
        mCrashRecoveryHandler = CrashRecoveryHandler.initialize(this);
        mCrashRecoveryHandler.preloadCrashState();
        mFactory = new BrowserWebViewFactory(browser);

        mUrlHandler = new UrlHandler(this);
        mIntentHandler = new IntentHandler(mActivity, this);
        mPageDialogsHandler = new PageDialogsHandler(mActivity, this);

        startHandler();
        mBookmarksObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                int size = mTabControl.getTabCount();
                for (int i = 0; i < size; i++) {
                    mTabControl.getTab(i).updateBookmarkedStatus();
                }
            }

        };
        browser.getContentResolver().registerContentObserver(
                BrowserContract.Bookmarks.CONTENT_URI, true, mBookmarksObserver);

        mNetworkHandler = new NetworkStateHandler(mActivity, this);
        // Start watching the default geolocation permissions
        mSystemAllowGeolocationOrigins =
                new SystemAllowGeolocationOrigins(mActivity.getApplicationContext());
        mSystemAllowGeolocationOrigins.start();

        openIconDatabase();
        
        setDefaultBookmarkSaveFolder();
    }

    @Override
    public void start(final Intent intent) {
        // mCrashRecoverHandler has any previously saved state.
        mCrashRecoveryHandler.startRecovery(intent);
    }

    void doStart(final Bundle icicle, final Intent intent) {
        // Unless the last browser usage was within 24 hours, destroy any
        // remaining incognito tabs.

        Calendar lastActiveDate = icicle != null ?
                (Calendar) icicle.getSerializable("lastActiveDate") : null;
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        final boolean restoreIncognitoTabs = !(lastActiveDate == null
            || lastActiveDate.before(yesterday)
            || lastActiveDate.after(today));

        // Find out if we will restore any state and remember the tab.
        final long currentTabId =
                mTabControl.canRestoreState(icicle, restoreIncognitoTabs);

        if (currentTabId == -1) {
            // Not able to restore so we go ahead and clear session cookies.  We
            // must do this before trying to login the user as we don't want to
            // clear any session cookies set during login.
            CookieManager.getInstance().removeSessionCookie();
        }

        GoogleAccountLogin.startLoginIfNeeded(mActivity,
                new Runnable() {
                    @Override public void run() {
                        onPreloginFinished(icicle, intent, currentTabId,
                                restoreIncognitoTabs);
                    }
                });
    }

    private void onPreloginFinished(Bundle icicle, Intent intent, long currentTabId,
            boolean restoreIncognitoTabs) {
        if (currentTabId == -1) {
            BackgroundHandler.execute(new PruneThumbnails(mActivity, null));
            if (intent == null) {
                // This won't happen under common scenarios. The icicle is
                // not null, but there aren't any tabs to restore.
                openTabToHomePage();
            } else {
                final Bundle extra = intent.getExtras();
                // Create an initial tab.
                // If the intent is ACTION_VIEW and data is not null, the Browser is
                // invoked to view the content by another application. In this case,
                // the tab will be close when exit.
                UrlData urlData = IntentHandler.getUrlDataFromIntent(intent);
                Tab t = null;
                if (urlData.isEmpty()) {
                    t = openTabToHomePage();
                } else {
                    t = openTab(urlData);
                }
                if (t != null) {
                    t.setAppId(intent.getStringExtra(Browser.EXTRA_APPLICATION_ID));
                }
                WebView webView = t.getWebView();
                if (extra != null) {
                    int scale = extra.getInt(Browser.INITIAL_ZOOM_LEVEL, 0);
                    if (scale > 0 && scale <= 1000) {
                        webView.setInitialScale(scale);
                    }
                }
            }
            mUi.updateTabs(mTabControl.getTabs());
        } else {
            mTabControl.restoreState(icicle, currentTabId, restoreIncognitoTabs,
                    mUi.needsRestoreAllTabs());
            List<Tab> tabs = mTabControl.getTabs();
            ArrayList<Long> restoredTabs = new ArrayList<Long>(tabs.size());
            for (Tab t : tabs) {
                restoredTabs.add(t.getId());
            }
            BackgroundHandler.execute(new PruneThumbnails(mActivity, restoredTabs));
            if (tabs.size() == 0) {
                openTabToHomePage();
            }
            mUi.updateTabs(tabs);
            // TabControl.restoreState() will create a new tab even if
            // restoring the state fails.
            setActiveTab(mTabControl.getCurrentTab());
            // Intent is non-null when framework thinks the browser should be
            // launching with a new intent (icicle is null).
            if (intent != null) {
                mIntentHandler.onNewIntent(intent);
            }
        }
        // Read JavaScript flags if it exists.
        String jsFlags = getSettings().getJsEngineFlags();
        if (intent != null
                && BrowserActivity.ACTION_SHOW_BOOKMARKS.equals(intent.getAction())) {
            bookmarksOrHistoryPicker(ComboViews.Bookmarks);
        }
    }

    private static class PruneThumbnails implements Runnable {
        private Context mContext;
        private List<Long> mIds;

        PruneThumbnails(Context context, List<Long> preserveIds) {
            mContext = context.getApplicationContext();
            mIds = preserveIds;
        }

        @Override
        public void run() {
            ContentResolver cr = mContext.getContentResolver();
            if (mIds == null || mIds.size() == 0) {
                cr.delete(Thumbnails.CONTENT_URI, null, null);
            } else {
                int length = mIds.size();
                StringBuilder where = new StringBuilder();
                where.append(Thumbnails._ID);
                where.append(" not in (");
                for (int i = 0; i < length; i++) {
                    where.append(mIds.get(i));
                    if (i < (length - 1)) {
                        where.append(",");
                    }
                }
                where.append(")");
                cr.delete(Thumbnails.CONTENT_URI, where.toString(), null);
            }
        }

    }

    @Override
    public WebViewFactory getWebViewFactory() {
        return mFactory;
    }

    @Override
    public void onSetWebView(Tab tab, WebView view) {
        mUi.onSetWebView(tab, view);
    }

    @Override
    public void createSubWindow(Tab tab) {
        endActionMode();
        WebView mainView = tab.getWebView();
        WebView subView = mFactory.createWebView((mainView == null)
                ? false
                : mainView.isPrivateBrowsingEnabled());
        mUi.createSubWindow(tab, subView);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    void setUi(UI ui) {
        mUi = ui;
    }

    @Override
    public BrowserSettings getSettings() {
        return mSettings;
    }

    IntentHandler getIntentHandler() {
        return mIntentHandler;
    }

    @Override
    public UI getUi() {
        return mUi;
    }

    int getMaxTabs() {
        return mActivity.getResources().getInteger(R.integer.max_tabs);
    }

    @Override
    public TabControl getTabControl() {
        return mTabControl;
    }

    @Override
    public List<Tab> getTabs() {
        return mTabControl.getTabs();
    }

    // Open the icon database.
    private void openIconDatabase() {
        // We have to call getInstance on the UI thread
        final WebIconDatabase instance = WebIconDatabase.getInstance();
        BackgroundHandler.execute(new Runnable() {

            @Override
            public void run() {
                instance.open(mActivity.getDir("icons", 0).getPath());
            }
        });
    }

    private void startHandler() {
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case OPEN_BOOKMARKS:
                        bookmarksOrHistoryPicker(ComboViews.Bookmarks);
                        break;
                    case FOCUS_NODE_HREF:
                    {                    	
                        String url = (String) msg.getData().get("url");
                        String title = (String) msg.getData().get("title");
                        String src = (String) msg.getData().get("src");                      
                        if ((url == "")||url==null) url = src; // use image if no anchor
                        if (TextUtils.isEmpty(url)) {                       	
                            break;
                        }
                        HashMap focusNodeMap = (HashMap) msg.obj;
                        WebView view = (WebView) focusNodeMap.get("webview");
                        // Only apply the action if the top window did not change.
                        if (getCurrentTopWebView() != view) {                        	
                            break;
                        }
                        switch (msg.arg1) {
                            case R.id.open_context_menu_id:
                                loadUrlFromContext(url);
                                break;
//                            case R.id.view_image_context_menu_id:
//                                loadUrlFromContext(src);
//                                break;
                            case R.id.anchor_open_newtab_menu_id:
                          
                                final Tab parent = mTabControl.getCurrentTab();
                              
                                openTab(url, parent,
                                       true, true);
                                break;
                            case R.id.anchor_cope_link_menu_id:                           	
                                copy(url);
                                break;
                            case R.id.image_cope_image_url_menu_id:                           
                            	 copy(url);
                            	break;
                            case R.id.kong_cope_tab_menu_id:
                           	    copy(url);
                             	break;
                         //   case R.id.save_link_context_menu_id:
                            case R.id.image_save_image_menu_id:
                                DownloadHandler.onDownloadStartNoStream(
                                        mActivity, url, view.getSettings().getUserAgentString(),
                                        null, null, null, view.isPrivateBrowsingEnabled());
                                break;
                        }
                        break;
                    }

                    case LOAD_URL:
                        loadUrlFromContext((String) msg.obj);
                        break;

                    case STOP_LOAD:
                        stopLoading();
                        break;

                    case RELEASE_WAKELOCK:
                        if (mWakeLock != null && mWakeLock.isHeld()) {
                            mWakeLock.release();
                            // if we reach here, Browser should be still in the
                            // background loading after WAKELOCK_TIMEOUT (5-min).
                            // To avoid burning the battery, stop loading.
                            mTabControl.stopAllLoading();
                        }
                        break;

                    case UPDATE_BOOKMARK_THUMBNAIL:
                        Tab tab = (Tab) msg.obj;
                        if (tab != null) {
                            updateScreenshot(tab);
                        }
                        break;
                        
                    case SAVE_BOOKMARK:
                    	if(msg.arg1 == 1) {
//                    		Toast.makeText(getContext(), getActivity().getResources().getString(R.string.bookmark_saved), Toast.LENGTH_SHORT).show();
                    	}else {
                    		Toast.makeText(getContext(), getActivity().getResources().getString(R.string.bookmark_not_saved), Toast.LENGTH_SHORT).show();
                    		
                    	}
                    	break;
                }
            }
        };

    }

    @Override
    public Tab getCurrentTab() {
        return mTabControl.getCurrentTab();
    }

    @Override
    public void shareCurrentPage() {
        shareCurrentPage(mTabControl.getCurrentTab());
    }

    private void shareCurrentPage(Tab tab) {
        if (tab != null) {
            sharePage(mActivity, tab.getTitle(),
                    tab.getUrl(), tab.getFavicon(),
                    createScreenshot(tab.getWebView(),
                            getDesiredThumbnailWidth(mActivity),
                            getDesiredThumbnailHeight(mActivity)));
        }
    }

    /**
     * Share a page, providing the title, url, favicon, and a screenshot.  Uses
     * an {@link Intent} to launch the Activity chooser.
     * @param c Context used to launch a new Activity.
     * @param title Title of the page.  Stored in the Intent with
     *          {@link Intent#EXTRA_SUBJECT}
     * @param url URL of the page.  Stored in the Intent with
     *          {@link Intent#EXTRA_TEXT}
     * @param favicon Bitmap of the favicon for the page.  Stored in the Intent
     *          with {@link Browser#EXTRA_SHARE_FAVICON}
     * @param screenshot Bitmap of a screenshot of the page.  Stored in the
     *          Intent with {@link Browser#EXTRA_SHARE_SCREENSHOT}
     */
    static final void sharePage(Context c, String title, String url,
            Bitmap favicon, Bitmap screenshot) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, url);
        send.putExtra(Intent.EXTRA_SUBJECT, title);
        send.putExtra(Browser.EXTRA_SHARE_FAVICON, favicon);
        send.putExtra(Browser.EXTRA_SHARE_SCREENSHOT, screenshot);
        try {
            c.startActivity(Intent.createChooser(send, c.getString(
                    R.string.choosertitle_sharevia)));
        } catch(android.content.ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }

    private void copy(CharSequence text) {
        ClipboardManager cm = (ClipboardManager) mActivity
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    // lifecycle

    @Override
    public void onConfgurationChanged(Configuration config) {
        mConfigChanged = true;
        // update the menu in case of a locale change
        mActivity.invalidateOptionsMenu();
        if (mPageDialogsHandler != null) {
            mPageDialogsHandler.onConfigurationChanged(config);
        }
        screenPortrait = (config.orientation == Configuration.ORIENTATION_PORTRAIT);
        if(addBookmarkViewIsShowing()) {
        	handlerAddBkmkViewState(config);
        }
        mUi.onConfigurationChanged(config);
    }

    private void handlerAddBkmkViewState(Configuration config) {
    	if(config.orientation == Configuration.ORIENTATION_PORTRAIT) {
    		//竖屏，如果有键盘弹出，则上移
    		if(AddBookmarkViewAnim.keyboardShowing) {
    			bookmarkViewAnim.setViewUp();
    		}
    	}else {
    		//横屏
    		bookmarkViewAnim.setViewCenter();
    	}
	}

	@Override
    public void handleNewIntent(Intent intent) {
        if (!mUi.isWebShowing()) {
            mUi.showWeb(false);
        }
        mIntentHandler.onNewIntent(intent);
    }

    @Override
    public void onPause() {
        if (mUi.isCustomViewShowing()) {
            hideCustomView();
        }
        if (mActivityPaused) {
            Log.e(LOGTAG, "BrowserActivity is already paused.");
            return;
        }
        mActivityPaused = true;
        Tab tab = mTabControl.getCurrentTab();
        if (tab != null) {
            tab.pause();
            if (!pauseWebViewTimers(tab)) {
                if (mWakeLock == null) {
                    PowerManager pm = (PowerManager) mActivity
                            .getSystemService(Context.POWER_SERVICE);
                    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Browser");
                }
                mWakeLock.acquire();
                mHandler.sendMessageDelayed(mHandler
                        .obtainMessage(RELEASE_WAKELOCK), WAKELOCK_TIMEOUT);
            }
        }
        mUi.onPause();
        mNetworkHandler.onPause();

        WebView.disablePlatformNotifications();
        NfcHandler.unregister(mActivity);
        if (sThumbnailBitmap != null) {
            sThumbnailBitmap.recycle();
            sThumbnailBitmap = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save all the tabs
        Bundle saveState = createSaveState();

        // crash recovery manages all save & restore state
        mCrashRecoveryHandler.writeState(saveState);
        mSettings.setLastRunPaused(true);
    }

    /**
     * Save the current state to outState. Does not write the state to
     * disk.
     * @return Bundle containing the current state of all tabs.
     */
    /* package */ Bundle createSaveState() {
        Bundle saveState = new Bundle();
        mTabControl.saveState(saveState);
        if (!saveState.isEmpty()) {
            // Save time so that we know how old incognito tabs (if any) are.
            saveState.putSerializable("lastActiveDate", Calendar.getInstance());
        }
        return saveState;
    }

    @Override
    public void onResume() {
        if (!mActivityPaused) {
            Log.e(LOGTAG, "BrowserActivity is already resumed.");
            return;
        }
        mSettings.setLastRunPaused(false);
        mActivityPaused = false;
        Tab current = mTabControl.getCurrentTab();
        if (current != null) {
            current.resume();
            resumeWebViewTimers(current);
        }
        releaseWakeLock();

        mUi.onResume();
        mNetworkHandler.onResume();
        WebView.enablePlatformNotifications();
        NfcHandler.register(mActivity, this);
        if (mVoiceResult != null) {
            mUi.onVoiceResult(mVoiceResult);
            mVoiceResult = null;
        }
        if(((PhoneUi)mUi).mShowNav) {
        	BaseUi.changeStatusBar(mActivity, false);
        }else {
//        	mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        	BaseUi.changeStatusBar(mActivity, true);
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mHandler.removeMessages(RELEASE_WAKELOCK);
            mWakeLock.release();
        }
    }

    /**
     * resume all WebView timers using the WebView instance of the given tab
     * @param tab guaranteed non-null
     */
    private void resumeWebViewTimers(Tab tab) {
        boolean inLoad = tab.inPageLoad();
        if ((!mActivityPaused && !inLoad) || (mActivityPaused && inLoad)) {
            CookieSyncManager.getInstance().startSync();
            WebView w = tab.getWebView();
            WebViewTimersControl.getInstance().onBrowserActivityResume(w);
        }
    }

    /**
     * Pause all WebView timers using the WebView of the given tab
     * @param tab
     * @return true if the timers are paused or tab is null
     */
    private boolean pauseWebViewTimers(Tab tab) {
        if (tab == null) {
            return true;
        } else if (!tab.inPageLoad()) {
            CookieSyncManager.getInstance().stopSync();
            WebViewTimersControl.getInstance().onBrowserActivityPause(getCurrentWebView());
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        if (mUploadHandler != null && !mUploadHandler.handled()) {
            mUploadHandler.onResult(Activity.RESULT_CANCELED, null);
            mUploadHandler = null;
        }
        if (mTabControl == null) return;
        mUi.onDestroy();
        // Remove the current tab and sub window
        Tab t = mTabControl.getCurrentTab();
        if (t != null) {
            dismissSubWindow(t);
            removeTab(t);
        }
        mActivity.getContentResolver().unregisterContentObserver(mBookmarksObserver);
        // Destroy all the tabs
        mTabControl.destroy();
        WebIconDatabase.getInstance().close();
        // Stop watching the default geolocation permissions
        mSystemAllowGeolocationOrigins.stop();
        mSystemAllowGeolocationOrigins = null;
    }

    protected boolean isActivityPaused() {
        return mActivityPaused;
    }

    @Override
    public void onLowMemory() {
        mTabControl.freeMemory();
    }

    @Override
    public boolean shouldShowErrorConsole() {
        return mShouldShowErrorConsole;
    }

    protected void setShouldShowErrorConsole(boolean show) {
        if (show == mShouldShowErrorConsole) {
            // Nothing to do.
            return;
        }
        mShouldShowErrorConsole = show;
        Tab t = mTabControl.getCurrentTab();
        if (t == null) {
            // There is no current tab so we cannot toggle the error console
            return;
        }
        mUi.setShouldShowErrorConsole(t, show);
    }
    @Override
    public void setShouldShowErrorView(boolean show){
    	 Tab t = mTabControl.getCurrentTab();
         if (t == null) {
             // There is no current tab so we cannot toggle the error console
             return;
         }
    	 mUi.setShouldShowErrorView(t, show);
    }
    
    @Override
	public Bitmap getErrorBitmap(Tab tab, int mCaptureWidth, int mCaptureHeight, int mainViewWidth) {
		return mUi.getErrorBitmap(tab, mCaptureWidth, mCaptureHeight, mainViewWidth);
	}
    
    @Override
    public boolean getErrorViewState(){
    	
    	return  mUi.getErrorViewState();
    }
    public void stopLoading() {
        mLoadStopped = true;
        Tab tab = mTabControl.getCurrentTab();
        WebView w = getCurrentTopWebView();
        if (w != null) {
        	tab.setmLoadStopped(mLoadStopped);
            w.stopLoading();
            mUi.onPageStopped(tab);
        }
    }

    boolean didUserStopLoading() {
        return mLoadStopped;
    }

    // WebViewController

    @Override
    public void onPageStarted(Tab tab, WebView view, Bitmap favicon) {
    	mUi.onShowTitleBar();
        // We've started to load a new page. If there was a pending message
        // to save a screenshot then we will now take the new page and save
        // an incorrect screenshot. Therefore, remove any pending thumbnail
        // messages from the queue.
        mHandler.removeMessages(Controller.UPDATE_BOOKMARK_THUMBNAIL,
                tab);

        // reset sync timer to avoid sync starts during loading a page
        CookieSyncManager.getInstance().resetSync();

        if (!mNetworkHandler.isNetworkUp()) {
            view.setNetworkAvailable(false);
        }

        // when BrowserActivity just starts, onPageStarted may be called before
        // onResume as it is triggered from onCreate. Call resumeWebViewTimers
        // to start the timer. As we won't switch tabs while an activity is in
        // pause state, we can ensure calling resume and pause in pair.
        if (mActivityPaused) {
            resumeWebViewTimers(tab);
        }
        mLoadStopped = false;
        endActionMode();

        mUi.onTabDataChanged(tab);
      

        String url = tab.getUrl();
        // update the bookmark database for favicon
//        maybeUpdateFavicon(tab, null, url, favicon);

        Performance.tracePageStart(url);

        // Performance probe
        if (false) {
            Performance.onPageStarted();
        }

    }

    @Override
    public void onPageFinished(Tab tab) {
    	Log.i("xie", "-----------------onPageFinished---"+tab.getUrl());
    	if(!mLoadStopped) {
    		if(!tab.isError()) {
    			tab.setmLastSucUrl(tab.getUrl());
    			putTitleToMap(tab);
    		}
    		if(etBookmarkUrl != null) {
        		if(!tab.getUrl().trim().equals(etBookmarkUrl.getText().toString().trim())) {
        			setCrtBookmarkDialog();
        		}
    		}

    	}else {
    		mLoadStopped = false;
    	}
        mCrashRecoveryHandler.backupState();
        mUi.onTabDataChanged(tab);

        // Performance probe
        if (false) {
            Performance.onPageFinished(tab.getUrl());
        }

        Performance.tracePageFinished();
    }

    private void putTitleToMap(Tab tab) {
    	if(!urlTitleMap.containsKey(tab.getUrl()) 
		    || (urlTitleMap.get(tab.getUrl()).equals("")) 
			|| (urlTitleMap.get(tab.getUrl()).equals(mActivity.getResources().getString(R.string.no_title)))) 
    	{
    		String title = "";
    		if(tab.getTitle() == null) {
    			title = tab.getUrl();
    		}else {
    			if(tab.getTitle().equals(mActivity.getResources().getString(R.string.cannot_find_the_webpage))) {
    				title = mActivity.getResources().getString(R.string.no_title);
    			}else if(tab.getTitle().equals(mActivity.getResources().getString(R.string.title_bar_loading))) {
    				title = tab.getUrl();
    			}else {
    				title = tab.getTitle();
    			}
    		}
			urlTitleMap.put(tab.getUrl(),title);
		}
	}

	@Override
    public void onProgressChanged(Tab tab) {
        int newProgress = tab.getLoadProgress();
        if (newProgress == 100) {
            CookieSyncManager.getInstance().sync();
            // onProgressChanged() may continue to be called after the main
            // frame has finished loading, as any remaining sub frames continue
            // to load. We'll only get called once though with newProgress as
            // 100 when everything is loaded. (onPageFinished is called once
            // when the main frame completes loading regardless of the state of
            // any sub frames so calls to onProgressChanges may continue after
            // onPageFinished has executed)
            if (tab.inPageLoad()) {
                updateInLoadMenuItems(mCachedMenu, tab);
            } else if (mActivityPaused && pauseWebViewTimers(tab)) {
                // pause the WebView timer and release the wake lock if it is
                // finished while BrowserActivity is in pause state.
                releaseWakeLock();
            }
            if (!tab.isPrivateBrowsingEnabled()
                    && !TextUtils.isEmpty(tab.getUrl())
                    && !tab.isSnapshot()) {
                // Only update the bookmark screenshot if the user did not
                // cancel the load early and there is not already
                // a pending update for the tab.
                if (tab.shouldUpdateThumbnail() &&
                        (tab.inForeground() && !didUserStopLoading()
                        || !tab.inForeground())) {
                    if (!mHandler.hasMessages(UPDATE_BOOKMARK_THUMBNAIL, tab)) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(
                                UPDATE_BOOKMARK_THUMBNAIL, 0, 0, tab),
                                500);
                    }
                }
            }
        } else {
            if (!tab.inPageLoad()) {
                // onPageFinished may have already been called but a subframe is
                // still loading
                // updating the progress and
                // update the menu items.
                updateInLoadMenuItems(mCachedMenu, tab);
            }
        }
        mUi.onProgressChanged(tab);
    }

    @Override
    public void onUpdatedSecurityState(Tab tab) {
    	Log.i("xie", "-----------------onUpdatedSecurityState---"+tab.getUrl());
        mUi.onTabDataChanged(tab);
    }
   
    @Override
    public void onReceivedTitle(Tab tab, final String title) {
    	
        mUi.onTabDataChanged(tab);
        final String pageUrl = tab.getOriginalUrl();
        if (TextUtils.isEmpty(pageUrl) || pageUrl.length()
                >= SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH) {
            return;
        }
        // Update the title in the history database if not in private browsing mode
        if (!tab.isPrivateBrowsingEnabled()) {
            DataController.getInstance(mActivity, AuroraDataController.class.getName()).updateHistoryTitle(pageUrl, title);
            putTitleToMap(tab);
        }
    }

    @Override
    public void onFavicon(Tab tab, WebView view, Bitmap icon) {
    	if(!iconMap.containsKey(tab.getUrl().trim()) || (iconMap.get(tab.getUrl().trim()).get() == null)) {
    		iconMap.put(tab.getUrl().trim(), new SoftReference<Bitmap>(icon));
    	}
    	if(etBookmarkUrl != null) {
    		setCrtBookmarkDialog();
    	}
        mUi.onTabDataChanged(tab);
        maybeUpdateFavicon(tab, view.getOriginalUrl(), view.getUrl(), icon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(Tab tab, WebView view, String url) {
        return mUrlHandler.shouldOverrideUrlLoading(tab, view, url);
    }

    @Override
    public boolean shouldOverrideKeyEvent(KeyEvent event) {
        if (mMenuIsDown) {
            // only check shortcut key when MENU is held
            return mActivity.getWindow().isShortcutKey(event.getKeyCode(),
                    event);
        } else {
            return false;
        }
    }

    @Override
    public boolean onUnhandledKeyEvent(KeyEvent event) {
        if (!isActivityPaused()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                return mActivity.onKeyDown(event.getKeyCode(), event);
            } else {
                return mActivity.onKeyUp(event.getKeyCode(), event);
            }
        }
        return false;
    }

    @Override
    public void doUpdateVisitedHistory(Tab tab, boolean isReload) {
        // Don't save anything in private browsing mode
        if (tab.isPrivateBrowsingEnabled()) return;
        String url = tab.getOriginalUrl();

        if (TextUtils.isEmpty(url)
                || url.regionMatches(true, 0, "about:", 0, 6)) {
            return;
        }
        DataController.getInstance(mActivity, AuroraDataController.class.getName()).updateVisitedHistory(url);
        mCrashRecoveryHandler.backupState();
    }
    
    @Override
	public void doUpdateVisitedHistory(Tab tab, boolean isReload, String origUrl) {
    	 // Don't save anything in private browsing mode
        if (tab.isPrivateBrowsingEnabled()) return;
        String url = tab.getOriginalUrl();
        
        if (TextUtils.isEmpty(url)
                || url.regionMatches(true, 0, "about:", 0, 6)) {
            return;
        }
        if(!url.equals(origUrl)) {
        	url = origUrl;
        }
        DataController.getInstance(mActivity, AuroraDataController.class.getName()).updateVisitedHistory(url);
        mCrashRecoveryHandler.backupState();
	}

    @Override
    public void getVisitedHistory(final ValueCallback<String[]> callback) {
        AsyncTask<Void, Void, String[]> task =
                new AsyncTask<Void, Void, String[]>() {
            @Override
            public String[] doInBackground(Void... unused) {
                return Browser.getVisitedHistory(mActivity.getContentResolver());
            }
            @Override
            public void onPostExecute(String[] result) {
                callback.onReceiveValue(result);
            }
        };
        task.execute();
    }

    @Override
    public void onReceivedHttpAuthRequest(Tab tab, WebView view,
            final HttpAuthHandler handler, final String host,
            final String realm) {
        String username = null;
        String password = null;

        boolean reuseHttpAuthUsernamePassword
                = handler.useHttpAuthUsernamePassword();

        if (reuseHttpAuthUsernamePassword && view != null) {
            String[] credentials = view.getHttpAuthUsernamePassword(host, realm);
            if (credentials != null && credentials.length == 2) {
                username = credentials[0];
                password = credentials[1];
            }
        }

        if (username != null && password != null) {
            handler.proceed(username, password);
        } else {
            if (tab.inForeground() && !handler.suppressDialog()) {
                mPageDialogsHandler.showHttpAuthentication(tab, handler, host, realm);
            } else {
                handler.cancel();
            }
        }
    }

    @Override
    public void onDownloadStart(Tab tab, String url, String userAgent,
            String contentDisposition, String mimetype, String referer,
            long contentLength) {
        WebView w = tab.getWebView();
        DownloadHandler.onDownloadStart(mActivity, url, userAgent,
                contentDisposition, mimetype, referer, w.isPrivateBrowsingEnabled());
        if (w.copyBackForwardList().getSize() == 0) {
            // This Tab was opened for the sole purpose of downloading a
            // file. Remove it.
            if (tab == mTabControl.getCurrentTab()) {
                // In this case, the Tab is still on top.
                goBackOnePageOrQuit();
            } else {
                // In this case, it is not.
                closeTab(tab);
            }
        }
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return mUi.getDefaultVideoPoster();
    }

    @Override
    public View getVideoLoadingProgressView() {
        return mUi.getVideoLoadingProgressView();
    }

    @Override
    public void showSslCertificateOnError(WebView view, SslErrorHandler handler,
            SslError error) {
        mPageDialogsHandler.showSSLCertificateOnError(view, handler, error);
    }

    @Override
    public void showAutoLogin(Tab tab) {
        assert tab.inForeground();
        // Update the title bar to show the auto-login request.
        mUi.showAutoLogin(tab);
    }

    @Override
    public void hideAutoLogin(Tab tab) {
        assert tab.inForeground();
        mUi.hideAutoLogin(tab);
    }

    // helper method

    /*
     * Update the favorites icon if the private browsing isn't enabled and the
     * icon is valid.
     */
    private void maybeUpdateFavicon(Tab tab, final String originalUrl,
            final String url, Bitmap favicon) {
        if (favicon == null) {
            return;
        }
        if (!tab.isPrivateBrowsingEnabled()) {
            Bookmarks.updateFavicon(mActivity
                    .getContentResolver(), originalUrl, url, favicon);
        }
    }

    @Override
    public void bookmarkedStatusHasChanged(Tab tab) {
        // TODO: Switch to using onTabDataChanged after b/3262950 is fixed
        mUi.bookmarkedStatusHasChanged(tab);
    }

    // end WebViewController

    protected void pageUp() {
        getCurrentTopWebView().pageUp(false);
    }

    protected void pageDown() {
        getCurrentTopWebView().pageDown(false);
    }

    // callback from phone title bar
    @Override
    public void editUrl() {
        if (mOptionsMenuOpen) mActivity.closeOptionsMenu();
        mUi.editUrl(false, true);
    }

    @Override
    public void showCustomView(Tab tab, View view, int requestedOrientation,
            WebChromeClient.CustomViewCallback callback) {
        if (tab.inForeground()) {
            if (mUi.isCustomViewShowing()) {
                callback.onCustomViewHidden();
                return;
            }
            mUi.showCustomView(view, requestedOrientation, callback);
            // Save the menu state and set it to empty while the custom
            // view is showing.
            mOldMenuState = mMenuState;
            mMenuState = EMPTY_MENU;
            mActivity.invalidateOptionsMenu();
        }
    }

    @Override
    public void hideCustomView() {
        if (mUi.isCustomViewShowing()) {
            mUi.onHideCustomView();
            // Reset the old menu state.
            mMenuState = mOldMenuState;
            mOldMenuState = EMPTY_MENU;
            mActivity.invalidateOptionsMenu();
            BaseUi.changeStatusBar(mActivity, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        if (getCurrentTopWebView() == null) return;
        switch (requestCode) {
            case PREFERENCES_PAGE:
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    String action = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (PreferenceKeys.PREF_PRIVACY_CLEAR_HISTORY.equals(action)) {
                        mTabControl.removeParentChildRelationShips();
                    }
                }
                break;
            case FILE_SELECTED:
                // Chose a file from the file picker.
                if (null == mUploadHandler) break;
                mUploadHandler.onResult(resultCode, intent);
                break;
            case COMBO_VIEW:
                if (intent == null || resultCode != Activity.RESULT_OK) {
                    break;
                }
                mUi.showWeb(false);
                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    Tab t = getCurrentTab();
                    Uri uri = intent.getData();
                    loadUrl(t, uri.toString());
                } else if (intent.hasExtra(ComboViewActivity.EXTRA_OPEN_ALL)) {
                    String[] urls = intent.getStringArrayExtra(
                            ComboViewActivity.EXTRA_OPEN_ALL);
                    Tab parent = getCurrentTab();
                    for (String url : urls) {
                    	Log.i("jie", "------------------3");
                        parent = openTab(url, parent,
                                !mSettings.openInBackground(), true);
                    }
                } else if (intent.hasExtra(ComboViewActivity.EXTRA_OPEN_SNAPSHOT)) {
                    long id = intent.getLongExtra(
                            ComboViewActivity.EXTRA_OPEN_SNAPSHOT, -1);
                    if (id >= 0) {
                        Toast.makeText(mActivity, "Snapshot Tab no longer supported",
                            Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case VOICE_RESULT:
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    ArrayList<String> results = intent.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    if (results.size() >= 1) {
                        mVoiceResult = results.get(0);
                    }
                }
                break;
                
            case SELECT_FOLDER_RESULT:
            	if(resultCode == SELECT_FOLDER_RESULT && intent != null) {
            		selectedFolderId = intent.getLongExtra("selected_id", 1);
            		selectedFolderName = intent.getStringExtra("selected_title");
            		selectedFolderName = (selectedFolderName.equals("Bookmarks") ? mActivity.getResources().getString(R.string.bookmarks) : selectedFolderName);
            		if(tvSaveFolder != null && selectedFolderName != null) {
            			tvSaveFolder.setText(selectedFolderName);
            		}
            	}
            	break;
            default:
                break;
        }
        getCurrentTopWebView().requestFocus();
    }

    /**
     * Open the Go page.
     * @param startWithHistory If true, open starting on the history tab.
     *                         Otherwise, start with the bookmarks tab.
     */
    @Override
    public void bookmarksOrHistoryPicker(ComboViews startView) {
        if (mTabControl.getCurrentWebView() == null) {
            return;
        }
        // clear action mode
        if (isInCustomActionMode()) {
            endActionMode();
        }
        Bundle extras = new Bundle();
        // Disable opening in a new window if we have maxed out the windows
        extras.putBoolean(BrowserBookmarksPage.EXTRA_DISABLE_WINDOW,
                !mTabControl.canCreateNewTab());
        mUi.showComboView(startView, extras);
    }

    // combo view callbacks

    // key handling
    protected void onBackKey() {
        if (!mUi.onBackKey()) {
            WebView subwindow = mTabControl.getCurrentSubWindow();
            if (subwindow != null) {
                if (subwindow.canGoBack()) {
                    subwindow.goBack();
                } else {
                    dismissSubWindow(mTabControl.getCurrentTab());
                }
            } else {
                goBackOnePageOrQuit();
            }
        }
    }

    protected boolean onMenuKey() {
        return mUi.onMenuKey();
    }

    // menu handling and state
    // TODO: maybe put into separate handler

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mMenuState == EMPTY_MENU) {
            return false;
        }
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.browser, menu);
        menu.removeItem(R.id.incognito_menu_id); //add by gary.gou for bug 7597
        return true;
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v instanceof TitleBar) {
			return;
		}
		if (!(v instanceof WebView)) {
			return;
		}
		final WebView webview = (WebView) v;
		WebView.HitTestResult result = webview.getHitTestResult();
		if (result == null) {
			return;
		}

		int type = result.getType();

		Log.i("xxxx", "__________result.getType();__________________" + type);
		 if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
			 Log.i("xxxx", "__________result.getType();_________________111_" + type);
		 Log.w(LOGTAG,
		 "We should not show context menu when nothing is touched");
		 return;
		 }
		 
		if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
			Log.i("xxxx", "__________result.getType();_________222_________" + type);
			// let TextView handles context menu
			return;
		}

		// Note, http://b/issue?id=1106666 is requesting that
		// an inflated menu can be used again. This is not available
		// yet, so inflate each time (yuk!)
		MenuInflater inflater = mActivity.getMenuInflater();
		inflater.inflate(R.menu.browsercontext, menu);
		boolean showNewTab = mTabControl.canCreateNewTab();
		// Show the correct menu group
		final String extra = result.getExtra();
		Log.i("xxxx", "__________result.getExtra();________" + extra);
		// if (extra == null) return;
		menu.setGroupVisible(R.id.PHONE_MENU,
				type == WebView.HitTestResult.PHONE_TYPE);
		menu.setGroupVisible(R.id.EMAIL_MENU,
				type == WebView.HitTestResult.EMAIL_TYPE);
		menu.setGroupVisible(R.id.GEO_MENU,
				type == WebView.HitTestResult.GEO_TYPE);
		menu.setGroupVisible(R.id.Aurora_IMAGE_MENU,
				type == WebView.HitTestResult.IMAGE_TYPE);
		menu.setGroupVisible(R.id.Aurora_ANCHOR_MENU,
				type == WebView.HitTestResult.SRC_ANCHOR_TYPE
						|| type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE);
		menu.setGroupVisible(R.id.Aurora_KONG_MENU,
				type == WebView.HitTestResult.UNKNOWN_TYPE);
		// Setup custom handling depending on the type
		switch (type) {
		case WebView.HitTestResult.PHONE_TYPE:
			menu.setHeaderTitle(Uri.decode(extra));
			menu.findItem(R.id.dial_context_menu_id).setIntent(
					new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_TEL
							+ extra)));
			Intent addIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
			addIntent.putExtra(Insert.PHONE, Uri.decode(extra));
			addIntent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
			menu.findItem(R.id.add_contact_context_menu_id)
					.setIntent(addIntent);
			menu.findItem(R.id.copy_phone_context_menu_id)
					.setOnMenuItemClickListener(new Copy(extra));
			break;

		case WebView.HitTestResult.EMAIL_TYPE:
			menu.setHeaderTitle(extra);
			menu.findItem(R.id.email_context_menu_id).setIntent(
					new Intent(Intent.ACTION_VIEW, Uri
							.parse(WebView.SCHEME_MAILTO + extra)));
			menu.findItem(R.id.copy_mail_context_menu_id)
					.setOnMenuItemClickListener(new Copy(extra));
			break;

		case WebView.HitTestResult.GEO_TYPE:
			menu.setHeaderTitle(extra);
			menu.findItem(R.id.map_context_menu_id).setIntent(
					new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_GEO
							+ URLEncoder.encode(extra))));
			menu.findItem(R.id.copy_geo_context_menu_id)
					.setOnMenuItemClickListener(new Copy(extra));
			break;

		case WebView.HitTestResult.SRC_ANCHOR_TYPE:
		case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:// 链接
			Log.i("xxxx", "______链接____________" + extra);
			menu.setHeaderTitle(extra);
			// decide whether to show the open link in new tab option
			
			Log.i("xxxx", "______链接_____22_______" + showNewTab+getMaxTabs());
			MenuItem newTabItemBackGround = menu
					.findItem(R.id.anchor_open_background_menu_id);// 后台
			
		//	newTabItemBackGround.setEnabled(false);
		//	newTabItemBackGround.setVisible(false);
			
			newTabItemBackGround
					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							Log.i("xxxx", "______后台打开____________"+mSettings.openInBackground());
							if(toastMaxTabsWarning()) {
								return true;
							}
//							// TODO Auto-generated method stub
							final Tab parent = mTabControl.getCurrentTab();

							openTab(extra, parent,
									false, true);
							return true;
						}
					});
			
//			if (showNewTab) {
//			newTabItemBackGround
//					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//
//						@Override
//						public boolean onMenuItemClick(MenuItem arg0) {
//							Log.i("xxxx", "______后台打开____________"+mSettings.openInBackground());
//							// TODO Auto-generated method stub
//							final Tab parent = mTabControl.getCurrentTab();
//
//							openTab(extra, parent,
//									false, true);
//							return true;
//
//						}
//					});
//			}else{
//				newTabItemBackGround.setEnabled(false);	
//			}
			MenuItem newTabItem = menu
					.findItem(R.id.anchor_open_newtab_menu_id);// 前台

			// newTabItem.setTitle(getSettings().openInBackground()
			// ? R.string.contextmenu_openlink_newwindow_background
			// : R.string.contextmenu_openlink_newwindow);
			// newTabItem.setVisible(showNewTab);
			
			newTabItem
			.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					Log.i("xxxx", "______前台打开____________");
					if(toastMaxTabsWarning()) {
						return true;
					}
					final Tab parent = mTabControl.getCurrentTab();

					openTab(extra, parent,
							true, true);
					return true;
				}
			});
			
			/*if (showNewTab) {
				newTabItem
						.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								Log.i("xxxx", "______前台打开____________");
								final Tab parent = mTabControl.getCurrentTab();

								openTab(extra, parent,
										true, true);
								return true;
							}
						});
				
			}else{
				newTabItem.setEnabled(false);	
			}*/
			MenuItem share_link = menu.findItem(R.id.anchor_share_tab_menu_id);// 分享
			share_link
					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							sharePage(mActivity, null, extra, null, null);
							return true;
						}
					});
			// anchor_search_in_tab_menu_id
//			MenuItem searchInTab = menu
//					.findItem(R.id.anchor_search_in_tab_menu_id);// chazhao
//			searchInTab
//					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//						@Override
//						public boolean onMenuItemClick(MenuItem item) {							
//							findOnPage();
//							return true;
//						}
//					});

			if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
				break;
			}
			// otherwise fall through to handle image part
		case WebView.HitTestResult.IMAGE_TYPE:// 图片
			MenuItem image_open_background = menu
					.findItem(R.id.image_open_background_menu_id);
			menu.setHeaderTitle(extra);
			image_open_background
			.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if(toastMaxTabsWarning()) {
						return true;
					}
					final Tab parent = mTabControl.getCurrentTab();

					openTab(extra, parent,
							false, true);

					return true;
				}
			});
			/*if (showNewTab) {
			
			image_open_background
					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							final Tab parent = mTabControl.getCurrentTab();

							openTab(extra, parent,
									false, true);

							return true;
						}
					});
			}else{
				image_open_background.setEnabled(false);		
			}*/
			MenuItem imagen_newTabItem = menu
					.findItem(R.id.image_open_newtab_menu_id);
			imagen_newTabItem
			.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if(toastMaxTabsWarning()) {
						return true;
					}
					final Tab parent = mTabControl.getCurrentTab();

					openTab(extra, parent,
							true, true);
					return true;
				}
			});
			/*if (showNewTab) {
			imagen_newTabItem
					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {

							final Tab parent = mTabControl.getCurrentTab();

							openTab(extra, parent,
									true, true);
							return true;
						}
					});
			}else {
				imagen_newTabItem.setEnabled(false);			
			}*/
			menu.findItem(R.id.image_save_image_menu_id)
					.setOnMenuItemClickListener(
							new Download(mActivity, extra, webview
									.isPrivateBrowsingEnabled(), webview
									.getSettings().getUserAgentString()));
			
			break;
		case WebView.HitTestResult.UNKNOWN_TYPE:

			menu.setHeaderTitle(extra);

			MenuItem kongNewTabItemBackGround = menu
					.findItem(R.id.kong_open_background_menu_id);

			kongNewTabItemBackGround
					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

						@Override
						public boolean onMenuItemClick(MenuItem arg0) {
							if(toastMaxTabsWarning()) {
								return true;
							}
							// TODO Auto-generated method stub
							final Tab parent = mTabControl.getCurrentTab();

							openTab(extra, parent,
									mSettings.openInBackground(), true);
							return true;

						}
					});

			MenuItem kongNewTabItem = menu
					.findItem(R.id.kong_open_newtab_menu_id);

			kongNewTabItem
					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {

							final HashMap<String, WebView> hrefMap = new HashMap<String, WebView>();
							hrefMap.put("webview", webview);
							final Message msg = mHandler
									.obtainMessage(FOCUS_NODE_HREF,
											R.id.anchor_open_newtab_menu_id, 0,
											hrefMap);
							webview.requestFocusNodeHref(msg);
							return true;
						}
					});

			MenuItem kongShare_link = menu
					.findItem(R.id.kong_share_tab_menu_id);
			kongShare_link
					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							sharePage(mActivity, null, extra, null, null);
							return true;
						}
					});

//			MenuItem kongSearchInTab = menu
//					.findItem(R.id.kong_srarch_in_tab_menu_id);
//			kongSearchInTab
//					.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//						@Override
//						public boolean onMenuItemClick(MenuItem item) {
//
//							findOnPage();
//							return true;
//						}
//					});
			break;
		default:
			Log.w(LOGTAG, "We should not get here.");
			break;
		}
		// update the ui
		mUi.onContextMenuCreated(menu);
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean toastMaxTabsWarning() {
		if(mTabControl.getTabCount() == getMaxTabs()) {
			String toastContent = mActivity.getString(R.string.max_tabs_toast,getMaxTabs());
			Toast.makeText(mActivity, toastContent, Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

    /**
     * As the menu can be open when loading state changes
     * we must manually update the state of the stop/reload menu
     * item
     */
    private void updateInLoadMenuItems(Menu menu, Tab tab) {
        if (menu == null) {
            return;
        }
        MenuItem dest = menu.findItem(R.id.stop_reload_menu_id);
        MenuItem src = ((tab != null) && tab.inPageLoad()) ?
                menu.findItem(R.id.stop_menu_id):
                menu.findItem(R.id.reload_menu_id);
        if (src != null) {
            dest.setIcon(src.getIcon());
            dest.setTitle(src.getTitle());
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateInLoadMenuItems(menu, getCurrentTab());
        // hold on to the menu reference here; it is used by the page callbacks
        // to update the menu based on loading state
        mCachedMenu = menu;
        // Note: setVisible will decide whether an item is visible; while
        // setEnabled() will decide whether an item is enabled, which also means
        // whether the matching shortcut key will function.
        switch (mMenuState) {
            case EMPTY_MENU:
                if (mCurrentMenuState != mMenuState) {
                    menu.setGroupVisible(R.id.MAIN_MENU, false);
                    menu.setGroupEnabled(R.id.MAIN_MENU, false);
                    menu.setGroupEnabled(R.id.MAIN_SHORTCUT_MENU, false);
                }
                break;
            default:
                if (mCurrentMenuState != mMenuState) {
                    menu.setGroupVisible(R.id.MAIN_MENU, true);
                    menu.setGroupEnabled(R.id.MAIN_MENU, true);
                    menu.setGroupEnabled(R.id.MAIN_SHORTCUT_MENU, true);
                }
                updateMenuState(getCurrentTab(), menu);
                break;
        }
        mCurrentMenuState = mMenuState;
        return mUi.onPrepareOptionsMenu(menu);
    }

    @Override
    public void updateMenuState(Tab tab, Menu menu) {
        boolean canGoBack = false;
        boolean canGoForward = false;
        boolean isHome = false;
        boolean isDesktopUa = false;
        boolean isLive = false;
        if (tab != null) {
            canGoBack = tab.canGoBack();
            canGoForward = tab.canGoForward();
            isHome = mSettings.getHomePage().equals(tab.getUrl());
            isDesktopUa = mSettings.hasDesktopUseragent(tab.getWebView());
            isLive = !tab.isSnapshot();
        }
        final MenuItem back = menu.findItem(R.id.back_menu_id);
        back.setEnabled(canGoBack);

        final MenuItem home = menu.findItem(R.id.homepage_menu_id);
        home.setEnabled(!isHome);

        final MenuItem forward = menu.findItem(R.id.forward_menu_id);
        forward.setEnabled(canGoForward);

        final MenuItem source = menu.findItem(isInLoad() ? R.id.stop_menu_id
                : R.id.reload_menu_id);
        final MenuItem dest = menu.findItem(R.id.stop_reload_menu_id);
        if (source != null && dest != null) {
            dest.setTitle(source.getTitle());
            dest.setIcon(source.getIcon());
        }
        menu.setGroupVisible(R.id.NAV_MENU, isLive);

        // decide whether to show the share link option
        PackageManager pm = mActivity.getPackageManager();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        ResolveInfo ri = pm.resolveActivity(send,
                PackageManager.MATCH_DEFAULT_ONLY);
        menu.findItem(R.id.share_page_menu_id).setVisible(ri != null);

        boolean isNavDump = mSettings.enableNavDump();
        final MenuItem nav = menu.findItem(R.id.dump_nav_menu_id);
        nav.setVisible(isNavDump);
        nav.setEnabled(isNavDump);

        boolean showDebugSettings = mSettings.isDebugEnabled();
        final MenuItem uaSwitcher = menu.findItem(R.id.ua_desktop_menu_id);
        uaSwitcher.setChecked(isDesktopUa);
        menu.setGroupVisible(R.id.LIVE_MENU, isLive);
        menu.setGroupVisible(R.id.SNAPSHOT_MENU, !isLive);
        menu.setGroupVisible(R.id.COMBO_MENU, false);

        mUi.updateMenuState(tab, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (null == getCurrentTopWebView()) {
            return false;
        }
        if (mMenuIsDown) {
            // The shortcut action consumes the MENU. Even if it is still down,
            // it won't trigger the next shortcut action. In the case of the
            // shortcut action triggering a new activity, like Bookmarks, we
            // won't get onKeyUp for MENU. So it is important to reset it here.
            mMenuIsDown = false;
        }
        if (mUi.onOptionsItemSelected(item)) {
            // ui callback handled it
            return true;
        }
        switch (item.getItemId()) {
            // -- Main menu
            case R.id.new_tab_menu_id:
                openTabToHomePage();
                break;

            case R.id.incognito_menu_id:
                openIncognitoTab();
                break;

            case R.id.close_other_tabs_id:
                closeOtherTabs();
                break;

            case R.id.goto_menu_id:
                editUrl();
                break;

            case R.id.bookmarks_menu_id:
                bookmarksOrHistoryPicker(ComboViews.Bookmarks);
                break;

            case R.id.history_menu_id:
                bookmarksOrHistoryPicker(ComboViews.History);
                break;

            case R.id.snapshots_menu_id:
                bookmarksOrHistoryPicker(ComboViews.Snapshots);
                break;

            case R.id.add_bookmark_menu_id:
//                bookmarkCurrentPage();
//            	showBookmarkCrtPageDialog();
                break;

            case R.id.stop_reload_menu_id:
                if (isInLoad()) {
                    stopLoading();
                } else {
                    getCurrentTopWebView().reload();
                }
                break;

            case R.id.back_menu_id:
                getCurrentTab().goBack();
                break;

            case R.id.forward_menu_id:
                getCurrentTab().goForward();
                break;

            case R.id.close_menu_id:
                // Close the subwindow if it exists.
                if (mTabControl.getCurrentSubWindow() != null) {
                    dismissSubWindow(mTabControl.getCurrentTab());
                    break;
                }
                closeCurrentTab();
                break;

            case R.id.homepage_menu_id:
                Tab current = mTabControl.getCurrentTab();
                loadUrl(current, mSettings.getHomePage());
                break;

            case R.id.preferences_menu_id:
                openPreferences();
                break;

            case R.id.find_menu_id:
            	Log.i("xxxx", "____________find_menu_id_________________");
                findOnPage();
                break;

            case R.id.page_info_menu_id:
                showPageInfo();
                break;

            case R.id.snapshot_go_live:
                goLive();
                return true;

            case R.id.share_page_menu_id:
                Tab currentTab = mTabControl.getCurrentTab();
                if (null == currentTab) {
                    return false;
                }
                shareCurrentPage(currentTab);
                break;

            case R.id.dump_nav_menu_id:
                getCurrentTopWebView().debugDump();
                break;

            case R.id.zoom_in_menu_id:
                getCurrentTopWebView().zoomIn();
                break;

            case R.id.zoom_out_menu_id:
                getCurrentTopWebView().zoomOut();
                break;

            case R.id.view_downloads_menu_id:
                viewDownloads();
                break;

            case R.id.ua_desktop_menu_id:
                toggleUserAgent();
                break;

            case R.id.window_one_menu_id:
            case R.id.window_two_menu_id:
            case R.id.window_three_menu_id:
            case R.id.window_four_menu_id:
            case R.id.window_five_menu_id:
            case R.id.window_six_menu_id:
            case R.id.window_seven_menu_id:
            case R.id.window_eight_menu_id:
                {
                    int menuid = item.getItemId();
                    for (int id = 0; id < WINDOW_SHORTCUT_ID_ARRAY.length; id++) {
                        if (WINDOW_SHORTCUT_ID_ARRAY[id] == menuid) {
                            Tab desiredTab = mTabControl.getTab(id);
                            if (desiredTab != null &&
                                    desiredTab != mTabControl.getCurrentTab()) {
                                switchToTab(desiredTab);
                            }
                            break;
                        }
                    }
                }
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public void toggleUserAgent() {
        WebView web = getCurrentWebView();
        mSettings.toggleDesktopUseragent(web);
        web.loadUrl(web.getOriginalUrl());
    }

    @Override
    public void findOnPage() {
        getCurrentTopWebView().showFindDialog(null, true);
    }

    @Override
    public void openPreferences() {
        Intent intent = new Intent(mActivity, BrowserPreferencesPage.class);
        intent.putExtra(BrowserPreferencesPage.CURRENT_PAGE,
                getCurrentTopWebView().getUrl());
        mActivity.startActivityForResult(intent, PREFERENCES_PAGE);
    }

    @Override
    public void bookmarkCurrentPage() {
        Intent bookmarkIntent = createBookmarkCurrentPageIntent(false);
        if (bookmarkIntent != null) {
            mActivity.startActivity(bookmarkIntent);
        }
    }
    
    private void goLive() {
        Tab t = getCurrentTab();
        t.loadUrl(t.getUrl(), null);
    }

    @Override
    public void showPageInfo() {
        mPageDialogsHandler.showPageInfo(mTabControl.getCurrentTab(), false, null);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Let the History and Bookmark fragments handle menus they created.
        if (item.getGroupId() == R.id.CONTEXT_MENU) {
            return false;
        }

        int id = item.getItemId();
        boolean result = true;
        switch (id) {
            // -- Browser context menu
            case R.id.open_context_menu_id:
            case R.id.anchor_cope_link_menu_id:
            	Log.i("xxxx", "____onContextItemSelected_______anchor_cope_link_menu_id____________________");
          //  case R.id.copy_link_context_menu_id:
                final WebView webView = getCurrentTopWebView();
                if (null == webView) {
                    result = false;
                    break;
                }
                final HashMap<String, WebView> hrefMap =
                        new HashMap<String, WebView>();
                hrefMap.put("webview", webView);
                final Message msg = mHandler.obtainMessage(
                        FOCUS_NODE_HREF, id, 0, hrefMap);
                webView.requestFocusNodeHref(msg);
                break;
            case R.id.image_cope_image_url_menu_id:
            	
            	 final WebView webView2 = getCurrentTopWebView();
                 if (null == webView2) {
                     result = false;
                     break;
                 }
                 final HashMap<String, WebView> hrefMap2 =
                         new HashMap<String, WebView>();
                 hrefMap2.put("webview", webView2);
                 final Message msg2 = mHandler.obtainMessage(
                         FOCUS_NODE_HREF, id, 0, hrefMap2);
                 webView2.requestFocusNodeHref(msg2);
                 break;
            case R.id.kong_cope_tab_menu_id:
           	 final WebView webView3 = getCurrentTopWebView();
                if (null == webView3) {
                    result = false;
                    break;
                }
                final HashMap<String, WebView> hrefMap3 =
                        new HashMap<String, WebView>();
                hrefMap3.put("webview", webView3);
                final Message msg3 = mHandler.obtainMessage(
                        FOCUS_NODE_HREF, id, 0, hrefMap3);
                webView3.requestFocusNodeHref(msg3);
                break;

            default:
                // For other context menus
                result = onOptionsItemSelected(item);
        }
        return result;
    }

    /**
     * support programmatically opening the context menu
     */
    public void openContextMenu(View view) {
        mActivity.openContextMenu(view);
    }

    /**
     * programmatically open the options menu
     */
    public void openOptionsMenu() {
        mActivity.openOptionsMenu();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (mOptionsMenuOpen) {
            if (mConfigChanged) {
                // We do not need to make any changes to the state of the
                // title bar, since the only thing that happened was a
                // change in orientation
                mConfigChanged = false;
            } else {
                if (!mExtendedMenuOpen) {
                    mExtendedMenuOpen = true;
                    mUi.onExtendedMenuOpened();
                } else {
                    // Switching the menu back to icon view, so show the
                    // title bar once again.
                    mExtendedMenuOpen = false;
                    mUi.onExtendedMenuClosed(isInLoad());
                }
            }
        } else {
            // The options menu is closed, so open it, and show the title
            mOptionsMenuOpen = true;
            mConfigChanged = false;
            mExtendedMenuOpen = false;
            mUi.onOptionsMenuOpened();
        }
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        mOptionsMenuOpen = false;
        mUi.onOptionsMenuClosed(isInLoad());
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        mUi.onContextMenuClosed(menu, isInLoad());
    }

    // Helper method for getting the top window.
    @Override
    public WebView getCurrentTopWebView() {
        return mTabControl.getCurrentTopWebView();
    }

    @Override
    public WebView getCurrentWebView() {
        return mTabControl.getCurrentWebView();
    }

    /*
     * This method is called as a result of the user selecting the options
     * menu to see the download window. It shows the download window on top of
     * the current window.
     */
    void viewDownloads() {
        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        mActivity.startActivity(intent);
    }

    int getActionModeHeight() {
        TypedArray actionBarSizeTypedArray = mActivity.obtainStyledAttributes(
                    new int[] { android.R.attr.actionBarSize });
        int size = (int) actionBarSizeTypedArray.getDimension(0, 0f);
        actionBarSizeTypedArray.recycle();
        return size;
    }

    // action mode

    @Override
    public void onActionModeStarted(ActionMode mode) {
        mUi.onActionModeStarted(mode);
        mActionMode = mode;
    }

    /*
     * True if a custom ActionMode (i.e. find or select) is in use.
     */
    @Override
    public boolean isInCustomActionMode() {
        return mActionMode != null;
    }

    /*
     * End the current ActionMode.
     */
    @Override
    public void endActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    /*
     * Called by find and select when they are finished.  Replace title bars
     * as necessary.
     */
    @Override
    public void onActionModeFinished(ActionMode mode) {
        if (!isInCustomActionMode()) return;
        mUi.onActionModeFinished(isInLoad());
        mActionMode = null;
    }

    boolean isInLoad() {
        final Tab tab = getCurrentTab();
        return (tab != null) && tab.inPageLoad();
    }

    // bookmark handling

    /**
     * add the current page as a bookmark to the given folder id
     * @param folderId use -1 for the default folder
     * @param editExisting If true, check to see whether the site is already
     *          bookmarked, and if it is, edit that bookmark.  If false, and
     *          the site is already bookmarked, do not attempt to edit the
     *          existing bookmark.
     */
    @Override
    public Intent createBookmarkCurrentPageIntent(boolean editExisting) {
        WebView w = getCurrentTopWebView();
        if (w == null) {
            return null;
        }
        Intent i = new Intent(mActivity,
                AddBookmarkPage.class);
        i.putExtra(BrowserContract.Bookmarks.URL, w.getUrl());
        i.putExtra(BrowserContract.Bookmarks.TITLE, w.getTitle());
        String touchIconUrl = w.getTouchIconUrl();
        if (touchIconUrl != null) {
            i.putExtra(AddBookmarkPage.TOUCH_ICON_URL, touchIconUrl);
            WebSettings settings = w.getSettings();
            if (settings != null) {
                i.putExtra(AddBookmarkPage.USER_AGENT,
                        settings.getUserAgentString());
            }
        }
        i.putExtra(BrowserContract.Bookmarks.THUMBNAIL,
                createScreenshot(w, getDesiredThumbnailWidth(mActivity),
                getDesiredThumbnailHeight(mActivity)));
        i.putExtra(BrowserContract.Bookmarks.FAVICON, w.getFavicon());
        if (editExisting) {
            i.putExtra(AddBookmarkPage.CHECK_FOR_DUPE, true);
        }
        // Put the dialog at the upper right of the screen, covering the
        // star on the title bar.
        i.putExtra("gravity", Gravity.RIGHT | Gravity.TOP);
        return i;
    }
    

    
    /**
     * 获取当前页面的icon
     * @param key
     * @return
     */
    private Bitmap getCurrentBookmarkIcon(String key) {
    	if(key != null && iconMap.containsKey(key) && (iconMap.get(key).get() != null)) {
    		return iconMap.get(key).get();
    	}
    	return BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.default_icon_title);
    }
    
    /**
     * 设置添加书签对话框的图标，标题和地址
     */
    private void setCrtBookmarkDialog() {
    	if(getCurrentTab() != null && getCurrentTab().getmLastSucUrl() != null) {
    		etBookmarkUrl.setText(getCurrentTab().getmLastSucUrl().toString().trim());
    		tvSaveFolder.setText(selectedFolderName);
    		if(urlTitleMap.containsKey(getCurrentTab().getmLastSucUrl())) {
    			etBookmarkTitle.setText(urlTitleMap.get(getCurrentTab().getmLastSucUrl()));
    		}
    		
    		if(iconMap.containsKey(getCurrentTab().getmLastSucUrl()) && iconMap.get(getCurrentTab().getmLastSucUrl()).get() != null) {
    			ivBookmarkThumb.setImageBitmap(iconMap.get(getCurrentTab().getmLastSucUrl()).get());
    		}else {
    			ivBookmarkThumb.setImageResource(R.drawable.default_icon_title);
    		}
    	}
    }
    
    /**
     * Parse the data entered in the dialog and post a message to update the bookmarks database.
     */
    boolean save(String title, String url, String touchIconUrl, Bitmap thumbnail, Bitmap favicon) {
        String unfilteredUrl = UrlUtils.fixUrl(url);

        boolean emptyTitle = title.length() == 0;
        boolean emptyUrl = unfilteredUrl.trim().length() == 0;
        Resources r = getActivity().getResources();
        if (emptyTitle || emptyUrl) {
            if (emptyTitle) {
            	Toast.makeText(getActivity(), r.getText(R.string.bookmark_needs_title), 0).show();
            }
            if (emptyUrl) {
            	Toast.makeText(getActivity(), r.getText(R.string.bookmark_needs_url), 0).show();
            }
            return false;
        }
        url = unfilteredUrl.trim();
        
        try {
            // We allow bookmarks with a javascript: scheme, but these will in most cases
            // fail URI parsing, so don't try it if that's the kind of bookmark we have.

            if (!url.toLowerCase().startsWith("javascript:")) {
                URI uriObj = new URI(url);
                String scheme = uriObj.getScheme();
                if (!Bookmarks.urlHasAcceptableScheme(url)) {
                    // If the scheme was non-null, let the user know that we
                    // can't save their bookmark. If it was null, we'll assume
                    // they meant http when we parse it in the WebAddress class.
                    if (scheme != null) {
                    	Toast.makeText(getActivity(), r.getText(R.string.bookmark_cannot_save_url), 0).show();
                        return false;
                    }
                    WebAddress address;
                    try {
                        address = new WebAddress(unfilteredUrl);
                    } catch (ParseException e) {
                        throw new URISyntaxException("", "");
                    }
                    if (address.getHost().length() == 0) {
                        throw new URISyntaxException("", "");
                    }
                    url = address.toString();
                }
            }
        } catch (URISyntaxException e) {
        	Toast.makeText(getActivity(), r.getText(R.string.bookmark_url_not_valid), 0).show();
            return false;
        }

//        boolean urlUnmodified = url.equals(mOriginalUrl);
        boolean urlUnmodified = true;

        Bundle bundle = new Bundle();
        bundle.putString(BrowserContract.Bookmarks.TITLE, title);
        bundle.putString(BrowserContract.Bookmarks.URL, url);
        bundle.putParcelable(BrowserContract.Bookmarks.FAVICON, favicon);
        bundle.putParcelable(BrowserContract.Bookmarks.THUMBNAIL, thumbnail);
        bundle.putBoolean(AddBookmarkPage.REMOVE_THUMBNAIL, !urlUnmodified);
        bundle.putString(AddBookmarkPage.TOUCH_ICON_URL, touchIconUrl);
        
        // Post a message to write to the DB.
        Message msg = Message.obtain(mHandler, SAVE_BOOKMARK);
        msg.setData(bundle);
        // Start a new thread so as to not slow down the UI
        Thread t = new Thread(new SaveBookmarkRunnable(getActivity(), msg));
        t.start();
//        setResult(RESULT_OK);
        LogTag.logBookmarkAdded(url, "bookmarkview");
        return true;
    }
    
    /**
     * Runnable to save a bookmark, so it can be performed in its own thread.
     */
    private class SaveBookmarkRunnable implements Runnable {
        // FIXME: This should be an async task.
        private Message mMessage;
        private Context mContext;
        public SaveBookmarkRunnable(Context ctx, Message msg) {
            mContext = ctx.getApplicationContext();
            mMessage = msg;
        }
        public void run() {
            // Unbundle bookmark data.
            Bundle bundle = mMessage.getData();
            String title = bundle.getString(BrowserContract.Bookmarks.TITLE);
            String url = bundle.getString(BrowserContract.Bookmarks.URL);
            boolean invalidateThumbnail = bundle.getBoolean(AddBookmarkPage.REMOVE_THUMBNAIL);
            Bitmap thumbnail = invalidateThumbnail ? null
                    : (Bitmap) bundle.getParcelable(BrowserContract.Bookmarks.THUMBNAIL);
            String touchIconUrl = bundle.getString(AddBookmarkPage.TOUCH_ICON_URL);
            Bitmap favicon = bundle.getParcelable(BrowserContract.Bookmarks.FAVICON);

            // Save to the bookmarks DB.
            try {
            	if(Bookmarks.getBookmarkIdInTheGivedFolder(mContext, mContext.getContentResolver(), url, title, selectedFolderId) == -100) {
            		 final ContentResolver cr = getActivity().getContentResolver();
                     Bookmarks.addBookmark(getActivity(), false, url,
                             title, thumbnail,favicon, selectedFolderId);
//                     if (touchIconUrl != null) {
//                         new DownloadTouchIcon(mContext, cr, url).execute(mTouchIconUrl);
//                     }
            	}
                mMessage.arg1 = 1;
            } catch (IllegalStateException e) {
                mMessage.arg1 = 0;
            }
            mMessage.sendToTarget();
        }
    }

    // file chooser
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadHandler = new UploadHandler(this);
        mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
    }

    @Override
    public void showFileChooser(ValueCallback<Uri[]> callback, FileChooserParams params) {
        mUploadHandler = new UploadHandler(this);
        mUploadHandler.openFileChooser(callback, params);
    }

    // thumbnails

    /**
     * Return the desired width for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     * @param context Context for finding out the density of the screen.
     * @return desired width for thumbnail screenshot.
     */
    static int getDesiredThumbnailWidth(Context context) {
        return context.getResources().getDimensionPixelOffset(
                R.dimen.bookmarkThumbnailWidth);
    }

    /**
     * Return the desired height for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     * @param context Context for finding out the density of the screen.
     * @return desired height for thumbnail screenshot.
     */
    static int getDesiredThumbnailHeight(Context context) {
        return context.getResources().getDimensionPixelOffset(
                R.dimen.bookmarkThumbnailHeight);
    }

    static Bitmap createScreenshot(WebView view, int width, int height) {
        if (view == null || view.getContentHeight() == 0
                || view.getContentWidth() == 0) {
            return null;
        }
        // We render to a bitmap 2x the desired size so that we can then
        // re-scale it with filtering since canvas.scale doesn't filter
        // This helps reduce aliasing at the cost of being slightly blurry
        final int filter_scale = 2;
        int scaledWidth = width * filter_scale;
        int scaledHeight = height * filter_scale;
        if (sThumbnailBitmap == null || sThumbnailBitmap.getWidth() != scaledWidth
                || sThumbnailBitmap.getHeight() != scaledHeight) {
            if (sThumbnailBitmap != null) {
                sThumbnailBitmap.recycle();
                sThumbnailBitmap = null;
            }
            sThumbnailBitmap =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(sThumbnailBitmap);
        int contentWidth = view.getContentWidth();
        float overviewScale = scaledWidth / (view.getScale() * contentWidth);
        if (view instanceof BrowserWebView) {
            int dy = -((BrowserWebView)view).getTitleHeight();
            canvas.translate(0, dy * overviewScale);
        }

        canvas.scale(overviewScale, overviewScale);

        if (view instanceof BrowserWebView) {
            ((BrowserWebView)view).drawContent(canvas);
        } else {
            view.draw(canvas);
        }
        Bitmap ret = Bitmap.createScaledBitmap(sThumbnailBitmap,
                width, height, true);
        canvas.setBitmap(null);
        return ret;
    }

    private void updateScreenshot(Tab tab) {
        // If this is a bookmarked site, add a screenshot to the database.
        // FIXME: Would like to make sure there is actually something to
        // draw, but the API for that (WebViewCore.pictureReady()) is not
        // currently accessible here.

        WebView view = tab.getWebView();
        if (view == null) {
            // Tab was destroyed
            return;
        }
        final String url = tab.getUrl();
        final String originalUrl = view.getOriginalUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        // Only update thumbnails for web urls (http(s)://), not for
        // about:, javascript:, data:, etc...
        // Unless it is a bookmarked site, then always update
        if (!Patterns.WEB_URL.matcher(url).matches() && !tab.isBookmarkedSite()) {
            return;
        }

        final Bitmap bm = createScreenshot(view, getDesiredThumbnailWidth(mActivity),
                getDesiredThumbnailHeight(mActivity));
        if (bm == null) {
            return;
        }

        final ContentResolver cr = mActivity.getContentResolver();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                Cursor cursor = null;
                try {
                    // TODO: Clean this up
                    cursor = Bookmarks.queryCombinedForUrl(cr, originalUrl, url);
                    if (cursor != null && cursor.moveToFirst()) {
                        final ByteArrayOutputStream os =
                                new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 100, os);

                        ContentValues values = new ContentValues();
                        values.put(Images.THUMBNAIL, os.toByteArray());

                        do {
                            values.put(Images.URL, cursor.getString(0));
                            cr.update(Images.CONTENT_URI, values, null, null);
                        } while (cursor.moveToNext());
                    }
                } catch (IllegalStateException e) {
                    // Ignore
                } catch (SQLiteException s) {
                    // Added for possible error when user tries to remove the same bookmark
                    // that is being updated with a screen shot
                    Log.w(LOGTAG, "Error when running updateScreenshot ", s);
                } finally {
                    if (cursor != null) cursor.close();
                }
                return null;
            }
        }.execute();
    }

    private class Copy implements OnMenuItemClickListener {
        private CharSequence mText;

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            copy(mText);
            return true;
        }

        public Copy(CharSequence toCopy) {
            mText = toCopy;
        }
    }

    private static class Download implements OnMenuItemClickListener {
        private Activity mActivity;
        private String mText;
        private boolean mPrivateBrowsing;
        private String mUserAgent;
        private static final String FALLBACK_EXTENSION = "dat";
        private static final String IMAGE_BASE_FORMAT = "yyyy-MM-dd-HH-mm-ss-";

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (DataUri.isDataUri(mText)) {
                saveDataUri();
            } else {
                DownloadHandler.onDownloadStartNoStream(mActivity, mText, mUserAgent,
                        null, null, null, mPrivateBrowsing);
            }
            return true;
        }

        public Download(Activity activity, String toDownload, boolean privateBrowsing,
                String userAgent) {
            mActivity = activity;
            mText = toDownload;
            mPrivateBrowsing = privateBrowsing;
            mUserAgent = userAgent;
        }

        /**
         * Treats mText as a data URI and writes its contents to a file
         * based on the current time.
         */
        private void saveDataUri() {
            FileOutputStream outputStream = null;
            try {
                DataUri uri = new DataUri(mText);
                File target = getTarget(uri);
                outputStream = new FileOutputStream(target);
                outputStream.write(uri.getData());
                final DownloadManager manager =
                        (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
                 manager.addCompletedDownload(target.getName(),
                        mActivity.getTitle().toString(), false,
                        uri.getMimeType(), target.getAbsolutePath(),
                        uri.getData().length, true);
            } catch (IOException e) {
                Log.e(LOGTAG, "Could not save data URL");
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        // ignore close errors
                    }
                }
            }
        }

        /**
         * Creates a File based on the current time stamp and uses
         * the mime type of the DataUri to get the extension.
         */
        private File getTarget(DataUri uri) throws IOException {
            File dir = mActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            DateFormat format = new SimpleDateFormat(IMAGE_BASE_FORMAT, Locale.US);
            String nameBase = format.format(new Date());
            String mimeType = uri.getMimeType();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String extension = mimeTypeMap.getExtensionFromMimeType(mimeType);
            if (extension == null) {
                Log.w(LOGTAG, "Unknown mime type in data URI" + mimeType);
                extension = FALLBACK_EXTENSION;
            }
            extension = "." + extension; // createTempFile needs the '.'
            File targetFile = File.createTempFile(nameBase, extension, dir);
            return targetFile;
        }
    }

    /********************** TODO: UI stuff *****************************/

    // these methods have been copied, they still need to be cleaned up

    /****************** tabs ***************************************************/

    // basic tab interactions:

    // it is assumed that tabcontrol already knows about the tab
    protected void addTab(Tab tab) {
        mUi.addTab(tab);
    }

    protected void removeTab(Tab tab) {
        mUi.removeTab(tab);
        mTabControl.removeTab(tab);
        mCrashRecoveryHandler.backupState();
    }

    @Override
    public void setActiveTab(Tab tab) {
        // monkey protection against delayed start
        if (tab != null) {
            mTabControl.setCurrentTab(tab);
            // the tab is guaranteed to have a webview after setCurrentTab
            mUi.setActiveTab(tab);
        }
    }
  
    protected void closeEmptyTab() {
        Tab current = mTabControl.getCurrentTab();
        if (current != null
                && current.getWebView().copyBackForwardList().getSize() == 0) {
            closeCurrentTab();
        }
    }

    protected void reuseTab(Tab appTab, UrlData urlData) {
        // Dismiss the subwindow if applicable.
        dismissSubWindow(appTab);
        // Since we might kill the WebView, remove it from the
        // content view first.
        mUi.detachTab(appTab);
        // Recreate the main WebView after destroying the old one.
        mTabControl.recreateWebView(appTab);
        // TODO: analyze why the remove and add are necessary
        mUi.attachTab(appTab);
        if (mTabControl.getCurrentTab() != appTab) {
            switchToTab(appTab);
            loadUrlDataIn(appTab, urlData);
        } else {
            // If the tab was the current tab, we have to attach
            // it to the view system again.
            setActiveTab(appTab);
            loadUrlDataIn(appTab, urlData);
        }
    }

    // Remove the sub window if it exists. Also called by TabControl when the
    // user clicks the 'X' to dismiss a sub window.
    @Override
    public void dismissSubWindow(Tab tab) {
        removeSubWindow(tab);
        // dismiss the subwindow. This will destroy the WebView.
        tab.dismissSubWindow();
        WebView wv = getCurrentTopWebView();
        if (wv != null) {
            wv.requestFocus();
        }
    }

    @Override
    public void removeSubWindow(Tab t) {
        if (t.getSubWebView() != null) {
            mUi.removeSubWindow(t.getSubViewContainer());
        }
    }

    @Override
    public void attachSubWindow(Tab tab) {
        if (tab.getSubWebView() != null) {
            mUi.attachSubWindow(tab.getSubViewContainer());
            getCurrentTopWebView().requestFocus();
        }
    }

    private Tab showPreloadedTab(final UrlData urlData) {
        if (!urlData.isPreloaded()) {
            return null;
        }
        final PreloadedTabControl tabControl = urlData.getPreloadedTab();
        final String sbQuery = urlData.getSearchBoxQueryToSubmit();
        if (sbQuery != null) {
            if (!tabControl.searchBoxSubmit(sbQuery, urlData.mUrl, urlData.mHeaders)) {
                // Could not submit query. Fallback to regular tab creation
                tabControl.destroy();
                return null;
            }
        }
        // check tab count and make room for new tab
        if (!mTabControl.canCreateNewTab()) {
            Tab leastUsed = mTabControl.getLeastUsedTab(getCurrentTab());
            if (leastUsed != null) {
                closeTab(leastUsed);
            }
        }
        Tab t = tabControl.getTab();
        t.refreshIdAfterPreload();
        mTabControl.addPreloadedTab(t);
        addTab(t);
        setActiveTab(t);
        return t;
    }

    // open a non inconito tab with the given url data
    // and set as active tab
    public Tab openTab(UrlData urlData) {
    	Log.i("xie", "------- openTab(UrlData urlData)-----------");
        Tab tab = showPreloadedTab(urlData);
        if (tab == null) {
            tab = createNewTab(false, true, true);
            if ((tab != null) && !urlData.isEmpty()) {      	
                loadUrlDataIn(tab, urlData);
            }
        }
        return tab;
    }

    @Override
    public Tab openTabToHomePage() {
        return openTab(mSettings.getHomePage(), false, true, false);
    }

    @Override
    public Tab openIncognitoTab() {
    	Log.i("jie", "------------------6");
        return openTab(INCOGNITO_URI, true, true, false);
    }

    @Override
    public Tab openTab(String url, boolean incognito, boolean setActive,
            boolean useCurrent) {
    	Log.i("jie", "------------------7");
        return openTab(url, incognito, setActive, useCurrent, null);
    }

    @Override
    public Tab openTab(String url, Tab parent, boolean setActive,
            boolean useCurrent) {
    	Log.i("jie", "------------------8");
        return openTab(url, (parent != null) && parent.isPrivateBrowsingEnabled(),
                setActive, useCurrent, parent);
    }

    public Tab openTab(String url, boolean incognito, boolean setActive,
            boolean useCurrent, Tab parent) {
    	Log.i("xie", "------- openTab       -----------");
        Tab tab = createNewTab(incognito, setActive, useCurrent);
        if (tab != null) {
            if (parent != null && parent != tab) {
                parent.addChildTab(tab);
            }
            if (url != null) {
            	
                loadUrl(tab, url);
            }
        }
        return tab;
    }

    // this method will attempt to create a new tab
    // incognito: private browsing tab
    // setActive: ste tab as current tab
    // useCurrent: if no new tab can be created, return current tab
    private Tab createNewTab(boolean incognito, boolean setActive,
            boolean useCurrent) {
        Tab tab = null;
        if (mTabControl.canCreateNewTab()) {
            tab = mTabControl.createNewTab(incognito);
            addTab(tab);
            if (setActive) {
                setActiveTab(tab);
            }
        } else {
            if (useCurrent) {
                tab = mTabControl.getCurrentTab();
                reuseTab(tab, null);
            } else {
                mUi.showMaxTabsWarning();
            }
        }
        return tab;
    }
     
    /**
     * @param tab the tab to switch to
     * @return boolean True if we successfully switched to a different tab.  If
     *                 the indexth tab is null, or if that tab is the same as
     *                 the current one, return false.
     */
    @Override
    public boolean switchToTab(Tab tab) {
        Tab currentTab = mTabControl.getCurrentTab();
        if (tab == null || tab == currentTab) {
            return false;
        }
        setActiveTab(tab);
        return true;
    }

    @Override
    public void closeCurrentTab() {
        closeCurrentTab(false);
    }

    protected void closeCurrentTab(boolean andQuit) {
        if (mTabControl.getTabCount() == 1) {
            mCrashRecoveryHandler.clearState();
            mTabControl.removeTab(getCurrentTab());
            mActivity.finish();
            return;
        }
        final Tab current = mTabControl.getCurrentTab();
        final int pos = mTabControl.getCurrentPosition();
        Tab newTab = current.getParent();
        if (newTab == null) {
            newTab = mTabControl.getTab(pos + 1);
            if (newTab == null) {
                newTab = mTabControl.getTab(pos - 1);
            }
        }
        if (andQuit) {
            mTabControl.setCurrentTab(newTab);
            closeTab(current);
        } else if (switchToTab(newTab)) {
            // Close window
            closeTab(current);
        }
    }

    /**
     * Close the tab, remove its associated title bar, and adjust mTabControl's
     * current tab to a valid value.
     */
    @Override
    public void closeTab(Tab tab) {
        if (tab == mTabControl.getCurrentTab()) {
            closeCurrentTab();
        } else {
            removeTab(tab);
        }
    }

    /**
     * Close all tabs except the current one
     */
    @Override
    public void closeOtherTabs() {
        int inactiveTabs = mTabControl.getTabCount() - 1;
        for (int i = inactiveTabs; i >= 0; i--) {
            Tab tab = mTabControl.getTab(i);
            if (tab != mTabControl.getCurrentTab()) {
                removeTab(tab);
            }
        }
    }

    // Called when loading from context menu or LOAD_URL message
    protected void loadUrlFromContext(String url) {
        Tab tab = getCurrentTab();
        WebView view = tab != null ? tab.getWebView() : null;
        // In case the user enters nothing.
        if (url != null && url.length() != 0 && tab != null && view != null) {
            url = UrlUtils.smartUrlFilter(url);
            if (!((BrowserWebView) view).getWebViewClient().
                    shouldOverrideUrlLoading(view, url)) {
                loadUrl(tab, url);
            }
        }
    }

    /**
     * Load the URL into the given WebView and update the title bar
     * to reflect the new load.  Call this instead of WebView.loadUrl
     * directly.
     * @param view The WebView used to load url.
     * @param url The URL to load.
     */
    @Override
    public void loadUrl(Tab tab, String url) {
        loadUrl(tab, url, null);
    }
    
    protected void loadUrl(Tab tab, String url, Map<String, String> headers) {
        if (tab != null) {
            dismissSubWindow(tab);
            tab.loadUrl(url, headers);
            mUi.onProgressChanged(tab);
        }
    }

    /**
     * Load UrlData into a Tab and update the title bar to reflect the new
     * load.  Call this instead of UrlData.loadIn directly.
     * @param t The Tab used to load.
     * @param data The UrlData being loaded.
     */
    protected void loadUrlDataIn(Tab t, UrlData data) {
        if (data != null) {
            if (data.isPreloaded()) {
                // this isn't called for preloaded tabs
            } else {
                if (t != null && data.mDisableUrlOverride) {
                    t.disableUrlOverridingForLoad();
                }
                loadUrl(t, data.mUrl, data.mHeaders);
            }
        }
    }

    @Override
    public void onUserCanceledSsl(Tab tab) {
        // TODO: Figure out the "right" behavior
        if (tab.canGoBack()) {
            tab.goBack();
        } else {
            tab.loadUrl(mSettings.getHomePage(), null);
        }
    }

    void goBackOnePageOrQuit() {
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            /*
             * Instead of finishing the activity, simply push this to the back
             * of the stack and let ActivityManager to choose the foreground
             * activity. As BrowserActivity is singleTask, it will be always the
             * root of the task. So we can use either true or false for
             * moveTaskToBack().
             */
            mActivity.moveTaskToBack(true);
            return;
        }
        if (current.canGoBack()) {
            current.goBack();
        } else {
            // Check to see if we are closing a window that was created by
            // another window. If so, we switch back to that window.
            Tab parent = current.getParent();
            if (parent != null) {
                switchToTab(parent);
                // Now we close the other tab
                closeTab(current);
            } else {
                if ((current.getAppId() != null) || current.closeOnBack()) {
                    closeCurrentTab(true);
                }
                /*
                 * Instead of finishing the activity, simply push this to the back
                 * of the stack and let ActivityManager to choose the foreground
                 * activity. As BrowserActivity is singleTask, it will be always the
                 * root of the task. So we can use either true or false for
                 * moveTaskToBack().
                 */
                mActivity.moveTaskToBack(true);
            }
        }
    }

    /**
     * helper method for key handler
     * returns the current tab if it can't advance
     */
    private Tab getNextTab() {
        int pos = mTabControl.getCurrentPosition() + 1;
        if (pos >= mTabControl.getTabCount()) {
            pos = 0;
        }
        return mTabControl.getTab(pos);
    }

    /**
     * helper method for key handler
     * returns the current tab if it can't advance
     */
    private Tab getPrevTab() {
        int pos  = mTabControl.getCurrentPosition() - 1;
        if ( pos < 0) {
            pos = mTabControl.getTabCount() - 1;
        }
        return  mTabControl.getTab(pos);
    }

    boolean isMenuOrCtrlKey(int keyCode) {
        return (KeyEvent.KEYCODE_MENU == keyCode)
                || (KeyEvent.KEYCODE_CTRL_LEFT == keyCode)
                || (KeyEvent.KEYCODE_CTRL_RIGHT == keyCode);
    }

    /**
     * handle key events in browser
     *
     * @param keyCode
     * @param event
     * @return true if handled, false to pass to super
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean noModifiers = event.hasNoModifiers();
        // Even if MENU is already held down, we need to call to super to open
        // the IME on long press.
        if (!noModifiers && isMenuOrCtrlKey(keyCode)) {
            mMenuIsDown = true;
            return false;
        }

        WebView webView = getCurrentTopWebView();
        Tab tab = getCurrentTab();
        if (webView == null || tab == null) return false;

        boolean ctrl = event.hasModifiers(KeyEvent.META_CTRL_ON);
        boolean shift = event.hasModifiers(KeyEvent.META_SHIFT_ON);

        switch(keyCode) {
            case KeyEvent.KEYCODE_TAB:
                if (event.isCtrlPressed()) {
                    if (event.isShiftPressed()) {
                        // prev tab
                        switchToTab(getPrevTab());
                    } else {
                        // next tab
                        switchToTab(getNextTab());
                    }
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_SPACE:
                // WebView/WebTextView handle the keys in the KeyDown. As
                // the Activity's shortcut keys are only handled when WebView
                // doesn't, have to do it in onKeyDown instead of onKeyUp.
                if (shift) {
                    pageUp();
                } else if (noModifiers) {
                    pageDown();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
            	if(addBookmarkViewIsShowing()) {
            		if(!bookmarkViewAnim.doNotAnim) {
            			hideBookmarkCrtPageView();
            		}
            		return true;
            	}
                if (!noModifiers) break;
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_FORWARD:
                if (!noModifiers) break;
                tab.goForward();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (ctrl) {
                    tab.goBack();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (ctrl) {
                    tab.goForward();
                    return true;
                }
                break;
//          case KeyEvent.KEYCODE_B:    // menu
//          case KeyEvent.KEYCODE_D:    // menu
//          case KeyEvent.KEYCODE_E:    // in Chrome: puts '?' in URL bar
//          case KeyEvent.KEYCODE_F:    // menu
//          case KeyEvent.KEYCODE_G:    // in Chrome: finds next match
//          case KeyEvent.KEYCODE_H:    // menu
//          case KeyEvent.KEYCODE_I:    // unused
//          case KeyEvent.KEYCODE_J:    // menu
//          case KeyEvent.KEYCODE_K:    // in Chrome: puts '?' in URL bar
//          case KeyEvent.KEYCODE_L:    // menu
//          case KeyEvent.KEYCODE_M:    // unused
//          case KeyEvent.KEYCODE_N:    // in Chrome: new window
//          case KeyEvent.KEYCODE_O:    // in Chrome: open file
//          case KeyEvent.KEYCODE_P:    // in Chrome: print page
//          case KeyEvent.KEYCODE_Q:    // unused
//          case KeyEvent.KEYCODE_R:
//          case KeyEvent.KEYCODE_S:    // in Chrome: saves page
            case KeyEvent.KEYCODE_T:
                // we can't use the ctrl/shift flags, they check for
                // exclusive use of a modifier
                if (event.isCtrlPressed()) {
                    if (event.isShiftPressed()) {
                        openIncognitoTab();
                    } else {
                        openTabToHomePage();
                    }
                    return true;
                }
                break;
//          case KeyEvent.KEYCODE_U:    // in Chrome: opens source of page
//          case KeyEvent.KEYCODE_V:    // text view intercepts to paste
//          case KeyEvent.KEYCODE_W:    // menu
//          case KeyEvent.KEYCODE_X:    // text view intercepts to cut
//          case KeyEvent.KEYCODE_Y:    // unused
//          case KeyEvent.KEYCODE_Z:    // unused
        }
        // it is a regular key and webview is not null
         return mUi.dispatchKey(keyCode, event);
    }

    /**
     * 隐藏添加书签对话框
     */
    private void hideBookmarkCrtPageView() {
		if(registeredReceiver) {
			mActivity.unregisterReceiver(receiver);
			registeredReceiver = false;
		}
		bookmarkViewAnim.startAnim(AddBookmarkViewAnim.NORMAL_HIDE);
	}
    
    @Override
	public void showBookmarkCrtPageView() {
		if(addBookmarkContainer != null && addBookmarkContainer.getVisibility() == View.GONE) {
			mActivity.registerReceiver(receiver, new IntentFilter(Intent.ACTION_INPUT_METHOD_SHOW));
			mActivity.registerReceiver(receiver, new IntentFilter(Intent.ACTION_INPUT_METHOD_HIDE));
			
			registeredReceiver = true;
			setBookmarkCrtPageViewData();
			bookmarkViewAnim.startAnim(AddBookmarkViewAnim.NORMAL_SHOW);
			addBookmarkContainer.setVisibility(View.VISIBLE);
		}
		
	}
    
    /**
     * 设置添加书签弹出框数据
     */
    private void setBookmarkCrtPageViewData() {
    	final WebView w = getCurrentTopWebView();
    	if(w == null) {
    		return;
    	}
    	setCrtBookmarkDialog();
		btnCancleBkmk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(AddBookmarkViewAnim.keyboardShowing) {
					addBkmkViewDownHide = true;
					bookmarkViewAnim.setAddBkmkViewDownHide(addBkmkViewDownHide);
					hideSystemKeyBoard(mActivity, addBookmarkView);
				}else {
					bookmarkViewAnim.startAnim(AddBookmarkViewAnim.NORMAL_HIDE);
				}
			}
		});
		btnSaveBkmk.setEnabled(true);
		btnSaveBkmk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String title = etBookmarkTitle.getText().toString().trim();
            	String url = etBookmarkUrl.getText().toString().trim();
            	Bitmap bmpFavicon = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.default_icon_title);
            	if(iconMap.containsKey(url) && iconMap.get(url).get() != null) {
            		bmpFavicon = iconMap.get(url).get();
            	}
            	
            	if(save(title, url, w.getTouchIconUrl(), createScreenshot(w, getDesiredThumbnailWidth(mActivity),
                        getDesiredThumbnailHeight(mActivity)), bmpFavicon)) {
            		// TODO 动画
            		if(ivBookmarkThumb != null) {
            			int location[] = new int[2];
            			ivBookmarkThumb.getLocationOnScreen(location);
            			if(AddBookmarkViewAnim.keyboardShowing) {
        					addBkmkViewTransHide = true;
        					hideSystemKeyBoard(mActivity, addBookmarkView);
        				}
        				bookmarkViewAnim.startAnim(AddBookmarkViewAnim.TRANS_AND_HIDE,toolbarBookmark);
        				v.setEnabled(false);
            		}
            	};
			}
		});
    	
	}

	/**
     * 添加书签对话框是否显示
     * @return
     */
	private boolean addBookmarkViewIsShowing() {
    	if(addBookmarkContainer != null && addBookmarkContainer.getVisibility() == View.VISIBLE) {
    		return true;
    	}
		return false;
	}

	@Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch(keyCode) {
        case KeyEvent.KEYCODE_BACK:
            if (mUi.isWebShowing()) {
                bookmarksOrHistoryPicker(ComboViews.History);
                return true;
            }
            break;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isMenuOrCtrlKey(keyCode)) {
            mMenuIsDown = false;
            if (KeyEvent.KEYCODE_MENU == keyCode
                    && event.isTracking() && !event.isCanceled()) {
                return onMenuKey();
            }
        }
        if (!event.hasNoModifiers()) return false;
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (event.isTracking() && !event.isCanceled()) {
                    onBackKey();
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean isMenuDown() {
        return mMenuIsDown;
    }

    @Override
    public boolean onSearchRequested() {
        mUi.editUrl(false, true);
        return true;
    }

    @Override
    public boolean shouldCaptureThumbnails() {
        return mUi.shouldCaptureThumbnails();
    }

    @Override
    public boolean supportsVoice() {
        PackageManager pm = mActivity.getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    @Override
    public void startVoiceRecognizer() {
        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        mActivity.startActivityForResult(voice, VOICE_RESULT);
    }

    @Override
    public void setBlockEvents(boolean block) {
        mBlockEvents = block;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return mBlockEvents;
    }

	@Override
	public void changeStatueOfWebToolsButton() {
		// TODO Auto-generated method stub
		mUi.changeStatueOfWebToolsForwardBackword();
		
	}

	@Override
	public void setDefaultBookmarkSaveFolder() {
		selectedFolderId = 1;
        selectedFolderName = mActivity.getResources().getString(R.string.bookmarks);
	}

	@Override
	public void setAddBookmarkView(FrameLayout frameLayout) {
		LinearLayout bookmarkView = (LinearLayout)frameLayout.findViewById(R.id.ll_add_bookmark_view);
		toolbarBookmark = (ImageView)frameLayout.findViewById(R.id.toolbar_bookmark);
		addBookmarkContainer = (LinearLayout) frameLayout.findViewById(R.id.ll_add_bookmark_container);
		addBookmarkContainer.setBackgroundColor(0x99000000);
		addBookmarkView = bookmarkView;
		bookmarkViewAnim = new AddBookmarkViewAnim(this.addBookmarkView,this);
		addbookmarkBg = R.anim.add_bookmark_star;
		addbookmarkSrc = R.drawable.toolbar_selector_tab_bookmark;
    	ivBookmarkThumb = (ImageView)addBookmarkContainer.findViewById(R.id.iv_add_to_bookmarks_thumb);
    	etBookmarkTitle = (AuroraEditText)addBookmarkContainer.findViewById(R.id.et_add_to_bookmarks_title);
    	etBookmarkUrl = (AuroraEditText)addBookmarkContainer.findViewById(R.id.et_add_to_bookmarks_url);
    	((TextView)addBookmarkContainer.findViewById(R.id.tv_add_to_bookmarks_location))
    									.setText(mActivity.getResources().getString(R.string.pref_privacy_location_title));
		tvSaveFolder = (TextView)addBookmarkContainer.findViewById(R.id.tv_add_to_bookmarks_save_foler);
		LinearLayout llChooseSaveFolder = (LinearLayout)addBookmarkContainer.findViewById(R.id.ll_add_to_bookmarks_save_foler);
    	llChooseSaveFolder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), BookmarksFolderActivity.class);
				intent.putExtra("selected_id", selectedFolderId);
				intent.putExtra("selected_title", selectedFolderName);
				getActivity().startActivityForResult(intent, SELECT_FOLDER_RESULT);
			}
		});
    	btnCancleBkmk = (Button)addBookmarkContainer.findViewById(R.id.btn_add_bookmark_view_cancle);
    	btnSaveBkmk = (Button)addBookmarkContainer.findViewById(R.id.btn_add_bookmark_view_save);
    	etBookmarkTitle.addTextChangedListener(new EditTextTextWatcher(btnSaveBkmk, etBookmarkTitle, etBookmarkUrl));
    	etBookmarkUrl.addTextChangedListener(new EditTextTextWatcher(btnSaveBkmk, etBookmarkTitle, etBookmarkUrl));
	}
	
	/**
	 * 隐藏系统键盘
	 * @param context
	 * @param v
	 */
	private void hideSystemKeyBoard(Context context,View v) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

}