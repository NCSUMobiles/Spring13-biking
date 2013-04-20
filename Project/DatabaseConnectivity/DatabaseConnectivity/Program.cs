using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;
using System.Configuration;
using System.Data.SqlClient;


namespace DatabaseConnectivity
{
    public class Program
    {
        public static void Main(string[] args)
        {
            string connectionstring = ConfigurationManager.AppSettings.Get("connectionString");
            SqlConnection con = new SqlConnection(connectionstring);
            SqlCommand cmd = con.CreateCommand();
            cmd.CommandText = @"SELECT * FROM Routes";
            con.Open();
            using (SqlDataReader reader = cmd.ExecuteReader())
            {
                while (reader.Read())
                {
                    Console.WriteLine("{0} {1} {2}",
                    reader.GetInt32(0), reader.GetString(1), reader.GetString(2));
                }
            }
        }
    }
}
