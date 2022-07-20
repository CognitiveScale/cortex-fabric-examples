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

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.examples.local.CustomSecretsClient;
import com.c12e.cortex.profiles.CortexSession;
import io.delta.tables.DeltaTable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SetEnvironmentVariable(key = CustomSecretsClient.CONNECTION_SECRET_ENV, value = "secret-value")
@SetEnvironmentVariable(key = CustomSecretsClient.STREAMING_SECRET_ENV, value = "streaming-secret-value")
public class ProfilesExamplesTest {

    private static int EXPECTED_COUNT = 100;

    // Output capturing taken from picocli example: https://picocli.info/#_diy_output_capturing
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private Application app;

    @BeforeEach
    void createApplication() {
        app = new Application();
    }

    @BeforeEach
    void setupStreamCapture() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Executes the given command in the CLI application.
     *
     * @param command String command to execute.
     * @return The integer exit code from executing the command.
     */
    int execute(String command) {
        return new CommandLine(app).execute(command.split("\\s"));
    }

    /**
     * Standard output ({@code System.out}) captured during each test execution.
     *
     * @return String value from standard output.
     */
    String output() {
        return out.toString();
    }

    /**
     * Standard error ({@code System.err}) captured during each test execution.
     *
     * @return String value from standard error.
     */
    String error() {
        return err.toString();
    }

    private static void assertExitWithoutError(int exitCode) {
        assertEquals(0, exitCode);
    }
    private static void assertConnectionHasData(String project, String connection, int n) {
        CortexSession session = new SessionExample().getCortexSession();
        Dataset<Row> dataset = session.read().connection(project, connection).load();
        assertEquals(n, dataset.count(),
                String.format("Connection is expected to have %s rows!", n));
    }

    private static void assertDataSourceHasData(String project, String dataSource, int n) {
        CortexSession session = new SessionExample().getCortexSession();
        Dataset<Row> dataset = session.read().dataSource(project, dataSource).load().toDF();
        assertEquals(n, dataset.count(),
                String.format("DataSource is expected to have %s rows!", n));
    }

    private static void assertProfilesHasData(String project, String profileSchema, int n) {
        CortexSession session = new SessionExample().getCortexSession();
        Dataset<Row> profiles = session.read().profile(project, profileSchema).load().toDF();
        assertEquals(n, profiles.count(),
                String.format("'%s' Profiles are expected (# rows as dataset)!", n));
    }


    @Test
    @DisplayName("(local) join-connections -p local  -l member-base-file -r member-flu-risk-file -w member-joined-file -c member_id")
    public void testJoinConnections() {
        int exitCode = execute("join-connections -p local -l member-base-file -r member-flu-risk-file -w member-joined-file -c member_id");
        assertExitWithoutError(exitCode);
        assertConnectionHasData("local", "member-joined-file", EXPECTED_COUNT);
    }

    @Test
    @DisplayName("(local) datasource-refresh -p local -d member-base-ds")
    public void testDataSourceRefresh() {
        int exitCode = execute("datasource-refresh -p local -d member-base-ds");
        assertExitWithoutError(exitCode);
        assertDataSourceHasData("local", "member-base-ds", EXPECTED_COUNT);
    }

    @Test
    @DisplayName("(local) build-profile -p local -ps member-profile")
    public void testProfileBuild() {
        int exitCode = execute("build-profile -p local -ps member-profile");
        assertExitWithoutError(exitCode);
        assertProfilesHasData("local", "member-profile", EXPECTED_COUNT);
    }

    @Test
    @Disabled("Requires S3 filestream and access keys setup (along with expectedRows)")
    @DisplayName("ds-streaming -p local -d member-base-s3-stream-write")
    public void testStreamingDataSource() {
        // Update this value depending on
        int expectedRows = EXPECTED_COUNT;
        int exitCode = execute("ds-streaming -p local -d member-base-s3-stream-write");
        assertExitWithoutError(exitCode);
        assertDataSourceHasData("local", "member-base-s3-stream-write", expectedRows);
    }

    @Test
    @Disabled("Requires manual setup of BIGQUERY_CRED environment variable")
    @DisplayName("bigquery -p local -i bigquery -o sink")
    public void testBigQuery() {
        int exitCode = execute("bigquery -p local -i bigquery -o sink");
        assertExitWithoutError(exitCode);
    }

    @Test
    @Disabled("Requires manual setup of CData jars and setting of CData OEM Key and Product Checksum")
    @DisplayName("(local) cdata -p local -i cdata-csv -o sink")
    public void testCData() {
        int exitCode = execute("cdata -p local -i cdata-csv -o sink");
        assertExitWithoutError(exitCode);
        assertConnectionHasData("local", "sink", EXPECTED_COUNT);
    }
}
