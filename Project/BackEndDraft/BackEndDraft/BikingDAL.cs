using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.SqlClient;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Diagnostics;


namespace BackEndDraft
{
    public class BikingDAL
    {
        public string connectionString = ConfigurationManager.AppSettings.Get("connectionString");
        SqlConnection con;
        string sSource="Biking DAL";
        string sLog="Application";
        string sEvent;

        public List<RoutePoint> getRoutePoints()
        {
            try
            {
                using (con = new SqlConnection(connectionString))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        cmd.CommandType = CommandType.StoredProcedure;
                        cmd.CommandText = "sp_get_routepoints";
                        SqlDataAdapter adapter = new SqlDataAdapter(cmd);
                        DataTable dt = new DataTable();
                        adapter.Fill(dt);

                        IEnumerable<DataRow> rowsCollection = dt.AsEnumerable();
                        List<DataRow> list=rowsCollection.ToList<DataRow>();
                        List<RoutePoint> result = list.Select(x => new RoutePoint() { latitude = Convert.ToDouble(x["Latitude"]), longitude = Convert.ToDouble(x["Longitude"]) }).ToList<RoutePoint>();

                        return result;

                    }
                }
            }
            catch (Exception ex)
            {
            }
            return null;
        }

        public RoutePoint getLocation(string username)
        {
            RoutePoint current_loc=null;
            try
            {
                using (con = new SqlConnection(connectionString))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        cmd.CommandType = CommandType.StoredProcedure;
                        cmd.CommandText = "sp_get_location";
                        var userid_param = cmd.Parameters.AddWithValue("@username", username);
                        
                        SqlParameter lat_param = new SqlParameter("@latitude", SqlDbType.Float);
                        lat_param.Direction = ParameterDirection.Output;
                        cmd.Parameters.Add(lat_param);

                        SqlParameter long_param = new SqlParameter("@longitude", SqlDbType.Float);
                        long_param.Direction = ParameterDirection.Output;
                        cmd.Parameters.Add(long_param);

                        SqlParameter return_param = new SqlParameter("@present", SqlDbType.Int);
                        return_param.Direction = ParameterDirection.Output;
                        cmd.Parameters.Add(return_param);


                        cmd.ExecuteNonQuery();
                        if (1 == Convert.ToInt32(return_param.Value))
                        {
                            current_loc = new RoutePoint();
                            current_loc.latitude = Convert.ToDouble(lat_param.Value);
                            current_loc.longitude = Convert.ToDouble(long_param.Value);
                        }
                    }
                }
            }
            catch (Exception ex)
            {

            }
            return null;
            
        }

        public int InsertUser(string userid, string password)
        {
            //OperationResult result = new OperationResult();
            int returnVal = -1;
            try
            {
                using (con = new SqlConnection(connectionString))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        cmd.CommandType = CommandType.StoredProcedure;
                        cmd.CommandText = "sp_insert_user";
                        var userid_param = cmd.Parameters.AddWithValue("@username", userid);
                        var password_param = cmd.Parameters.AddWithValue("@password", password);
                        
                        SqlParameter return_param = new SqlParameter("@result", SqlDbType.Int);
                        return_param.Direction = ParameterDirection.Output;
                        cmd.Parameters.Add(return_param);
                        


                        userid_param.SqlDbType = SqlDbType.NVarChar;
                        password_param.SqlDbType = SqlDbType.NVarChar;
                        
                        cmd.ExecuteNonQuery();
                        returnVal = Convert.ToInt32(return_param.Value);
                        //if (1 == Convert.ToInt32(return_param.Value))
                        //{
                        //    result.status = true;
                        //    result.Message = "User Signed up";
                        //}
                        //else if (2 == Convert.ToInt32(return_param.Value))
                        //{
                        //    result.status = false;
                        //    result.Message = "User Sign Up Failed";
                        //}

                    }
                }
            }
            catch (Exception ex)
            {
                returnVal = -2;
                //result.status = false;
                //result.Message = "Data Exception";
            }
            return returnVal;

        }

        public OperationResult Login(string userid, string password)
        {
            OperationResult result = new OperationResult();
            try
            {
                using (con = new SqlConnection(connectionString))
                {
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        cmd.CommandType = CommandType.StoredProcedure;
                        cmd.CommandText = "sp_get_password";
                        var userid_param = cmd.Parameters.AddWithValue("@username", userid);

                        SqlParameter password_param = new SqlParameter("@password", SqlDbType.VarChar);
                        password_param.Direction = ParameterDirection.Output;
                        password_param.Size = 8000;
                        cmd.Parameters.Add(password_param);
                        userid_param.SqlDbType = SqlDbType.NVarChar;
                        

                        cmd.ExecuteNonQuery();
                        if (password == password_param.Value as string)
                        {
                            result.status = true;
                            result.Message = "User Logged In";
                        }
                        else
                        {
                            result.status = false;
                            result.Message = "User Login Failed";
                        }

                    }
                }
            }
            catch (Exception ex)
            {
                result.status = false;
                result.Message = "Data Exception";
            }
            return result;

        }

        public OperationResult SaveRoute(Route route)
        {
            OperationResult result = new OperationResult();
            
            var routeDetails = new DataTable();
            
            routeDetails.Columns.Add("RouteID", typeof(int));
            routeDetails.Columns.Add("Username", typeof(string));
            routeDetails.Columns.Add("Title", typeof(string));
            routeDetails.Columns.Add("Description", typeof(string));
            routeDetails.Columns.Add("Speed", typeof(float));
            routeDetails.Columns.Add("StartTime", typeof(string));
            routeDetails.Columns.Add("EndTime", typeof(string));
            routeDetails.Columns.Add("Distance", typeof(float));
            routeDetails.Columns.Add("WeatherInfo", typeof(string));
            
            var routePoints = new DataTable();
            routePoints.Columns.Add("latitude", typeof(Double));
            routePoints.Columns.Add("longitude", typeof(Double));

            routeDetails.Rows.Add(new Object[] {route.routeid,route.username,route.title,route.description,route.speed,route.starttime,route.endtime,route.distance,route.weatherinfo});
            foreach (RoutePoint point in route.pointCollection)
            {
                routePoints.Rows.Add(new Object[] {point.latitude,point.longitude});
            }
            try
            {
                using (con = new SqlConnection(connectionString))
                {    
                    con.Open();
                    using (SqlCommand cmd = con.CreateCommand())
                    {
                        cmd.CommandType = CommandType.StoredProcedure;
                        cmd.CommandText = "sp_insertRoute";
                        var routeParam = cmd.Parameters.AddWithValue("@route", routeDetails);
                        var routePointsParams = cmd.Parameters.AddWithValue("@routePoints", routePoints);

                        routeParam.SqlDbType = SqlDbType.Structured;
                        routePointsParams.SqlDbType = SqlDbType.Structured;

                        SqlParameter result_param = new SqlParameter("@result", SqlDbType.Int);
                        result_param.Direction = ParameterDirection.Output;
                        cmd.Parameters.Add(result_param);

                        cmd.ExecuteNonQuery();
                        int status = Convert.ToInt32(result_param.Value);
                        if (status == 1)
                        {
                            result.status = true;
                            result.Message = "Saved Route Successfully";
                        }
                        else if (status == 0)
                        {
                            result.status = false;
                            result.Message = "Exception in Procedure";
                        }
                        else if (status == 2)
                        {
                            result.status = false;
                            result.Message = "Duplicate";
                        }
                        else if (status == 3)
                        {
                            result.status = false;
                            result.Message = "Invalid User";
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                result.status = false;
                result.Message = ex.Message;
                sEvent = ex.Message;
                if (!EventLog.SourceExists(sSource))
                    EventLog.CreateEventSource(sSource, sLog);
                EventLog.WriteEntry(sSource, sEvent);
                EventLog.WriteEntry(sSource, sEvent, EventLogEntryType.Error, 234);
            }            
            return result;
        }
    }
}