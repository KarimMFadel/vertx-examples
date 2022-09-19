
# testing using curl

curl http://localhost:8080/api/v1/hello -H "AUTH_TOKEN:mySuperSecretAuthToken"

curl http://localhost:8080/api/v1/hello/karim -H "AUTH_TOKEN:mySuperSecretAuthToken"


# incase you send non exist path so it will return
curl http://localhost:8080/api/v1/ -H "AUTH_TOKEN:mySuperSecretAuthToken"
<html><body><h1>Resource not found</h1></body></html>

# in case we go with the root path we will get the index.html page  
curl http://localhost:8080/ -H "AUTH_TOKEN:mySuperSecretAuthToken"
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Hello Static Content</title>
</head>
<body>
        <h1>Hello Vert.X Static Content</h1>
</body>
</html>

# if you send 
curl http://localhost:8080/api/v1/hello/karim -vvv

*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /api/v1/hello/karim HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.79.1
> Accept: */*
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 401 Unauthorized
< content-length: 0
<
* Connection #0 to host localhost left intact