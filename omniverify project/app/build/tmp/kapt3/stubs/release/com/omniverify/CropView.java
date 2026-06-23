package com.omniverify;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001:\u0001;B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJ(\u0010\u001f\u001a\u00020\u000e2\u0006\u0010 \u001a\u00020\u00072\u0006\u0010!\u001a\u00020\u00072\u0006\u0010\"\u001a\u00020\u00072\u0006\u0010#\u001a\u00020\u0007H\u0014J\u0006\u0010$\u001a\u00020\u000eJ\u001a\u0010%\u001a\u00020\u000e2\b\b\u0002\u0010 \u001a\u00020\u00072\b\b\u0002\u0010!\u001a\u00020\u0007J\u0010\u0010&\u001a\u00020\u000e2\u0006\u0010\'\u001a\u00020(H\u0014J \u0010)\u001a\u00020\u000e2\u0006\u0010\'\u001a\u00020(2\u0006\u0010*\u001a\u00020\u00162\u0006\u0010+\u001a\u00020\u0016H\u0002J\u0010\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020/H\u0016J\u0018\u00100\u001a\u00020\u00072\u0006\u0010*\u001a\u00020\u00162\u0006\u0010+\u001a\u00020\u0016H\u0002J(\u00101\u001a\u00020-2\u0006\u00102\u001a\u00020\u00162\u0006\u00103\u001a\u00020\u00162\u0006\u00104\u001a\u00020\u00162\u0006\u00105\u001a\u00020\u0016H\u0002J\u0018\u00106\u001a\u00020\u000e2\u0006\u00107\u001a\u00020\u00162\u0006\u00108\u001a\u00020\u0016H\u0002J\b\u00109\u001a\u00020\u000eH\u0002J\b\u0010:\u001a\u0004\u0018\u00010\u000bR\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R*\u0010\f\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u000b\u0012\u0004\u0012\u00020\u000e\u0018\u00010\rX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0016X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006<"}, d2 = {"Lcom/omniverify/CropView;", "Landroid/view/View;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "<init>", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "cropRect", "Landroid/graphics/RectF;", "onCropChangeListener", "Lkotlin/Function1;", "", "getOnCropChangeListener", "()Lkotlin/jvm/functions/Function1;", "setOnCropChangeListener", "(Lkotlin/jvm/functions/Function1;)V", "mode", "Lcom/omniverify/CropView$TouchMode;", "lastTouchX", "", "lastTouchY", "activeHandle", "handleRadius", "paint", "Landroid/graphics/Paint;", "handlePaint", "eraserPaint", "overlayPaint", "onSizeChanged", "w", "h", "oldw", "oldh", "clear", "resetCropRect", "onDraw", "canvas", "Landroid/graphics/Canvas;", "drawHandle", "x", "y", "onTouchEvent", "", "event", "Landroid/view/MotionEvent;", "getClickedHandle", "isNear", "x1", "y1", "x2", "y2", "resizeRect", "dx", "dy", "normalizeBounds", "getCropRect", "TouchMode", "app_release"})
public final class CropView extends android.view.View {
    @org.jetbrains.annotations.Nullable()
    private android.graphics.RectF cropRect;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super android.graphics.RectF, kotlin.Unit> onCropChangeListener;
    @org.jetbrains.annotations.NotNull()
    private com.omniverify.CropView.TouchMode mode = com.omniverify.CropView.TouchMode.NONE;
    private float lastTouchX = 0.0F;
    private float lastTouchY = 0.0F;
    private int activeHandle = -1;
    private final float handleRadius = 45.0F;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint paint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint handlePaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint eraserPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint overlayPaint = null;
    
    @kotlin.jvm.JvmOverloads()
    public CropView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads()
    public CropView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads()
    public CropView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<android.graphics.RectF, kotlin.Unit> getOnCropChangeListener() {
        return null;
    }
    
    public final void setOnCropChangeListener(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super android.graphics.RectF, kotlin.Unit> p0) {
    }
    
    @java.lang.Override()
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }
    
    public final void clear() {
    }
    
    public final void resetCropRect(int w, int h) {
    }
    
    @java.lang.Override()
    protected void onDraw(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas canvas) {
    }
    
    private final void drawHandle(android.graphics.Canvas canvas, float x, float y) {
    }
    
    @java.lang.Override()
    public boolean onTouchEvent(@org.jetbrains.annotations.NotNull()
    android.view.MotionEvent event) {
        return false;
    }
    
    private final int getClickedHandle(float x, float y) {
        return 0;
    }
    
    private final boolean isNear(float x1, float y1, float x2, float y2) {
        return false;
    }
    
    private final void resizeRect(float dx, float dy) {
    }
    
    private final void normalizeBounds() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.graphics.RectF getCropRect() {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/omniverify/CropView$TouchMode;", "", "<init>", "(Ljava/lang/String;I)V", "NONE", "DRAWING", "MOVING", "RESIZING", "app_release"})
    static enum TouchMode {
        /*public static final*/ NONE /* = new NONE() */,
        /*public static final*/ DRAWING /* = new DRAWING() */,
        /*public static final*/ MOVING /* = new MOVING() */,
        /*public static final*/ RESIZING /* = new RESIZING() */;
        
        TouchMode() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.omniverify.CropView.TouchMode> getEntries() {
            return null;
        }
    }
}