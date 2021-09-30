/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.utility;

import com.c12e.cortex5.BasePluginHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Objects;

public class CortexPluginHelper extends BasePluginHelper implements HarnessPluginHelper {

    public static final String BASE_DIR = "/tmp/jdbc-service/";
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    public static final String SHARED_PROJECT = "shared";

    private String jwt;
    private String apiEndpoint;
    private Method classLoaderAddPath;
    private String implementationVersion;

    public CortexPluginHelper() {

    }


    protected File getLocalFile(String location) {
        File localFile = Paths.get(BASE_DIR + location).normalize().toFile();
        getLogger(this.getClass()).warn("Found file at: " + localFile.toURI().toString());
        if (localFile.exists() && FileUtils.sizeOf(localFile) > 0) {
            return localFile;
        }
        return null;
    }

    @Override
    public File getManagedContent(String location) {
        try {
            File localFile = getLocalFile(location);
            if (Objects.nonNull(localFile)) {
                System.out.println("Found file at: " + localFile.toURI().toString());
                return localFile;
            }
            Request request = new Request.Builder().url(String.format("%s/fabric/v4/projects/%s/content/%s",
                    apiEndpoint, SHARED_PROJECT, location))
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();
            Response res = HTTP_CLIENT.newCall(request).execute();
            if (!res.isSuccessful()) {
                throw new RuntimeException("Unable to download: " + location);
            }
            File downloaded = Paths.get(BASE_DIR + location).normalize().toFile();
            FileUtils.copyInputStreamToFile(res.body().byteStream(), downloaded);
            addToClasspath(downloaded.toURI().toURL());
            return downloaded;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public void addToClasspath(URL path) {
        try {
            if (Objects.isNull(classLoaderAddPath)) {
                Class<URLClassLoader> urlClass = URLClassLoader.class;
                classLoaderAddPath = urlClass.getDeclaredMethod("addURL", URL.class);
                classLoaderAddPath.setAccessible(true);
            }

            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            classLoaderAddPath.invoke(urlClassLoader, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getImplementationVersion() {
        if (Objects.isNull(implementationVersion)) {
            implementationVersion = getClass().getPackage().getImplementationVersion();
        }

        return implementationVersion;
    }
}
