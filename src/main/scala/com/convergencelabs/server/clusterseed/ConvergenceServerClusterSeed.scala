package com.convergencelabs.server.clusterseed

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import scala.util.control.NonFatal

import org.apache.logging.log4j.LogManager

import akka.actor.ActorSystem
import akka.actor.Address
import akka.cluster.Cluster
import grizzled.slf4j.Logging

object ConvergenceAkkaClusterSeed extends Logging {
  var system: Option[ActorSystem] = None
  var cluster: Option[Cluster] = None

  def main(args: Array[String]) {
    Try {
      Option(System.getenv().get("CLUSTER_SEED_NODES")) match {
        case Some(seedNodesEnv) =>
          info(s"Starting Convergence Server Cluster Seed")

          val ClusterName = "Convergence";

          val _system = ActorSystem(ClusterName);
          system = Some(_system)
          info("Actor system started")

          _system.actorOf(AkkaClusterListener.props());

          scala.sys.addShutdownHook {
            this.shutdown()
          }

          val seedNodes = seedNodesEnv.split(",").toList
          val addresses = seedNodes.map { seedNode =>
            val (hostname, port) = if (seedNode.contains(":")) {
              val parts = seedNode.split(":")
              val hostname = parts(0)
              val port = parts(1).toInt
              (hostname, port)
            } else {
              (seedNode, 2551)
            }

            Address("akka.tcp", ClusterName, hostname, port)
          }

          info(s"Joining cluster with seed nodes: ${addresses}")

          info("Creating and joining cluster")
          val _cluster = Cluster(_system)
          _cluster.joinSeedNodes(addresses)
          cluster = Some(_cluster)
        case None =>
          error("Can not join the cluster because the CLUSTER_SEED_NODES environment was not set. Exiting")
      }
    }.recover {
      case NonFatal(cause) =>
        error("Error starting Convergence Server Cluster Seed", cause)
        shutdown();
    }
  }

  def shutdown(): Unit = {
    cluster.foreach {
      info("Leaving cluster.")
      c => c.leave(c.selfAddress)
    }

    system.foreach { s =>
      info("Shutting down actor system")
      Try {
        Await.result(s.whenTerminated, FiniteDuration(10, TimeUnit.SECONDS))
      } recover {
        case cause: TimeoutException =>
          warn("The actor system did not shut down in the allowed time.")
      } map { _ =>
        info("Convergence Akka Cluster Seed is shutdown.")
      }
    }

    LogManager.shutdown();
  }
}
  