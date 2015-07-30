package se.skl.skltpservices.npoadapter

trait HasBaseURL {
  
    val baseURL:String = if (System.getProperty("baseUrl") != null && !System.getProperty("baseUrl").isEmpty()) {
                           System.getProperty("baseUrl")
                         } else {
                           "http://localhost:33001/npoadapter/"
                         }

}