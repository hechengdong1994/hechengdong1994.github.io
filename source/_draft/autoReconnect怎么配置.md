---
title: Hello World
date: 2022-08-18
---
背景

使用上的疑问

参数含义

官方描述

> Should the driver try to re-establish stale and/or dead connections? If enabled the driver will throw an exception for queries issued on a stale or dead connection, which belong to the current transaction, but will attempt reconnect before the next query issued on the connection in a new transaction. The use of this feature is not recommended, because it has side effects related to session state and data consistency when applications don't handle SQLExceptions properly, and is only designed to be used when you are unable to configure your application to handle SQLExceptions resulting from dead and stale connections properly. Alternatively, as a last option, investigate setting the MySQL server variable 'wait_timeout' to a high value, rather than the default of 8 hours.

翻译

> 驱动程序是否应该尝试对陈旧和/或死亡的连接进行重新建立？如果启用，驱动程序将对在陈旧或死亡的连接上发出的属于当前事务的查询抛出一个异常，但会在新事务中的连接上发出下一个查询之前尝试重新连接。不推荐使用这个功能，因为当应用程序没有正确处理SQLExceptions时，它有与会话状态和数据一致性相关的副作用，并且只在你无法配置你的应用程序来正确处理由死连接和陈旧连接产生的SQLExceptions时使用。另外，作为最后的选择，调查一下将MySQL服务器变量'wait_timeout'设置为一个高的值，而不是默认的8小时。

源码解析

场景分析

普通连接

主从连接

连接池管理连接

总结

对使用场景的概括
