package util

import java.util.UUID

import com.trueaccord.scalapb.TypeMapper

/**
  * Wrap a UUID in a string for typed access of UUIDs in protocol buffers messages
  *
  * @author Sören Brunk
  */
object UUIDWrapper {
  implicit val typeMapper: TypeMapper[String, UUID] = TypeMapper(UUID.fromString)(_.toString)
}
