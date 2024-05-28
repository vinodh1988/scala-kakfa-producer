import JsonFormats._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.apache.kafka.clients.producer.ProducerRecord
import spray.json._

object WebServer {
  implicit val system = ActorSystem(Behaviors.empty, "MyActorSystem")

  val producer = KafkaProducerFactory.createProducer()

  def main(args: Array[String]): Unit = {
    val route =
      post {
        path("write-message") {
          entity(as[Person]) { person =>
            // Convert the person object to JSON string
            val jsonString = person.toJson.toString()
            val record = new ProducerRecord[String, String]("people-topic", jsonString)
            producer.send(record)

            println(s"Sent to Kafka: $jsonString")  // Debugging output
            complete(StatusCodes.OK, s"Message sent to Kafka: $jsonString")
          }
        }
      }

    Http().newServerAt("localhost", 8080).bind(route)
    println("Server online at http://localhost:8080/")
  }
}
