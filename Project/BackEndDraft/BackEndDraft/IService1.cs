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
        OperationResult getRoutes(Stream body);

        [OperationContract]
        [WebInvoke(ResponseFormat = WebMessageFormat.Json, RequestFormat = WebMessageFormat.Json)]
        string getRoutesNew(Route route);

        
        // TODO: Add your service operations here
    }

    [DataContract(Namespace="")]
    public class RoutePoint
    {
        [DataMember(IsRequired=true,Order=1,Name="latitude")]
        public Double latitude;

        [DataMember(IsRequired = true, Order = 2, Name = "longitude")]
        public Double longitude;


    }

    [DataContract(Namespace = "")]
    public class OperationResult
    {
        [DataMember(Order=1)]
        public bool status;

        [DataMember(Order = 2)]
        public string Message;
    }

    [DataContract(Namespace="")]
    public class Route
    {
        [DataMember(IsRequired = true, Order = 1)]
        public string title;

        [DataMember(IsRequired=false,Order=2)]
        public string description;

        [DataMember(IsRequired=true,Order=3)]
        public float speed;

        [DataMember(IsRequired = true, Order = 4)]
        public float duration;

        [DataMember(IsRequired = true, Order = 5)]
        public float distance;

        [DataMember(Order = 6)]
        public List<RoutePoint> pointCollection;
    }


    
}
