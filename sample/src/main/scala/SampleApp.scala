import blobstore.fs.FileStore
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import de.thatscalaguy.layer4s.layers.blobstore.BlobstoreLayer
import de.thatscalaguy.layer4s.scalacache.ScalaCacheLayer
import de.thatscalaguy.layer4s.{Key, Repository, RepositoryBuilder}
import io.circe.Json
import io.circe.generic.auto._
import scalacache.caffeine.CaffeineCache

import java.nio.file.Path

case class User(name: String, email: String)

object SampleApp extends IOApp{

  def app(repo: Repository[IO, User], repo2: Repository[IO, Json]):IO[Unit] = for {
//    _ <- repo.put(Key("sven" :: Nil), User("sven", "sven@pinkeye.de"))
    _ <- repo.get(Key("sven" :: Nil))
//    _ <- repo.put(Key("lalala" :: Nil), User("arsch", "lalalal"))
//    _ <- repo.delete(Key("lalala" :: Nil))

    _ <- repo2.put(Key("Test" :: Nil), Json.obj("test" -> Json.fromInt(1)))
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = (for{
    blocker <- Blocker[IO]

    store = FileStore[IO](Path.of("/Users/sven/Projects/thatscalaguy/layer4s/data/1"), blocker)
    store2 = FileStore[IO](Path.of("/Users/sven/Projects/thatscalaguy/layer4s/data/2"), blocker)
    repo <- RepositoryBuilder.apply[IO, User]("user")
      .appendLayer(ScalaCacheLayer(CaffeineCache[Json]))
      .appendLayer(BlobstoreLayer(store2))
      .appendLayer(BlobstoreLayer(store))
      .resource

    repo2 <- RepositoryBuilder.apply[IO, Json]("user2")
      .appendLayer(BlobstoreLayer(store))
      .resource
  } yield app(repo, repo2)).use(identity).as(ExitCode.Success)
}
