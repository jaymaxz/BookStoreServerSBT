import java.io.{IOException, InputStreamReader}
import java.util

import com.google.gson.Gson
import com.rabbitmq.client.{ConnectionFactory, DeliverCallback, Delivery}
import com.sun.net.httpserver.{HttpExchange, HttpHandler}

object Main extends App {
  private val LIST_REQ_QUEUE_NAME = "List Request Queue"
  private val LIST_RES_QUEUE_NAME = "List Response Queue"
  private val DETAILS_REQ_QUEUE_NAME = "Details Request Queue"
  private val DETAILS_RES_QUEUE_NAME = "Details Response Queue"
  private val ADD_REQ_QUEUE_NAME = "Add Request Queue"
  private val ADD_RES_QUEUE_NAME = "Add Response Queue"
  private val DELETE_REQ_QUEUE_NAME = "Delete Request Queue"
  private val DELETE_RES_QUEUE_NAME = "Delete Response Queue"
  var bookList: util.ArrayList[Book] = null

  bookList = new util.ArrayList[Book]
  val book1 = new Book(1, "The Hobbit")
  bookList.add(book1)
  val book2 = new Book(2, "Lord of the Rings")
  bookList.add(book2)
  val factory = new ConnectionFactory
  factory.setHost("localhost")
  val connection = factory.newConnection
  val channel = connection.createChannel

  channel.queueDeclare(LIST_REQ_QUEUE_NAME, false, false, false, null)
  System.out.println(" [*] Waiting for messages. To exit press CTRL+C")
  val deliverCallback: DeliverCallback = (consumerTag: String, delivery: Delivery) => {
    def foo(consumerTag: String, delivery: Delivery) = {
      val message = new String(delivery.getBody, "UTF-8")
      System.out.println(" [x] Received '" + message + "'")
      val gson = new Gson
      val response = gson.toJson(bookList)
      channel.queueDeclare(LIST_RES_QUEUE_NAME, false, false, false, null)
      channel.basicPublish("", LIST_RES_QUEUE_NAME, null, response.getBytes("UTF-8"))
      System.out.println(" [x] Sent '" + response + "'")
    }

    foo(consumerTag, delivery)
  }
  channel.basicConsume(LIST_REQ_QUEUE_NAME, true, deliverCallback, (consumerTag: String) => {
    def foo(consumerTag: String) = {
    }

    foo(consumerTag)
  })

  channel.queueDeclare(DETAILS_REQ_QUEUE_NAME, false, false, false, null)
  System.out.println(" [*] Waiting for messages. To exit press CTRL+C")
  val deliverCallback2: DeliverCallback = (consumerTag: String, delivery: Delivery) => {
    def foo(consumerTag: String, delivery: Delivery) = {
      val message = new String(delivery.getBody, "UTF-8")
      System.out.println(" [x] Received '" + message + "'")
      val gson = new Gson
      val value = gson.fromJson(message, classOf[Int])
      var index = value
      import scala.collection.JavaConversions._
      for (book <- bookList) {
        if (book.bookID == value) index = value
      }
      val response = gson.toJson(bookList.get(index - 1))
      channel.queueDeclare(DETAILS_RES_QUEUE_NAME, false, false, false, null)
      channel.basicPublish("", DETAILS_RES_QUEUE_NAME, null, response.getBytes("UTF-8"))
      System.out.println(" [x] Sent '" + response + "'")
    }

    foo(consumerTag, delivery)
  }
  channel.basicConsume(DETAILS_REQ_QUEUE_NAME, true, deliverCallback2, (consumerTag: String) => {
    def foo(consumerTag: String) = {
    }

    foo(consumerTag)
  })
  channel.queueDeclare(ADD_REQ_QUEUE_NAME, false, false, false, null)
  System.out.println(" [*] Waiting for messages. To exit press CTRL+C")
  val deliverCallback3: DeliverCallback = (consumerTag: String, delivery: Delivery) => {
    def foo(consumerTag: String, delivery: Delivery) = {
      val message = new String(delivery.getBody, "UTF-8")
      System.out.println(" [x] Received '" + message + "'")
      val gson = new Gson
      val bookName = gson.fromJson(message, classOf[String])
      val newBook = new Book(bookList.get(bookList.size - 1).bookID + 1, bookName)
      bookList.add(newBook)
      val response = gson.toJson(newBook)
      channel.queueDeclare(ADD_RES_QUEUE_NAME, false, false, false, null)
      channel.basicPublish("", ADD_RES_QUEUE_NAME, null, response.getBytes("UTF-8"))
      System.out.println(" [x] Sent '" + response + "'")
    }

    foo(consumerTag, delivery)
  }
  channel.basicConsume(ADD_REQ_QUEUE_NAME, true, deliverCallback3, (consumerTag: String) => {
    def foo(consumerTag: String) = {
    }

    foo(consumerTag)
  })
  channel.queueDeclare(DELETE_REQ_QUEUE_NAME, false, false, false, null)
  System.out.println(" [*] Waiting for messages. To exit press CTRL+C")
  val deliverCallback4: DeliverCallback = (consumerTag: String, delivery: Delivery) => {
    def foo(consumerTag: String, delivery: Delivery) = {
      val message = new String(delivery.getBody, "UTF-8")
      System.out.println(" [x] Received '" + message + "'")
      val gson = new Gson
      val value = gson.fromJson(message, classOf[Int])
      var index = value
      import scala.collection.JavaConversions._
      for (book <- bookList) {
        if (book.bookID == value) index = value
      }
      val bookDeleted = bookList.get(index - 1)
      bookList.remove(index - 1)
      val response = gson.toJson(bookDeleted)
      channel.queueDeclare(DELETE_RES_QUEUE_NAME, false, false, false, null)
      channel.basicPublish("", DELETE_RES_QUEUE_NAME, null, response.getBytes("UTF-8"))
      System.out.println(" [x] Sent '" + response + "'")
    }

    foo(consumerTag, delivery)
  }
  channel.basicConsume(DELETE_REQ_QUEUE_NAME, true, deliverCallback4, (consumerTag: String) => {
    def foo(consumerTag: String) = {
    }

    foo(consumerTag)
  })

  /*HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
          server.createContext("/getAllBooks", new MyHandler());
          server.createContext("/getBookDetails", new MyHandler2());
          server.createContext("/addBook", new MyHandler3());
          server.createContext("/deleteBook", new MyHandler4());
          server.setExecutor(null); // creates a default executor
          server.start();*/

  class MyHandler extends HttpHandler {
    @throws[IOException]
    override def handle(t: HttpExchange): Unit = {
      val gson = new Gson
      val response = gson.toJson(bookList)
      t.sendResponseHeaders(200, response.length)
      val os = t.getResponseBody
      os.write(response.getBytes)
      os.close()
    }
  }

  class MyHandler2 extends HttpHandler {
    @throws[IOException]
    override def handle(t: HttpExchange): Unit = {
      val inputStream = t.getRequestBody
      val gson = new Gson
      val value = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"), classOf[Int])
      var index = value
      import scala.collection.JavaConversions._
      for (book <- bookList) {
        if (book.bookID == value) index = value
      }
      val response = gson.toJson(bookList.get(index - 1))
      t.sendResponseHeaders(200, response.length)
      val os = t.getResponseBody
      os.write(response.getBytes)
      os.close()
    }
  }

  class MyHandler3 extends HttpHandler {
    @throws[IOException]
    override def handle(t: HttpExchange): Unit = {
      val inputStream = t.getRequestBody
      val gson = new Gson
      val bookName = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"), classOf[String])
      val newBook = new Book(bookList.get(bookList.size - 1).bookID + 1, bookName)
      bookList.add(newBook)
      val response = gson.toJson(newBook)
      t.sendResponseHeaders(200, response.length)
      val os = t.getResponseBody
      os.write(response.getBytes)
      os.close()
    }
  }

  class MyHandler4 extends HttpHandler {
    @throws[IOException]
    override def handle(t: HttpExchange): Unit = {
      val inputStream = t.getRequestBody
      val gson = new Gson
      val value = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"), classOf[Int])
      var index = value
      import scala.collection.JavaConversions._
      for (book <- bookList) {
        if (book.bookID == value) index = value
      }
      val bookDeleted = bookList.get(index - 1)
      bookList.remove(index - 1)
      val response = gson.toJson(bookDeleted)
      t.sendResponseHeaders(200, response.length)
      val os = t.getResponseBody
      os.write(response.getBytes)
      os.close()
    }
  }
}
