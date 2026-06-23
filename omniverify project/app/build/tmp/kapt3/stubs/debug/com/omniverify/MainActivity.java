package com.omniverify;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010!H\u0014J\b\u0010\"\u001a\u00020\u001fH\u0014J\b\u0010#\u001a\u00020\u001fH\u0002J\b\u0010$\u001a\u00020\u001fH\u0002J\b\u0010%\u001a\u00020\u001fH\u0002J\b\u0010&\u001a\u00020\u001fH\u0002J\b\u0010\'\u001a\u00020\u001fH\u0002J\b\u0010(\u001a\u00020\u001fH\u0002J\u0010\u0010)\u001a\u00020\u001f2\u0006\u0010*\u001a\u00020\u0005H\u0002J\u0010\u0010+\u001a\u00020\u001f2\u0006\u0010,\u001a\u00020\u0005H\u0002J\b\u0010-\u001a\u00020\u001fH\u0014R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u00020\u001cX\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u001d\u00a8\u0006."}, d2 = {"Lcom/omniverify/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "<init>", "()V", "isServiceActive", "", "btnToggle", "Landroid/widget/FrameLayout;", "ivToggleIcon", "Landroid/widget/ImageView;", "tvToggleAction", "Landroid/widget/TextView;", "statusDot", "Landroid/view/View;", "tvStatusBadge", "tvStatusMessage", "rvHistory", "Landroidx/recyclerview/widget/RecyclerView;", "historyAdapter", "Lcom/omniverify/ScanHistoryAdapter;", "clHistoryOverlay", "ivMenu", "btnCloseHistory", "overlayPermissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "projectionLauncher", "statusReceiver", "Landroid/content/BroadcastReceiver;", "Landroid/content/BroadcastReceiver;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "setupHistoryRecyclerView", "setupBottomNavigation", "checkPermissionAndStart", "requestMediaProjection", "startFloatingService", "stopFloatingService", "updateUI", "active", "showHistory", "show", "onDestroy", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private boolean isServiceActive = false;
    private android.widget.FrameLayout btnToggle;
    private android.widget.ImageView ivToggleIcon;
    private android.widget.TextView tvToggleAction;
    private android.view.View statusDot;
    private android.widget.TextView tvStatusBadge;
    private android.widget.TextView tvStatusMessage;
    private androidx.recyclerview.widget.RecyclerView rvHistory;
    private com.omniverify.ScanHistoryAdapter historyAdapter;
    private android.view.View clHistoryOverlay;
    private android.widget.ImageView ivMenu;
    private android.widget.ImageView btnCloseHistory;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> overlayPermissionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> projectionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver statusReceiver = null;
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    private final void setupHistoryRecyclerView() {
    }
    
    private final void setupBottomNavigation() {
    }
    
    private final void checkPermissionAndStart() {
    }
    
    private final void requestMediaProjection() {
    }
    
    private final void startFloatingService() {
    }
    
    private final void stopFloatingService() {
    }
    
    private final void updateUI(boolean active) {
    }
    
    private final void showHistory(boolean show) {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
}