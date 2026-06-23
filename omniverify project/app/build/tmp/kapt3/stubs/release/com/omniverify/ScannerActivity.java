package com.omniverify;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0018H\u0014J\u0010\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\u0018H\u0014J\b\u0010\u001b\u001a\u00020\u0016H\u0002J\b\u0010\u001c\u001a\u00020\u0016H\u0002J\b\u0010\u001d\u001a\u00020\u0016H\u0002J\b\u0010\u001e\u001a\u00020\u0016H\u0002J\b\u0010\u001f\u001a\u00020\u0016H\u0002J\u0010\u0010 \u001a\u00020\u00162\u0006\u0010!\u001a\u00020\rH\u0002J\b\u0010\"\u001a\u00020\u0016H\u0002J\u0010\u0010#\u001a\u00020\u00162\u0006\u0010$\u001a\u00020\u000bH\u0002J\u0010\u0010%\u001a\u00020\u00162\u0006\u0010$\u001a\u00020\u000bH\u0002J\b\u0010&\u001a\u00020\u0016H\u0002J\u0010\u0010\'\u001a\u00020\u00162\u0006\u0010(\u001a\u00020)H\u0002J\u0010\u0010*\u001a\u00020\u00162\u0006\u0010+\u001a\u00020\u0007H\u0002J\u0010\u0010,\u001a\u00020\u00162\u0006\u0010-\u001a\u00020\u0007H\u0002J\u0016\u0010.\u001a\u00020\u00162\f\u0010/\u001a\b\u0012\u0004\u0012\u00020100H\u0002J\u0018\u00102\u001a\u00020\u00162\u0006\u00103\u001a\u0002012\u0006\u00104\u001a\u000205H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\r0\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00066"}, d2 = {"Lcom/omniverify/ScannerActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "<init>", "()V", "binding", "Lcom/omniverify/databinding/ActivityScannerBaseBinding;", "scanType", "", "selectedImageFile", "Ljava/io/File;", "targetBitmap", "Landroid/graphics/Bitmap;", "photoUri", "Landroid/net/Uri;", "api", "Lcom/omniverify/OmniApi;", "pickImage", "Landroidx/activity/result/ActivityResultLauncher;", "Landroidx/activity/result/PickVisualMediaRequest;", "takePhoto", "requestPermissionLauncher", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onSaveInstanceState", "outState", "setupBottomNavigation", "setupUI", "setupListeners", "checkPermissionAndLaunchCamera", "launchCamera", "startCropping", "uri", "confirmCrop", "processQRLocally", "bitmap", "uploadCroppedImage", "performAnalysis", "showLoading", "isLoading", "", "verifyText", "text", "verifyLink", "url", "handleResponse", "response", "Lretrofit2/Response;", "Lcom/omniverify/VerifyResponse;", "saveScanToHistory", "body", "confidence", "", "app_release"})
public final class ScannerActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.omniverify.databinding.ActivityScannerBaseBinding binding;
    private java.lang.String scanType;
    @org.jetbrains.annotations.Nullable()
    private java.io.File selectedImageFile;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap targetBitmap;
    @org.jetbrains.annotations.Nullable()
    private android.net.Uri photoUri;
    @org.jetbrains.annotations.NotNull()
    private final com.omniverify.OmniApi api = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> pickImage = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> takePhoto = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestPermissionLauncher = null;
    
    public ScannerActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onSaveInstanceState(@org.jetbrains.annotations.NotNull()
    android.os.Bundle outState) {
    }
    
    private final void setupBottomNavigation() {
    }
    
    private final void setupUI() {
    }
    
    private final void setupListeners() {
    }
    
    private final void checkPermissionAndLaunchCamera() {
    }
    
    private final void launchCamera() {
    }
    
    private final void startCropping(android.net.Uri uri) {
    }
    
    private final void confirmCrop() {
    }
    
    private final void processQRLocally(android.graphics.Bitmap bitmap) {
    }
    
    private final void uploadCroppedImage(android.graphics.Bitmap bitmap) {
    }
    
    private final void performAnalysis() {
    }
    
    private final void showLoading(boolean isLoading) {
    }
    
    private final void verifyText(java.lang.String text) {
    }
    
    private final void verifyLink(java.lang.String url) {
    }
    
    private final void handleResponse(retrofit2.Response<com.omniverify.VerifyResponse> response) {
    }
    
    private final void saveScanToHistory(com.omniverify.VerifyResponse body, int confidence) {
    }
}