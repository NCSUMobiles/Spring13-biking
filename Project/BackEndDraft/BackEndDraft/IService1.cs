using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;
using System.ServiceModel.Activation;
using System.Data;
using System.IO;

namespace BackEndDraft
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the interface name "IService1" in both code and config file together.
    [ServiceContract(Namespace = "")]
    public interface IService1
    {

        [OperationContract]
        [WebGet(ResponseFormat = WebMessageFormat.Json)]
        string GetData();

        [OperationContract]
        [WebInvoke(ResponseFormat = WebMessageFormat.Json)]
        List<OperationResult> SaveRoute(Stream body);

        [OperationContract]
        [WebInvoke(ResponseFormat = WebMessageFormat.Json, RequestFormat = WebMessageFormat.Json)]
        string getRoutesNew(Route route);

        [OperationContract]
        [WebInvoke(ResponseFormat = WebMessageFormat.Json, RequestFormat = WebMessageFormat.Json)]
        string getPostData(Stream body);

        [OperationContract]
        [WebInvoke(ResponseFormat = WebMessageFormat.Json)]
        User signup(Stream data);

        [OperationContract]
        [WebInvoke(ResponseFormat = WebMessageFormat.Json)]
        User login(Stream data);

        [OperationContract]
        [WebGet(ResponseFormat = WebMessageFormat.Json)]
        List<RoutePoint> getRoutePoints();

        [OperationContract]
        [WebInvoke(ResponseFormat = WebMessageFormat.Json)]
        UserStatus getLocation(Stream data);


        
        // TODO: Add your service operations here
    }

    [DataContract(Namespace = "")]
    public class User
    {
        [DataMember(Name = "user")]
        public string user;

        [DataMember(Name = "pass")]
        public string pass;

        [DataMember(Name = "ERROR")]
        public string ERROR;
    }

    [DataContract(Namespace="")]
    public class RoutePoint
    {
        [DataMember(IsRequired=true,Name="latitude")]
        public Double latitude;

        [DataMember(IsRequired = true, Name = "longitude")]
        public Double longitude;


    }


    [DataContract(Namespace = "")]
    public class UserStatus
    {
        [DataMember(IsRequired = true)]
        public RoutePoint location;

        [DataMember(IsRequired = true, Name = "present")]
        public bool isPresent;


    }

    [DataContract(Namespace = "")]
    public class OperationResult
    {
        [DataMember]
        public bool status;

        [DataMember]
        public string Message;

        [DataMember(IsRequired = false)]
        public int routeid;
    }

    [DataContract(Namespace="")]
    public class Route
    {

        [DataMember(IsRequired = true)]
        public int routeid;

        [DataMember(IsRequired = true)]
        public string username;

        [DataMember(IsRequired = true)]
        public string title;

        [DataMember(IsRequired=false)]
        public string description;

        [DataMember(IsRequired=true)]
        public float speed;

        [DataMember(IsRequired = true)]
        public string starttime;

        [DataMember(IsRequired = true)]
        public string endtime;

        [DataMember(IsRequired = true)]
        public float distance;

        [DataMember(IsRequired = false)]
        public string weatherinfo;

        [DataMember(IsRequired=true)]
        public List<RoutePoint> pointCollection;
    }


    [DataContract(Namespace = "")]
    public class RoutesCollection
    {
        [DataMember(IsRequired = true)]
        public List<Route> routeCollection;
    }   
}
