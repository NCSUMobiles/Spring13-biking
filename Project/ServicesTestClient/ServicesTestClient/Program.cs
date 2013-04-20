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


namespace ServicesTestClient
{
    [DataContract]
    public class RoutePoint
    {
        [DataMember(IsRequired = true, Order = 1, Name = "latitude")]
        public Double latitude;

        [DataMember(IsRequired = true, Order = 2, Name = "longitude")]
        public Double longitude;

        public RoutePoint(Double longitude, Double latitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    [DataContract]
    public class Route
    {
        [DataMember(IsRequired = true, Order = 1)]
        public string title;

        [DataMember(IsRequired = false, Order = 2)]
        public string description;

        [DataMember(IsRequired = true, Order = 3)]
        public float speed;

        [DataMember(IsRequired = true, Order = 4)]
        public float duration;

        [DataMember(IsRequired = true, Order = 5)]
        public float distance;

        [DataMember(Order = 6)]
        public List<RoutePoint> pointCollection;

        public Route(string title, string desc, float speed, float duration, float distance, List<RoutePoint> coll)
        {
            this.title = title;
            this.description = desc;
            this.speed = speed;
            this.duration = duration;
            this.distance = distance;
            this.pointCollection = coll;
        }
    }

   


    public class Program
    {
        public static Route getRoute()
        {
            List<RoutePoint> list = new List<RoutePoint>();
            for (int i = 1; i < 50; i++)
            {
                list.Add(new RoutePoint(i,i));
                //list.Add(new RoutePoint(11.0, 11.0));
                //list.Add(new RoutePoint(12.0, 12.0));
            }

            Route route=new Route("WAN IP test","Useless",10,20,12,list);
            return route;
        }
        public static void Main(string[] args)
        {
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create("http://24.163.56.21:2001/Service1.svc/getroutes");
            request.Method = "POST";

            MemoryStream stream=new MemoryStream();
            DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(Route));
            Route route=getRoute();

            serializer.WriteObject(stream,route);

            stream.Position=0;
            StreamReader reader=new StreamReader(stream);
            string data=reader.ReadToEnd();




            //string jsonData = @"{""channels"":[""""],""data"":{""alert"":""Sample Message"",""action"":""MyAction"",""id"":""116""}}";
            byte[] postArray = Encoding.UTF8.GetBytes(data);

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
            Console.WriteLine("Sent the Post Successfully");
        }
    }

    
   
}
