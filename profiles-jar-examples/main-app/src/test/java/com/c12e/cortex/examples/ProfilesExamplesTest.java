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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfilesExamplesTest {


    @Test
    public void testJoinConnections() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("join-conns",
                "-p",
                "mctest30",
                "-l",
                "member-base-file",
                "-r",
                "member-flu-risk-file",
                "-w",
                "member-joined-file");
        assertEquals(0, exitCode);
    }

    @Test
    public void testDataSourceRw() {
        Application app = new Application();
        CommandLine cmd = new CommandLine(app);

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("ds-rw",
                "-p",
                "mctest30",
                "-d",
                "member-base-ds");
        assertEquals(0, exitCode);
    }

    @Test
    @Disabled("Can run with BIGQUERY_CRED env var set")
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
}
