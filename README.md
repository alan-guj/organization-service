# 企业组织架构和资源管理

## 目的
作为一个企业应用，很多的系统功能都与企业本身的组织架构和内部资源密切相关。包括但不限于以下方面：
* 权限控制

确定系统中的哪些用户可以使用哪些功能，查看哪些数据。
这些权限与用户所在的组织部门，用户在组织中的角色，以及用户的用户组，以及相关数据或资源在企业架构中的位置相关。
本模块为其他功能模块提供了相关信息，便于各功能模块确定相应的权限。
* 数据分析

为数据分析提供基础的分析维度。
在企业的运营数据分析中，需要知道各个数据之间的层次和关联关系。
这些关联关系与企业的组织架构和资源所在位置密切相关。
本模块所提供的组织架构和资源位置，为企业运营数据的分析提供了基础。

# 开发环境调试地址
* http://cc-dev.jyx365.top:8810/api/v1.0

* Swagger UI : http://cc-dev.jyx365.top:8810/swagger-ui.html

## 编译
```shell
#编译并执行单元测试
./gradlew build
#编译不进行单元测试
./gradlew assemble
```
编译结果为`./build/libs/organizationService-0.0.1-SNAPSHOT.jar`

## 运行
```shell
#启动
#{profile-name}是当前配置的名称，对应到consul中`config/OrganizationService,{profile-name}/data`的内容，默认为dev
#{hostname}是在consul上注册服务所使用的主机名或地址，默认为hostname
orgsrvctl start {profile-name} {hostname}

#停止
orgsrvctl stop

```

