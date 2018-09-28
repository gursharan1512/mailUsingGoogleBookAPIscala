import java.util.Properties

import com.google.gson.{JsonObject, JsonParser}
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

import scala.util.control.Breaks

class MailHelper {
  def sendMail(from : String,password : String,to : String,sub : String,category : String): Unit = {
    //Get properties object
    val props : Properties = new Properties()
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.socketFactory.port", "465")
    props.put("mail.smtp.socketFactory.class",
      "javax.net.ssl.SSLSocketFactory")
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.port", "465")
    //get Session
    val session : Session= Session.getDefaultInstance(props,
      new javax.mail.Authenticator() {
        override protected def getPasswordAuthentication() : PasswordAuthentication = {
          return new PasswordAuthentication(from,password)
        }
      })
    //compose message
    try {
      val message : MimeMessage = new MimeMessage(session)
      message.addRecipient(Message.RecipientType.TO,new InternetAddress(to))
      message.setSubject(sub)
      //message.setText(msg)
      //message.setContent("<h2 style=\"margin-left: 100px\">Top books of fiction category :</h2>","text/html")
      val email = mailContentBuilder(category)
      message.setContent(email,"text/html")
      //send message
      Transport.send(message)
      System.out.println("message sent successfully")
    } catch {
      case e: MessagingException => throw new RuntimeException(e)
    }
  }

  def mailContentBuilder(category: String): String = {
    var email = new StringBuilder
    email.append("<h2 style=\"margin-left: 15%; color: black\">Top books of "+category+" category :</h2>")
    val data = new GetTopBooksDetails().getRestContent("https://www.googleapis.com/books/v1/volumes?q=subject:"+category)
    val parser: JsonParser = new JsonParser()
    val obj: JsonObject = parser.parse(data).getAsJsonObject()
    val getAllBooks = obj.get("items").getAsJsonArray
    val loop = new Breaks
    loop.breakable {
      var j = 0
      for (i <- 0 to getAllBooks.size()) {
        var author = ""
        var description = ""
        /*if (getAllBooks.get(j).isJsonNull) {*/
        if (j < getAllBooks.size()) {
          val bookDetail = getAllBooks.get(j).getAsJsonObject
          val bookInfo = bookDetail.get("volumeInfo").getAsJsonObject
          val title = bookInfo.get("title").getAsString
          if (bookInfo.has("authors")) {
            author = bookInfo.get("authors").getAsJsonArray.get(0).getAsString
          }
          if (bookInfo.has("description")) {
            description = bookInfo.get("description").getAsString
          }
          val thumbnail = bookInfo.get("imageLinks").getAsJsonObject.get("smallThumbnail").getAsString
          val bookLink = bookInfo.get("infoLink").getAsString
          if (bookDetail.get("saleInfo").getAsJsonObject.get("isEbook").getAsString.equals("true")) {
            val price = bookDetail.get("saleInfo").getAsJsonObject.get("retailPrice").getAsJsonObject.get("amount")
            email.append("<table align=\"center\" style=\"width: 70%;border-top: solid;border-width: 1.2px; padding-top: 13px;padding-bottom: 13px;\">"
              + "<tr>"
              + "<td  align=\"center\" style=\"width:20%;height:230px\">"
              + "<img src=\"" + thumbnail + "\">"
              + "</td>"
              + "<td align=\"left\" valign=\"top\" style=\"width:60%;height:230px\">"
              + "<a href=\""+bookLink+"\" style=\"line-height: 100%; font-size:28px;color: #006699; text-decoration: none\">" + title + "</a>"
              + "<span style=\"color: black\"> by </span>"
              + "<br>"
              + "<span style=\"font:16px Arial,sans-serif; color: #666;\"> " + author + " </span>"
              + "<br>"
              + "<h3 style=\"margin: 0; color: black; padding-top:6px\">Description :</h3> <textarea rows=\"6\" cols=\"65\" maxlength=\"65\" style=\"color: black; border-style: none; font:14px Arial,sans-serif\">" + description + "</textarea>"
              + "</td>"
              + "<td align=\"left\" valign=\"top\" style=\"width:20%;height:230px; border-left: 1px solid lightgrey; padding-left: 7px;\">"
              + "<div style=\"font:16px Arial,sans-serif; color: #666;\">Price</div>"
              + "<hr>"
              + "<h4 style=\"margin: 0;padding-top: 5px; color: black\">" + price + "</h4>"
              + "</td>"
              + "</tr>"
              + "</table>")
            j = j + 1
          }
          else {
            j = j + 1
          }
        }
        else
          loop.break
      }
    }
    email.toString()
  }
}
