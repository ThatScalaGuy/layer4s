package de.thatscalaguy.layer4s

import cats.Parallel
import cats.effect.Concurrent
import cats.implicits._
import io.circe._
import io.circe.syntax._
import cats.effect.concurrent.Semaphore

class Repository[F[_]: Concurrent : Parallel, Entity](name: String, layers: Seq[Layer[F]] = Seq.empty, writeLock: Semaphore[F])(implicit encoder: Encoder[Entity], decoder: Decoder[Entity]) {

  def put(key: Key, entity: Entity): F[Unit] = for {
    _ <- writeLock.acquire
    _ <- layers.parTraverse(_.put(key.setRepositoryPrefix(name), entity.asJson))
    _ <- writeLock.release
  } yield ()

  def get(key: Key): F[Option[Entity]] = {
    def walker(layer: Layer[F], tail: Seq[Layer[F]], reassign: Seq[Layer[F]] = Seq.empty): F[Option[Entity]] = {
      layer.get(key.setRepositoryPrefix(name)).flatMap{
        case Some(json) => json.as[Entity] match {
          case Left(err) => Concurrent[F].raiseError(err)
          case Right(value) => reassign.parTraverse(_.put(key.setRepositoryPrefix(name), json)).as(value.some)
        }
        case None if tail.isEmpty => none[Entity].pure[F]
        case None => walker(tail.head, tail.tail, reassign :+ layer)
      }
    }

    layers match {
      case Nil => none[Entity].pure[F]
      case list => walker(list.head, list.tail)
    }
  }

  def delete(key: Key): F[Unit] = layers.parTraverse(_.delete(key.setRepositoryPrefix(name))).as(())

  def modify(key: Key)(update: Option[Entity] => F[Entity]): F[Unit] = for {
    _ <- writeLock.acquire
    entity <- get(key.setRepositoryPrefix(name))
    updatedEntity <- update(entity)
    _ <- layers.parTraverse(_.put(key.setRepositoryPrefix(name), updatedEntity.asJson))
    _ <- writeLock.release
  } yield ()
}
