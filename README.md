# Tahona DI #
Automatic dependency injection that works on Desktop, Web and Android.

Spring support library: [here](https://github.com/tahonaPL/di-spring)

### Installation ###

Maven:
```
<groupId>pl.tahona</groupId>
<artifactId>di</artifactId>
<version>1.0.0</version>
```

Gradle:
```
  compile 'pl.tahona:di:1.0.0'
```
## Basic use: ###
### Container initialization ###
```java
//Create Injector:
	final Injector injector = new Injector();

//Register beans
	injector.register(AnnotationScopeLocalBean.class);
	injector.register(UserService.class);
	injector.register(ClientService.class);
	injector.register(ConnectionService.class);
		
	final BeanContainer beanContainer = new BeanContainer(injector);
	beanContainer.initialize();
```
### Bean injection @Wire ###

``` java
class UserService {
  @Wire private ConnectionService connectionService;
  //...
}
```
### Bean injection - constructor ###

``` java
@Service
class UserService {
    private ConnectionService connectionService;

    public UserService (ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
}
```

### @Init annotation ###
Method executed after bean injection. 
```java
 class UserService {
 	@Wire private ConnectionService connectionService;

  	@Init
  	private init () {
        connectionService.prepareForConnection();
  	}
}
```

### Bean Scanner ###
```java
@Component
class UserService {
    @Wire private ConnectionService connectionService;

    @Init
    private init () {
        connectionService.prepareForConnection();
    }
}

...

Map<String,Class> classes = new BeanScanner("com.test").scan();
Injector in = new Injector()
in.registerAll(classes);

BeanContainer b = new BeanContainer(in);
b.initialize()
```

Spring annotations (@Component, @Service, @Repository) are in [DI-Spring](https://github.com/tahonaPL/di-spring)


