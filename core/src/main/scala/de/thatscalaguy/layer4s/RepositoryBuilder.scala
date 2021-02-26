package de.thatscalaguy.layer4s

import cats.Parallel
import cats.effect.concurrent.Semaphore
import cats.effect.{Concurrent, Resource}
import cats.implicits._
import io.circe.{Decoder, Encoder}

class RepositoryBuilder[F[_]: Concurrent : Parallel, Entity] private(
                                    name: String,
                                    layers: Seq[Layer[F]] = Seq.empty
                                  ) {

  def setName(name: String): RepositoryBuilder[F, Entity] = copy(name = name)

  def appendLayer(layer: Layer[F]): RepositoryBuilder[F, Entity] = copy(layers = layers :+ layer)

  private def copy(
                  name: String = this.name,
                  layers: Seq[Layer[F]] = this.layers
                  ): RepositoryBuilder[F, Entity] = new RepositoryBuilder[F, Entity](
    name, layers
  )

  def resource(implicit encoder: Encoder[Entity], decoder: Decoder[Entity]): Resource[F, Repository[F, Entity]] = for {
    writeLock <- Resource.liftF(Semaphore[F](1))
    repository <- Resource.make(new Repository[F, Entity](name, layers,writeLock).pure[F])(_ => ().pure[F])
  } yield repository

}

object RepositoryBuilder {
  def apply[F[_]: Concurrent : Parallel, Entity](name: String): RepositoryBuilder[F, Entity] = new RepositoryBuilder[F, Entity](name)
}
