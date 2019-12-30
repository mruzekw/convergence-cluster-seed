<div align="center">
  <img alt="Convergence Logo" height="80" src="https://convergence.io/assets/img/convergence-logo.png" >
</div>

# Convergence Cluster Seed
This project contains the Convergence Cluster seed. It is a lightweight [Akka](https://akka.io/) cluster node that can be used to bootstrap the cluster. The project contains almost no code other than that required to start up the akka cluster. This makes it very unlikely that the node will crash, and minimizes the amount of memory needed to maintain reliable cluster seed nodes.

## Building
The project can be built using the build script:

```shell script
scripts/build.sh
```

## Running

- `CLUSTER_SEED_NODES`: A comma separated list of seed nodes. For example `host1,host2`
- `AKKA_LOG_LEVEL`: A Log4J Log Level such as `INFO`, `DEBUG`
- `AKKA_EXTERNAL_HOSTNAME`: The hostname that external hosts will connect to the cluster seed. Required if the cluster seed is behind a proxy, or deployed in a docker environment like Kubernetes.
- `AKKA_PORT`: The TCP Port used for Akka Remoting.

To run the container execute the following command:

```shell script
docker run --rm \
  --publish 2551:2551 \
  --env CLUSTER_SEED_NODES="localhost" \
  --env AKKA_EXTERNAL_HOSTNAME="localhost" \
  --env AKKA_PORT="2551" \
  convergencelabs/convergence-cluster-seed
```
