# Testcontainers - 설정

# dependency

```java
testImplementation "org.testcontainers:testcontainers:1.17.3"
testImplementation "org.testcontainers:junit-jupiter:1.17.3"
testImplementation "org.testcontainers:mysql:1.17.3"
```

# Account

먼저 테스트에 사용할 Account Entity와 AccountService의 기본적인 CRUD 로직입니다

```java
@Entity
@Getter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Account(String name) {
        this.name = name;
    }

    public Account update(String name) {
        this.name = name;
        return this;
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(String name) {
        return accountRepository.save(new Account(name));
    }

    public Account findAccount(long accountId) {
        return accountRepository.findById(accountId).get();
    }

    public long deleteAccount(long accountId) {
        accountRepository.deleteById(accountId);
        return accountId;
    }

    public Account updateAccount(long accountId, String name) {
        Account savedAccount = findAccount(accountId);

        return savedAccount.update(name);
    }
}
```

# Testcontainer를 이용한 테스트 환경 갖추는 방법 2가지

이 부분에서 3,4 일간 삽질을 했습니다,, 그래서 이 부분을 깨달았을 때 얼마나 기뻤는지 ,,, 

정말 오랜시간 삽질한 만큼 많이 얻은 것 같습니다!! 

정확하지 않은 정보가 있을 수도 있기 때문에 피드백해주시면 감사하겠습니다

본론으로 넘어가서 Testcontainers를 이용한 테스트 방법에는 2가지가 있습니다

jdbc url을 명시하는 방법, Java code를 이용하여 직접 test containers를 생성하는 방법입니다

## Java code를 이용한 test container 생성

```java
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AccountServiceTest {

			//... 

} 
```

## @Testcontainers, @Container

테스트 컨테이너를 사용할 테스트 클래스에 `@Testcontainers` 어노테이션을 사용합니다. Jupiter extension을 연동한다는 의미입니다.

해당 어노테이션은 `@Container` 가 붙여진 필드를 찾아 해당 컨테이너의 라이프 사이클 메서드를 실행해줍니다

만약 `@Container` 가 붙은 필드가 `static` 이라면 테스트 클래스에 존재하는 테스트 메서드에서 컨테이너가 `share` 되고, `인스턴스 변수`일 경우 테스트 메서드를 실행할 때마다 container를 생성합니다

## AccountTest

```java
@Container
public static MySQLContainer mysqlContainer =
        new MySQLContainer("mysql:8.0.28")
                .withDatabaseName("foo");

static {
    mysqlContainer.start();
}

@DynamicPropertySource
static void mySqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
}
```

mysql:8.0.28 docker image를 통해 foo라는 데이터베이스 컨테이너를 실행시킵니다

`@DynamicPropertySource` 를 이용하여 생성한 컨테이너를 맵핑시켜줘야합니다

### @DynamicPropertySource가 무엇인가요?

우리가 DB에 연결을 할 땐 일반적으로 yml 파일에 url, username, hostname, database name 등을 명시하여 사용합니다,, 이러한 경우를 정적 리소스에 연결하기 때문에 가능합니다

하지만 testcontainers처럼 컨테이너가 실행될 때마다 random port에 연결되는 동적인 상황에선  yml 파일을 동적으로 설정하고 변경하기에는 어려움이 있습니다

그래서 이처럼 구성이 “동적”이라면 Spring 에서 제공하는 `@DynamicPropertySource` 를 이용하여 동적으로 설정하고 변경할 수 있습니다

## 정적, 인스턴스 필드 차이에 따른 docker 생성

위에서 말했다시피 인스턴스 변수이기 때문에 메서드마다 컨테이너가 실행될 것입니다

```java
@Test
@DisplayName("test container 실행 확인")
void isRunningTestContainer() throws Exception {
    assertTrue(mysqlContainer.isRunning());
}

@Test
@DisplayName("Account 저장")
void saveAccount() throws Exception {
    //given
    Account account = new Account("account1");

    //when
    Account savedAccount = accountRepository.save(account);

    //then
    assertThat(savedAccount.getId()).isEqualTo(account.getId());
}
```
![Untitled](https://user-images.githubusercontent.com/62413589/182014621-2853beb6-1aed-46d9-9498-605fd3984e15.png)
![Untitled 1](https://user-images.githubusercontent.com/62413589/182014627-09431d72-e388-4390-893e-ce93355d3929.png)


두 테스트 모두 docker container가 실행되는 것을 볼 수 있습니다

그러면 이제 container를 static 변수로 바꿔보겠습니다

![Untitled 2](https://user-images.githubusercontent.com/62413589/182014636-16237962-a584-40f6-aaef-1f9ba3e2b69b.png)
![Untitled 3](https://user-images.githubusercontent.com/62413589/182014644-acf7c1d2-2d5c-4136-bb39-99ff6e1fb318.png)

테스트 클래스가 실행될 때 “~~한 번~~" 컨테이너가 실행되는 것을 알 수 있습니다

### 어? 그런데 로그를 보면 두 개의 컨테이너가 생성이 되는데, 이건 컨테이너가 한개만 생성된게 아니지 않나요?

![Untitled 4](https://user-images.githubusercontent.com/62413589/182014651-956cdc94-8560-452e-847a-272b69197c8d.png)

container image 이름이 각각 mysql, testcontainers/ryuk 라는 것을 알 수 있습니다

[ryuk](https://hub.docker.com/r/testcontainers/ryuk) 컨테이너는 Testcontainers 라이브러리에서 자체적으로 시작되는 보조 컨테이너로써, 테스트가 모두 끝나고 컨테이너를 삭제하는데 도움을 줍니다. 이 뿐만 아니라 설정에 따라 네트워크, 볼륨, 이미지 등을 제거할 수 있습니다

# jdbc url을 사용하여 테스트 컨테이너 생성

또 다른 방법은 yml파일에서 jdbc url을 명시해줌으로써 컨테이너를 자동적으로 생성할 수 있는 방법입니다

[공식문서](https://www.testcontainers.org/modules/databases/jdbc/)에 따르면  `JDBC URL support를 사용한다면, url에 있는 hostname, port number, db name을 무시하면서 container 인스턴스를 Testcontainers에서 자동으로 생성해줍니다.`  

## application-test.yml 설정

```java
spring:
  datasource:
    url: jdbc:tc:mysql:8.0.28:///
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver

//jpa 설정...
```

url을 보시면 database name도 없고 tc라는 접두사도 보입니다

Testcontainers를 사용하려면 일반적인 url 과 다르게 `tc:` 를 붙여줘야합니다

그리고 Testcontainers에서는 default 로 `test` 라는 database를 만들어주기 때문에 database name을 명시하지 않아도 됩니다

공식문서를 보면 Spring Boot 2.3.0 이하 버젼은 driver-class-name을 명시해줘야한다고 합니다. 

하지만 저는 spring boot 2.6.10을 사용하고 있는데 class name을 명시해주지 않으면 driver를 찾을 수 없다고 해서 명시해줬습니다,, (이유를 모르겠음,,)
![Untitled 5](https://user-images.githubusercontent.com/62413589/182014656-2761eee8-2adf-41f2-8f85-fc42e661a891.png)

## AccountServiceTest

위에서 직접 mysql containers를 생성한 테스트 코드와 완전히 다른 것을 볼 수 있습니다

```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
//@Testcontainers
class AccountServiceTest {

//    @Container
//    public static MySQLContainer mysqlContainer =
//            new MySQLContainer("mysql:8.0.28")
//                    .withDatabaseName("foo");
//
//    static {
//        mysqlContainer.start();
//    }
//
//    @DynamicPropertySource
//    static void mySqlProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", mysqlContainer::getUsername);
//        registry.add("spring.datasource.password", mysqlContainer::getPassword);
//    }

    @Autowired
    AccountRepository accountRepository;

//    @Test
//    @DisplayName("test container 실행 확인")
//    void isRunningTestContainer() throws Exception {
//        assertTrue(mysqlContainer.isRunning());
//    }

    ///Account 저장 Test

    @Test
    @DisplayName("Account 수정")
    void updateAccount() throws Exception {
        //given
        Account account = new Account("account1");
        Account savedAccount = accountRepository.save(account);

        //when
        savedAccount.update("account2");

        Account updatedAccount = accountRepository.findById(account.getId()).get();

        //then
        assertThat(updatedAccount.getName()).isEqualTo("account2");
    }

}
```

대부분의 코드들이 주석처리됐습니다,,!! 

직접 컨테이너를 생성하지 않았기 때문에 mysqlContainer를 생성하는 코드를 작성하지 않아도 되고, url, username, password도 맵핑하지 않아도 됩니다

그러면서 직접 컨테이너를 생성했을 때와 같이 ryuk, mysql 컨테이너가 생성됩니다

![Untitled 6](https://user-images.githubusercontent.com/62413589/182014664-d8d31804-c351-4200-a0f7-0416a0e3476a.png)
![image](https://user-images.githubusercontent.com/62413589/182014670-13443d10-623e-41e8-9217-0ab3ff2b200c.png)

### testcontainers를 싱글톤으로 관리하려면 직접 컨테이너 인스턴스를 생성해야하나요?

그렇지 않습니다,, yml을 사용한다면 testcontainers가 자동적으로 싱글톤으로 관리됩니다

![Untitled 8](https://user-images.githubusercontent.com/62413589/182014676-15c76790-7fd8-46a4-949f-ffc5b418d7d8.png)

![Untitled 9](https://user-images.githubusercontent.com/62413589/182014680-66a2bb0e-3eed-495e-b891-62502c06aec6.png)

mysql container가 생성되고 나서 각 테스트 메서드에는 container가 생성되는 로그가 찍혀있지 않습니다

> 대부분의 testcontainers는 싱글톤으로 관리하는 것이 편리하고, 빠르기 때문에 testcontainers 라이브러리에서는 jdbc url support를 사용하면 자동적으로 싱글톤으로 생성해주지 않을까라는 합리적 의심을 해봅니다,,
> 

# 결론

이처럼 두 가지 방법중에 누가봐도 두 번째 방법이 편해보이는 것을 알 수 있습니다. 공식문서에서도 처음에 url을 사용하는 방법을 알려줍니다

그리고 컨테이너 인스턴스를 직접 생성했을 때의 설명 부분에서 `url을 사용할 수 없는 환경`이거나 `컨테이너의 세심한 설정`을 위해 사용한다고 합니다

하지만 Acceptancetest를 할 때는 트랜잭션이 보장되지 않기 때문에 싱글톤으로 관리되는 testcontainersrk 무조건 멱등할 수 없다고 생각합니다. 그래서 데이터 관리의 필요성을 느낍니다,,

다음 글에서는 단위 테스트를 실행할 때는 testcontainers를 사용하지 않아도 되기 때문에 테스트를 분리해보도록 하겠습니다

# REFERENCES

[testcontainers 실행 시 ryuk 컨테이너가 생성되는 이유](https://stackoverflow.com/questions/68965286/why-does-spring-boot-spawn-2-test-containers-with-singleton-container-approach)
