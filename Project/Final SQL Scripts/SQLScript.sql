USE [BikingDraft]
GO
/****** Object:  User [testuser]    Script Date: 5/3/2013 5:33:55 AM ******/
CREATE USER [testuser] FOR LOGIN [testuser] WITH DEFAULT_SCHEMA=[dbo]
GO
ALTER ROLE [db_owner] ADD MEMBER [testuser]
GO
/****** Object:  UserDefinedTableType [dbo].[route]    Script Date: 5/3/2013 5:33:55 AM ******/
CREATE TYPE [dbo].[route] AS TABLE(
	[RouteID] [int] NULL,
	[Username] [nvarchar](max) NULL,
	[Title] [nvarchar](50) NULL,
	[Description] [nvarchar](max) NULL,
	[Speed] [float] NULL,
	[StartTime] [nvarchar](50) NULL,
	[EndTime] [nvarchar](50) NULL,
	[Distance] [float] NULL,
	[WeatherInfo] [nvarchar](max) NULL
)
GO
/****** Object:  UserDefinedTableType [dbo].[routePointsTable]    Script Date: 5/3/2013 5:33:55 AM ******/
CREATE TYPE [dbo].[routePointsTable] AS TABLE(
	[latitude] [decimal](20, 10) NULL,
	[longitude] [decimal](20, 10) NULL
)
GO
/****** Object:  StoredProcedure [dbo].[sp_get_location]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[sp_get_location]
	@username nvarchar(max),
	@latitude float output,
	@longitude float output,
	@present int output
AS
BEGIN
	DECLARE @userid int;
	DECLARE @count	int;
	DECLARE @location geography;
	SELECT @count=COUNT(*) FROM dbo.Users WHERE username=@username
	IF @count=0
	BEGIN
		SET @present=0
		SET @latitude=-1
		SET @longitude=-1
		return 0
	END
	SELECT @userid=userid FROM dbo.Users WHERE username=@username
	PRINT @userid
	SELECT @location=Location FROM ActiveSessions WHERE UserId=@userid
	SET @latitude=@location.Lat
	SET @longitude=@location.Long
	SET @present=1
END

GO
/****** Object:  StoredProcedure [dbo].[sp_get_password]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[sp_get_password]
	@username nvarchar(max),
	@password nvarchar(max) OUTPUT
AS
BEGIN
	SELECT @password=Password FROM dbo.Users WHERE username=@username
	IF @@ROWCOUNT=0
	BEGIN
		SET @password='Empty'
	END
END

GO
/****** Object:  StoredProcedure [dbo].[sp_get_routepoints]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[sp_get_routepoints]
AS
BEGIN
	SELECT Location.Lat as Latitude,Location.Long as Longitude FROM RoutePoints
END

GO
/****** Object:  StoredProcedure [dbo].[sp_insert_user]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[sp_insert_user]
	@username nvarchar(max),
	@password nvarchar(max),
	@result int output
AS
BEGIN
	DECLARE @count int
	SELECT @count=COUNT(*) FROM dbo.Users WHERE username=@username
	IF @count=0
	BEGIN
		PRINT 'Username not exists'
		BEGIN TRAN
			INSERT INTO dbo.Users VALUES(@username, @password)
			IF @@ERROR<>0
			BEGIN 
				PRINT 'ERROR Not zero'
				ROLLBACK TRAN
				SET @result= 0
			END
			ELSE
			BEGIN
				PRINT 'ERROR =0'
				COMMIT TRAN
				SET @result=1
			END
	END
	ELSE
	BEGIN
		PRINT 'HERE'
		SET @result=2
	END
END

GO
/****** Object:  StoredProcedure [dbo].[sp_insertRoute]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_insertRoute] 
	@route route READONLY,
	@routePoints routePointsTable READONLY,
	@result INT OUTPUT
AS
BEGIN
	SET NOCOUNT ON
	DECLARE @routeID INT;
	DECLARE @count INT; 
	DECLARE @username nvarchar(max)
	DECLARE @userid int;
	DECLARE @time nvarchar(50);
			
	SELECT @username=Username FROM @route
	SELECT @count=COUNT(*) FROM dbo.Users WHERE username=@username
	IF @count=0
	BEGIN
		BEGIN TRAN
		INSERT INTO dbo.Users VALUES(@username,@username)
		COMMIT
		--SET @result=3;
		--return 3;
	END

	SELECT @userid=userid FROM dbo.Users WHERE username=@username
	SELECT @routeID=RouteID FROM @route
	SELECT @time=EndTime FROM @route

	SELECT @count=COUNT(*) FROM dbo.Routes WHERE RouteID=@routeID AND UserId=@userid AND EndTime=@time

	IF @count=0
	BEGIN 
		BEGIN TRANSACTION
		BEGIN TRY
			INSERT INTO dbo.Routes
			SELECT RouteID,@userid,Title,[Description],Speed,StartTime,EndTime,Distance,WeatherInfo FROM @route

			SET @routeID= SCOPE_IDENTITY();

			INSERT INTO RoutePoints 
			SELECT @routeID, geography::STGeomFromText((SELECT 'POINT(' + CONVERT(VARCHAR(40),longitude) + ' ' + CONVERT(VARCHAR(40), latitude) + ')'), 4326)
			FROM @routePoints
			COMMIT TRANSACTION
			SET @result=1;
			RETURN 1
		END TRY
		BEGIN CATCH
			ROLLBACK TRANSACTION;
			DECLARE @msg NVARCHAR(MAX) = ERROR_MESSAGE();
			RAISERROR(@msg, 11, 1);
			SET @result=0
			RETURN 0
		END CATCH
	END
	ELSE
	BEGIN
		SET @result=2
		RETURN 2
	END
END


GO
/****** Object:  StoredProcedure [dbo].[sp_update_location]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_update_location]
	@username	nvarchar(max),
	@latitude	decimal(20,10),
	@longitude	decimal(20,10),
	@result		int output
AS
BEGIN
	DECLARE @userid int;
	DECLARE @count	int;

	SELECT @count=COUNT(*) FROM dbo.Users WHERE username=@username
	IF @count=0
	BEGIN
		SET @result=2
		return 2
	END
	SELECT @userid=userid FROM dbo.Users WHERE username=@username
	PRINT @userid
	SELECT @count=COUNT(*) FROM dbo.ActiveSessions WHERE UserId=@userid
	PRINT @count
	IF @count=0
	BEGIN
		BEGIN TRAN
	BEGIN TRY
		PRINT 'INSERT'
		INSERT INTO ActiveSessions VALUES(@userid,geography::STGeomFromText((SELECT 'POINT(' + CONVERT(VARCHAR(40),@longitude) + ' ' + CONVERT(VARCHAR(40), @latitude) + ')'), 4326))
		IF @@ERROR=0
		BEGIN
			COMMIT TRAN
			SET @result=1
			return 1
		END
		ELSE
		BEGIN
			ROLLBACK TRAN
			SET @result=0
			return 0
		END
	END TRY
	BEGIN CATCH
		ROLLBACK TRAN
			SET @result=3
			return 0
	END CATCH
	END
	ELSE
	BEGIN
		BEGIN TRAN
		BEGIN TRY
			PRINT 'UPDATE'
			UPDATE ActiveSessions SET Location=geography::STGeomFromText((SELECT 'POINT(' + CONVERT(VARCHAR(16),@longitude) + ' ' + CONVERT(VARCHAR(16), @latitude) + ')'), 4326) WHERE UserId=@userid
			IF @@ERROR=0
			BEGIN
				COMMIT TRAN
				SET @result=1
				return 1
			END
			ELSE
			BEGIN
				ROLLBACK TRAN
				SET @result=0
				return 0
			END
	END TRY
	BEGIN CATCH
		ROLLBACK TRAN
			SET @result=3
			return 0
	END CATCH
	END
	
END


GO
/****** Object:  Table [dbo].[ActiveSessions]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ActiveSessions](
	[sessionid] [int] IDENTITY(1,1) NOT NULL,
	[UserId] [int] NULL,
	[Location] [geography] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[sessionid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[RoutePoints]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RoutePoints](
	[RouteID] [int] NULL,
	[Location] [geography] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Routes]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Routes](
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[RouteID] [int] NOT NULL,
	[UserId] [int] NULL,
	[Title] [nvarchar](50) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[Speed] [float] NOT NULL,
	[StartTime] [nvarchar](50) NOT NULL,
	[EndTime] [nvarchar](50) NOT NULL,
	[Distance] [float] NOT NULL,
	[WeatherInfo] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Users]    Script Date: 5/3/2013 5:33:55 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Users](
	[userid] [int] IDENTITY(1,1) NOT NULL,
	[username] [nvarchar](max) NOT NULL,
	[password] [nvarchar](max) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[userid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
ALTER TABLE [dbo].[ActiveSessions]  WITH CHECK ADD FOREIGN KEY([UserId])
REFERENCES [dbo].[Users] ([userid])
GO
ALTER TABLE [dbo].[RoutePoints]  WITH CHECK ADD FOREIGN KEY([RouteID])
REFERENCES [dbo].[Routes] ([ID])
GO
ALTER TABLE [dbo].[Routes]  WITH CHECK ADD FOREIGN KEY([UserId])
REFERENCES [dbo].[Users] ([userid])
GO
