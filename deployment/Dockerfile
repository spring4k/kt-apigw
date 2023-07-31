#FROM openjdk:11
FROM 3.39.117.244:5000/base/openjdk/openjdk11:jdk-11.0.16.1_1-alpine


ENV TZ Asia/Seoul

RUN mkdir -p /apigw
RUN mkdir -p /apigw/cusLibs

ADD ./jar/apigw-0.0.1-SNAPSHOT.jar /apigw/apigw.jar
ADD ./cusLibs/* /apigw/cusLibs/

RUN mkdir -p /data/license
RUN mkdir -p /data/file
RUN mkdir -p /data/log

ARG PROFILE
ARG JAVA_OPTS

ENV PROFILE=$PROFILE \ 
		JAVA_OPTS=$JAVA_OPTS
		
ENV JVM_ARGS="-XshowSettings:vm"

WORKDIR /apigw

RUN jar -xf /apigw/apigw.jar
RUN rm -rf /apigw/apigw.jar

EXPOSE 8080

ENTRYPOINT java -Dspring.profiles.active=${PROFILE} ${JVM_ARGS} ${JAVA_OPTS} \
			-Dcommon.file.mountPath=/data/file -Dlogging.file.path=/data/log \
			-classpath "/apigw/BOOT-INF/classes:/apigw/BOOT-INF/lib/*:/apigw/cusLibs/*" \
			com.ktds.act.apigw.ApigwApplication
