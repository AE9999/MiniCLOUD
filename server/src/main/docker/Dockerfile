FROM java:8
RUN mkdir -p /root/.docker \
    && curl -L https://github.com/docker/machine/releases/download/v0.6.0/docker-machine-`uname -s`-`uname -m` > /bin/docker-machine \
    && chmod +x /bin/docker-machine \
    && curl -L https://get.docker.com/builds/Linux/x86_64/docker-1.10.0 > /bin/docker \
    && chmod +x /bin/docker
ADD server.jar /