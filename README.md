# Lantern Java Byte Code Injector

## 사용법

아래 코드를 app의 build.gradle 에 추가한다
```
...
apply plugin: 'hello.thinkcode.demo.plugin'

...

buildscript {
    repositories {
    	// (Maven Central 에 올려두지 않았다면 로컬 lib로 추가 하므로 아래 코드 추가)
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath 'hello.thinkcode.gradle:demoPlugin:1.0.0-SNAPSHOT'
    }
}

...

repositories {
	// (Maven Central 에 올려두지 않았다면 로컬 lib로 추가 하므로 아래 코드 추가)
    flatDir {
        dirs 'libs'
    }
    mavenCentral()
}
```

안드로이드의 Build task 중 :app:compileDebugJavaWithJavac 에 코드를 삽입하며
실제 Release mode로 배포할 경우 :app:compileReleaseJavaWithJavac 로 바꿔야 할듯