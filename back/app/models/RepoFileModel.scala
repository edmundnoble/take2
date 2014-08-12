package models

import play.api._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._

import com.github.nscala_time.time.Imports._

import utils._
import utils.DateConversions._

case class RepoFile(
        file: String,
        var lastCommit: String,
        var lastUpdated: DateTime) {
    private val Table = TableQuery[RepoFileModel]
    def insert() = {
        // TODO(sandy): check to see if this exists already

        DB.withSession { implicit session =>
            Table += this
        }
    }

    def save() = {
        DB.withSession { implicit session =>
            Table.filter(_.file === file).update(this)
        }
    }

    def touch(commitId: String, timestamp: DateTime) = {
        if (lastUpdated < timestamp) {
            Logger.info("touching " + file)
            lastCommit = commitId
            lastUpdated = timestamp
            save()
        }
    }
}

object RepoFile {
    private val Table = TableQuery[RepoFileModel]

    def getAll: Map[String, RepoFile] = {
        DB.withSession { implicit session =>
            Table.list
        }.map { record =>
            record.file -> (record: RepoFile)
        }.toMap
    }

    def getByFile(file: String): Option[RepoFile] = {
        DB.withSession { implicit session =>
            Table.filter(_.file === file).firstOption
        }
    }

    def touchFiles(filenames: Seq[String], branch: String, commitId: String, timestamp: DateTime) = {
        filenames.map { filename =>
            getByFile(filename) match {
                case None => {
                    Logger.info("adding " + filename)
                    RepoFile(
                        filename,
                        commitId,
                        timestamp
                    ).insert()
                }

                case Some(file) => {
                    // HACK(sandy): only update file timestamps for master
                    Todo.hack
                    if (branch == RepoModel.defaultBranch) {
                        file.touch(commitId, timestamp)
                    }
                }
            }
        }
    }

    def getFilesOpenedSince(since: DateTime): Map[String, Int] = {
        DB.withSession { implicit session =>
            TableQuery[SnapshotModel]
            .where(_.commit === RepoModel.lastCommit)
            .where(x => x.timestamp > since)
            .list
        }.groupBy(_.file).toSeq.map {
            case (k, v) => k -> v.length
        }.toMap
    }
}

class RepoFileModel(tag: Tag) extends Table[RepoFile](tag, "RepoFile") {
    def file = column[String]("file", O.PrimaryKey)
    def lastCommit = column[String]("lastCommit")
    def lastUpdated = column[DateTime]("lastUpdated")

    val repoFile = RepoFile.apply _
    def * = (file, lastCommit, lastUpdated) <> (repoFile.tupled, RepoFile.unapply _)
}

