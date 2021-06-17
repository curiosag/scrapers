mvn package -Dmaven.test.skip=true
 scp ./target/*.jar sm@flat:/home/sm/run
 scp ./target/*.jar sm@fat:/home/sm/dev/run