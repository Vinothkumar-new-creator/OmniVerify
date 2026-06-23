package com.omniverify;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0092\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 L2\u00020\u0001:\u0001LB\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0014\u0010\u001f\u001a\u0004\u0018\u00010 2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0016J\b\u0010#\u001a\u00020$H\u0016J\b\u0010%\u001a\u00020$H\u0002J\b\u0010&\u001a\u00020$H\u0002J\b\u0010\'\u001a\u00020$H\u0002J\b\u0010(\u001a\u00020$H\u0002J\u0010\u0010)\u001a\u00020$2\u0006\u0010*\u001a\u00020\u000bH\u0002J \u0010+\u001a\u00020$2\u0006\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020-2\u0006\u0010*\u001a\u00020\u000bH\u0002J(\u0010/\u001a\u00020$2\u0006\u00100\u001a\u0002012\u0006\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020-2\u0006\u0010*\u001a\u00020\u000bH\u0002J\b\u00102\u001a\u00020$H\u0002J\b\u00103\u001a\u00020$H\u0002J\u0018\u00104\u001a\u00020$2\u0006\u00105\u001a\u00020\u001e2\u0006\u0010*\u001a\u00020\u000bH\u0002J\u0018\u00106\u001a\u00020$2\u0006\u00107\u001a\u00020\u001e2\u0006\u00108\u001a\u00020\u0007H\u0002J\u0010\u00109\u001a\u00020$2\u0006\u0010:\u001a\u00020;H\u0002J\b\u0010<\u001a\u00020$H\u0002J\u0018\u0010=\u001a\u00020$2\u0006\u00107\u001a\u00020\u001e2\u0006\u00108\u001a\u00020\u0007H\u0002J\u0010\u0010>\u001a\u00020$2\u0006\u0010?\u001a\u00020\u000bH\u0002J\u0018\u0010@\u001a\u00020$2\u0006\u0010?\u001a\u00020\u000b2\u0006\u0010A\u001a\u00020;H\u0002J\u0016\u0010B\u001a\u00020$2\u0006\u0010C\u001a\u00020;2\u0006\u0010D\u001a\u00020-J\b\u0010E\u001a\u00020$H\u0002J\u0010\u0010F\u001a\u00020$2\u0006\u0010G\u001a\u00020HH\u0002J\b\u0010I\u001a\u00020JH\u0002J\b\u0010K\u001a\u00020$H\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006M"}, d2 = {"Lcom/omniverify/FloatingAssistantService;", "Landroid/app/Service;", "<init>", "()V", "windowManager", "Landroid/view/WindowManager;", "floatingView", "Landroid/view/View;", "params", "Landroid/view/WindowManager$LayoutParams;", "isMenuVisible", "", "hideHandler", "Landroid/os/Handler;", "hideRunnable", "Ljava/lang/Runnable;", "isTucked", "mediaProjectionManager", "Landroid/media/projection/MediaProjectionManager;", "mediaProjection", "Landroid/media/projection/MediaProjection;", "virtualDisplay", "Landroid/hardware/display/VirtualDisplay;", "imageReader", "Landroid/media/ImageReader;", "miniResultView", "cropOverlayView", "api", "Lcom/omniverify/OmniApi;", "fullScreenshot", "Landroid/graphics/Bitmap;", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "", "resetHideTimer", "tuckAssistantIntoCorner", "untuckAssistantFromCorner", "setupFloatingButton", "takeScreenshotAndLaunchCrop", "isQr", "captureImageFromReader", "width", "", "height", "processCapturedImage", "image", "Landroid/media/Image;", "stopVirtualDisplay", "fullStopProjection", "showCropOverlay", "screenshot", "scanAndVerifyQr", "bitmap", "overlayView", "sendQrToBackend", "qrData", "", "dismissCropOverlay", "uploadCroppedImage", "showAnalysisDialog", "isText", "performAnalysis", "input", "showMiniResultPopup", "verdict", "confidence", "dismissMiniResult", "toggleMenu", "menu", "Landroid/widget/LinearLayout;", "createNotification", "Landroid/app/Notification;", "onDestroy", "Companion", "app_release"})
public final class FloatingAssistantService extends android.app.Service {
    private android.view.WindowManager windowManager;
    private android.view.View floatingView;
    private android.view.WindowManager.LayoutParams params;
    private boolean isMenuVisible = false;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler hideHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable hideRunnable = null;
    private boolean isTucked = false;
    @org.jetbrains.annotations.Nullable()
    private android.media.projection.MediaProjectionManager mediaProjectionManager;
    @org.jetbrains.annotations.Nullable()
    private android.media.projection.MediaProjection mediaProjection;
    @org.jetbrains.annotations.Nullable()
    private android.hardware.display.VirtualDisplay virtualDisplay;
    @org.jetbrains.annotations.Nullable()
    private android.media.ImageReader imageReader;
    @org.jetbrains.annotations.Nullable()
    private android.view.View miniResultView;
    @org.jetbrains.annotations.Nullable()
    private android.view.View cropOverlayView;
    private com.omniverify.OmniApi api;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap fullScreenshot;
    private static int projectionResultCode = -1;
    @org.jetbrains.annotations.Nullable()
    private static android.content.Intent projectionIntent;
    @org.jetbrains.annotations.NotNull()
    public static final com.omniverify.FloatingAssistantService.Companion Companion = null;
    
    public FloatingAssistantService() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void resetHideTimer() {
    }
    
    private final void tuckAssistantIntoCorner() {
    }
    
    private final void untuckAssistantFromCorner() {
    }
    
    private final void setupFloatingButton() {
    }
    
    private final void takeScreenshotAndLaunchCrop(boolean isQr) {
    }
    
    private final void captureImageFromReader(int width, int height, boolean isQr) {
    }
    
    private final void processCapturedImage(android.media.Image image, int width, int height, boolean isQr) {
    }
    
    private final void stopVirtualDisplay() {
    }
    
    private final void fullStopProjection() {
    }
    
    private final void showCropOverlay(android.graphics.Bitmap screenshot, boolean isQr) {
    }
    
    private final void scanAndVerifyQr(android.graphics.Bitmap bitmap, android.view.View overlayView) {
    }
    
    private final void sendQrToBackend(java.lang.String qrData) {
    }
    
    private final void dismissCropOverlay() {
    }
    
    private final void uploadCroppedImage(android.graphics.Bitmap bitmap, android.view.View overlayView) {
    }
    
    private final void showAnalysisDialog(boolean isText) {
    }
    
    private final void performAnalysis(boolean isText, java.lang.String input) {
    }
    
    public final void showMiniResultPopup(@org.jetbrains.annotations.NotNull()
    java.lang.String verdict, int confidence) {
    }
    
    private final void dismissMiniResult() {
    }
    
    private final void toggleMenu(android.widget.LinearLayout menu) {
    }
    
    private final android.app.Notification createNotification() {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001c\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0010"}, d2 = {"Lcom/omniverify/FloatingAssistantService$Companion;", "", "<init>", "()V", "projectionResultCode", "", "getProjectionResultCode", "()I", "setProjectionResultCode", "(I)V", "projectionIntent", "Landroid/content/Intent;", "getProjectionIntent", "()Landroid/content/Intent;", "setProjectionIntent", "(Landroid/content/Intent;)V", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final int getProjectionResultCode() {
            return 0;
        }
        
        public final void setProjectionResultCode(int p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final android.content.Intent getProjectionIntent() {
            return null;
        }
        
        public final void setProjectionIntent(@org.jetbrains.annotations.Nullable()
        android.content.Intent p0) {
        }
    }
}