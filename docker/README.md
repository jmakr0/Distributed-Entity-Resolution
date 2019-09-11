# Docker Setup

In order to test our setup distributively on one machine, we deployed our approaches into docker containers.

## Build

To build the images, just execute:

```
./build_images.sh
```

The script [build_images.sh](build_images.sh) uses maven, builds a `JAR` from the current code-base and creates a 
[base](Dockerfile-base), [master](Dockerfile-base), and [worker](Dockerfile-base) image.
