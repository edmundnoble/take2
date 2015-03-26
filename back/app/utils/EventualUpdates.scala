package utils

import java.util.concurrent.{TimeUnit, Executors}
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import scala.collection.mutable
import com.github.nscala_time.time.Imports._
import play.api.Play.current

trait EventualUpdates extends Flyweight {
  private val toBeAdded = mutable.Buffer[T]()
  private val scheduler = Executors.newScheduledThreadPool(1)
  private val updateLock = new Object()
  @volatile private var updating = false

  def timeFromCreationToUpdate: Duration

  override def create(row: T): T = {
    updateLock.synchronized {
      if (!updating) {
        updating = true
        scheduler.schedule(new Runnable()   {
          override def run(): Unit = actualUpdate(toBeAdded)
        }, timeFromCreationToUpdate.getMillis, TimeUnit.MILLISECONDS)
      }
      toBeAdded += row
      row
    }
  }

  private def actualUpdate(rows: Seq[T]): Unit = {
    updateLock.synchronized {
      updating = false
      DB.withTransaction { implicit session =>
        cached.synchronized {
          rows.foreach { row =>
            val newRow = insert(row)
            cached += newRow.id -> new Access(newRow, DateTime.now)
          }
        }
      }
    }
  }

}
