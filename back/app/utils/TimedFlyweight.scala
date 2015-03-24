package utils

import java.util.concurrent.{TimeUnit, Executors, ScheduledExecutorService}

import com.github.nscala_time.time.Imports._

object TimedFlyweight {
  val ReclaimThreads = 2
  lazy val ReclaimScheduler = Executors.newScheduledThreadPool(ReclaimThreads)
  private def scheduleReclaim(reclaimTime: Duration, flyweight: TimedFlyweight) = {
    ReclaimScheduler.scheduleWithFixedDelay(new Runnable() {
      override def run(): Unit = flyweight.reclaim(DateTime.now - reclaimTime)
    }, reclaimTime.getMillis, reclaimTime.getMillis, TimeUnit.MILLISECONDS)
  }
}

trait TimedFlyweight extends Flyweight {
  import TimedFlyweight._
  
  def reclaimPeriod: Duration

  scheduleReclaim(reclaimPeriod, this)
}
