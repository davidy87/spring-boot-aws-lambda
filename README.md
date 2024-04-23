# Spring Boot + AWS Lambda
___
Spring Boot 3 Application + AWS Lambda Demo

## References
* https://github.com/aws/serverless-java-container/wiki/Quick-start---Spring-Boot3
* https://github.com/aws/serverless-java-container/tree/main/samples/springboot3

<br>

# Getting Started

## Pre-requisites
___

* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
* [SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)
* [Gradle](https://gradle.org/)

<br>

## 1. build.gradle 설정
___

### 의존성 추가

```
dependencies {
    implementation ('org.springframework.boot:spring-boot-starter-web') {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
    }
    
    // 
    implementation 'com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.0.1'
    implementation 'com.amazonaws:aws-lambda-java-core:1.2.2'
    implementation 'com.amazonaws:aws-lambda-java-events:3.11.1'
    runtimeOnly 'com.amazonaws:aws-lambda-java-log4j2:1.5.1'
    
    // 생략
}
```

### 프로젝트 패키지 빌드
zip 파일 아카이브를 사용하여 직접 함수를 배포할 때 사용한다.


```
task buildZip(type: Zip) {
    from compileJava
    from processResources
    dependencies {
        exclude(group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat')
    }
    into('lib') {
        from(configurations.compileClasspath) {
            exclude 'tomcat-embed-*'
        }
    }
}

build.dependsOn buildZip
```

### 참고사항
의존성 추가와 빌드 설정에 내장 Tomcat을 제외했다. AWS Lambda에 배포 시, 내장 Tomcat은 필요하지 않기 때문에 제외했다.
* 참고: https://github.com/aws/serverless-java-container/wiki/Quick-start---Spring-Boot3#3-packaging-the-application
* 내장 Tomcat을 제외하지 않아도 잘 작동하는 것을 확인했다. 따라서, 제외하는 것이 필수는 아닌 듯하다.

<br>

## 2. template.yml 생성
___

AWS SAM CLI을 통해 빌드 및 배포를 위해 `template.yml`을 작성해야 한다.
* 참고: [SAM template](https://github.com/aws/serverless-application-model)


```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: Spring Boot + AWS Lambda demo project

Globals:
  Api:
    # API Gateway regional endpoints
    EndpointConfiguration: REGIONAL

Resources:
  DemoFunction:
    Type: AWS::Serverless::Function
    Properties:
      Handler: demo.springlambda.handler.StreamLambdaHandler::handleRequest
      Runtime: java17
      CodeUri: .
      Architectures:
        - x86_64
      MemorySize: 2048
      Policies: AWSLambdaBasicExecutionRole
      Timeout: 60
      SnapStart:
        ApplyOn: PublishedVersions
      AutoPublishAlias: prod
      Events:
        HelloWorld:
          Type: Api
          Properties:
            Path: /{proxy+}
            Method: ANY

Outputs:
  DemoApplicationApi:
    Description: URL for application
    Value: !Sub 'https://${ServerlessRestApi}.execute-api.${AWS::Region}.amazonaws.com'
    Export:
      Name: DemoApplicationApi
```

<br>

## 3. 빌드 및 배포
___

### 빌드
```
sam build
```

### Local에 함수 호출 테스트
```
sam local start-api
```
* Local 사용 시, Docker 설치 및 실행 필수
* Docker를 실행했는데도 오류가 생긴다면? --> [해결 방법](https://github.com/aws/aws-sam-cli/issues/5646)

### AWS Lambda에 배포
```
sam deploy --guided
```
