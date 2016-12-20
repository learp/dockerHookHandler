FROM ubuntu:14.04 

RUN \ 
	apt-get update && \ 
	apt-get install -y software-properties-common && \ 
	echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
	add-apt-repository -y ppa:webupd8team/java && \
	apt-get update && \
	apt-get install -y oracle-java8-installer && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer

RUN \
    apt-get update && \
    apt-get -y install lsof

WORKDIR /root
RUN \
    wget -o my_log http://mirrors.sonic.net/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz && \
    tar xzvf apache-maven-3.3.9-bin.tar.gz && \
    rm apache-maven-3.3.9-bin.tar.gz

WORKDIR /data
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV M2_HOME=/root/apache-maven-3.3.9
ENV PATH=$PATH:$M2_HOME/bin

ADD src /root/dockerHookHandler/src
ADD pom.xml /root/dockerHookHandler

EXPOSE 8084

WORKDIR /root/dockerHookHandler
RUN \
    mvn package && \
    rm -r src && \
    rm pom.xml


CMD java -jar /root/dockerHookHandler/target/dockerHubHookHandler-1.0-jar-with-dependencies.jar