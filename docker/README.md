# Docker

To test our setup distributively on one machine, we deployed our approaches into docker containers to isolate them against each other.

## Build

To build the images, just execute:

```
./build_images.sh
```

### Details

The script [build_images.sh](build_images.sh) uses maven to build a `JAR` of the current code-base and creates a 
[docker image](Dockerfile-DER) for the master and worker. The following parameters have to be set for the build process:
* `JAR_NAME` - The name of the JAR that will be executed within the container
* `ROLE` - Either the application will be run as worker or master
* `PORT` - The port that will be exposed on the host
* `CONFIG_FILE` - The name of the config file

## Mounts

Once the container is executed, the application uses the following directories if not changed via the [config file](..approaches/optimistic/src/main/resources/default.conf):

* `/app/data` - The location of the data; given in a *csv* format
* `/app/conf` - The application's config file location
* `/app/log` - The application logs all its output to this directory

**Example-1**
```
docker run -it --rm \
           -v /DATA_PATH:/app/data \
           -v /CONF_PATH:/app/conf \
           -v /LOG_PATH:/app/log \
           rdse/master
```

The `CONFIG_FILE` evironment variable enables you to have different config files in the same `/CONF_PATH` directory:

**Example-2**
```
docker run -it --rm \
           -v /DATA_PATH:/app/data \
           -v /CONF_PATH:/app/conf \
           -v /LOG_PATH:/app/log \
           -e CONFIG_FILE=test_1.conf \
           rdse/master
```

## Usage

We further apply this setup to test our implementation. Please check out the [testing folder](../tests).
