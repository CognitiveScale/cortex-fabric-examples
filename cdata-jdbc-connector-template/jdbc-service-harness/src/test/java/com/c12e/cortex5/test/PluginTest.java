/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
//package com.c12e.cortex5.test;
//
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.c12e.cortex5.utility.CortexPluginHelper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.networknt.server.Server;
//import okhttp3.*;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.h2.Driver;
//import org.h2.tools.RunScript;
//import org.junit.*;
//import org.junit.contrib.java.lang.system.EnvironmentVariables;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.nio.file.Paths;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class PluginTest {
//
//    @Rule
//    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
//    private static final String CONNECTION_URL = "jdbc:h2:mem:test";
//    private static final String CONNECTION_URL_CREATE = CONNECTION_URL + ";INIT=RUNSCRIPT FROM 'classpath:sql/create.sql'";
//
//    @BeforeClass
//    public static void createDB() throws SQLException {
//        DriverManager.registerDriver(new Driver());
//        DriverManager.getConnection(CONNECTION_URL_CREATE, "user", "password");
//    }
//
//    @Before
//    public void cleanDB() throws SQLException {
//        try {
//            File script = new File(getClass().getResource("/sql/data.sql").getFile());
//            RunScript.execute(DriverManager.getConnection(CONNECTION_URL, "user", "password"), new FileReader(script));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException("could not initialize with script");
//        }
//    }
//
//    @Test
//    public void testLifeCycle() throws Exception {
//
//        if (StringUtils.isBlank(System.getenv("plugin.jar"))) {
//            environmentVariables.set("plugin.jar", "../jdbc-plugin-cdata/build/libs/jdbc-plugin-cdata.jar");
//        }
////        FileUtils.copyFile(new File(System.getenv("plugin.jar")), Paths.get( System.getenv("plugin.jar")).normalize().toFile());
//
//        environmentVariables.set("startup.token", "eyJraWQiOiJ0cG1TcDlTd2dOVWxaUUM3eEllM3dxd0ZQVzZFY2pUcm4yaHBmQ25Ialo0IiwiYWxnIjoiRWREU0EifQ.eyJzdWIiOiJmNDJkNzRiMS1kNjdiLTQ4ZGMtYWIwOS01ZDA3NzFkNmI3YzAiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2MzEzNTg5NTIsImV4cCI6MTYzMTQ0NTM1Mn0.Ns0W6pGjqmOUyebqL7vwLp9E9RKaFD82G_2Fwao8q7B23Qv09TyJStCtRmXk1_LV9IfUr50mJzW3FPFwebuAAQ");
//
//        Server.main(new String[]{});
//
//        OkHttpClient client = new OkHttpClient();
//        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//        String post = FileUtils.readFileToString(new File(getClass().getResource("/testRequest.json").getFile()), "UTF-8");
//        RequestBody postBody = RequestBody.create(JSON, post);
//        Request request = new Request.Builder().url("http://localhost:9292/invoke")
//                .addHeader("Authorization", "Bearer eyJraWQiOiJ0cG1TcDlTd2dOVWxaUUM3eEllM3dxd0ZQVzZFY2pUcm4yaHBmQ25Ialo0IiwiYWxnIjoiRWREU0EifQ.eyJzdWIiOiJmNDJkNzRiMS1kNjdiLTQ4ZGMtYWIwOS01ZDA3NzFkNmI3YzAiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2MzEzNTg5NTIsImV4cCI6MTYzMTQ0NTM1Mn0.Ns0W6pGjqmOUyebqL7vwLp9E9RKaFD82G_2Fwao8q7B23Qv09TyJStCtRmXk1_LV9IfUr50mJzW3FPFwebuAAQ")
//                .post(postBody)
//                .build();
//        Response res = client.newCall(request).execute();
//        System.out.println(res);
//        if(!res.isSuccessful()){
//            throw new RuntimeException("unable to invoke /invoke");
//        }
////        Assert.assertEquals("OK", response.getStatus());
//    Assert.assertEquals("[{\"1\":\"1\"}]", res.body().string());
//    }
//}
//
//
