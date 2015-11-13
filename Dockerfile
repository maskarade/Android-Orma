FROM gfx2015/android:latest

MAINTAINER FUJI Goro <g.psy.va@gmail.com>

ENV PROJECT /project

RUN mkdir $PROJECT
WORKDIR $PROJECT

ADD . $PROJECT

RUN echo "sdk.dir=$ANDROID_HOME" > local.properties && \
    ./gradlew --stacktrace androidDependencies 

CMD ./gradlew --stacktrace check
