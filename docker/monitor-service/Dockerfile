FROM openjdk:8-jre-alpine
ENV TIME_ZONE Asia/Shanghai
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add tzdata && cp /usr/share/zoneinfo/${TIME_ZONE} /etc/localtime \
    && echo ${TIME_ZONE} > /etc/timezone \
    && apk del tzdata
COPY ./monitor-service-*.jar /app/monitor-service.jar
COPY ./*.yml /app/config/
WORKDIR /app
EXPOSE 5000
CMD ["java", "-jar", "-Dspring.profiles.active=prod", "monitor-service.jar"]