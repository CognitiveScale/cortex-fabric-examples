/*
 * Copyright 2022 Cognitive Scale, Inc. All Rights Reserved.
 *
 *  See LICENSE.txt for details.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.c12e.cortex.examples;

import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.CortexSecretsClient;
import com.c12e.cortex.profiles.client.LocalRemoteStorageClient;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import com.c12e.cortex.profiles.intercept.TracingTimingMethodInterceptor;
import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.phoenix.LocalCatalog;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import com.c12e.cortex.profiles.intercept.TracingTimingMethodInterceptor;
import com.google.inject.Guice;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class BaseCommand {

    /**
     * Default properties, can be overridden by spark-submit config or the SparkConfig
     * @return
     */
    protected Map<String, String> getDefaultProps() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put(CortexSession.CATALOG_KEY, LocalCatalog.class.getName());
        defaults.put(CortexSession.METHOD_PROXY_KEY, TracingTimingMethodInterceptor.class.getName());
        defaults.put(CortexSession.LOCAL_CATALOG_DIR_KEY, "src/main/resources/spec/");
        defaults.put(CortexSession.STORAGE_CLIENT_KEY, LocalRemoteStorageClient.class.getName());
        defaults.put("spark.app.name", "CortexProfilesExample");
        defaults.put("spark.master", "local[*]");
        defaults.put("spark.ui.enabled", "true");
        defaults.put("spark.delta.logStore.gs.impl", "io.delta.storage.GCSLogStore");
        defaults.put("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS");
        defaults.put("spark.sql.shuffle.partitions", "10");
        defaults.put("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        defaults.put("spark.hadoop.fs.s3a.fast.upload.buffer", "disk");
        defaults.put("spark.hadoop.fs.s3a.fast.upload", "true");
        defaults.put("spark.hadoop.fs.s3a.block.size", "128M");
        defaults.put("spark.hadoop.fs.s3a.multipart.size", "512M");
        defaults.put("spark.hadoop.fs.s3a.multipart.threshold", "512M");
        defaults.put("spark.hadoop.fs.s3a.fast.upload.active.blocks", "2048");
        defaults.put("spark.hadoop.fs.s3a.committer.threads", "2048");
        defaults.put("spark.hadoop.fs.s3a.max.total.tasks", "2048");
        defaults.put("spark.hadoop.fs.s3a.threads.max", "2048");
        defaults.put("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension");
        defaults.put("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog");
        defaults.put("spark.databricks.delta.schema.autoMerge.enabled", "true");
        defaults.put("spark.databricks.delta.merge.repartitionBeforeWrite.enabled", "true");
        return defaults;
    }

    /**
     * Spark session, can override defaults and spark-submit configuration here by setting config options in the
     * builder config methods
     * @param defaults
     * @return
     */
    protected SparkSession getSparkSession(Map<String, String> defaults) {
        SparkConf sparkConf = new SparkConf(); //from spark-submit
        defaults.forEach(sparkConf::setIfMissing); //set defaults if they don't already exist
        return SparkSession.builder()
                .config(sparkConf) //can override with any additional .config(key, value) here
                .getOrCreate();
    }

    /**
     * Retrieve the cortex session, may include a mock secrets client if it is not in a cluster
     * @param session - the spark session
     * @param localSecrets - the map of project -> path/value for secrets
     * @return
     */
    protected CortexSession getCortexSession(SparkSession session, LocalSecretClient.LocalSecrets localSecrets) {

        CortexSession cortexSession;
        // third connection needs to access secrets when connected to dci-dev
        // bypassing when hitting remotely as service is only exposed on the cluster
        try {
            String master = session.conf().get("spark.kubernetes.driver.master");
            //in a cluster, the default secrets service can connect through the internal service apis
            cortexSession = CortexSession.newSession(session);
        }catch(NoSuchElementException e) {
            //use the local secret service as we are not in a cluster and need access to a connection secret
            //create a new CortexSession through the Guice createInjector method
            cortexSession = Guice.createInjector(
                    new LocalMockedSecretsModule(session, new HashMap<>(), localSecrets)).getInstance(CortexSession.class);
        }

        return cortexSession;
    }


    /**
     * Creating a mocked secrets module for the third connection as it requires access to an internal service to
     * retrieve secrets. When on the cluster we will bypass the use of this module (see the run method)
     *
     * outside of testing a new Guice module should not be instantiated
     */
    public static class LocalMockedSecretsModule extends CortexSession.CortexSessionModule {

        public LocalSecretClient.LocalSecrets localSecrets;
        public LocalMockedSecretsModule(SparkSession sparkSession, Map<String, String> sessionOptions, LocalSecretClient.LocalSecrets localSecrets) {
            super(sparkSession, sessionOptions);
            this.localSecrets = localSecrets;
        }

        @Override
        protected void configure() {
            super.configure();
            //overrides annotations
            bind(CortexSecretsClient.class).toInstance(new LocalSecretClient(localSecrets));
        }
    }

}
