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
        [DataMember(IsRequired = true, Name = "latitude")]
        public Double latitude;

        [DataMember(IsRequired = true, Name = "longitude")]
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
        [DataMember(IsRequired = true)]
        public int routeid;

        [DataMember(IsRequired = true)]
        public string username;

        [DataMember(IsRequired = true)]
        public string title;

        [DataMember(IsRequired = false)]
        public string description;

        [DataMember(IsRequired = true)]
        public string starttime;

        [DataMember(IsRequired = true)]
        public string endtime;
        
        [DataMember(IsRequired = true)]
        public float speed;

        [DataMember(IsRequired = true)]
        public float distance;

        [DataMember(IsRequired = false)]
        public string weatherinfo;

        [DataMember]
        public List<RoutePoint> pointCollection;

        public Route(int roleid,string title, string desc, float speed, float distance, string weatherinfo, List<RoutePoint> coll)
        {
            this.routeid = roleid;
            this.username = "rjagird";
            this.title = title;
            this.description = desc;
            this.speed = speed;
            this.starttime = DateTime.Now.ToString();
            this.endtime = DateTime.Now.ToString();
            this.distance = distance;
            this.weatherinfo = weatherinfo;
            this.pointCollection = coll;
        }
    }

    [DataContract(Namespace="")]
    public class RoutesCollection
    {
        [DataMember(IsRequired=true)]
        public List<Route> routeCollection;

        public RoutesCollection(List<Route> coll)
        {
            if (coll != null && coll.Count > 0)
            {
                this.routeCollection = coll;
            }
        }
    }

   


    public class Program
    {
        public static Route getRoute(int roleid)
        {
            //Double latitude=89.123456789;
            //Double longitude = -123.123456789;

            List<RoutePoint> list = new List<RoutePoint>();
            for (int i = 1; i < 1000; i++)
            {
                //list.Add(new RoutePoint(longitude - i, latitude - i));
                list.Add(new RoutePoint(90,90));
                //list.Add(new RoutePoint(i,i));
            }

            Route route=new Route(roleid,"WAN IP test","Useless",10,12,"Cloudy",list);
            return route;
        }

        public static RoutesCollection getRoutes()
        {
            List<Route> routes = new List<Route>();
            for (int i = 1; i <= 2; i++)
            {
                Route current = getRoute(i);
                routes.Add(current);
            }

            RoutesCollection collection = new RoutesCollection(routes);
            return collection;
        }

        public static void Main1(string[] args)
        {
            ///getroutepoints
            //HttpWebRequest request = (HttpWebRequest)WebRequest.Create("http://24.163.56.21:2001/Service1.svc/SaveRoute");
            //HttpWebRequest request = (HttpWebRequest)WebRequest.Create("http://24.40.139.4:2001/Service1.svc/SaveRoute");
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create("http://152.46.16.223:2001/BikingService/Service1.svc/SaveRoute");
            request.Method = "POST";

            MemoryStream stream = new MemoryStream();
            DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(RoutesCollection));
            RoutesCollection routes = getRoutes();

            serializer.WriteObject(stream, routes);

            stream.Position = 0;
            StreamReader reader = new StreamReader(stream);
            string data = reader.ReadToEnd();

            //Console.WriteLine(data);



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
