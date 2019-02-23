# Behave Pro Server Gatling Benchmark


This is a Gatling performance tests repository for Behave Pro for Jira plugin. These tests will run a baseline Jira, a plain Jira with no plugin, alongside an Jira with a Behave Pro installed.

# Install

First make sure you have `upkeep-jira-upload-addon-maven-plugin` and `upkeep-jira-cloudformation-maven-plugin` installed in your maven!

# Dataset

You will need to populate the database. You can either use the provided `behave-pro-server-benchmark/scripts/data-generator` which will create Behave Pro dataset. Please note that the dataset requires that you have already created projects and issues and users. You can use the Atlassian official data generator plugin for Jira to do that. **Note that this script works with postgres only!**

You will also need to update the CSV data in the `behave-pro-server-benchmark/src/test/resources/data`. This CSV data has been populated using `s3://behave-pro-server-performance-dataset/dataset-large-jira.psql`.

If you need to restore the postgres database, you can use the `s3://behave-pro-server-performance-dataset/dataset-large-jira.psql` dump file and then restore it via:

```bash
pg_restore -j 4 -v -n public -h localhost -U jirauser -d jiradb dataset-large-jira-with-behave-pro.psql
```

If you have populated the database yourself, you can dump it to a file via:

```bash
pg_dump -n public -v -Fc -d jiradb -U jirauser > path_to_file.psql
```

To use the database when running remotely, you will need to upload it to a S3 bucket and modify the bucket parameters in the pom.xml

# Running Locally

Simply start Jira locally, for example using a docker:

```bash
docker run --net=host -t dchevell/jira-software
``` 

I highly recommend using postgres as well:

```bash
docker run --net=host -v /var/lib/postgres/data/:/var/lib/postgres/data/ -it postgres:9.4
```

You can run the benchmark locally via:

```bash
mvn veirfy -Plocal -Pbehave-profile
```

The simulation results will be located in `behave-pro-server-benchmark/target/gatling/behaveprosimulation-<unix timestamp>` as html files.

The command above will also install the Behave Pro plugin with 3h timebomb license. To disable installing the plugin, simply add `-DskipAddonUpload=true` to the command above. if you wish to not to upload the 3h timebomb license by default, simply add `-DskipAddonLicense=true` to the command above.

To change the number of users or duration, you will need to modify either BehaveProSimulation.scala or JiraSimulation.scala classes in the test source directory.

# Pom.xml properties

* `behaveProBuild=1006104` - The build number from <https://marketplace.atlassian.com/apps/1211664/behave-pro-for-bdd-jira-git-cucumber/version-history>
* `skipDownloadGatling=false` - Set to true to skip downloading gatling (Only runs if missing)
* `skipCloudFormation=false` - Set to true if you need to skip building the data center on AWS
* `skipAddonUpload=false` - Set to true to skip uploading the addon
* `skipAddonLicense=false` - Set to true to skip adding a license to the addon

