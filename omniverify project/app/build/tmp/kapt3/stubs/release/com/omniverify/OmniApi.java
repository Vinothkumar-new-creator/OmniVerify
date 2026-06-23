package com.omniverify;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\'J$\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0014\b\u0001\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\tH\'J$\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0014\b\u0001\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\tH\'J$\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0014\b\u0001\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\tH\'\u00a8\u0006\r\u00c0\u0006\u0003"}, d2 = {"Lcom/omniverify/OmniApi;", "", "verifyImage", "Lretrofit2/Call;", "Lcom/omniverify/VerifyResponse;", "file", "Lokhttp3/MultipartBody$Part;", "verifyText", "body", "", "", "verifyLink", "verifyQr", "app_release"})
public abstract interface OmniApi {
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "verify")
    @org.jetbrains.annotations.NotNull()
    public abstract retrofit2.Call<com.omniverify.VerifyResponse> verifyImage(@retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file);
    
    @retrofit2.http.POST(value = "verify-text")
    @org.jetbrains.annotations.NotNull()
    public abstract retrofit2.Call<com.omniverify.VerifyResponse> verifyText(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> body);
    
    @retrofit2.http.POST(value = "verify-link")
    @org.jetbrains.annotations.NotNull()
    public abstract retrofit2.Call<com.omniverify.VerifyResponse> verifyLink(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> body);
    
    @retrofit2.http.POST(value = "verify-qr")
    @org.jetbrains.annotations.NotNull()
    public abstract retrofit2.Call<com.omniverify.VerifyResponse> verifyQr(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> body);
}