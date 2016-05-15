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
### @Wire annotation

``` java
class UserService {
  @Wire private ConnectionService connectionService;
  //...
}
```



###Init annotation
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
