# Introduction #

The goal of this project is to extend the [Apache Tuscany](http://http://tuscany.apache.org/) with the support for dynamically updating components in a running system in a safe and low-disruptive way.


# Background Information #

Please refer to the following papers for the problem and solutions of dynamic component update:
  * [Version-Consistency](http://conup.googlecode.com/svn/wiki/esec121-ma.pdf)
  * [Tranquility](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=4359466)
  * [Quiescence](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=60317)

# Overall Architecture #

![https://conup.googlecode.com/svn/wiki/imgs/ConupProjectIntroduction/1.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupProjectIntroduction/1.jpg)

We have extended Tuscany from four points, you can figure out from the aboving figure.
  * Transaction Management
Here, Transaction Management includes: the creation of tx id, the lifecycle management of transaction
  * Local future/past dep management
  * Interceptor for ack/notify
  * Component Lifecycle Management

# Overall Conup-Serivce module design #
In order to make our conup-service module be reusable, we design it as the following figure.
![https://conup.googlecode.com/svn/wiki/imgs/ConupProjectIntroduction/2.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupProjectIntroduction/2.jpg)

Any application server implements our spi in its own implmentation, it can use our conup service to make dynamic update.

# More specific Conup-Serivce module design #
![https://conup.googlecode.com/svn/wiki/imgs/ConupProjectIntroduction/3.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupProjectIntroduction/3.jpg)

### Tuscany extension ###
  * TransactionManager inculdes:TxLifeCycleManager and TxDepMonitor
> > i. TxLifeCycleManager is in charge of the creation of transaction id.


> ii. TxDepMonitor is used to response to the notification of the inserted code, and take respect action.

  * CompLifeCycleManager is in charge of the lifecycle management of the component(here we extend tuscany component lifecycle to accomdate to the update)

### Conup SPI ###
Conup spi module is used to make our conup-service more reusable, any application server implements our spi in its own implmentation, it can use our conup service to make dynamic update.

### Conup core ###
  * DynamicDependenceManager: manage the local future/past deps
  * ValidityKeepingAlgorithm: includes three algorithms(Version Consistency, Quiescence, Tranquillity)
  * OndemandSetup: in order to reduce the overhead of manage the runtime deps, we have implemented ondemand setup to setup the dynamic deps only after receiving update request.

### Communication ###
  * RemoteManagement
provides RemoteConfService and ComponentConfigService, RemoteConfService can send update, ondemand. ComponentConfigService can query component names host by the node, component's status, component's freeness strategy etc.

  * PeerToPeer
Component uses this module to communicate with peer component, current version only implements DepNotifyService and OndemandDynDepSetupService