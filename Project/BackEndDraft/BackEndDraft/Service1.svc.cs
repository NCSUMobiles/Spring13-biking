using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;
using System.ServiceModel.Activation;
using System.Configuration;
using System.Data.SqlClient;
using System.Data;
using System.IO;
using System.Runtime.Serialization.Json;
using System.Text;

namespace BackEndDraft
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "Service1" in code, svc and config file together.
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Allowed)]
    public class Service1 : IService1
    {
        public string GetData()
        {

            return string.Format("Hello RJ! Welcome Back Hero");
        }

        private string getPostData(Stream body)
        {
            try
            {
                byte[] buffer = new byte[10000];
                int bytesRead, totalBytesRead = 0;
                do
                {
                    bytesRead = body.Read(buffer, 0, buffer.Length);
                    totalBytesRead += bytesRead;
                } while (bytesRead > 0);
                var str = System.Text.Encoding.UTF8.GetString(buffer, 0, totalBytesRead);
                return str;
            }
            catch (Exception ex)
            {

            }
            return string.Empty;
        }

        private Route getRoute(string jsonData)
        {
            
            try
            {
                MemoryStream stream = new MemoryStream(Encoding.UTF8.GetBytes(jsonData));
                stream.Position = 0;
                DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(Route));
                Route route = (Route)serializer.ReadObject(stream);
                if (route != null)
                {
                    return route;
                }
                
            }
            catch (Exception ex)
            {
                
            }
            return null;

        }

        public OperationResult getRoutes(Stream body)
        {
            OperationResult result = new OperationResult();
            BikingDAL dal = new BikingDAL();
            string jsonData = getPostData(body);
            if (!string.IsNullOrEmpty(jsonData))
            {
                Route route = getRoute(jsonData);
                if (route != null)
                {
                    result = dal.SaveRoute(route);
                }
                else
                {
                    result.status = false;
                    result.Message = "JSON Parse Exception";
                }
            }
            else
            {
                result.status = false;
                result.Message = "Post params Empty";
            }
            
            return result;
        }

        public string getRoutesNew(Route route)
        {
            return route.title;
        }



       
    }
}
