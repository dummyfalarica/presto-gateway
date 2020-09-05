/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.plugin.hive;

import com.google.common.collect.ImmutableSet;
import io.prestosql.plugin.hive.s3.HiveS3Config;
import io.prestosql.plugin.hive.s3.PrestoS3ConfigurationInitializer;
import org.apache.hadoop.fs.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.testng.util.Strings.isNullOrEmpty;

public abstract class AbstractTestHiveFileSystemS3
        extends AbstractTestHiveFileSystem
{
    private String awsAccessKey;
    private String awsSecretKey;
    private String writableBucket;
    private String testDirectory;

    protected void setup(
            String host,
            int port,
            String databaseName,
            String awsAccessKey,
            String awsSecretKey,
            String writableBucket,
            String testDirectory,
            boolean s3SelectPushdownEnabled)
    {
        checkArgument(!isNullOrEmpty(host), "Expected non empty host");
        checkArgument(!isNullOrEmpty(databaseName), "Expected non empty databaseName");
        checkArgument(!isNullOrEmpty(awsAccessKey), "Expected non empty awsAccessKey");
        checkArgument(!isNullOrEmpty(awsSecretKey), "Expected non empty awsSecretKey");
        checkArgument(!isNullOrEmpty(writableBucket), "Expected non empty writableBucket");
        checkArgument(!isNullOrEmpty(testDirectory), "Expected non empty testDirectory");
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.writableBucket = writableBucket;
        this.testDirectory = testDirectory;

        setup(host, port, databaseName, s3SelectPushdownEnabled, createHdfsConfiguration());
    }

    private HdfsConfiguration createHdfsConfiguration()
    {
        ConfigurationInitializer s3Config = new PrestoS3ConfigurationInitializer(new HiveS3Config()
                .setS3AwsAccessKey(awsAccessKey)
                .setS3AwsSecretKey(awsSecretKey));
        HdfsConfigurationInitializer initializer = new HdfsConfigurationInitializer(new HdfsConfig(), ImmutableSet.of(s3Config));
        return new HiveHdfsConfiguration(initializer, ImmutableSet.of());
    }

    @Override
    protected Path getBasePath()
    {
        // HDP 3.1 does not understand s3:// out of the box.
        return new Path(format("s3a://%s/%s/", writableBucket, testDirectory));
    }
}