package models

import com.github.nscala_time.time.Imports._
import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Logger
import play.api.Play.current

object DashboardModel {
  private val Table = TableQuery[SnapshotModel]

  private def computeExperts(dataset: Seq[Snapshot]): Map[User, Float] = {
    val counts =
      dataset.groupBy(_.user).map { case(k, v) =>
        k -> v.length
      }

    val totals = (0f /: counts)(_ + _._2)
    counts.map { case (k, v) =>
      k -> (v / totals)
    }
  }

  def getMostActiveUsers(since: DateTime): Seq[(User, Int)] = {
    User.getActiveSince(since)
      .toSeq
      .sortBy(-_._2) // sort by descending viewsigs
      .take(10)
  }

  def getFileExperts(file: String): Map[User, Float] = {
    computeExperts {
      DB.withSession { implicit session =>
        Table
          .where(_.file === file)
          .list
      }
    }
  }

  def getAbsoluteExperts(): Map[User, Float] = {
    val expertResults =
      RepoFile
        .getAll
        .map(_._1)
        .map(getFileExperts)

    // FOLD ALL THE MAPS
    (Map[User, Float]() /: expertResults)((a, b) =>
        a ++ b.map{ case(k, v) => k -> (v + a.getOrElse(k, 0f)) } )
  }
}

