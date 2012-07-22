This directory has some one-line batch files that illustrate how to run the build with Maven profiles turned on and off.

See www.expressui.com for more documentation.

Details about the .bat files:
    build.bat:        run this first, does a standard clean install
    runJetty.bat:     run this second, deploys the application and launches it in Jetty
    buildKeepDB.bat:  does a build but does not regenerate the database schema and data
    cobertura.bat:    runs tests with code coverage
    buildProdDB.bat:  runs the build against a production database. You must configure the project for
                      your production database before running this.