using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Data.SqlClient;
using System.Data;
using System.Configuration;

namespace BackEndDraft
{
    public class BikingDAL
    {
        public string connectionString = ConfigurationManager.AppSettings.Get("connectionString");
        SqlConnection con;

        public OperationResult SaveRoute(Route route)
        {
            OperationResult result = new OperationResult();
            
            var routeDetails = new DataTable();
            routeDetails.Columns.Add("Title", typeof(string));
            routeDetails.Columns.Add("Description", typeof(string));
            routeDetails.Columns.Add("Speed", typeof(float));
            routeDetails.Columns.Add("Duration", typeof(float));
            routeDetails.Columns.Add("Distance", typeof(float));

            var routePoints = new DataTable();
            routePoints.Columns.Add("latitude", typeof(decimal));
            routePoints.Columns.Add("longitude", typeof(decimal));

            routeDetails.Rows.Add(new Object[] {route.title,route.description,route.speed,route.duration,route.distance});
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

                        int status = cmd.ExecuteNonQuery();
                        
                        result.status = true;
                        result.Message = "Saved Route Successfully";
                        
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
    }
}