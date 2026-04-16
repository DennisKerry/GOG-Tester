cd /d "C:\Users\Dennis\OneDrive - Florida Gulf Coast University\2026 Spring\CEN 4072\Project"
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
del target\test-classes\com\gog\tests\SignupPageTest.class 2>nul
mvn test -Dtest=SignupPageTest > test_out.txt 2>&1
