# Docker Setup

To test our setup distributively on one machine, we deployed our approaches into docker containers.

## Build

To build the images, just execute:

```
./build_images.sh
```

### Details

The script [build_images.sh](build_images.sh) uses maven to build a `JAR` of the current code-base and creates a 
[docker image](Dockerfile-DER) for the master and worker. The following parameters have to set for the build process:
* `JAR_NAME`: The name of the JAR that will be executed within the container
* `ROLE`: Either the application will be run as worker or master
* `PORT`: The port that will be exposed on the host
* `CONFIG_FILE`: The name of the configuration file

## Mounts

todo: describe mounting points

* `/app/data`:
* `/app/conf`:
* `/app/log`:

* provide an example and reference test
