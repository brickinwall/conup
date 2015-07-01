# Introduction #

The following figure shows the static configuration of our component-based sample system:

![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/1.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/1.jpg)

To Run samples in our distribution (conup-0.9.0-DU/samples) in your virtual machine, you need to make sure the following things:

## step 1 ##
Download all things in tags/conup-0.9.0-DU

## step 2 ##
Run mvn install in root directory of your all download files.

![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/2.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/2.jpg)

## step 3 ##
Then go to distribution folder, you will see conup-0.9.0-DU folder, this folder is uncompressed from conup-0.9.0-DU.tar.gz in the same folder.

## step 4 ##
Setup your environment path:
  * export TUSCANY\_HOME=/home/xxxx/conup-0.9.0-DU
  * export PATH=$PATH:${TUSCANY\_HOME }/bin
Please make sure all above setting are correct.

## step 5 ##
Change ip address in all projects in moules/samples/authUpdate folder.
The default ip is 10.0.2.15, please change it to your host ip address. For example, in conup-sample-auth you need to change auth.composite file. Other three nodes, you also need to change.(you do not need to change port!)

![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/3.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/3.jpg)

There are other project in moudules/samples/ authUpdate, you should do the following things:
In conup-sample-configuration-client, you need to change ip addresses in ConfServiceImpl.java and configuration.composite.
In conup-sample-visitor, you need to change ip addresses in visitor.composite.

After doing all these changes, you need to run mvn install, the created jars will be automatically copied to TUSCANY\_HOME/samples/xxxx, then we can move to next step.

## step 6 ##
Start all four nodes in your terminals like the following figures.
![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/4.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/4.jpg)

## step 7 ##
After all nodes are started, we can type “installed” in terminal to see which nodes we have started
![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/5.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/5.jpg)

## step 8 ##
#### a.Invoke PortalComponent’s PortalService in portal node’s terminal using the following command ####
“execute” is the service method name; “BEGIN, nju, cs” are the three parameters that are required by this method. In our example, we will display all execution path in our result string like the following figure.
![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/6.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/6.jpg)

INFO: aa2cbb89-ec24-4074-a4a6-07660f99b1d:BEGINPortalComponent.execute.version.1, AuthComponent.getToken.version.1, ProcComponent.process.version.1, AuthComponent.verify.version.1, DBComponent.dbOperation.version.1

**_aa2cbb89-ec24-4074-a4a6-d07660f99b1d_** is root transaction id; **_BEGIN_** is our input parameter which is used to identify the start of execution;**_PortalComponent.execute_** is our execution path identifier, in the execution path we will also display the implementation’s version using **_version.1_**

In every component implementation, we have added a variable which was used to identify implementation’s version number. We use this number to test whether our algorithm is right or not.

#### b.Send update command to AuthComponent. ####
In paper example, we update AuthComponent, we have put our new version of the component implementation :
![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/7.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/7.jpg)
Attention: here the new version of the component implementation is in the form of a .class file with its package name sub-folder.

start conup-sample-configuration-client and send update message to target component using the following commands.
invoke ConfigComponent/ConfService update AuthComponent /home/artemis/Documents/20121225-distribution/tuscany-sca-2.0-DU/samples/update

For simplicity, here we send update command to AuthComponent, the first parameter is the target component, next parameter is the new version class where you place(In your environment, the new version classes should be /xxx/xxx/conup-0.9.0-DU/samples/update). when you finish updating, you can invoke Portal service to test whether your update is right.

![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/8.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/8.jpg)

## step 9 ##
We have implement a service access tool to help you make some tests on our algorithm

#### a.Start four nodes: portal, proc, auth, db. ####
#### b.Start config node. ####
![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/9.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/9.jpg)

#### c.Start service visitor node. ####
![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/10.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/10.jpg)
After start this node, you can specify the time of accessing specify service like the following figure: the last parameter is the times that you want to access.
![https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/11.jpg](https://conup.googlecode.com/svn/wiki/imgs/ConupSampleRunningReadMe/11.jpg)

During the service invoking, you can use config node to send update request to AuthComponent

If you want to use different algorithm and strategy, you can change them in Conup.xml which locates in bin folder in our distribution