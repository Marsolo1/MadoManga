# MadoManga

This is a small application using [ScalarDB](https://github.com/scalar-labs/scalardb) for our class project

# Starting the databases

First, you need to use Docker. A basic Docker-compose file is provided so you can start the stack by simply using the following command:

```bash
docker-compose up -d
```

And stop it using

```bash
docker-compose down
```

Once the database is started, the schema must be applied to the servers. This can be done by getting the latest version of ScalarDB's schema loader [here](https://github.com/scalar-labs/scalardb/releases) (download `scalardb-schema-loader-<version>.jar`). Then, simply run the following command:

```bash
java -jar scalardb-schema-loader-<version>.jar --config scalardb.properties -f schema.json --coordinator
```

# Running the program

Simply run the Main function inside `src/Main.java`

