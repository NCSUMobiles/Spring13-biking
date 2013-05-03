ALTER PROCEDURE dbo.sp_insertRoute 
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
		SET @result=3;
		return 3;
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
			SELECT @routeID, geography::STGeomFromText((SELECT 'POINT(' + CONVERT(VARCHAR(16),longitude) + ' ' + CONVERT(VARCHAR(16), latitude) + ')'), 4326)
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

ALTER PROCEDURE sp_update_location
	@username	nvarchar(max),
	@latitude	float,
	@longitude	float,
	@result		int output
AS
BEGIN
	DECLARE @userid int;
	DECLARE @count	int;

	PRINT @latitude;
	PRINT @longitude

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
		INSERT INTO ActiveSessions VALUES(@userid,geography::STGeomFromText((SELECT 'POINT(' + CONVERT(VARCHAR(16),@longitude) + ' ' + CONVERT(VARCHAR(16), @latitude) + ')'), 4326))
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

CREATE PROCEDURE sp_get_location
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

CREATE PROCEDURE sp_get_routepoints
AS
BEGIN
	SELECT Location.Lat as Latitude,Location.Long as Longitude FROM RoutePoints
END

SELECT * FROM RoutePoints
SELECT RouteID,Location.Lat,Location.Long FROM RoutePoints

EXEC sp_get_routepoints

DECLARE @result int 
EXEC sp_update_location 'rjagird',89.12345678,-123.123456789,@result OUTPUT
SELECT @result

DECLARE @result int 
DECLARE @latitude float
DECLARE @longitude float
EXEC sp_get_location 'rjagird',@latitude output,@longitude output,@result output
SELECT @latitude,@longitude,@result

SELECT * FROM ActiveSessions


DECLARE @newroute as route;
DECLARE @newroutePoints as routePointsTable;
DECLARE @retval as int;

INSERT INTO @newroute VALUES( 1,'rjagird','My Third Trip','Useless','10.0','04/24/2013','04/24/2014','20.0','Cloudy')

INSERT INTO @newroutePoints(latitude,longitude)
SELECT 89.12345678,-123.123456789 UNION ALL
SELECT 88.12345678,-122.123456789 UNION ALL
SELECT 87.12345678,-121.123456789;

EXECUTE dbo.sp_insertRoute
	@newroute,@newroutePoints,@retval OUTPUT;

SELECT @retval;

DECLARE @latitude decimal(20,10)=89.12345678
DECLARE @longitude decimal(20,10)=123.12345678
BEGIN TRAN
INSERT INTO ActiveSessions VALUES(2,geography::Point(@latitude, @longitude, 4326))
COMMIT
BEGIN TRAN
DELETE FROM ActiveSessions
COMMIT
SELECT * FROM ActiveSessions
SELECT Location.Lat, Location.Long FROM ActiveSessions
SELECT Location, Location.Lat, Location.Long FROM RoutePoints;