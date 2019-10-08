package io.elasticjob.lite.api.strategy.impl;
/*
* AverageAllocationJobShardingStrategy的缺点是，一旦分片数量小于作业节点数，作业将永远分配至ip地址考前的作业节点
* ，导致ip靠后的作业节点空闲。eg：如果有三台服务器，分成2片 则是1=[0] 2= [1] 3=[0]
*
* OdevitySortByNameJobShardingStrategy则可以根据作业名称重新分配作业节点负载
* 如果有三台作业节点，分成2片，作业名称的哈希值为奇数，则每台作业节点分到的分片是： 1=[0],2=[1],3=[]
* 如果有三台作业节点，分成2片，作业名称的哈希值为偶数，则每台作业节点分到的分片是： 3=[0],2=[1],1=[]
*
*  如果要实现自定义分片策略的话，通过定义累实现JobShardingStrategy接口就可以了
*  实现之后，配置实现类的全路径到 Lite作业配置（LiteJobConfiguration）的jobShardingStrategyClass属性
*  作业进行分片计算时，作业分片策略工厂（JobShardingStrategyFactory）会创建作业分片策略实例
*
* */