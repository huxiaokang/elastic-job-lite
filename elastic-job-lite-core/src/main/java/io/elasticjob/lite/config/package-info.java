package io.elasticjob.lite.config;

/*
*  job的相关的配置信息
*
*   JobCoreConfiguration 作业核心配置
*
*   JobTypeConfiguration 作业类型配置 分为三种类型 SimpleJobConfiguration、ScriptJobConfiguration、DataflowJobConfiguration三种作业类型，
*   在JobTypeConfiguration中调用JobCoreConfiguration
*
*   JobRootConfiguration 作业配置根接口 实现类是 LiteJobConfiguration  Lite作业配置 该类中调用 JobTypeConfiguration
*
* */