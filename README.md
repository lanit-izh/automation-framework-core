## Lanit automation framework-core
### Назначенние
Фреймворк объединяет управление зависимостями от Selenium WebDriver, Yandex Ashot, qameta/Atlas и citrus.
Для наиболее простого развёртывания используйте коробочную версию [Lanit AT framework Box](https://github.com/lanit-izh/automation-framework-box.git). В данном кратком руководстве по использованию будет рассказано о подключении данного ядра, а так же вспомогательных библиотек, не включённых в ядро для большей свободы выбора тестового фреймворка, фреймворков отчётности, логгеров и т.п. 
### Использование
Добавьте в зависимости к своему maven-проекту 
```xml
<dependency>
  <groupId>com.github.lanit-izh</groupId>
  <artifactId>automation-framework-core</artifactId>
  <version>4.0.9</version>
</dependency>
```
или gradle-проекту:
```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    compile("com.github.lanit-izh:automation-framework-core:4.0.9")
}
```
Для работы так же потребуются дополнительные зависимости, которые могут отличаться от ваших предпочтений. Для примера мы используем Cucumber TestNG для запуска тестов и Allure для отчётности:
```xml pom.xml
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <version>4.2.4</version>
        </dependency>
        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-cucumber4-jvm</artifactId>
            <version>2.12.1</version>
        </dependency>
```
то же для gradle
```groovy build.gradle
    compile 'io.cucumber:cucumber-testng:4.2.4'
    compile 'io.qameta.allure:allure-cucumber4-jvm:2.12.1'
```

Создайте TestNG-раннер для фреймворка в `test/java`:
```java
@CucumberOptions(
        plugin = {"pretty",
                "io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm",
                "json:target/cucumber.json",
        },
        glue = {"steps", "hooks"},
        features = "classpath:features"
)
public class TestsRunner extends AbstractTestNGCucumberTests {
}
```
В пакете `test` создайте подпакеты  `steps` и `hooks`. В пакете `test.steps` будут сохраняться имплементации шагов  Cucumber. В пакете `test.hooks` различные настроечные шаги, например шаги перед сценарием или регистрация `TypeRegistry`.

В пакете `test.hooks` создайте при необходимости файл с хуками кукумбера. Чтобы получить доступ ко всем встроенным методам, наследуйте его от `AbstractFrameworkSteps`. В примере ниже используются включённые в [коробочную версию фреймворка](https://github.com/lanit-izh/automation-framework-box.git) утилитарные классы и расширения Atlas.
```
    @Before
    public void setUp() {
        // здесь подключаются расширения Atlas. Подробнее о них в проекте https://github.com/qameta/atlas
        Atlas atlas = getAtlas();
        atlas.extension(new ElementReadyClickSendKeysExtension());
        atlas.extension(new IsDisplayedExtension());
    }

    @After
    public void tearDown(Scenario scenario) {
        if (driverIsActive()) { // прикрепляем финальный скриншот страницы к отчёту. AllureHelper доступен в коробочной версии фреймворка
            AllureHelper.attachPageSource(getDriver().getPageSource().getBytes(StandardCharsets.UTF_8));
            AllureHelper.attachScreenShot("Скриншот последней операции", getScreenShooter().takeScreenshot());
            shutdownDriver();
        }
        // Если используется citrus для тестирования api
        MessageTracingTestListener messageTracingTestListener = (MessageTracingTestListener) getEndpointByName("messageTracingTestListener");
        messageTracingTestListener.onTestFinish(getCitrusRunner().getTestCase());
        // вывод софт-ассёртов. В коробочной версии реализовано на базе ExtendedAssert TestNG
        softAssert().assertAll();
        softAssert().flush();
    }
```
### Создание PageObjects
Создание страниц-объектов можно посмотреть в коробочной версии фреймворка. Для упрощения реализации шагов рекомендуется придерживаться ряда правил:
* выбирать минимально 


