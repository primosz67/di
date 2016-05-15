# DI




How to use: 

#Basic use:

class UserService {
  @Wire private ConnectionService connectionService;
  
  @Init
  private init () {
      connectionService.prepareForConnection();
  }
  //...
}

//Create Injector:
    final Injector injector = new Injector();

//Register beans
		injector.register(AnnotationScopeLocalBean.class);
		injector.register(UserService.class);
		injector.register(ClientService.class);
		injector.register(ConnectionService.class);
		
		final BeanContainer beanContainer = new BeanContainer(injector);
		beanContainer.initialize();
		
		
		
