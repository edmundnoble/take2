package utils

import com.github.nscala_time.time.Imports._
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

trait Flyweight {
  type Key
  type T <: {val id: Key} // scalastyle:ignore

  def rawGet(key: Key): Option[T]

  protected def insert(row: T)(implicit session: Session): T

  protected case class Access(obj: T, var lastTouched: DateTime)

  protected val cached = scala.collection.mutable.Map[Key, Access]()

  def inMemory: Seq[T] = cached.toMap.map(_._2.obj).toSeq

  def getById(key: Key): Option[T] = {
    cached.get(key).map { access =>
      access.lastTouched = DateTime.now
      Some(access.obj)
    }.getOrElse {
      rawGet(key).map { obj =>
        cached.synchronized {
          cached += key -> new Access(obj, DateTime.now)
        }
        obj
      }
    }
  }

  def isLoaded(key: Key): Boolean = {
    cached.contains(key)
  }

  def preload(objs: Seq[T]) = {
    val now = DateTime.now

    cached.synchronized {
      val (toUpdate, toAdd) = objs.partition { obj =>
        cached.contains(obj.id)
      }

      cached ++= toAdd.map { obj =>
        obj.id -> new Access(obj, now)
      }

      toUpdate.map { obj =>
        cached(obj.id).lastTouched = now
      }
    }
  }

  def expire(key: Key): Unit = {
    cached.synchronized {
      cached -= key
    }
  }

  def expire(keys: Seq[Key]): Unit = {
    cached.synchronized {
      cached --= keys
    }
  }

  def clear(): Unit = {
    cached.synchronized {
      cached.clear()
    }
  }

  def reclaim(beforeWhen: DateTime): Unit = {
    cached.synchronized {
      cached --= cached.filter {
        case (key, access) => access.lastTouched < beforeWhen
      }.keys
    }
  }

  def create(row: T): T = {
    cached.synchronized {
      DB.withSession { implicit session =>
        val newRow = insert(row)
        cached += newRow.id -> new Access(newRow, DateTime.now)
        newRow
      }
    }
  }
}

