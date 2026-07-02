# 一次性设置:生成 Gradle Wrapper jar

本仓库**没有**提交 `gradle/wrapper/gradle-wrapper.jar`(约 60KB,二进制)。
首次 clone 后需要生成:

## 方法 A:本机有 Gradle

```bash
cd qingqiuyue-android
gradle wrapper --gradle-version 8.10.2
```

会自动生成 `gradle/wrapper/gradle-wrapper.jar` + `gradlew` + `gradlew.bat`(若没有)。

## 方法 B:没装 Gradle

下载 [gradle-wrapper.jar](https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradle/wrapper/gradle-wrapper.jar)
放到 `gradle/wrapper/gradle-wrapper.jar`。

## 方法 C:Android Studio 自动生成

用 Android Studio 打开项目时,Gradle 会自动检测到 wrapper 缺失并提示生成,点确认即可。

## 验证

```bash
./gradlew --version
# 应输出 Gradle 8.10.2 + JVM 版本
```