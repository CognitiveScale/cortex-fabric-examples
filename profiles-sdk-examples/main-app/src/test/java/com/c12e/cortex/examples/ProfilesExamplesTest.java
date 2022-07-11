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

import com.c12e.cortex.examples.local.CustomSecretsClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SetEnvironmentVariable(key = CustomSecretsClient.CONNECTION_SECRET_ENV, value = "secret-value")
@SetEnvironmentVariable(key = CustomSecretsClient.STREAMING_SECRET_ENV, value = "streaming-secret-value")
public class ProfilesExamplesTest {

    @Test
    @DisplayName("(local) join-connections -p local  -l member-base-file -r member-feedback-file -w member-joined-file -c member_id")
    public void testJoinConnections() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("join-connections",
                "-p", "local",
                "-l", "member-base-file",
                "-r", "member-feedback-file",
                "-w", "member-joined-file",
                "-c", "member_id");
        // TODO(LA): Check that the connection data has been written
        assertEquals(0, exitCode);
    }

    @Test
    @DisplayName("(local) datasource-refresh -p local -d member-base-ds")
    public void testDataSourceRefresh() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("datasource-refresh",
                "-p",
                "local",
                "-d",
                "member-base-ds");
        // TODO(LA): Check the DataSource data has been written
        assertEquals(0, exitCode);
    }

    @Test
    @DisplayName("(local) build-profile -p local -ps member-profile")
    public void testProfileBuild() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("build-profile",
                "-p",
                "local",
                "-ps",
                "member-profile");
        // TODO(LA): Check the Profile DeltaTable has been written
        assertEquals(0, exitCode);
    }

    @Test
    @Disabled("Requires S3 filestream and access keys")
    @DisplayName("ds-streaming -p local -d member-base-s3-stream-write")
    public void testStreamingDataSource() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("ds-streaming",
                "-p",
                "local",
                "-d",
                "member-base-s3-stream-write");
        // TODO(LA): Check the Profile DeltaTable has been written
        assertEquals(0, exitCode);
    }

    @Test
    @Disabled("Can run with BIGQUERY_CRED env var set")
    @DisplayName("bigquery -p mctest30 -i bigquery -o sink")
    public void testBigQuery() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("bigquery",
                "-p",
                "mctest30",
                "-i",
                "bigquery",
                "-o",
                "sink");
        assertEquals(0, exitCode);
    }

    @Test
    @Disabled("requires CData jar setup")
    @DisplayName("(local) cdata -p local -i cdata-csv -o sink")
    public void testCData() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("cdata",
                "-p", "local",
                "-i", "cdata-csv",
                "-o", "sink");
        // TODO(LA): Assert datasource has been written
        assertEquals(0, exitCode);
    }
}
