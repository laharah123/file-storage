This project represents a simple file storage system implementation

There are 5 REST APIs supported (see [here][files.yaml]):
- 3 for CRUD operations on the files (create, read, update, delete)
- one for getting the number of files in the file storage
- one for searching for files with a regex

Also, for testing purposes, a Postman collection is included. Please see [here][postman]

From the [application.yml][application.yml] file you can change the following properties:
- filestorage.file.basePath - the parent path of the file storage directory
- filestorage.file.uploadMaxSize - the maximum size of the files uploaded, expressed in bytes
- filestorage.file.pageSize - how many entries should the search API bring in one call

[files.yaml]: src/main/resources/apispecs/files.yaml
[postman]: src/main/resources/test/File%20Operations.postman_collection.json
[application.yml]: src/main/resources/application.yml