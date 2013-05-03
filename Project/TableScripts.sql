INSERT INTO dbo.Routes VALUES('My Second Trip','Absolutely Useless2','10.0','30.00','10',GETDATE())
SELECT * from Routes;
SELECT * FROM dbo.[Routes];


USE [BikingDraft]
GO

/****** Object:  Table [dbo].[Routes]    Script Date: 07/04/2013 06:49:16 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

/****** Object:  Table [dbo].[Users]    Script Date: 22/04/2013 05:26:42 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Users](
	[userid] [int] IDENTITY(1,1) PRIMARY KEY,
	[username] [nvarchar](max) NOT NULL,
	[password] [nvarchar](max) NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

DROP TABLE ActiveSessions
CREATE TABLE [dbo].[ActiveSessions](
	[sessionid] [int] IDENTITY(1,1) PRIMARY KEY,
	[UserId] [int] FOREIGN KEY REFERENCES Users(userid),
	[Location][geography] NOT NULL
)


CREATE TABLE [dbo].[Routes](
	[ID] [int] IDENTITY(1,1) PRIMARY KEY,
	[RouteID] [int] NOT NULL,
	[UserId] [int] FOREIGN KEY REFERENCES Users(userid),
	[Title] [nvarchar](50) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[Speed] [float] NOT NULL,
	[StartTime] [nvarchar](50) NOT NULL,
	[EndTime] [nvarchar](50) NOT NULL,
	[Distance] [float] NOT NULL,
	[WeatherInfo]	[nvarchar](max)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

CREATE TABLE [dbo].[RoutePoints](
	[RouteID]	[int]	 FOREIGN KEY REFERENCES Routes(ID),
	[Location]	[Geography] NOT NULL,
)

CREATE Type dbo.route AS TABLE
(
	[RouteID]		[int],
	[Username]		[nvarchar](max),
	[Title]			[nvarchar](50),
	[Description]	[nvarchar](max),
	[Speed]			[float],
	[StartTime]		[nvarchar](50),
	[EndTime]		[nvarchar](50),
	[Distance]		[float],
	[WeatherInfo]	[nvarchar](max)
)



CREATE Type dbo.routePointsTable AS TABLE
(
	[latitude]		decimal(20,10),
	[longitude]		decimal(20,10)
)

SELECT *
FROM sys.types
WHERE [name] IN ('route', 'routePointsTable');
GO

DROP Type route;
Drop Type routePointsTable;


select * from Routes;
SELECT * FROM RoutePoints;
select count(*) from RoutePoints;
select * from Users;

Begin tran
delete from RoutePoints;
delete from Routes;
commit;

BEGIN TRAN
INSERT INTO USER VALUES('','')
COMMIT
select * from users;



DROP TABLE Users;
GO


DECLARE @loc geography
SELECT @loc=Location FROM RoutePoints
SELECT @loc
SELECT @loc.Lat as Lat
SELECT @loc.Long as Long
