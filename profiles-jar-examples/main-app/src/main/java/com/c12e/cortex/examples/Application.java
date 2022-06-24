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

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * CLI application entrypoint for interacting with the example application.
 */
@Command(name = "profiles-example", version = "v1.0", mixinStandardHelpOptions = true, subcommands = {
        JoinConnections.class,
        JoinSpark.class,
        CData.class,
        BigQuery.class,
        DataSourceRW.class,
        BuildProfile.class,
        StreamingDataSource.class
})
public class Application {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
