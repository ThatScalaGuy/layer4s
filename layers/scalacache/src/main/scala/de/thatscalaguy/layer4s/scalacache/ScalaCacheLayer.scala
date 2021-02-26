package de.thatscalaguy.layer4s.scalacache

import cats.Applicative
import cats.effect.Async
import de.thatscalaguy.layer4s.{Key, Layer}
import io.circe.Json
import cats.implicits._
import scalacache.{Cache, Mode}

import scala.concurrent.duration._

final class ScalaCacheLayer[F[_]: Applicative : Async] private(cache: Cache[Json]) extends Layer[F] {

  private implicit val mode: Mode[F] = scalacache.CatsEffect.modes.async[F]

  override def put(key: Key, payload: Json): F[Unit] = cache.put[F](key.segments: _*)(payload, Some(300.seconds)).as(())
  override def get(key: Key): F[Option[Json]] = cache.get[F](key.segments: _*)
  override def delete(key: Key): F[Unit] = cache.remove[F](key.segments: _*).as(())
}

object ScalaCacheLayer {
  def apply[F[_]: Applicative : Async](cache: Cache[Json]) = new ScalaCacheLayer[F](cache)
}