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

CREATE TABLE [dbo].[Routes](
	[RouteID] [int] IDENTITY(1,1) PRIMARY KEY,
	[Title] [nvarchar](50) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[Speed] [float] NOT NULL,
	[Duration] [float] NOT NULL,
	[Distance] [float] NOT NULL,
	[Date]	[DateTime] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

CREATE TABLE [dbo].[RoutePoints](
	[RouteID]	[int]	 FOREIGN KEY REFERENCES Routes(RouteID),
	[Location]	[Geography] NOT NULL,
)

CREATE Type dbo.route AS TABLE
(
	[Title]			[nvarchar](50),
	[Description]	[nvarchar](max),
	[Speed]			[float],
	[Duration]		[float],
	[Distance]		[float]
		
)

CREATE Type dbo.routePointsTable AS TABLE
(
	[latitude]		[Decimal],
	[longitude]		[Decimal]
)

SELECT *
FROM sys.types
WHERE [name] IN ('route', 'routePointsTable');
GO


ALTER PROCEDURE dbo.sp_insertRoute 
	@route route READONLY,
	@routePoints routePointsTable READONLY
AS
BEGIN
	SET NOCOUNT ON
	DECLARE @routeID INT;

	BEGIN TRANSACTION
	BEGIN TRY

		INSERT INTO dbo.Routes
		SELECT Title,Description,Speed,Duration,Distance, GETDATE() FROM @route

		SET @routeID= SCOPE_IDENTITY();

		INSERT INTO RoutePoints 
		SELECT @routeID, geography::STGeomFromText((SELECT 'POINT(' + CONVERT(VARCHAR(16),longitude) + ' ' + CONVERT(VARCHAR(16), latitude) + ')'), 4326)
		FROM @routePoints
	COMMIT TRANSACTION
	return 1;
	END TRY
	BEGIN CATCH
	ROLLBACK TRANSACTION;
    DECLARE @msg NVARCHAR(MAX) = ERROR_MESSAGE();
    RAISERROR(@msg, 11, 1);
    return 0;
	END CATCH

END

DECLARE @newroute as route;
DECLARE @newroutePoints as routePointsTable;
DECLARE @retval as int;

INSERT INTO @newroute VALUES( 'My Third Trip','Useless','10.0','20.0','10.0')

INSERT INTO @newroutePoints(latitude,longitude)
SELECT 10.0,10.0 UNION ALL
SELECT 11.0,11.0 UNION ALL
SELECT 12.0,12.0;

EXECUTE @retval= dbo.sp_insertRoute
	@newroute,@newroutePoints;

SELECT @retval;

select * from Routes;
select * from RoutePoints;

Begin tran
delete from RoutePoints;
delete from Routes;
commit;





GO
