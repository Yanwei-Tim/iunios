

package  com.android.aurora;

import android.net.Uri;


oneway interface IAuroraMediaScannerListener
{
    /**
     * Called when a IMediaScannerService.scanFile() call has completed.
     * @param path the path to the file that has been scanned.
     * @param uri the Uri for the file if the scanning operation succeeded 
     * and the file was added to the media database, or null if scanning failed. 
     */
    void auroraScanCompleted(String path, in Uri uri);
}