# turbobytes-Java

Java library to access Turbobytes API

Sample usage, to purge a file

    TurbobytesAPI turbobytes = new TurbobytesAPI("xxxxAPI_KEYxxxx", "xxxxAPI_SECRETxxxx");
    System.out.println(turbobytes.purge("zone-name", "/path/to/foo.jpg, /path/to/bar.css"));
    //Show latest purges
    System.out.println(turbobytes.latest_purges("zone-name"));
