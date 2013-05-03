using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.IO;
using System.Web;
using System.Runtime.Serialization.Json;
using System.ServiceModel.Web;
using System.Runtime.Serialization;
using System.Collections.Specialized;


namespace ServicesTestClient
{
    public class signup_test
    {
        public static void Main(String[] args)
        {
            //HttpWebRequest request = (HttpWebRequest)WebRequest.Create("http://24.163.56.21:2001/Service1.svc/login");
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create("http://152.46.16.223:2001/BikingService/Service1.svc/login");
            //HttpWebRequest request = (HttpWebRequest)WebRequest.Create("http://192.168.10.82:2001/Service1.svc/signup");
            request.Method = "POST";

            NameValueCollection postParams = new NameValueCollection();
            postParams.Add("username", "vkolli");
            postParams.Add("password", "Infy@123");

            var parameters = new StringBuilder();
 
            foreach (var key in postParams.AllKeys)
            {
                parameters.AppendFormat("{0}={1}&",
                    HttpUtility.UrlEncode(key),
                    HttpUtility.UrlEncode(postParams[key]));
            }
 
            parameters.Length -= 1;

            byte[] postArray = Encoding.UTF8.GetBytes(parameters.ToString());

            request.ContentLength = postArray.Length;
            request.ContentType = "text/plain";

            Stream writeStream = request.GetRequestStream();
            writeStream.Write(postArray, 0, postArray.Length);
            writeStream.Close();

            HttpWebResponse response = (HttpWebResponse)request.GetResponse();
            if (response.StatusCode == HttpStatusCode.Created || true)
            {
                StreamReader responseReader = new StreamReader(response.GetResponseStream());
                String responseString = responseReader.ReadToEnd();
                Console.WriteLine(responseString);
                response.Close();
            }
        }
    }
}
