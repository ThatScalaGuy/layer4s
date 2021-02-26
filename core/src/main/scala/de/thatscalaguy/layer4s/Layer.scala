package de.thatscalaguy.layer4s

import io.circe.Json

trait Layer[F[_]] {
  def put(key: Key, payload: Json): F[Unit]
  def get(key: Key): F[Option[Json]]
  def delete(key: Key): F[Unit]
}
