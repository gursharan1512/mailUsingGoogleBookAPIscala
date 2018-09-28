import java.io.IOException

import org.apache.http.{HttpEntity, HttpResponse}
import org.apache.http.client.{ClientProtocolException, HttpClient}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

class GetTopBooksDetails {
  def getRestContent(url: String): String = {
    var apiOutput= ""
    try {
      // create HTTP Client
      val httpClient : HttpClient = HttpClientBuilder.create().build()
      // Create new getRequest with below mentioned URL
      val getRequest : HttpGet = new HttpGet(url)
      // Add additional header to getRequest which accepts application/xml data
      getRequest.addHeader("accept", "application/xml")
      // Execute your request and catch response
      val response : HttpResponse = httpClient.execute(getRequest)
      // Check for HTTP response code: 200 = success
      //verify the valid error code first
      val statusCode = response.getStatusLine().getStatusCode()
      if (statusCode != 200)
      {
        throw new RuntimeException("Failed with HTTP error code : " + statusCode)
      }
      //Now pull back the response object
      val httpEntity : HttpEntity = response.getEntity();
      apiOutput = EntityUtils.toString(httpEntity);
      return apiOutput
    } catch {
      case e : ClientProtocolException => {
        e.printStackTrace()
        null
      }
      case ex: IOException => {
        ex.printStackTrace()
        null
      }
    }
  }
}
