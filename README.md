# top-n

## Building

```bash
mvn package
```
    
## Running

```bash
java -jar path/to/target/top-n-1.0-jar-with-dependencies.jar [OPTIONS]
```
    
## Usage

```bash
usage: java -jar top-n-1.0-jar-with-dependencies.jar [OPTIONS]
 -b,--blocksize <arg>   File slice size in bytes. Defaults to 10MB
 -i,--input <arg>       Input file
 -n <arg>               Number of elements to find
 -o,--output <arg>      Output file
```
