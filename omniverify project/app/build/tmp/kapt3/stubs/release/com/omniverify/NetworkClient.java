package com.omniverify;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u0012\u001a\u00020\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\f\u001a\u00020\r8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0014"}, d2 = {"Lcom/omniverify/NetworkClient;", "", "<init>", "()V", "BASE_URL", "", "APP_SHARED_SECRET", "authInterceptor", "Lokhttp3/Interceptor;", "okHttpClient", "Lokhttp3/OkHttpClient;", "warmUpClient", "api", "Lcom/omniverify/OmniApi;", "getApi", "()Lcom/omniverify/OmniApi;", "api$delegate", "Lkotlin/Lazy;", "warmUp", "", "app_release"})
public final class NetworkClient {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String BASE_URL = "https://omniverify.onrender.com/";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String APP_SHARED_SECRET = "CHARLIEISTHEBEST";
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.Interceptor authInterceptor = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient okHttpClient = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.OkHttpClient warmUpClient = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy api$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.omniverify.NetworkClient INSTANCE = null;
    
    private NetworkClient() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.omniverify.OmniApi getApi() {
        return null;
    }
    
    /**
     * Fire-and-forget ping to /health. Call this the moment the floating
     * assistant turns on (or the app opens) -- NOT right before a scan.
     * If the backend is on a free tier that sleeps after inactivity, this
     * gives it time to wake up in the background while the user is still
     * looking for something to crop, instead of making the actual scan
     * request eat that cold-start delay.
     */
    public final void warmUp() {
    }
}