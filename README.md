mocamsg
=======

Moca message interceptor

  Openfire网络不好的情况下经常丢消息，一般情况都是服务器端路由到客户端的时候发生的，所以利用Openfire的插件机制，进行消息的拦截，对所有拦截到的消息进行缓存，保证消息在服务端一定不会丢失。增加Redis缓存层，把所有在线消息都保存至Redis，利用消息回执机制，Client收到消息会根据该条消息的messageId发出一个消息回执到Redis，Redis根据这个messageId作为缓存的key针对性的清除消息缓存。为了不改变OpenFire的源代码本插件没有重写 OfflineMessageStore 的离线机制，离线消息还是会直接存储在 MySQL 的 ofOffline 表当中。离线消息会触发JPush消息推送。
