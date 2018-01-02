# DI
Simple Automatic dependency injection.

##Basic use:

###Container initialization
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
### Bean injection @Wire

``` java
class UserService {
  @Wire private ConnectionService connectionService;
  //...
}
```
### Bean injection - constructor

``` java
@Service
class UserService {
    private ConnectionService connectionService;

    public UserService (ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
}
```



### @Init annotation
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

### Bean Scanner
```java

@Component
class UserService {
 	@Wire private ConnectionService connectionService;
  	@Init
  	private init () {
      	connectionService.prepareForConnection();
  	}
}


    Map<String,Class> classes = new BeanScanner("com.test").scan();
    Injector in = new Injector()
    in.registerAll(classes);

    BeanContainer b = new BeanContainer(in);
    b.initialize()


```


Bean scan annotations: @Component, @Service, @Repository


