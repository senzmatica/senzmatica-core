Build Project: mvn clean install

To run:    mvn clean spring-boot:run

#### Build Project:

mvn clean install

#### Change java version in macos

export JAVA_HOME=`/usr/libexec/java_home -v 1.8`

#### Java versions

    sudo update-alternatives --config java
    
    sudo update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/jre/bin/java
    
    sudo update-alternatives --set javac /usr/lib/jvm/java-11-openjdk-amd64/bin/javac

    source /etc/environment