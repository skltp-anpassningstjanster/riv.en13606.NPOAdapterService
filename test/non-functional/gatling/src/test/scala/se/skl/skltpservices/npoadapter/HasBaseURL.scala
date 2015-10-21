package se.skl.skltpservices.npoadapter

trait HasBaseURL {
  
    val baseURL:String = if (System.getProperty("baseURL") != null && !System.getProperty("baseURL").isEmpty()) {
                           System.getProperty("baseURL")
                         } else {
                           "http://localhost:33001/npoadapter/"
                         }

}