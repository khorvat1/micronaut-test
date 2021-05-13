FROM ghcr.io/graalvm/graalvm-ce:java11-21.1.0 AS graalvm
RUN gu install native-image

WORKDIR /home/app
COPY . .
RUN microdnf install unzip curl -y
RUN curl -k -L -X GET https://services.gradle.org/distributions/gradle-6.8.3-bin.zip > gradle-6.8.3-bin.zip && \
  mkdir /opt/gradle && \
  ls -la && unzip -d /opt/gradle gradle-6.8.3-bin.zip && \
  export PATH=$PATH:/opt/gradle/gradle-6.8.3/bin && \
  gradle clean assemble && \
  mv build/layers/libs /home/app/libs && \
  mv build/layers/resources /home/app/resources && \
  mv build/layers/application.jar /home/app/application.jar && \
  native-image -H:Class=com.example.ApplicationKt -H:Name=application --no-fallback -cp /home/app/libs/*.jar:/home/app/resources:/home/app/application.jar

FROM frolvlad/alpine-glibc:alpine-3.12
RUN apk update && apk add libstdc++
COPY --from=graalvm /home/app/application /app/application

ENV PORT=8080

EXPOSE $PORT

ENTRYPOINT ["/app/application"]