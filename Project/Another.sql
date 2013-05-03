ALTER PROCEDURE sp_insert_user
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
		SET @result=0
	END
END

ALTER PROCEDURE sp_get_password
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



DECLARE @retval as int;
EXECUTE sp_insert_user 'pvthakka','Infy@123',@retval output
SELECT @retval
COMMIT

DECLARE @password nvarchar(max)
EXEC sp_get_password 'ldmello', @password OUTPUT
SELECT @password
select * from dbo.Users;

