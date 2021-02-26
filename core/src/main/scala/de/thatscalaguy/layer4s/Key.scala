package de.thatscalaguy.layer4s

case class Key(segments: Seq[String] ) {
  def setRepositoryPrefix(name: String): Key = copy(name +: segments)
}

object Key {
  def of(segments: String*): Key = Key(segments)
}
