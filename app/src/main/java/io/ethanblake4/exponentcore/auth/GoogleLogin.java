package io.ethanblake4.exponentcore.auth;

import android.support.annotation.Nullable;
import android.util.ArrayMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.ethanblake4.exponentcore.Exponent;
import io.ethanblake4.exponentcore.Logging;
import io.ethanblake4.exponentcore.model.GoogleTokenInfo;
import io.ethanblake4.exponentcore.model.error.AuthException;
import io.ethanblake4.exponentcore.model.error.NeedsBrowserException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleLogin {

    private static final String LOGIN_URL = "https://android.clients.google.com/auth";

    private static ArrayMap<String, String> parseAuthResponse(InputStream in) throws IOException {
        ArrayMap<String, String> response = new ArrayMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            Logging.i(line);
            String[] s = line.split("=", 2);
            response.put(s[0], s[1]);
        }
        return response;
    }

    /**
     * Performs a Login HTTP request using {@link OkHttpClient}
     *
     * @param url     URL
     * @param body a form body containing login params
     * @return ArrayMap of response values
     */
    private static ArrayMap<String, String> okLoginCall(String url, FormBody body) throws IOException {
        Request request = new Request.Builder()
                .header("User-Agent", Exponent.INSTANCE.getLoginUserAgent())
                .url(url).post(body).build();
        Response r = Exponent.client.newCall(request).execute();
        if (r.body() != null) return parseAuthResponse(Objects.requireNonNull(r.body()).byteStream());
        else {
            Logging.e(r.code() + " returned by login call 1, no response body so returning null");
            return null;
        }
    }

    private static FormBody.Builder baseFormBuilder(String email, String device_id, String service) {
        return new FormBody.Builder()
                .add("accountType", "HOSTED_OR_GOOGLE")
                .add("Email", email)
                .add("has_permission", "1")
                .add("source", "android")
                .add("androidId", device_id)
                .add("service", service)
                .add("device_country", Exponent.INSTANCE.getDeviceCountryCode())
                .add("operatorCountry", Exponent.INSTANCE.getOperatorCountryCode())
                .add("lang", Exponent.INSTANCE.getDeviceLanguage())
                .add("sdk_version", String.valueOf(Exponent.INSTANCE.getLoginSDKVersion()));
    }

    private static FormBody createMasterAuthForm(String email, String password) throws IOException, GeneralSecurityException {
        return baseFormBuilder(email, Exponent.INSTANCE.getAndroidID(), "ac2dm")
                .add("EncryptedPasswd", AuthEncryptor.encrypt(email, password))
                .build();
    }

    private static FormBody createClientLoginForm(String email, String masterToken, String service, String app, String sig) {
        return baseFormBuilder(email, Exponent.INSTANCE.getGsfID(), service)
                .add("add_account", "1")
                .add("EncryptedPasswd", masterToken)
                .add("app", app)
                .add("client_sig", sig != null ? sig : "38918a453d07199354f8b19af05ec6562ced5788")
                .build();
    }

    private static FormBody createOAuthForm(String email, String masterToken,
                                            String service, String app, String sig,
                                            String callerSig, boolean system) {
        return baseFormBuilder(email, Exponent.INSTANCE.getGsfID(), service)
                .add("app", "com.google.android.music")
                .add("callerPkg", app)
                .add("callerSig", callerSig != null ? callerSig : "38918a453d07199354f8b19af05ec6562ced5788")
                .add("client_sig", sig != null ? sig : "38918a453d07199354f8b19af05ec6562ced5788")
                .add("ACCESS_TOKEN", "1")
                .add("system_partition", system ? "1" : "0")
                .add("Token", masterToken)
                .build();
    }

    private static GoogleTokenInfo masterAuthInfo(ArrayMap<String,String> response) throws NeedsBrowserException, AuthException {
        List<String> services = null;
        String SID = null;
        String LSID = null;

        if (!response.containsKey("Token")) {
            Logging.w("masterAuthInfo(): Token not found in response");
            if(response.containsKey("Error"))  {
                Logging.w("Response contains error: "+response.get("Error"));
                if(response.get("Error").trim().equals("NeedsBrowser")) {
                    throw new NeedsBrowserException(response.get("Url"));
                } else {
                    throw new AuthException(response.get("Error"));
                }
            }
            return null;
        }

        if(response.containsKey("services"))
            services = Arrays.asList(response.get("services").split(","));
        if(response.containsKey("SID")) SID = response.get("SID");
        if(response.containsKey("LSID")) LSID = response.get("LSID");

        return new GoogleTokenInfo(response.get("Token"), SID, LSID, services);
    }

    /* -------------- PUBLIC METHODS --------------- */

    /**
     * Retrieve an Android master token
     * This endpoint is usually called by Google Play services to register a device
     * on initial activation.
     * @param email user's email
     * @param password user's password, will be encrypted
     * @return the master token
     */
    @Nullable
    public static GoogleTokenInfo masterAuthSync(String email, String password)
            throws NeedsBrowserException, AuthException, IOException, GeneralSecurityException {

        ArrayMap<String, String> response = okLoginCall(LOGIN_URL, createMasterAuthForm(email, password));

        if (response == null) {
            Logging.d("Null response received in masterAuthSync()");
            return null;
        }

        return masterAuthInfo(response);
    }

}
