OslcQuery = Java.type("com.ibm.tivoli.oslc.OslcQuery");
RESTRequest = Java.type("com.ibm.tivoli.oslc.RESTRequest");

OslcUtils = Java.type("com.ibm.tivoli.maximo.oslc.OslcUtils");
LocalURIGenerator = Java.type("com.ibm.tivoli.maximo.oslc.provider.LocalURIGenerator");
OslcNsPrefixCache = Java.type("com.ibm.tivoli.maximo.oslc.provider.OslcNsPrefixCache");
OslcQueryHandler = Java.type("com.ibm.tivoli.maximo.oslc.provider.OslcQueryHandler");
SavedQueryHandler = Java.type("com.ibm.tivoli.maximo.oslc.provider.SavedQueryHandler");

RuntimeException = Java.type("java.lang.RuntimeException");
StringBuffer = Java.type("java.lang.StringBuffer");

UpgConstants = Java.type("psdi.configure.UpgConstants");

ConversionUtil = Java.type("psdi.iface.mos.ConversionUtil");

GLFormat = Java.type("psdi.mbo.GLFormat");
MboSetInfo = Java.type("psdi.mbo.MboSetInfo");
SqlFormat = Java.type("psdi.mbo.SqlFormat");

MXServer = Java.type("psdi.server.MXServer");

main();

function main() {
    // example function
}

var scriptConfig = {
    "autoscript": "examplejs",
    "description": "Example Automation Script",
    "version": "1.0.0",
    "active": true,
    "logLevel": "INFO"
}
