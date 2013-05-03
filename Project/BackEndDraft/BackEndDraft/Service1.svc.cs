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
using System.Diagnostics;
using System.Web;
using System.Collections.Specialized;
using System.ServiceModel.Channels;
using System.Xml;

namespace BackEndDraft
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "Service1" in code, svc and config file together.
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Allowed)]
    public class Service1 : IService1
    {
        public List<RoutePoint> getRoutePoints()
        {
            BikingDAL dal = new BikingDAL();
            return dal.getRoutePoints();
        }

        public string GetData()
        {
            return "Hello RJ";
        }
        public string getPostData(Stream body)
        {
            try
            {
                Message message = OperationContext.Current.RequestContext.RequestMessage;
                MessageBuffer buffer = message.CreateBufferedCopy(Int32.MaxValue);
                message = buffer.CreateMessage();

                var copy = buffer.CreateMessage();
                XmlDictionaryReader bodyReader = copy.GetReaderAtBodyContents();
                bodyReader.ReadStartElement("Binary");
                byte[] bodyBytes = bodyReader.ReadContentAsBase64();
                string messageBody = Encoding.UTF8.GetString(bodyBytes);

                return messageBody;
            }
            catch (Exception ex)
            {
            }
            return string.Empty;
            //return jsonString;
            //try
            //{
            //    byte[] buffer = new byte[1000];
            //    int bytesRead, totalBytesRead = 0;
            //    do
            //    {
            //        bytesRead = body.Read(buffer, 0, buffer.Length);
            //        totalBytesRead += bytesRead;
            //    } while (bytesRead > 0);
            //    var str = System.Text.Encoding.UTF8.GetString(buffer, 0, totalBytesRead);
            //    return str;
            //}
            //catch (Exception ex)
            //{

            //}
            //return string.Empty;
        }

        public UserStatus getLocation(Stream body)
        {
            UserStatus status=new UserStatus();
            try
            {
                BikingDAL dal=new BikingDAL();
                string s_params = getPostData(body);
                NameValueCollection postParams = HttpUtility.ParseQueryString(s_params);
                string userid = postParams["username"];
                RoutePoint loc= dal.getLocation(userid);
                if(loc!=null)
                {
                    status.isPresent=true;
                    status.location=loc;
                }
                else
                {
                    status.isPresent=false;
                    status.location.latitude=-1;
                    status.location.longitude=-1;
                }
            }
            catch (Exception ex)
            {
                status.isPresent = false;
                status.location.latitude = -1;
                status.location.longitude = -1;
            }
            return status;
        }

        private RoutesCollection getRoute(string jsonData)
        {
            
            try
            {
                MemoryStream stream = new MemoryStream(Encoding.UTF8.GetBytes(jsonData));
                stream.Position = 0;
                DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(RoutesCollection));
                RoutesCollection routes = (RoutesCollection)serializer.ReadObject(stream);
                if (routes != null)
                {
                    return routes;
                }
                
            }
            catch (Exception ex)
            {
                
            }
            return null;

        }

        public List<OperationResult> SaveRoute(Stream body)
        {
            
            List<OperationResult> result = new List<OperationResult>();
            try
            {
                BikingDAL dal = new BikingDAL();
                string jsonData = getPostData(body);
                if (!string.IsNullOrEmpty(jsonData))
                { 
                    RoutesCollection routes = getRoute(jsonData);
                    if (routes != null)
                    {

                        foreach (Route currentRoute in routes.routeCollection)
                        {
                            OperationResult curr_result = dal.SaveRoute(currentRoute);
                            curr_result.routeid = currentRoute.routeid;
                            result.Add(curr_result);
                        }
                    }
                    else
                    {
                        OperationResult error_result = new OperationResult();
                        error_result.status = false;
                        error_result.Message = "JSON Parse Exception";
                        result.Add(error_result);

                    }
                }
                else
                {
                    OperationResult error_result = new OperationResult();
                    error_result.status = false;
                    error_result.Message = "Post Params Empty";
                    result.Add(error_result);

                }
            }
            catch (Exception ex)
            {
                OperationResult error_result = new OperationResult();
                error_result.status = false;
                error_result.Message = ex.Message;
                result.Add(error_result);
            }
            return result;
        }

        public string getRoutesNew(Route route)
        {
            return route.title;
        }

        public User signup(Stream data)
        {
            OperationResult result = new OperationResult();
            User currentUser = null;
            try
            {
                string s_params = getPostData(data);
                NameValueCollection postParams = HttpUtility.ParseQueryString(s_params);
                string userid = postParams["uname"];
                string password = postParams["passwd"];
                BikingDAL dal = new BikingDAL();
                int returnVal = dal.InsertUser(userid, password);
                if (1==returnVal)
                {
                    currentUser = new User();
                    currentUser.user = userid;
                    currentUser.pass = password;
                }
                else if (2 == returnVal)
                {
                    currentUser = new User();
                    currentUser.user = userid;
                    currentUser.pass = password;
                    currentUser.ERROR = "Duplicate";
                }
                
            }
            catch (Exception ex)
            {
                //result.status = false;
                //result.Message = ex.Message;
            }
            return currentUser;
        }

        public User login(Stream data)
        {
            OperationResult result = new OperationResult();
            User currentUser = null;
            try
            {
                string s_params = getPostData(data);
                NameValueCollection postParams = HttpUtility.ParseQueryString(s_params);
                string userid = postParams["username"];
                string password = postParams["password"];
                BikingDAL dal = new BikingDAL();
                result = dal.Login(userid, password);
                if (result.status)
                {
                    currentUser = new User();
                    currentUser.user = userid;
                    currentUser.pass = password;
                    return currentUser;
                }
            }
            catch (Exception ex)
            {
                //result.status = false;
                //result.Message = ex.Message;
            }
            return currentUser;
        }


       
    }
}
