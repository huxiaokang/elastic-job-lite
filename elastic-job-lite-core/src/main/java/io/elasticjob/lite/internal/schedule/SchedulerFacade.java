/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.listener.ElasticJobListener;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.election.LeaderService;
import io.elasticjob.lite.internal.instance.InstanceService;
import io.elasticjob.lite.internal.listener.ListenerManager;
import io.elasticjob.lite.internal.monitor.MonitorService;
import io.elasticjob.lite.internal.reconcile.ReconcileService;
import io.elasticjob.lite.internal.server.ServerService;
import io.elasticjob.lite.internal.sharding.ExecutionService;
import io.elasticjob.lite.internal.sharding.ShardingService;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 为调度器提供内部服务的门面类.
 * 
 * @author zhangliang
 */
public final class SchedulerFacade {

    /**
     * 作业名称
     */
    private final String jobName;

    /**
     * 作业配置服务
     */
    private final ConfigurationService configService;

    /**
     * 主节点服务
     */
    private final LeaderService leaderService;

    /**
     * 作业服务器服务
     */
    private final ServerService serverService;

    /**
     * 作业服务器服务
     */
    private final InstanceService instanceService;

    /**
     * 作业分配服务
     */
    private final ShardingService shardingService;

    /**
     * 执行作业服务
     */
    private final ExecutionService executionService;

    /**
     * 作业监控服务
     */
    private final MonitorService monitorService;

    /**
     * 调节作业不一致状态服务
     */
    private final ReconcileService reconcileService;

    /**
     * 作业注册中心的监听器管理者
     */
    private ListenerManager listenerManager;
    
    public SchedulerFacade(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        configService = new ConfigurationService(regCenter, jobName);
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        instanceService = new InstanceService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        monitorService = new MonitorService(regCenter, jobName);
        reconcileService = new ReconcileService(regCenter, jobName);
    }
    
    public SchedulerFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final List<ElasticJobListener> elasticJobListeners) {
        this.jobName = jobName;
        configService = new ConfigurationService(regCenter, jobName);
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        instanceService = new InstanceService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        monitorService = new MonitorService(regCenter, jobName);
        reconcileService = new ReconcileService(regCenter, jobName);
        listenerManager = new ListenerManager(regCenter, jobName, elasticJobListeners);
    }
    
    /**
     * 获取作业触发监听器.
     *
     * @return 作业触发监听器
     */
    public JobTriggerListener newJobTriggerListener() {
        return new JobTriggerListener(executionService, shardingService);
    }
    
    /**
     * 更新作业配置.
     *
     * @param liteJobConfig 作业配置
     * @return 更新后的作业配置
     */
    public LiteJobConfiguration updateJobConfiguration(final LiteJobConfiguration liteJobConfig) {
        configService.persist(liteJobConfig);
        return configService.load(false);
    }
    
    /**
     * 注册作业启动信息.
     * 
     * @param enabled 作业是否启用
     */
    public void registerStartUpInfo(final boolean enabled) {
        //开启所有监听器
        listenerManager.startAllListeners();
        //选举主节点
        leaderService.electLeader();
        //持久化作业服务器上线信息
        serverService.persistOnline(enabled);
        //持久化作业运行实例信息
        instanceService.persistOnline();
        //设置重新分配的标记
        shardingService.setReshardingFlag();
        //初始化监听服务
        monitorService.listen();
        //初始化 调度作业不一致状态服务
        if (!reconcileService.isRunning()) {
            reconcileService.startAsync();
        }
    }

    /**
     * 终止作业调度.
     */
    public void shutdownInstance() {
        if (leaderService.isLeader()) {
            leaderService.removeLeader();
        }
        monitorService.close();
        if (reconcileService.isRunning()) {
            reconcileService.stopAsync();
        }
        JobRegistry.getInstance().shutdown(jobName);
    }
}
