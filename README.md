# top-n

## Building
```bash
mvn package
```
    
## Usage
```bash
usage: java -jar top-n-1.0-jar-with-dependencies.jar [OPTIONS]
 -b,--blocksize <arg>   File slice size in bytes. Defaults to 10MB
 -i,--input <arg>       Input file
 -n <arg>               Number of elements to find
 -o,--output <arg>      Output file
```

## Output
```bash
<truncated log information>
INFO: Top 10 numbers in data/200million.txt: [2147483523, 2147481965, 2147481237, 2147478830, 2147475382, 2147475365, 2147474242, 2147473532, 2147473046, 2147472921]
```
