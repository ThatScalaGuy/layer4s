package de.thatscalaguy.layer4s.layers.blobstore

import blobstore.{Path, Store}
import cats.ApplicativeError
import cats.implicits._
import cats.effect.{Concurrent, ContextShift}
import de.thatscalaguy.layer4s.layers.blobstore.BlobstoreLayer.ThrowableApplicativeError
import de.thatscalaguy.layer4s.{Key, Layer}
import fs2.Stream
import io.circe._
import io.circe.parser._

final class BlobstoreLayer[F[_]: Concurrent : ContextShift : ThrowableApplicativeError] private(store: Store[F]) extends Layer[F] {

  private def keyToPath(key: Key): Path = {
    Path(key.segments.mkString("/") + ".json")
  }

  override def put(key: Key, payload: Json): F[Unit] = for {
    _ <- Stream.emit[F, String](payload.noSpaces)
      .through(fs2.text.utf8Encode)
      .through(store.put(keyToPath(key)))
      .compile.foldMonoid
  } yield ()

  override def get(key: Key): F[Option[Json]] =
    store.get(keyToPath(key), 1024)
      .through(fs2.text.utf8Decode)
      .compile.foldMonoid
      .map(decode[Json])
      .map{
        case Left(_) => none[Json] //TODO: throw exception?
        case Right(value) => value.some
      }
      .recover {
        case _: java.nio.file.NoSuchFileException => none[Json]
      }

  override def delete(key: Key): F[Unit] = store.remove(keyToPath(key))
}

object BlobstoreLayer {
  type ThrowableApplicativeError[F[_]] = ApplicativeError[F, Throwable]

  def apply[F[_]: Concurrent : ContextShift](store: Store[F]) =
    new BlobstoreLayer[F](store)
}
