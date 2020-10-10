def upload(String sourceFilePath, String targetFileName, String clientID, String clientSecret, String refreshToken, List<String> parentFolderIDs = [])
{
   bat label: "Upload files to Google Drive", script: """
      SET "AccessToken="
      FOR /f "tokens=1,2 delims=:, " %%U in ('
      curl ^
      --request POST ^
      --data "client_id=${clientID}&client_secret=${clientSecret}&refresh_token=${refreshToken}&grant_type=refresh_token" ^
      https://accounts.google.com/o/oauth2/token ^| findstr /i "\"access_token\""
      ') DO SET "AccessToken=,%%~V"
      IF DEFINED AccessToken(SET "AccessToken=%AccessToken:~1%") ELSE (SET "AccessToken=n/a")
      SET AccessToken
      curl -X POST ^
      -H "Authorization: Bearer %AccessToken%" ^
      -F "metadata={ kind: 'drive#file', name: '${targetFileName}', parents: ['${parentFolders.join('\',\'')}'] };type=application/json;charset=UTF-8" ^
      -F "file=@${sourceFilePath}" ^
      https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart
   """
}
