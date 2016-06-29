# Description
**MiniCLOUD** is a Satisfiability (SAT) Solver based on Minisat, Docker and RabbitMQ, capable of running in the Digital Ocean cloud service. Using both RabbitMQ & Digital Ocean makes **MiniCLOUD** very scalable. While **MiniCLOUD** is written in Java it uses JNI to make its calls to Minisat thus preserving the speed inherit in  C++. For more info check [here](http://aetinkering.blogspot.nl/2016/05/cloud-sat-solving-for-masses.html).

# Architecture
**MiniCLOUD** consists of 3 parts:
- The **web controller** where users can upload CNFs specified in the dimacs format.
- The **worker** which is a a node that solves assigned SAT problems.
- A RabbitMQ message broker through which worker nodes exchange learnt unit clauses.

When a CNF is submitted, a number of workers is specified. Each of these workers is given a the assumptions to split the search space.

# Building
Note this build has only been tested on the linux-x86_64 platform
  - Install maven.
  - Install & Run docker.
  - Install docker-machine (https://docs.docker.com/machine/)
  - Install docker-compose (https://docs.docker.com/compose/)
  - Run installDependencies.sh to install the minisat native libraries.
  - Run generate.sh in distribution/certs
  - Run mvn install in main directory.

# Running
**MiniCLOUD** supports 3 running modes:
- **Local**: **MiniCLOUD** runs locally, using the host docker deamon for each new container instantiation.
- **LocalVM**: **MiniCLOUD** runs locally, but instantiates each docker container in a new VirtualBox VM.
- **OceanVM**: The web interface is still hosted locally, but RabbitMQ and Solver instances run on dedicated VMs in de Ocean cloud.

For each mode there is a directory with a corresponding start script ``start.sh`` in the ``distribution``directory. These start scripts should be run in their native directories. For the  **Local** & **OceanVM** profiles there is also a debug setup, which starts the webserver in a remote debug mode. The **LocalVM** debug mode can be started by adding the flag `--debug` when running the start script. After the context has been loaded go to ``http://localhost:8080`` to use the webinterface. After files have been uploaded, a job with the specified amount of workers will start. All workers will return their answers in the commandline. Watch for lines starting with ``Recieved answer:``



# Configuring

## Running on **MiniCLOUD**
**MiniCLOUD** should work locally out of the box, but running on digital ocean requires an account there. Please note that digital ocean is paid service :-)
Running on digital ocean requires setting the `oceanToken` property in the file `./server/src/main/resources/application.properties`. Obtaining a `oceanToken` is explained here https://www.digitalocean.com/community/tutorials/how-to-use-the-digitalocean-api-v2

## Using your own or updated workers
Note that both the **LocalVM** & **OceanVM** running profiles pull their docker images from the main docker hub, which only I can update. if you want to use your own custom worker, update instances of ``aereg/sat-worker-minisat`` with your own docker image.

# **!!! WARNING !!!**
**MiniCLOUD** does currently not ensure the termination of VMs it starts, so please after usage check if they are still up. Especially on Digital Ocean :-)

# Future Improvements
This is a __v0.0.1__ version of **MiniCLOUD** possible improvements include:
- Use the more efficient Glucose sat solver instead of minisat
- Support solver setups to be send in assignments.
- Don't use budgets of 5000 conflicts to force workers to check for arrived unit clauses, but instead use a threading scheme to update when new fresh unit clauses arrive.
- Improve RabbitMQ message setup so messages send before solvers start are still picked up.
- Show results of solving assigments on the webinterface
- Show statistics on the webinterface
- Many more things :-)
