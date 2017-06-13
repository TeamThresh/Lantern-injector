# Lantern Java Byte Code Injector

## 사용법

아래 코드를 app의 build.gradle 에 추가한다
```
...
apply plugin: 'com.lantern.lantern'

...

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath 'com.lantern:lantern-injector:0.1.65'
    }
}

...

repositories {
    mavenCentral()
}
```