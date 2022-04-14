package localchat

import akka.dispatch.{DispatcherPrerequisites, ExecutorServiceConfigurator, ExecutorServiceFactory}
import com.typesafe.config.Config
import javafx.application.Platform

import java.util.Collections
import java.util.concurrent.{AbstractExecutorService, ExecutorService, ThreadFactory, TimeUnit}


abstract class GUIExecutorService extends AbstractExecutorService {
  def execute(command: Runnable): Unit

  def shutdown(): Unit = ()

  def shutdownNow() = Collections.emptyList[Runnable]

  def isShutdown = false

  def isTerminated = false

  def awaitTermination(l: Long, timeUnit: TimeUnit) = true
}

object JavaFXExecutorService extends GUIExecutorService {
  override def execute(command: Runnable) = Platform.runLater(command)
}


class JavaFXEventThreadExecutorServiceConfigurator(config: Config, prerequisites: DispatcherPrerequisites) extends ExecutorServiceConfigurator(config, prerequisites) {
  private val factory = new ExecutorServiceFactory {
    def createExecutorService: ExecutorService = JavaFXExecutorService
  }

  def createExecutorServiceFactory(id: String, threadFactory: ThreadFactory): ExecutorServiceFactory = factory
}



