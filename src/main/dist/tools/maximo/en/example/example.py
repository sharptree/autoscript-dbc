i = 1

def main():
    try:
        f = 12
        try:
            f2 = 1
        finally:
            fg = 5
    except:
        f4 = 2
    finally:
        f2 = 1

main()

scriptConfig="""{
    "autoscript": "EXAMPLEPY",
    "description": "An example deployment script",
    "version": "1.0.1",
    "active": true,
    "logLevel": "ERROR",
    "autoScriptVars": [
        {
            "varname": "first",
            "description": "A test variable",
            "varBindingType": "ATTRIBUTE",
            "varType": "INOUT",
            "allowOverride": true,
            "noValidation": false,
            "noAccessCheck": false,
            "noAction": false
        }
    ],
    "scriptLaunchPoints": [
        {
            "launchPointName": "EXAMPLEPY",
            "launchPointType": "OBJECT",
            "active": true,
            "description": "A test object launch point",
            "objectName": "LABOR",
            "save": true,
            "add": false,
            "update": true,
            "delete": false,
            "beforeSave": true,
            "launchPointVars": [
                {
                    "varName": "first",
                    "varBindingValue": "a value"
                }
            ]
        }
    ]
}"""